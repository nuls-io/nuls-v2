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

package io.nuls.block.manager;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.model.Chain;
import io.nuls.block.service.BlockService;
import io.nuls.block.service.ChainStorageService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.util.*;

/**
 * 分叉链管理器，维护主链、分叉链集合、孤儿链集合
 *
 * @author captain
 * @version 1.0
 * @date 18-11-16 下午2:29
 */
@Component
public class ChainManager {

    @Autowired
    private static BlockService blockService;
    @Autowired
    private static ChainStorageService chainStorageService;

    /**
     * 本机运行的所有主链集合，按照chainID区分
     */
    private static Map<Integer, Chain> masterChains = new HashMap<>();

    /**
     * 本机运行的所有分叉链集合，按照chainID区分
     */
    private static Map<Integer, SortedSet<Chain>> forkChains = new HashMap<>();

    /**
     * 本机运行的所有孤儿链集合，按照chainID区分
     */
    private static Map<Integer, SortedSet<Chain>> orphanChains = new HashMap<>();

    /**
     * forkChain比masterChain更长，切换主链
     * 切换分三步
     * 1.计算出最长分叉链与主链的分叉点，并得到要切换成主链的分叉链集合B
     * 2.回滚主链到分叉高度。
     * 3.依次添加分叉链集合B中的区块到主链
     *
     * @param chainId
     * @param masterChain
     * @param forkChain
     * @return
     */
    public static boolean switchChain(int chainId, Chain masterChain, Chain forkChain) {
        try {
            //1.获取主链与最长分叉链的分叉点，并记录从分叉点开始的最长分叉链路径
            Stack<Chain> switchChainPath = new Stack<>();
            while (forkChain.getParent() != null) {
                switchChainPath.push(forkChain);
                forkChain = forkChain.getParent();
            }
            Chain topForkChain = switchChainPath.peek();
            long forkHeight = topForkChain.getStartHeight();
            long masterChainEndHeight = masterChain.getEndHeight();

            //2.回滚主链
            //2.1 主链回滚所生成的新分叉链
            Chain masterForkChain = new Chain();
            LinkedList<NulsDigestData> hashList = new LinkedList<>();
            List<Block> blockList = new ArrayList<>();
            masterForkChain.setParent(masterChain);
            masterForkChain.setStartHeight(forkHeight);
            masterForkChain.setEndHeight(masterChainEndHeight);
            masterForkChain.setChainId(chainId);
            masterForkChain.setPreviousHash(topForkChain.getPreviousHash());
            masterForkChain.setHashList(hashList);
            masterForkChain.setType(ChainTypeEnum.FORK);

            //2.2 回滚主链到指定高度，回滚掉的区块收集起来放入分叉链数据库
            long rollbackHeight = masterChainEndHeight;
            do {
                Block block = blockService.rollbackBlock(chainId, rollbackHeight--);
                //block非空说明回滚成功
                if (block != null) {
                    blockList.add(block);
                    hashList.addLast(block.getHeader().getHash());
                } else {
                    return false;
                }
            } while (rollbackHeight >= forkHeight);

            //2.3 主链上低于topForkChain的链不用变动
            //2.4 主链上高于topForkChain的链重新链接到新分叉链masterForkChain
            SortedSet<Chain> higherChains = masterChain.getSons().tailSet(topForkChain);
            if (higherChains.size() > 1) {
                higherChains.remove(topForkChain);
                masterForkChain.setSons(higherChains);
                higherChains.forEach(e -> e.setParent(masterForkChain));
            }
            addForkChain(chainId, masterForkChain);
            chainStorageService.save(chainId, blockList);
            //至此，主链回滚完成
            Log.info("masterChain rollback complete");

            //3.依次添加最长分叉链路径上所有分叉链区块
            Chain chain = null;
            while (!switchChainPath.empty()) {
                chain = switchChainPath.pop();
                Chain subChain = switchChainPath.empty() ? null : switchChainPath.peek();
                boolean b = switchChain0(chainId, masterChain, chain, subChain);
                if (!b) {
                    throw new NulsRuntimeException(BlockErrorCode.ROLLBACK_CHAIN_ERROR);
                }
            }

        } catch (Exception e) {
            Log.error(e);
            return false;
        }
        return true;
    }

