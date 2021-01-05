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

package io.nuls.block.manager;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.model.Chain;
import io.nuls.block.model.CheckResult;
import io.nuls.block.rpc.call.ConsensusCall;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.block.service.BlockService;
import io.nuls.block.storage.ChainStorageService;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.*;

import static io.nuls.block.constant.Constant.MODULE_WAITING;
import static io.nuls.block.constant.Constant.MODULE_WORKING;

/**
 * 链管理器,维护主链、分叉链集合、孤儿链集合
 *
 * @author captain
 * @version 1.0
 * @date 18-11-16 下午2:29
 */
@Component
public class BlockChainManager {

    @Autowired
    private static BlockService blockService;
    @Autowired
    private static ChainStorageService chainStorageService;

    /**
     * 本机运行的所有主链集合,按照chainId区分
     */
    private static Map<Integer, Chain> masterChains = new HashMap<>();

    /**
     * 本机运行的所有分叉链集合,按照chainId区分
     */
    private static Map<Integer, SortedSet<Chain>> forkChains = new HashMap<>();

    /**
     * 本机运行的所有孤儿链集合,按照chainId区分
     */
    private static Map<Integer, SortedSet<Chain>> orphanChains = new HashMap<>();

    /**
     * forkChain比masterChain更长,切换主链
     * 切换分三步
     * 1.计算出最长分叉链与主链的分叉点,并得到要切换成主链的分叉链集合B
     * 2.回滚主链到分叉高度.
     * 3.依次添加分叉链集合B中的区块到主链
     *
     * @param chainId     链Id/chain id
     * @param masterChain
     * @param forkChain
     * @return
     */
    public static CheckResult switchChain(int chainId, Chain masterChain, Chain forkChain) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            logger.info("*switch chain start");
            logger.info("*masterChain-" + masterChain);
            logger.info("*forkChain-" + forkChain);
            //1.获取主链与最长分叉链的分叉点,并记录从分叉点开始的最长分叉链路径
            Deque<Chain> switchChainPath = new ArrayDeque<>();
            while (forkChain.getParent() != null) {
                switchChainPath.push(forkChain);
                forkChain = forkChain.getParent();
            }
            Chain topForkChain = switchChainPath.peek();
            long forkHeight = topForkChain.getStartHeight();
            long masterChainEndHeight = masterChain.getEndHeight();
            if (masterChainEndHeight < forkHeight) {
                logger.error("*masterChainEndHeight < forkHeight, data error");
                //重置网络
                NetworkCall.resetNetwork(chainId);
                //重新开启区块同步线程
                ConsensusCall.notice(chainId, MODULE_WAITING);
                TransactionCall.notice(chainId, MODULE_WAITING);
                BlockSynchronizer.syn(chainId);
                return new CheckResult(false, true);
            }
            logger.info("*calculate fork point complete, forkHeight=" + forkHeight);

