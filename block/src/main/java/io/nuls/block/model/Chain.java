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
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.manager.ContextManager;
import lombok.Data;

import java.util.*;

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
public class Chain {

    public static final Comparator<Chain> COMPARATOR = Comparator.comparingLong(Chain::getStartHeight).thenComparingInt(Chain::getStartHashCode);

    /**
     * 标记这个链是从哪个链分叉来的，一个链的parent不一定是主链
     */
    private Chain parent;

    /**
     * 标记所有从本链直接分叉出去的链集合，默认按起始高度从低到高排序，起始高度相同时，按照起始区块hash转换成int从低到高排序，在移除链时有用
     */
    private SortedSet<Chain> sons = new TreeSet<>(COMPARATOR);

    /**
     * 链ID
     */
    private int chainId;

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
     * 标记该链的类型
     */
    private ChainTypeEnum type;

    /**
     * 把subChain连接到本链this上
     * 所有subChain的son都要重新链接到this
     * this.sons新增subChain的son
     *
     * @param subChain
     * @return
     */
    public void append(Chain subChain) {
        if (!this.isMaster()) {
            this.getHashList().addAll(subChain.getHashList());
        }
        this.setEndHeight(subChain.getEndHeight());
        this.getSons().addAll(subChain.getSons());
        subChain.getSons().forEach(e -> e.setParent(this));
        subChain.setParent(this);
    }

    public boolean fork(Chain forkChain) {
        forkChain.setParent(this);
        return this.getSons().add(forkChain);
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
        if (isMaster()) {
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
        if (isMaster()) {
            return ContextManager.getContext(chainId).getGenesisBlock().getHeader().getHash();
        }
        return hashList.getFirst();
    }

    /**
     * 获取链的起始hash
     *
     * @return
     */
    public int getStartHashCode() {
        return getStartHash().hashCode();
    }

    /**
     * 判断本链是否为主链
     * @return
     */
    public boolean isMaster(){
        return this.type.equals(ChainTypeEnum.MASTER);
    }

    /**
     * 在链头插入一个区块，只有孤儿链会用到这个方法
     *
     * @param block
     */
    public void addFirst(Block block) {
        this.setPreviousHash(block.getHeader().getPreHash());
        this.setStartHeight(block.getHeader().getHeight());
        this.getHashList().addFirst(block.getHeader().getHash());
    }

    /**
     * 在链尾插入一个区块
     *
     * @param block
     */
    public void addLast(Block block) {
        this.setEndHeight(block.getHeader().getHeight());
        this.getHashList().addLast(block.getHeader().getHash());
    }

    /**
     * 使用一个区块生成一条链
     *
     * @param chainId
     * @param block
     * @param parent  生成分叉链时传父链，生成孤儿链时传null
     * @return
     */
    public static Chain generate(int chainId, Block block, Chain parent, ChainTypeEnum type) {
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
        chain.setPreviousHash(preHash);
        chain.setParent(parent);
        chain.setType(type);
        if (parent != null) {
            parent.getSons().add(chain);
        }
        return chain;
    }

    /**
     * 系统初始化时，由本地的最新区块生成主链
     *
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
        chain.setType(ChainTypeEnum.MASTER);
        chain.setParent(null);
        chain.setPreviousHash(block.getHeader().getPreHash());
        return chain;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Chain.class.getSimpleName() + "[", "]")
                .add("startHeight=" + startHeight)
                .add("endHeight=" + endHeight)
                .add("type=" + type)
                .toString();
    }
}