    /**
     * 从分叉链forkChain上取区块添加到主链masterChain上
     * 取多少个区块由forkChain与subChain的起始高度差计算得出
     * subChain是forkChain的子链之一，位于最长分叉链的路径上
     *
     * @param masterChain
     * @param forkChain
     * @param subChain
     * @return
     */
    private static boolean switchChain0(int chainId, Chain masterChain, Chain forkChain, Chain subChain) throws Exception {
        //1.计算要从forkChain上添加到主链上多少个区块
        //todo 待核实
        int target = 0;
        if (subChain != null) {
            target = (int) (subChain.getStartHeight() - forkChain.getStartHeight());
        } else {
            target = (int) (forkChain.getEndHeight() - forkChain.getStartHeight()) + 1;
        }

        //2.往主链上添加区块
        int count = 0;
        while (target > count) {
            NulsDigestData hash = forkChain.getHashList().pop();
            Block block = chainStorageService.query(chainId, hash);
            boolean saveBlock = blockService.saveBlock(chainId, block);
            if (saveBlock) {
                count++;
            } else {
                return false;
            }
        }

        //3.上一步结束后，如果forkChain中还有区块，组成新的分叉链，连接到主链上
        if (forkChain.getHashList().size() > 0) {
            Chain newForkChain = new Chain();
            newForkChain.setChainId(chainId);
            newForkChain.setStartHeight(target + forkChain.getStartHeight());
            newForkChain.setParent(masterChain);
            newForkChain.setEndHeight(forkChain.getEndHeight());
            newForkChain.setPreviousHash(subChain.getPreviousHash());
            newForkChain.setHashList(forkChain.getHashList());

            //4.低于subChain的链重新链接到主链masterChain
            SortedSet<Chain> lowerSubChains = forkChain.getSons().headSet(subChain);
            if (lowerSubChains.size() > 0) {
                lowerSubChains.forEach(e -> e.setParent(masterChain));
                masterChain.getSons().addAll(lowerSubChains);
            }

            //5.高于subChain的链重新链接到新生成的分叉链newForkChain
            SortedSet<Chain> higherSubChains = forkChain.getSons().tailSet(subChain);
            if (higherSubChains.size() > 1) {
                higherSubChains.remove(subChain);
                higherSubChains.forEach(e -> e.setParent(newForkChain));
                newForkChain.setSons(higherSubChains);
            }
            addForkChain(chainId, newForkChain);
        }

        //6.收尾工作
        deleteForkChain(chainId, forkChain);
        return true;
    }

    /**
     * 设置主链
     *
     * @param chainId
     * @return
     */
    public static void setMasterChain(int chainId, Chain chain) {
        masterChains.put(chainId, chain);
    }

    /**
     * 获取主链
     *
     * @param chainId
     * @return
     */
    public static Chain getMasterChain(int chainId) {
        return masterChains.get(chainId);
    }

    /**
     * 新增分叉链
     *
     * @param chainId
     * @return
     */
    public static boolean addForkChain(int chainId, Chain chain) {
        SortedSet<Chain> chains = forkChains.get(chainId);
        if (chains != null) {
            return chains.add(chain);
        } else {
            chains = new TreeSet<>(Chain.COMPARATOR);
            forkChains.put(chainId, chains);
            return chains.add(chain);
        }
    }

    /**
     * 直接从集合中删除分叉链，与removeForkChain的应用场景不一样
     *
     * @param chainId
     * @return
     */
    private static boolean deleteForkChain(int chainId, Chain chain) {
        return forkChains.get(chainId).remove(chain);
    }

    /**
     * 移除分叉链，分叉链占用空间超出限制时，清理空间
     *
     * @param chainId
     * @return
     */
    public static boolean removeForkChain(int chainId, Chain chain) throws Exception {
        boolean result = false;
        //无子链
        if (chain.getSons().size() == 0) {
            //更新父链的引用
            boolean r1 = chain.getParent().getSons().remove(chain);
            //移除区块存储
            boolean r2 = chainStorageService.remove(chainId, chain.getHashList());
            //移除内存中对象
            boolean r3 = forkChains.get(chainId).remove(chain);
            result = r1 && r2 && r3;
        }
        //有子链
        if (chain.getSons().size() > 0) {
            Chain lastSon = chain.getSons().last();
            //要从chain上移除多少个hash
            long remove = chain.getEndHeight() - lastSon.getStartHeight() + 1;
            List<NulsDigestData> removeHashList = new ArrayList<>();
            while (remove > 0) {
                NulsDigestData data = chain.getHashList().pollLast();
                removeHashList.add(data);
                remove--;
            }
            //更新chain的属性
            chain.getHashList().addAll(lastSon.getHashList());
            chain.setEndHeight(lastSon.getEndHeight());

            if (lastSon.getSons().size() > 0) {
                for (Chain son : lastSon.getSons()) {
                    son.setParent(chain);
                }
            }
            //移除区块存储
            boolean r1 = chainStorageService.remove(chainId, removeHashList);
            //移除内存中对象
            boolean r2 = forkChains.get(chainId).remove(lastSon);
            result = r1 && r2;
        }
        return result;
    }

    /**
     * 获取分叉链集合
     *
     * @param chainId
     * @return
     */
    public static SortedSet<Chain> getForkChains(int chainId) {
        SortedSet<Chain> chains = forkChains.get(chainId);
        return chains == null ? Collections.emptySortedSet() : chains;
    }