            //2.回滚主链
            //2.1 回滚主链到指定高度,回滚掉的区块收集起来放入分叉链数据库
            ArrayDeque<NulsHash> hashList = new ArrayDeque<>();
            Stack<Block> blockStack = new Stack<>();
            long rollbackHeight = masterChainEndHeight;
            logger.info("*rollback master chain begin, rollbackHeight=" + rollbackHeight);
            do {
                Block block = blockService.getBlock(chainId, rollbackHeight--);
                NulsHash hash = block.getHeader().getHash();
                if (blockService.rollbackBlock(chainId, BlockUtil.toBlockHeaderPo(block), false)) {
                    blockStack.push(block);
                    hashList.offerFirst(hash);
                    logger.info("*rollback master chain doing, success hash=" + hash);
                } else {
                    logger.info("*rollback master chain doing, fail hash=" + hash);
                    saveBlockToMasterChain(chainId, blockStack);
                    return new CheckResult(false, false);
                }
            } while (rollbackHeight >= forkHeight);
            logger.info("*rollback master chain end");
            //2.2 主链回滚所生成的新分叉链
            Chain masterForkChain = new Chain();
            masterForkChain.setParent(masterChain);
            masterForkChain.setStartHeight(forkHeight);
            masterForkChain.setEndHeight(masterChainEndHeight);
            masterForkChain.setChainId(chainId);
            masterForkChain.setPreviousHash(topForkChain.getPreviousHash());
            masterForkChain.setHashList(hashList);
            masterForkChain.setType(ChainTypeEnum.FORK);
            masterForkChain.setStartHashCode(hashList.getFirst().hashCode());
            logger.info("*generate new masterForkChain chain-" + masterForkChain);
            //2.3 主链上低于topForkChain的链不用变动
            //2.4 主链上高于topForkChain的链重新链接到新分叉链masterForkChain
            SortedSet<Chain> higherChains = masterChain.getSons().tailSet(topForkChain);
            if (higherChains.size() > 1) {
                logger.info("*higher than topForkChain-" + higherChains);
                higherChains.remove(topForkChain);
                masterForkChain.setSons(higherChains);
                higherChains.forEach(e -> e.setParent(masterForkChain));
            }
            addForkChain(chainId, masterForkChain);
            if (!chainStorageService.save(chainId, blockStack)) {
                logger.info("*error occur when save masterForkChain");
                append(masterChain, masterForkChain);
                return new CheckResult(false, false);
            }
            //至此,主链回滚完成
            logger.info("*masterChain rollback complete");

