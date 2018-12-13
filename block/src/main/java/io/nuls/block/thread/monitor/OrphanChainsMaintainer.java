/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.Node;
import io.nuls.block.utils.BlockDownloadUtils;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.tools.log.Log;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static io.nuls.block.constant.RunningStatusEnum.EXCEPTION;
import static io.nuls.block.constant.RunningStatusEnum.MAINTAIN_CHAINS;
import static io.nuls.block.constant.RunningStatusEnum.RUNNING;

/**
 * 孤儿链的形成原因分析：因为网络问题，在没有收到Block(100)的情况下，已经收到了Block(101)，此时Block(101)不能连接到主链上，形成孤儿链
 * 孤儿链定时维护处理器
 * 孤儿链处理大致流程：
 *      1.清理无效数据
 *      2.维护现有数据

 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class OrphanChainsMaintainer implements Runnable {

    private static final OrphanChainsMaintainer INSTANCE = new OrphanChainsMaintainer();

    private OrphanChainsMaintainer() {

    }

    public static OrphanChainsMaintainer getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {

        for (Integer chainId : ContextManager.chainIds) {
            try {
                //判断该链的运行状态，只有正常运行时才会有孤儿链的处理
                RunningStatusEnum status = ContextManager.getContext(chainId).getStatus();
                if (!status.equals(RUNNING)) {
                    Log.info("skip process, status is {}, chainId-{}", status, chainId);
                    return;
                }
                SortedSet<Chain> orphanChains = ChainManager.getOrphanChains(chainId);
                if (orphanChains.size() < 1) {
                    return;
                }
                List<Node> availableNodes = NetworkUtil.getAvailableNodes(chainId);
                //维护现有孤儿链，尝试在链首增加区块
                ContextManager.getContext(chainId).setStatus(MAINTAIN_CHAINS);
                for (Chain orphanChain : orphanChains) {
                    maintainOrphanChain(chainId, orphanChain, availableNodes);
                }
                ContextManager.getContext(chainId).setStatus(RUNNING);
            } catch (Exception e) {
                ContextManager.getContext(chainId).setStatus(EXCEPTION);
                Log.error("chainId-{},maintain OrphanChains fail!error msg is:{}", chainId, e.getMessage());
            }
        }
    }

    /**
     * 维护孤儿链，向其他节点请求孤儿链起始区块的上一个区块，仅限于没有父链的孤儿链
     *
     * @param chainId
     * @param orphanChain
     */
    private void maintainOrphanChain(int chainId, Chain orphanChain, List<Node> availableNodes) {
        if (orphanChain.getParent() != null) {
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
                orphanChain.setStartHeight(orphanChain.getStartHeight() - 1);
                orphanChain.setPreviousHash(block.getHeader().getPreHash());
                orphanChain.getHashList().addFirst(block.getHeader().getHash());
                return;
            }
        }
    }

}
