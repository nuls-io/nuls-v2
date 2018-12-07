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
import io.nuls.block.utils.NetworkUtil;
import io.nuls.tools.log.Log;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 孤儿链的形成原因分析：因为网络问题，在没有收到Block(100)的情况下，已经收到了Block(101)，此时Block(101)不能连接到主链上，形成孤儿链
 * 孤儿链定时处理器
 * 孤儿链处理大致流程：
 *      1.清理无效数据
 *      2.维护现有数据
 *      3.标记
 *      4.复制、清除
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
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

        for (Integer chainId : ContextManager.chainIds) {
            try {
                //判断该链的运行状态，只有正常运行时才会有孤儿链的处理
                RunningStatusEnum status = ContextManager.getContext(chainId).getStatus();
                if (!status.equals(RunningStatusEnum.RUNNING)) {
                    Log.info("skip process, status is {}, chainId-{}", status, chainId);
                    return;
                }
                Chain masterChain = ChainManager.getMasterChain(chainId);
                SortedSet<Chain> orphanChains = ChainManager.getOrphanChains(chainId);
                //1.清理链起始高度位于主链最新高度增减30(可配置)范围外的孤儿链
                long latestHeight = masterChain.getEndHeight();
                int heightRange = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.HEIGHT_RANGE));
                for (Chain orphanChain : orphanChains) {
                    if (Math.abs(orphanChain.getStartHeight() - latestHeight) > heightRange) {
                        //清理orphanChain，并递归清理orphanChain的所有子链
                        ChainManager.deleteOrphanChain(chainId, orphanChain, true);
                    }
                }

                List<Node> availableNodes = NetworkUtil.getAvailableNodes(chainId);
                //2.维护现有孤儿链，尝试在链首增加区块
                for (Chain orphanChain : orphanChains) {
                    maintainOrphanChain(chainId, orphanChain, availableNodes);
                }

                SortedSet<Chain> forkChains = ChainManager.getForkChains(chainId);
                //3.标记、变更链属性阶段
                for (Chain orphanChain : orphanChains) {
                    handle(orphanChain, masterChain, forkChains, orphanChains);
                }
                //4.复制、清除阶段
                SortedSet<Chain> maintainedOrphanChains = new TreeSet<>(Chain.COMPARATOR);
                for (Chain orphanChain : orphanChains) {

                    //如果标记为与主链相连，orphanChain不会复制到新的孤儿链集合，也不会进入分叉链集合，但是所有orphanChain的直接子链标记为ChainTypeEnum.MASTER_FORK
                    if (orphanChain.getType().equals(ChainTypeEnum.MASTER_APPEND)) {
                        orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.MASTER_FORK));
                        continue;
                    }
                    //如果标记为从主链分叉，orphanChain不会复制到新的孤儿链集合，但是会进入分叉链集合，所有orphanChain的直接子链标记为ChainTypeEnum.FORK_FORK
                    if (orphanChain.getType().equals(ChainTypeEnum.MASTER_FORK)) {
                        ChainManager.addForkChain(chainId, orphanChain);
                        orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
                        continue;
                    }

                    //如果标记为与分叉链相连，orphanChain不会复制到新的孤儿链集合，也不会进入分叉链集合，但是所有orphanChain的直接子链标记为ChainTypeEnum.FORK_FORK
                    if (orphanChain.getType().equals(ChainTypeEnum.FORK_APPEND)) {
                        orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
                        continue;
                    }
                    //如果标记为从分叉链分叉，orphanChain不会复制到新的孤儿链集合，但是会进入分叉链集合，所有orphanChain的直接子链标记为ChainTypeEnum.FORK_FORK
                    if (orphanChain.getType().equals(ChainTypeEnum.FORK_FORK)) {
                        ChainManager.addForkChain(chainId, orphanChain);
                        orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
                        continue;
                    }

                    //如果标记为与孤儿链相连，不会复制到新的孤儿链集合，所有orphanChain的直接子链会复制到新的孤儿链集合，类型不变
                    if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN_APPEND)) {
                        continue;
                    }
                    //如果标记为孤儿链(未变化)，或者从孤儿链分叉，复制到新的孤儿链集合
                    if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN)) {
                        maintainedOrphanChains.add(orphanChain);
                    }
                }
                ChainManager.setOrphanChains(chainId, maintainedOrphanChains);
            } catch (Exception e) {
                Log.error("chainId-{},maintain OrphanChains fail!error msg is:{}", chainId, e.getMessage());
            }
        }
    }

    private void handle(Chain orphanChain, Chain masterChain, SortedSet<Chain> forkChains, SortedSet<Chain> orphanChains) throws Exception {
        //1.判断与主链是否相连
        if (orphanChain.getParent() == null && tryAppend(masterChain, orphanChain)) {
            orphanChain.setType(ChainTypeEnum.MASTER_APPEND);
            return;
        }
        //2.判断是否从主链分叉
        if (orphanChain.getParent() == null && tryFork(masterChain, orphanChain)) {
            orphanChain.setType(ChainTypeEnum.MASTER_FORK);
        }

        for (Chain forkChain : forkChains) {
            //3.判断与分叉链是否相连
            if (orphanChain.getParent() == null && tryAppend(forkChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.FORK_APPEND);
                break;
            }
            //4.判断是否从分叉链分叉
            if (orphanChain.getParent() == null && tryFork(forkChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.FORK_FORK);
                break;
            }
        }

        for (Chain anotherOrphanChain : orphanChains) {
            //排除自身
            if (anotherOrphanChain.equals(orphanChain)) {
                continue;
            }
            //5.判断与孤儿链是否相连
            if (anotherOrphanChain.getParent() == null && tryAppend(orphanChain, anotherOrphanChain)) {
                anotherOrphanChain.setType(ChainTypeEnum.ORPHAN_APPEND);
                continue;
            }
            if (orphanChain.getParent() == null && tryAppend(anotherOrphanChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.ORPHAN_APPEND);
                continue;
            }

            //6.判断是否从孤儿链分叉
            if (anotherOrphanChain.getParent() == null && tryFork(orphanChain, anotherOrphanChain)) {
                continue;
            }
            if (orphanChain.getParent() == null && tryFork(anotherOrphanChain, orphanChain)) {
                continue;
            }
        }
    }

    /**
     * 尝试把subChain链接到mainChain的末尾，形成mainChain-subChain的结构
     * 两个链相连成功，需要从孤儿链集合中删除subChain
     * @param mainChain
     * @param subChain
     * @return
     */
    private boolean tryAppend(Chain mainChain, Chain subChain) throws Exception {
        if (mainChain.getEndHeight() + 1 == subChain.getStartHeight() && mainChain.getEndHash().equals(subChain.getPreviousHash())) {
            return ChainManager.append(mainChain, subChain);
        }
        return false;
    }

    /**
     * 尝试把subChain分叉到mainChain上
     * 分叉不需要从链集合中删除分叉的链
     * @param mainChain
     * @param subChain
     * @return
     */
    private boolean tryFork(Chain mainChain, Chain subChain) throws Exception {
        if (mainChain.getHashList().contains(subChain.getPreviousHash())) {
            return ChainManager.fork(mainChain, subChain);
        }
        return false;
    }

    /**
     * 维护孤儿链，向其他节点请求孤儿链起始区块的上一个区块，仅限于没有父链的孤儿链
     * todo 异步
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