            //3.依次添加最长分叉链路径上所有分叉链区块
            List<Chain> delete = new ArrayList<>();
            while (!switchChainPath.isEmpty()) {
                Chain chain = switchChainPath.pop();
                delete.add(chain);
                Chain subChain = switchChainPath.isEmpty() ? null : switchChainPath.peek();
                boolean b = switchChain0(chainId, masterChain, chain, subChain);
                if (!b) {
                    //切换链失败,恢复主链
                    //首先把切换失败过程中加到主链上的区块回滚掉
                    while (masterChain.getEndHeight() >= forkHeight) {
                        blockService.rollbackBlock(chainId, masterChain.getEndHeight(), false);
                    }
                    logger.info("*switchChain0 fail masterChain-" + masterChain + ",chain-" + chain + ",subChain-" +
                            subChain + ",masterForkChain-" + masterForkChain);
                    deleteForkChain(chainId, topForkChain, true);
                    append(masterChain, masterForkChain);
                    return new CheckResult(false, false);
                }
            }
            //6.收尾工作
            delete.forEach(e -> deleteForkChain(chainId, e, false));
            logger.info("*switch chain complete");
        } catch (Exception e) {
            logger.error("block chain switch fail, auto rollback fail, process exit.");
            System.exit(1);
        }
        return new CheckResult(true, false);
    }

    private static void saveBlockToMasterChain(int chainId, Stack<Block> blockStack) {
        //主链回滚中途失败,把前面回滚的区块再加回主链
        while (!blockStack.empty()) {
            if (!blockService.saveBlock(chainId, blockStack.pop(), false)) {
                ContextManager.getContext(chainId).getLogger().error("block chain switch fail, auto rollback fail, process exit.");
                System.exit(1);
            }
        }
    }

    /**
     * 从分叉链forkChain上取区块添加到主链masterChain上
     * 取多少个区块由forkChain与subChain的起始高度差计算得出
     * subChain是forkChain的子链之一,位于最长分叉链的路径上
     *
     * @param masterChain
     * @param forkChain
     * @param subChain
     * @return
     */
    private static boolean switchChain0(int chainId, Chain masterChain, Chain forkChain, Chain subChain) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        logger.info("*switchChain0 masterChain=" + masterChain + ",forkChain=" + forkChain + ",subChain=" + subChain);
        //1.计算要从forkChain上添加到主链上多少个区块
        int target;
        if (subChain != null) {
            target = (int) (subChain.getStartHeight() - forkChain.getStartHeight());
        } else {
            target = (int) (forkChain.getEndHeight() - forkChain.getStartHeight()) + 1;
        }
        logger.info("*switchChain0 target=" + target);
        //2.往主链上添加区块
        Deque<NulsHash> hashList = ((ArrayDeque<NulsHash>) forkChain.getHashList()).clone();
        int count = 0;
        while (target > count) {
            NulsHash hash = hashList.pop();
            Block block = chainStorageService.query(chainId, hash);
            boolean saveBlock = blockService.saveBlock(chainId, block, false);
            if (saveBlock) {
                count++;
            } else {
                logger.info("*switchChain0 saveBlock fail, hash=" + hash);
                return false;
            }
        }
        logger.info("*switchChain0 add block to master chain success");
        //3.上一步结束后,如果forkChain中还有区块,组成新的分叉链,连接到主链上
        if (!hashList.isEmpty()) {
            Chain newForkChain = new Chain();
            newForkChain.setChainId(chainId);
            newForkChain.setStartHeight(target + forkChain.getStartHeight());
            newForkChain.setParent(masterChain);
            newForkChain.setEndHeight(forkChain.getEndHeight());
            newForkChain.setPreviousHash(subChain.getPreviousHash());
            newForkChain.setHashList(hashList);
            newForkChain.setStartHashCode(hashList.getFirst().hashCode());
            logger.info("*switchChain0 newForkChain-" + newForkChain);

            //4.低于subChain的链重新链接到主链masterChain
            SortedSet<Chain> lowerSubChains = forkChain.getSons().headSet(subChain);
            if (!lowerSubChains.isEmpty()) {
                lowerSubChains.forEach(e -> e.setParent(masterChain));
                masterChain.getSons().addAll(lowerSubChains);
                lowerSubChains.forEach(e -> logger.info("*switchChain0 lowerSubChains-" + e));
            }

            //5.高于subChain的链重新链接到新生成的分叉链newForkChain
            SortedSet<Chain> higherSubChains = forkChain.getSons().tailSet(subChain);
            if (higherSubChains.size() > 1) {
                higherSubChains.remove(subChain);
                higherSubChains.forEach(e -> e.setParent(newForkChain));
                newForkChain.setSons(higherSubChains);
                higherSubChains.forEach(e -> logger.info("*switchChain0 higherSubChains-" + e));
            }
            addForkChain(chainId, newForkChain);
        }
        return true;
    }

    /**
     * 设置主链
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static void setMasterChain(int chainId, Chain chain) {
        masterChains.put(chainId, chain);
    }

    /**
     * 获取主链
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static Chain getMasterChain(int chainId) {
        return masterChains.get(chainId);
    }

    /**
     * 新增分叉链
     *
     * @param chainId 链Id/chain id
     */
    public static void addForkChain(int chainId, Chain chain) {
        boolean add = forkChains.get(chainId).add(chain);
        if (!add) {
            ContextManager.getContext(chainId).getLogger().warn("add fail, forkChain-" + chain);
        }
    }

    /**
     * 递归删除分叉链
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static void deleteForkChain(int chainId, Chain forkChain, boolean recursive) {
        forkChains.get(chainId).remove(forkChain);
        chainStorageService.remove(chainId, forkChain.getHashList());
        ContextManager.getContext(chainId).getLogger().info("delete Fork Chain-" + forkChain);
        if (recursive && !forkChain.getSons().isEmpty()) {
            forkChain.getSons().forEach(e -> deleteForkChain(chainId, e, true));
        }
    }

    /**
     * 获取分叉链集合
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static SortedSet<Chain> getForkChains(int chainId) {
        SortedSet<Chain> chains = forkChains.get(chainId);
        return chains == null ? Collections.emptySortedSet() : chains;
    }

    /**
     * 更新分叉链集合
     *
     * @param chainId 链Id/chain id
     */
    public static void setForkChains(int chainId, SortedSet<Chain> chains) {
        forkChains.put(chainId, chains);
    }

    /**
     * 新增孤儿链
     *
     * @param chainId 链Id/chain id
     */
    public static void addOrphanChain(int chainId, Chain chain) {
        boolean add = orphanChains.get(chainId).add(chain);
        if (!add) {
            ContextManager.getContext(chainId).getLogger().warn("add fail, orphanChain-" + chain);
        }
    }

    /**
     * 获取孤儿链集合
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static SortedSet<Chain> getOrphanChains(int chainId) {
        SortedSet<Chain> chains = orphanChains.get(chainId);
        return chains == null ? Collections.emptySortedSet() : chains;
    }

    /**
     * 更新孤儿链集合
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static void setOrphanChains(int chainId, SortedSet<Chain> chains) {
        orphanChains.put(chainId, chains);
    }

    /**
     * 两个链相连,形成mainChain-subChain结构
     * 如果mainChain是主链,除更新内存中chain的属性外,还需要把subChain的区块提交到主链
     * 如果mainChain不是主链,只需要更新内存中chain的属性,不需要操作区块数据
     *
     * @param mainChain
     * @param subChain  能连到其他链的一定是孤儿链,因为分叉链是从一个分叉区块开始构建的,分叉链初始化时已经设置了parent属性
     * @return
     */
    public static boolean append(Chain mainChain, Chain subChain) {
        int chainId = mainChain.getChainId();
        if (mainChain.isMaster()) {
            ConsensusCall.notice(chainId, MODULE_WAITING);
            TransactionCall.notice(chainId, MODULE_WAITING);
            List<Block> blockList = chainStorageService.query(subChain.getChainId(), subChain.getHashList());
            List<Block> savedBlockList = new ArrayList<>();
            for (Block block : blockList) {
                if (!blockService.saveBlock(chainId, block, false)) {
                    for (int i = savedBlockList.size() - 1; i >= 0; i--) {
                        if (!blockService.rollbackBlock(chainId, savedBlockList.get(i).getHeader().getHeight(), false)) {
                            ContextManager.getContext(chainId).getLogger().error("block chain data error, can't restore, system exit");
                            System.exit(1);
                        }
                    }
                    throw new NulsRuntimeException(BlockErrorCode.CHAIN_SWITCH_ERROR);
                } else {
                    savedBlockList.add(block);
                }
            }
            ConsensusCall.notice(chainId, MODULE_WORKING);
            TransactionCall.notice(chainId, MODULE_WORKING);
        }
        if (!mainChain.isMaster()) {
            mainChain.getHashList().addAll(subChain.getHashList());
        }
        mainChain.setEndHeight(subChain.getEndHeight());
        mainChain.getSons().addAll(subChain.getSons());
        subChain.getSons().forEach(e -> e.setParent(mainChain));
        subChain.setParent(mainChain);
        return true;
    }

    /**
     * 从mainChain分叉出subChain
     * 只需要更新内存中chain的属性,不需要操作区块数据
     *
     * @param mainChain
     * @param forkChain
     * @return
     */
    public static boolean fork(Chain mainChain, Chain forkChain) {
        forkChain.setParent(mainChain);
        return mainChain.getSons().add(forkChain);
    }

    /**
     * 递归删除孤儿链
     *
     * @param chainId     链Id/chain id
     * @param orphanChain 要删除的孤儿链
     */
    public static void deleteOrphanChain(int chainId, Chain orphanChain) {
        orphanChains.get(chainId).remove(orphanChain);
        chainStorageService.remove(chainId, orphanChain.getHashList());
        ContextManager.getContext(chainId).getLogger().info("delete Orphan Chain-" + orphanChain);
        if (!orphanChain.getSons().isEmpty()) {
            orphanChain.getSons().forEach(e -> deleteOrphanChain(chainId, e));
        }
    }

    /**
     * 初始化
     *
     * @param chainId 链Id/chain id
     */
    public static void init(int chainId) {
        forkChains.put(chainId, new TreeSet<>(Chain.COMPARATOR));
        orphanChains.put(chainId, new TreeSet<>(Chain.COMPARATOR));
    }
}
