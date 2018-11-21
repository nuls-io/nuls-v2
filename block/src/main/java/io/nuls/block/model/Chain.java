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
package io.nuls.block.model;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.manager.ContextManager;
import lombok.Data;

import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 链对象
 * 每一个链ID对应的链都可能形成下图的结构
 * <pre>
 * |--------------------主链
 * |
 * |
 * |
 * |____
 * |    |---------------分叉链1
 * |    |                               |------------孤儿链1
 * |    |                               |
 * |    |                               |
 * |    |                               |___
 * |                                    |   |--------孤儿链1-1
 * |                                    |   |
 * |____                                |   |
 * |    |---------------分叉链2
 * |    |
 * |    |____
 * |    |    |----------分叉链2-1
 * |    |    |
 * |    |    |
 * |    |    |
 * <pre/>
 * 主链上区块的存储是由区块管理模块与交易管理模块共同完成，其中区块管理模块存储区块头，交易管理模块存储交易数据。
 * 分叉链的区块存储由区块管理模块全权负责，数据库中缓存(blockHash, blockData)，至于每一个hash对应的区块属于哪一个分叉链，则由内存中的Chain对象进行维护
 * 系统正常运行时，内存中会维护主链-masterChain、分叉链集合-forkChains、孤儿链集合-orphanChains
 * 重启系统时，会根据主链最新区块重新在系统中构建masterChain，但是forkChains和orphanChains会废弃掉，数据库文件也会删除。
 * @author captain
 * @version 1.0
 * @date 18-11-15 下午1:54
 */
@Data
public class Chain implements Cloneable, Comparable {

    /**
     * 标记这个链是从哪个链分叉来的，一个链的parent不一定是主链
     */
    private Chain parent;

    /**
     * 标记所有从本链直接分叉出去的链集合，默认按起始高度从高到低排序，在移除链时有用
     */
    private SortedSet<Chain> sons = new TreeSet<>((a, b) -> (int) (a.getStartHeight() - b.getStartHeight()));

    /**
     * 链ID
     */
    private int chainId;

    /**
     * 孤儿链的年龄，每经过一次OrphanChainsMonitor处理并且链上没有加入新区块，年龄加一，达到一定年龄从内存中清除
     */
    private int age;

    /**
     * 链上起始区块的previousHash
     */
    private NulsDigestData previousHash;
    /**
     * 链的起始高度
     */
    private long startHeight;
    /**
     * 链的结束高度
     */
    private long endHeight;

    /**
     * 链上所有区块hash列表，主链不维护此属性
     */
    private LinkedList<NulsDigestData> hashList;

    /**
     * 是否是主链
     */
    private boolean master;

    /**
     * 把subChain连接到本链this上
     * 所有subChain的son都要重新链接到this
     * this.sons新增subChain的son
     * @param subChain
     * @return
     */
    public void append(Chain subChain) {
        this.getHashList().addAll(subChain.getHashList());
        this.setEndHeight(subChain.getEndHeight());
        this.setAge(0);
        this.setSons(subChain.getSons());
        subChain.getSons().forEach(e -> e.setParent(this));
    }

    /**
     * 判断两个链哪个更长
     *
     * @param chain
     * @return
     */
    public boolean longer(Chain chain) {
        return this.endHeight > chain.endHeight;
    }

    /**
     * 获取链的结束hash
     *
     * @return
     */
    public NulsDigestData getEndHash() {
        if (master) {
            return ContextManager.getContext(chainId).getLatestBlock().getHeader().getHash();
        }
        return hashList.getLast();
    }

    /**
     * 获取链的起始hash
     *
     * @return
     */
    public NulsDigestData getStartHash() {
        if (master) {
            return ContextManager.getContext(chainId).getGenesisBlock().getHeader().getHash();
        }
        return hashList.getFirst();
    }


    /**
     * 在链头插入一个区块，只有孤儿链会用到这个方法
     * @param block
     */
    public void addFirst(Block block) {
        this.setPreviousHash(block.getHeader().getPreHash());
        this.setStartHeight(block.getHeader().getHeight());
        this.getHashList().addFirst(block.getHeader().getHash());
    }

    /**
     * 在链尾插入一个区块
     * @param block
     */
    public void addLast(Block block) {
        this.setEndHeight(block.getHeader().getHeight());
        this.getHashList().addLast(block.getHeader().getHash());
    }

    /**
     * 使用一个区块生成一条链
     * @param chainId
     * @param block
     * @param parent    生成分叉链时传父链，生成孤儿链时传null
     * @return
     */
    public static Chain generate(int chainId, Block block, Chain parent) {
        long height = block.getHeader().getHeight();
        NulsDigestData hash = block.getHeader().getHash();
        NulsDigestData preHash = block.getHeader().getPreHash();
        Chain chain = new Chain();
        LinkedList<NulsDigestData> hashs = new LinkedList();
        hashs.add(hash);
        chain.setChainId(chainId);
        chain.setStartHeight(height);
        chain.setEndHeight(height);
        chain.setHashList(hashs);
        chain.setMaster(false);
        chain.setPreviousHash(preHash);
        chain.setParent(parent);
        if (parent != null) {
            parent.getSons().add(chain);
        }
        return chain;
    }

    /**
     * 系统初始化时，由本地的最新区块生成主链
     * @param chainId
     * @param block
     * @return
     */
    public static Chain generateMasterChain(int chainId, Block block) {
        long height = block.getHeader().getHeight();
        Chain chain = new Chain();
        chain.setChainId(chainId);
        chain.setStartHeight(0L);
        chain.setEndHeight(height);
        chain.setMaster(true);
        chain.setParent(null);
        return chain;
    }

    @Override
    public Chain clone() throws CloneNotSupportedException {
        Chain chain = new Chain();
        chain.setChainId(this.chainId);
        chain.setStartHeight(this.startHeight);
        chain.setEndHeight(this.endHeight);
        chain.setPreviousHash(this.previousHash);
        chain.setParent(this.parent);
        chain.setSons(this.sons);
        chain.setMaster(this.master);
        chain.setHashList(this.hashList);
        return chain;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
