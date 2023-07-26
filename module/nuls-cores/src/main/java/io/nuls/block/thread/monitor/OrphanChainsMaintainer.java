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
import io.nuls.base.data.NulsHash;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.model.Node;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.storage.ChainStorageService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.logback.NulsLogger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

import static io.nuls.block.constant.StatusEnum.RUNNING;
import static io.nuls.block.constant.StatusEnum.UPDATE_ORPHAN_CHAINS;

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
public class OrphanChainsMaintainer extends BaseMonitor {

    private ChainStorageService chainStorageService;

    private static final OrphanChainsMaintainer INSTANCE = new OrphanChainsMaintainer();

    private OrphanChainsMaintainer() {
        super();
        chainStorageService = SpringLiteContext.getBean(ChainStorageService.class);
    }

    public static OrphanChainsMaintainer getInstance() {
        return INSTANCE;
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger commonLog) {
        ChainParameters parameters = context.getParameters();
        int orphanChainMaxAge = parameters.getOrphanChainMaxAge();

        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
                if (!lock.validate(stamp)) {
                    continue;
                }
                if (orphanChains.isEmpty()) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                List<Node> availableNodes = NetworkCall.getAvailableNodes(chainId);
                //维护现有孤儿链,尝试在链首增加区块
                context.setStatus(UPDATE_ORPHAN_CHAINS);
                long l = System.nanoTime();
                for (Chain orphanChain : orphanChains) {
                    maintainOrphanChain(chainId, orphanChain, availableNodes, orphanChainMaxAge);
                    //孤儿链维护时间超过十秒，就退出
                    if (System.nanoTime() - l > 10000000000L) {
                        break;
                    }
                }
                break;
            }
        } finally {
            context.setStatus(RUNNING);
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    /**
     * 维护孤儿链,向其他节点请求孤儿链起始区块的上一个区块,仅限于没有父链的孤儿链
     *
     * @param chainId 链Id/chain id
     * @param orphanChain
     * @param orphanChainMaxAge
     */
    private void maintainOrphanChain(int chainId, Chain orphanChain, List<Node> availableNodes, int orphanChainMaxAge) {
        ChainContext context = ContextManager.getContext(chainId);
        Map<NulsHash, List<String>> orphanBlockRelatedNodes = context.getOrphanBlockRelatedNodes();
        //有父链的孤儿链,是从某孤儿链分叉得到的,不需要进行链首维护
        //链首高度为1时,不需要进行链首维护
        if (orphanChain.getParent() != null || orphanChain.getStartHeight() <= 1) {
            return;
        }
        AtomicInteger age = orphanChain.getAge();
        //孤儿链年龄超过限制,不需要进行链首维护
        if (age.get() > orphanChainMaxAge) {
            return;
        }
        NulsHash previousHash = orphanChain.getPreviousHash();
        Chain masterChain = BlockChainManager.getMasterChain(chainId);
        if (masterChain.getHashList().contains(previousHash)) {
            return;
        }
        Set<String> nodes = new HashSet<>();
        for (NulsHash nulsHash : orphanChain.getHashList()) {
            List<String> list = orphanBlockRelatedNodes.get(nulsHash);
            if (list != null) {
                nodes.addAll(list);
            }
        }
        //过滤可用的孤儿块维护节点
        Set<String> set = availableNodes.stream().map(Node::getId).collect(Collectors.toSet());
        nodes.retainAll(set);
        Block block;
        //向其他节点请求孤儿链起始区块的上一个区块
        long l = System.nanoTime();
        for (String availableNode : nodes) {
            block = BlockUtil.downloadBlockByHash(chainId, previousHash, availableNode, orphanChain.getStartHeight() - 1);
            if (block != null) {
                //从节点下载区块成功
                orphanChain.addFirst(block);
                chainStorageService.save(chainId, block);
                return;
            } else {
                //下载不到或者超时，把这个节点剔除
                for (NulsHash nulsHash : orphanChain.getHashList()) {
                    List<String> list = orphanBlockRelatedNodes.get(nulsHash);
                    list.remove(availableNode);
                    context.getLogger().warn("get block timeout, kick out this node-" + availableNode);
                }
            }
            //请求区块失败,孤儿链年龄加一
            age.incrementAndGet();
            if (age.get() > orphanChainMaxAge) {
                return;
            }
            if (System.nanoTime() - l > 10000000000L) {
                break;
            }
        }
    }

}
