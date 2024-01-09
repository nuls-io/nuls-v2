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
import io.nuls.base.data.NulsHash;
import io.nuls.block.constant.ChainTypeEnum;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chain Object
 * Each chainIDThe corresponding chains may form the structure shown in the following figure
 * <pre>
 * |--------------------Main chain
 * |
 * |
 * |
 * |____
 * |    |---------------Forked chain1
 * |    |                               |------------Orphan Chain1
 * |    |                               |
 * |    |                               |
 * |    |                               |___
 * |                                    |   |--------Orphan Chain1-1
 * |                                    |   |
 * |____                                |   |
 * |    |---------------Forked chain2
 * |    |
 * |    |____
 * |    |    |----------Forked chain2-1
 * |    |    |
 * |    |    |
 * |    |    |
 * <pre/>
 * The storage of blocks on the main chain is jointly completed by the block management module and the transaction management module,The block management module stores block headers,The transaction management module stores transaction data.
 * The block storage of forked chains is fully managed by the block management module,Cache in database(blockHash, blockData),As for each onehashWhich fork chain does the corresponding block belong to,Then, from the memoryChainObject maintenance
 * When the system is running normally,The main chain will be maintained in memory-masterChain、Set of forked chains-forkChains、Orphan Chain Collection-orphanChains
 * When restarting the system,We will rebuild the system based on the latest blocks in the main chainmasterChain,howeverforkChainsandorphanChainsWill be discarded,The database files will also be deleted.
 * @author captain
 * @version 1.0
 * @date 18-11-15 afternoon1:54
 */
public class Chain {

    public static final Comparator<Chain> COMPARATOR = Comparator.comparingLong(Chain::getStartHeight).thenComparingInt(Chain::getStartHashCode).thenComparingLong(Chain::getEndHeight).thenComparingInt(Chain::getEndHashCode);

    /**
     * Mark which chain this chain forked from,A chain ofparentNot necessarily the main chain
     */
    private Chain parent;

    /**
     * Mark all chain sets that directly branch out of this chain,Default sorting by starting height from low to high,When the starting height is the same,According to the starting blockhashconvert tointSort from low to high,Useful when removing chains
     */
    private SortedSet<Chain> sons = new TreeSet<>(COMPARATOR);

    /**
     * chainID
     */
    private int chainId;

    /**
     * On chain starting blockpreviousHash
     */
    private NulsHash previousHash;

    /**
     * The starting height of the chain(contain)
     */
    private long startHeight;

    /**
     * The beginning of the chainhashConvert toint,Use when sorting
     */
    private int startHashCode;

    /**
     * The end height of the chain(contain)
     */
    private long endHeight;

    /**
     * All blocks on the chainhashlist,Forked chain、Orphan chain maintains all blockshashIn memory,The main chain is only maintainedConfigConstant.HEIGHT_RANGEindividualhashIn memory
     */
    private Deque<NulsHash> hashList;

    /**
     * Mark the type of the chain
     */
    private ChainTypeEnum type;

    /**
     * Mark the age of the chain,Suitable for orphan chains
     */
    private AtomicInteger age = new AtomicInteger(0);

    /**
     * Get the start of the chainhash
     *
     * @return
     */
    public NulsHash getStartHash() {
        return hashList.getFirst();
    }

    /**
     * Get the end of the chainhash
     *
     * @return
     */
    public NulsHash getEndHash() {
        return hashList.getLast();
    }

    /**
     * Get the end of the chainhashcode
     *
     * @return
     */
    public int getEndHashCode() {
        return hashList.getLast().hashCode();
    }

    /**
     * Determine whether this chain is the main chain
     *
     * @return
     */
    public boolean isMaster() {
        if (type == null) {
            return false;
        }
        return type.equals(ChainTypeEnum.MASTER);
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

    public NulsHash getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(NulsHash previousHash) {
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

    public Deque<NulsHash> getHashList() {
        return hashList;
    }

    public void setHashList(Deque<NulsHash> hashList) {
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
     * Insert a block at the chain head,Only orphan chains will use this method
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
     * Insert a block at the end of the chain
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
