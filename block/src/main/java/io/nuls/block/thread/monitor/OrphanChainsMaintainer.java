/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.thread.monitor;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.model.Node;
import io.nuls.block.service.ChainStorageService;
import io.nuls.block.utils.BlockDownloadUtils;
import io.nuls.block.utils.LoggerUtil;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.constant.RunningStatusEnum.RUNNING;
import static io.nuls.block.constant.RunningStatusEnum.UPDATE_ORPHAN_CHAINS;
import static io.nuls.block.utils.LoggerUtil.commonLog;

/**
 * 孤儿链的形成原因分析：因为网络问题,在没有收到Block(100)的情况下,已经收到了Block(101),此时Block(101)不能连接到主链上,形成孤儿链
 * 孤儿链定时维护处理器
 * 孤儿链处理大致流程：
 * 1.清理无效数据
 * 2.维护现有数据
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class OrphanChainsMaintainer implements Runnable {

    @Autowired
    private static ChainStorageService chainStorageService;

    private static final OrphanChainsMaintainer INSTANCE = new OrphanChainsMaintainer();

    private OrphanChainsMaintainer() {
        chainStorageService = SpringLiteContext.getBean(ChainStorageService.class);
    }

    public static OrphanChainsMaintainer getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {

        for (Integer chainId : ContextManager.chainIds) {
            ChainContext context = ContextManager.getContext(chainId);
            NulsLogger commonLog = context.getCommonLog();
            try {
                //判断该链的运行状态,只有正常运行时才会有孤儿链的处理
                RunningStatusEnum status = context.getStatus();
                if (!status.equals(RUNNING)) {
                    commonLog.debug("skip process, status is " + status + ", chainId-" + chainId);
                    return;
                }
                ChainParameters parameters = ContextManager.getContext(chainId).getParameters();
                int orphanChainMaxAge = parameters.getOrphanChainMaxAge();

                StampedLock lock = context.getLock();
                long stamp = lock.tryOptimisticRead();
                try {
                    for (; ; stamp = lock.writeLock()) {
                        if (stamp == 0L) {
                            continue;
                        }
                        // possibly racy reads
                        SortedSet<Chain> orphanChains = ChainManager.getOrphanChains(chainId);
                        if (!lock.validate(stamp)) {
                            continue;
                        }
                        if (orphanChains.size() < 1) {
                            break;
                        }
                        stamp = lock.tryConvertToWriteLock(stamp);
                        if (stamp == 0L) {
                            continue;
                        }
                        // exclusive access
                        List<Node> availableNodes = NetworkUtil.getAvailableNodes(chainId);
                        //维护现有孤儿链,尝试在链首增加区块
                        context.setStatus(UPDATE_ORPHAN_CHAINS);
                        for (Chain orphanChain : orphanChains) {
                            maintainOrphanChain(chainId, orphanChain, availableNodes, orphanChainMaxAge);
                        }
                        break;
                    }
                } finally {
                    context.setStatus(RUNNING);
                    if (StampedLock.isWriteLockStamp(stamp)) {
                        lock.unlockWrite(stamp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                context.setStatus(RUNNING);
                commonLog.error("chainId-" + chainId + ",maintain OrphanChains fail!error msg is:" + e.getMessage());
            }
        }
    }

    /**
     * 维护孤儿链,向其他节点请求孤儿链起始区块的上一个区块,仅限于没有父链的孤儿链
     *
     * @param chainId
     * @param orphanChain
     * @param orphanChainMaxAge
     */
    private void maintainOrphanChain(int chainId, Chain orphanChain, List<Node> availableNodes, int orphanChainMaxAge) {
        //有父链的孤儿链，是从某孤儿链分叉得到的，不需要进行链首维护
        //链首高度为1时，不需要进行链首维护
        if (orphanChain.getParent() != null || orphanChain.getStartHeight() <= 1) {
            return;
        }
        AtomicInteger age = orphanChain.getAge();
        //孤儿链年龄超过限制，不需要进行链首维护
        if (age.get() > orphanChainMaxAge) {
            return;
        }
        NulsDigestData previousHash = orphanChain.getPreviousHash();
        Block block;
        //向其他节点请求孤儿链起始区块的上一个区块
        for (int i = 0, availableNodesSize = availableNodes.size(); i < availableNodesSize; i++) {
            Node availableNode = availableNodes.get(i);
            block = BlockDownloadUtils.getBlockByHash(chainId, previousHash, availableNode);
            if (block != null) {
                orphanChain.addFirst(block);
                chainStorageService.save(chainId, block);
                return;
            }
            //请求区块失败，孤儿链年龄加一
            age.incrementAndGet();
        }
    }

}
