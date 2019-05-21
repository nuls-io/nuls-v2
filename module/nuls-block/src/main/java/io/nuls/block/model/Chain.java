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

package io.nuls.block.model;

import com.google.common.base.Objects;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.block.constant.ChainTypeEnum;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
 * 主链上区块的存储是由区块管理模块与交易管理模块共同完成,其中区块管理模块存储区块头,交易管理模块存储交易数据.
 * 分叉链的区块存储由区块管理模块全权负责,数据库中缓存(blockHash, blockData),至于每一个hash对应的区块属于哪一个分叉链,则由内存中的Chain对象进行维护
 * 系统正常运行时,内存中会维护主链-masterChain、分叉链集合-forkChains、孤儿链集合-orphanChains
 * 重启系统时,会根据主链最新区块重新在系统中构建masterChain,但是forkChains和orphanChains会废弃掉,数据库文件也会删除.
 * @author captain
 * @version 1.0
 * @date 18-11-15 下午1:54
 */
public class Chain {

    public static final Comparator<Chain> COMPARATOR = Comparator.comparingLong(Chain::getStartHeight).thenComparingInt(Chain::getStartHashCode);

    /**
     * 标记这个链是从哪个链分叉来的,一个链的parent不一定是主链
     */
    private Chain parent;

    /**
     * 标记所有从本链直接分叉出去的链集合,默认按起始高度从低到高排序,起始高度相同时,按照起始区块hash转换成int从低到高排序,在移除链时有用
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
     * 链的起始高度(包含)
     */
    private long startHeight;

    /**
     * 链的起始hash转换为int,排序时用
     */
    private int startHashCode;

    /**
     * 链的结束高度(包含)
     */
    private long endHeight;

    /**
     * 链上所有区块hash列表,分叉链、孤儿链维护所有区块的hash在内存中,主链只维护ConfigConstant.HEIGHT_RANGE个hash在内存中
     */
    private Deque<NulsDigestData> hashList;

    /**
     * 标记该链的类型
     */
    private ChainTypeEnum type;

    /**
     * 标记该链的年龄,适用于孤儿链
     */
    private AtomicInteger age = new AtomicInteger(0);

    /**
     * 获取链的起始hash
     *
     * @return
     */
    public NulsDigestData getStartHash() {
        return hashList.getFirst();
    }

    /**
     * 获取链的结束hash
     *
     * @return
     */
    public NulsDigestData getEndHash() {
        return hashList.getLast();
    }

    /**
     * 判断本链是否为主链
     *
     * @return
     */
    public boolean isMaster() {
        return type.equals(ChainTypeEnum.MASTER);
    }

    public static Comparator<Chain> getCOMPARATOR() {
        return COMPARATOR;
    }

    public Chain getParent() {
        return parent;
    }

    public void setParent(Chain parent) {
        this.parent = parent;
    }

    public SortedSet<Chain> getSons() {
        return sons;
    }

    public void setSons(SortedSet<Chain> sons) {
        this.sons = sons;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public NulsDigestData getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(NulsDigestData previousHash) {
        this.previousHash = previousHash;
    }

    public long getStartHeight() {
        return startHeight;
    }

    public void setStartHeight(long startHeight) {
        this.startHeight = startHeight;
    }

    public int getStartHashCode() {
        return startHashCode;
    }

    public void setStartHashCode(int startHashCode) {
        this.startHashCode = startHashCode;
    }

    public long getEndHeight() {
        return endHeight;
    }

    public void setEndHeight(long endHeight) {
        this.endHeight = endHeight;
    }

    public Deque<NulsDigestData> getHashList() {
        return hashList;
    }

    public void setHashList(Deque<NulsDigestData> hashList) {
        this.hashList = hashList;
    }

    public ChainTypeEnum getType() {
        return type;
    }

    public void setType(ChainTypeEnum type) {
        this.type = type;
    }

    public AtomicInteger getAge() {
        return age;
    }

    public void setAge(AtomicInteger age) {
        this.age = age;
    }

    /**
     * 在链头插入一个区块,只有孤儿链会用到这个方法
     *
     * @param block
     */
    public void addFirst(Block block) {
        BlockHeader blockHeader = block.getHeader();
        this.setPreviousHash(blockHeader.getPreHash());
        this.setStartHeight(blockHeader.getHeight());
        this.getHashList().addFirst(blockHeader.getHash());
        this.setStartHashCode(blockHeader.getHash().hashCode());
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

    @Override
    public String toString() {
        return new StringJoiner(", ", Chain.class.getSimpleName() + "[", "]")
                .add("parentStartHashCode=" + (this.getParent() == null ? null : this.getParent().getStartHashCode()))
                .add("startHashCode=" + startHashCode)
                .add("startHeight=" + startHeight)
                .add("endHeight=" + endHeight)
                .add("type=" + type)
                .add("age=" + age)
                .add("hashList=" + hashList)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Chain chain = (Chain) o;
        return chainId == chain.chainId &&
                startHeight == chain.startHeight &&
                startHashCode == chain.startHashCode &&
                endHeight == chain.endHeight &&
                Objects.equal(previousHash, chain.previousHash) &&
                Objects.equal(hashList, chain.hashList) &&
                type == chain.type &&
                Objects.equal(age, chain.age);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chainId, previousHash, startHeight, startHashCode, endHeight, hashList, type, age);
    }
}