    /**
     * 更新分叉链集合
     *
     * @param chainId
     * @return
     */
    public static void setForkChains(int chainId, SortedSet<Chain> chains) {
        forkChains.put(chainId, chains);
    }

    /**
     * 新增孤儿链
     *
     * @param chainId
     * @return
     */
    public static boolean addOrphanChain(int chainId, Chain chain) {
        SortedSet<Chain> chains = orphanChains.get(chainId);
        if (chains != null) {
            return chains.add(chain);
        } else {
            chains = new TreeSet<>(Chain.COMPARATOR);
            orphanChains.put(chainId, chains);
            return chains.add(chain);
        }
    }

    /**
     * 移除孤儿链
     * 孤儿链占用空间超出限制时，清理空间
     *
     * @param chainId
     * @param chain
     * @throws Exception
     */
    public static boolean removeOrphanChain(int chainId, Chain chain) throws Exception {
        boolean result = false;
        //无子链
        if (chain.getSons().size() == 0) {
            if (chain.getParent() != null) {
                //更新父链的引用
                boolean r1 = chain.getParent().getSons().remove(chain);
                result = r1;
            }
            //移除区块存储
            boolean r2 = chainStorageService.remove(chainId, chain.getHashList());
            //移除内存中对象
            boolean r3 = orphanChains.get(chainId).remove(chain);
            result = r2 && r3;
        }
        //有子链
        if (chain.getSons().size() > 0) {
            Chain lastSon = chain.getSons().last();
            //要从chain上移除多少个hash
            long remove = chain.getEndHeight() - lastSon.getStartHeight() + 1;
            List<NulsDigestData> removeHashList = new ArrayList<>();
            while (remove > 0) {
                NulsDigestData data = chain.getHashList().pollLast();
                removeHashList.add(data);
                remove--;
            }
            //更新chain的属性
            chain.getHashList().addAll(lastSon.getHashList());
            chain.setEndHeight(lastSon.getEndHeight());

            if (lastSon.getSons().size() > 0) {
                for (Chain son : lastSon.getSons()) {
                    son.setParent(chain);
                }
            }
            //移除区块存储
            boolean r1 = chainStorageService.remove(chainId, removeHashList);
            //移除内存中对象
            boolean r2 = orphanChains.get(chainId).remove(lastSon);
            result = r1 && r2;
        }
        return result;
    }

    /**
     * 获取孤儿链集合
     *
     * @param chainId
     * @return
     */
    public static SortedSet<Chain> getOrphanChains(int chainId) {
        SortedSet<Chain> chains = orphanChains.get(chainId);
        return chains == null ? Collections.emptySortedSet() : chains;
    }

    /**
     * 更新孤儿链集合
     *
     * @param chainId
     * @return
     */
    public static void setOrphanChains(int chainId, SortedSet<Chain> chains) {
        orphanChains.put(chainId, chains);
    }

    /**
     * 两个链相连，形成mainChain-subChain结构
     * 如果mainChain是主链，除更新内存中chain的属性外，还需要把subChain的区块提交到主链
     * 如果mainChain不是主链，只需要更新内存中chain的属性，不需要操作区块数据
     *
     * @param mainChain
     * @param subChain  能连到其他链的一定是孤儿链，因为分叉链是从一个分叉区块开始构建的，分叉链初始化时已经设置了parent属性
     * @return
     */
    public static boolean append(Chain mainChain, Chain subChain) throws Exception {
        int chainId = mainChain.getChainId();
        if (mainChain.isMaster()) {
            List<Block> blockList = chainStorageService.query(subChain.getChainId(), subChain.getHashList());
            for (Block block : blockList) {
                if (!blockService.saveBlock(chainId, block)) {
                    throw new NulsRuntimeException(BlockErrorCode.CHAIN_MERGE_ERROR);
                }
            }
        }
        mainChain.append(subChain);
        return true;
    }

    /**
     * 从mainChain分叉出subChain
     * 只需要更新内存中chain的属性，不需要操作区块数据
     *
     * @param mainChain
     * @param forkChain
     * @return
     */
    public static boolean fork(Chain mainChain, Chain forkChain) {
        return mainChain.fork(forkChain);
    }

    /**
     * 清理孤儿链
     *
     * @param chainId
     * @param orphanChain
     */
    public static void deleteOrphanChain(Integer chainId, Chain orphanChain, boolean recursively) throws Exception {
        orphanChains.get(chainId).remove(orphanChain);
        chainStorageService.remove(chainId, orphanChain.getHashList());
        if (recursively) {
            for (Chain son : orphanChain.getSons()) {
                deleteOrphanChain(chainId, son, true);
            }
        }
    }
}
