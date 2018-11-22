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
import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.Node;
import io.nuls.block.thread.BlockDownloadUtils;
import io.nuls.block.utils.NetworkUtil;
import io.nuls.tools.log.Log;

import java.util.List;

/**
 * 孤儿链的形成原因分析：
 * 孤儿链定时处理器，遍历孤儿链集合，向其他节点请求孤儿链起始区块的上一个区块，如果成功则连到孤儿链上，随后尝试与主链、分叉链、孤儿链进行连接。
 * 如果一个孤儿链经过10(可配置)轮处理都没有更新区块，则丢弃该孤儿链
 * @author captain
 * @date 18-11-14 下午3:54
 * @version 1.0
 */
public class OrphanChainsMonitor implements Runnable {

    private static final OrphanChainsMonitor INSTANCE = new OrphanChainsMonitor();

    private OrphanChainsMonitor() {

    }

    public static OrphanChainsMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        try {
            for (Integer chainId : ContextManager.chainIds) {
                //判断该链的运行状态，只有正常运行时才会有孤儿链的处理
                RunningStatusEnum status = ContextManager.getContext(chainId).getStatus();
                if (!status.equals(RunningStatusEnum.RUNNING)){
                    Log.info("skip process, status is {}, chainId-{}", status, chainId);
                    return;
                }
                Chain masterChain = ChainManager.getMasterChain(chainId);
                List<Chain> forkChains = ChainManager.getForkChains(chainId);
                List<Chain> orphanChains = ChainManager.getOrphanChains(chainId);
                List<Node> availableNodes = NetworkUtil.getAvailableNodes(chainId);
                //遍历孤儿链集合
                int maxAge = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.ORPHAN_CHAIN_MAX_AGE));
                for (int i = 0, orphanChainsSize = orphanChains.size(); i < orphanChainsSize; i++) {
                    Chain orphanChain = orphanChains.get(i);
                    if (orphanChain.getAge() > maxAge) {
                        ChainManager.removeOrphanChain(chainId, orphanChain);
                    }
                    maintainOrphanChain(chainId, orphanChain, availableNodes);
                    //1.判断与主链是否相连
                    if (tryMerge(orphanChain, masterChain)) {
                        continue;
                    }
                    //2.判断与分叉链是否相连
                    for (int i1 = 0, forkChainsSize = forkChains.size(); i1 < forkChainsSize; i1++) {
                        Chain forkChain = forkChains.get(i1);
                        if (tryMerge(orphanChain, forkChain)) {
                            break;
                        }
                    }

                    //3.判断与孤儿链是否相连
                    for (int j = i; j < orphanChainsSize; j++) {
                        Chain otherOrphanChain = orphanChains.get(j);
                        if (tryMerge(orphanChain, otherOrphanChain)) {
                            break;
                        }
                    }

                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    /**
     * 两个if分支代表首尾互换，进行链接判断，只有双方都是孤儿链时可能用到
     * @param orphanChain
     * @param otherChain
     * @return
     */
    private boolean tryMerge(Chain orphanChain, Chain otherChain) throws Exception {
        if (otherChain.getEndHeight() + 1 == orphanChain.getStartHeight() && otherChain.getEndHash().equals(orphanChain.getPreviousHash())) {
            return ChainManager.merge(otherChain, orphanChain);
        }
        if (orphanChain.getEndHeight() + 1 == otherChain.getStartHeight() && orphanChain.getEndHash().equals(otherChain.getPreviousHash())) {
            return ChainManager.merge(orphanChain, otherChain);
        }
        return false;
    }

    /**
     * 维护孤儿链，向其他节点请求孤儿链起始区块的上一个区块
     * 返回false说明维护结束
     * @param chainId
     * @param orphanChain
     */
    private void maintainOrphanChain(int chainId, Chain orphanChain, List<Node> availableNodes){
        NulsDigestData previousHash = orphanChain.getPreviousHash();
        Block block;
        //向其他节点请求孤儿链起始区块的上一个区块
        for (Node availableNode : availableNodes) {
            block = BlockDownloadUtils.getBlockByHash(chainId, previousHash, availableNode);
            if (block != null) {
                orphanChain.addFirst(block);
                return;
            }
        }
        orphanChain.setAge(orphanChain.getAge() + 1);
    }

}
