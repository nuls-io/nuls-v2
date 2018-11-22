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
import io.nuls.block.model.Chain;
import io.nuls.block.service.BlockService;
import io.nuls.block.service.ChainStorageService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;

import java.util.*;

/**
 * 分叉链管理器，维护主链、分叉链集合、孤儿链集合
 * @author captain
 * @date 18-11-16 下午2:29
 * @version 1.0
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
    private static Map<Integer, List<Chain>> forkChains = new HashMap<>();

    /**
     * 本机运行的所有孤儿链集合，按照chainID区分
     */
    private static Map<Integer, List<Chain>> orphanChains = new HashMap<>();

    /**
     * todo 链切换待完善
     * forkChain比masterChain更长，切换主链
     * @param chainId
     * @param masterChain
     * @param forkChain
     * @return
     */
    public static boolean switchChain (int chainId, Chain masterChain, Chain forkChain){
        try {
            //1.获取主链与最长分叉链的分叉点
            Chain topForkChain = forkChain.clone();
            while (!topForkChain.getParent().isMaster()) {
                topForkChain = topForkChain.getParent();
            }
            long forkHeight = topForkChain.getStartHeight();
            long masterChainEndHeight = masterChain.getEndHeight();

            //主链回滚所生成的新分叉链
            Chain masterForkChain = new Chain();
            LinkedList<NulsDigestData> hashList = new LinkedList<>();
            List<Block> blockList = new ArrayList<>();
            masterForkChain.setMaster(false);
            masterForkChain.setParent(masterChain);
            masterForkChain.setStartHeight(forkHeight);
            masterForkChain.setEndHeight(masterChainEndHeight);
            masterForkChain.setChainId(chainId);
//            masterForkChain.setPreviousHash();
            masterForkChain.setHashList(hashList);

            long rollbackHeight = masterChainEndHeight;
            do{
                //2.回滚主链到指定高度，回滚掉的区块收集起来组成新的分叉链
                Block block = blockService.rollbackBlock(chainId, rollbackHeight--);
                //block非空说明回滚成功
                if (block != null) {
                    blockList.add(block);
                    hashList.addLast(block.getHeader().getHash());
                } else {
                    throw new NulsRuntimeException(BlockErrorCode.ROLLBACK_CHAIN_ERROR);
                }
            } while (rollbackHeight < forkHeight);

            //3.主链已经回滚到分叉点，开始添加新区块


            //4.移除已经成为主链的那些分叉链，保存新生成的分叉链
            Chain removeForkChain = forkChain.clone();
            while (!removeForkChain.getParent().isMaster()) {
                removeForkChain(chainId, removeForkChain);
                removeForkChain = removeForkChain.getParent();
            }
            addForkChain(chainId, masterForkChain);
            chainStorageService.save(chainId, blockList);
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 设置主链
     * @param chainId
     * @return
     */
    public static Chain setMasterChain(int chainId, Chain chain){
        return masterChains.put(chainId, chain);
    }

    /**
     * 获取主链
     * @param chainId
     * @return
     */
    public static Chain getMasterChain(int chainId){
        return masterChains.get(chainId);
    }

    /**
     * 新增分叉链
     * @param chainId
     * @return
     */
    public static boolean addForkChain(int chainId, Chain chain){
        return forkChains.get(chainId).add(chain);
    }

    /**
     * 移除分叉链
     * @param chainId
     * @return
     */
    public static void removeForkChain(int chainId, Chain chain) throws Exception {
        //无子链
        if (chain.getSons().size() == 0) {
            //更新父链的引用
            chain.getParent().getSons().remove(chain);
            //移除区块存储
            chainStorageService.remove(chainId, chain.getHashList());
            //移除内存中对象
            forkChains.get(chainId).remove(chain);
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
            chainStorageService.remove(chainId, removeHashList);
            //移除内存中对象
            forkChains.get(chainId).remove(lastSon);
        }
    }

    /**
     * 获取分叉链集合
     * @param chainId
     * @return
     */
    public static List<Chain> getForkChains(int chainId){
        return forkChains.get(chainId);
    }

    /**
     * 新增孤儿链
     * @param chainId
     * @return
     */
    public static boolean addOrphanChain(int chainId, Chain chain){
        return orphanChains.get(chainId).add(chain);
    }

    /**
     * 移除孤儿链
     * @param chainId
     * @return
     */
    public static void removeOrphanChain(int chainId, Chain chain) throws Exception {
        //无子链
        if (chain.getSons().size() == 0) {
            if (chain.getParent() != null) {
                //更新父链的引用
                chain.getParent().getSons().remove(chain);
            }
            //移除区块存储
            chainStorageService.remove(chainId, chain.getHashList());
            //移除内存中对象
            orphanChains.get(chainId).remove(chain);
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
            chainStorageService.remove(chainId, removeHashList);
            //移除内存中对象
            orphanChains.get(chainId).remove(lastSon);
        }
    }

    /**
     * 获取孤儿链集合
     * @param chainId
     * @return
     */
    public static List<Chain> getOrphanChains(int chainId){
        return orphanChains.get(chainId);
    }

    /**
     * 两个链相连
     * 如果mainChain是主链，除更新内存中chain的属性外，还需要把subChain的区块提交到主链
     * 如果mainChain不是主链，只需要更新内存中chain的属性，不需要操作区块数据
     * @param mainChain
     * @param subChain
     * @return
     */
    public static boolean merge(Chain mainChain, Chain subChain) throws Exception {
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
}
