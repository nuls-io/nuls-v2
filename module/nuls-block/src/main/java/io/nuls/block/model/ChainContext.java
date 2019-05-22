/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.block.model;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.block.cache.BlockCacher;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.thread.monitor.TxGroupRequestor;
import io.nuls.block.utils.LoggerUtil;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.CollectionUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

/**
 * 每个链ID对应一个{@link ChainContext},维护一些链运行期间的信息,并负责链的初始化、启动、停止、销毁操作
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午10:46
 */
public class ChainContext {
    /**
     * 代表该链的运行状态
     */
    private StatusEnum status;

    /**
     * 是否继续本次下载,中途发生异常置为false
     */
    private boolean doSyn;

    /**
     * 链ID
     */
    private int chainId;

    /**
     * 该链的系统交易类型
     */
    private List<Integer> systemTransactionType;

    /**
     * 最新区块
     */
    private Block latestBlock;

    /**
     * 创世区块
     */
    private Block genesisBlock;

    /**
     * 主链
     */
    private Chain masterChain;

    /**
     * 链的运行时参数
     */
    private ChainParameters parameters;

    /**
     * 获取锁对象
     * 清理数据库,区块同步,分叉链维护,孤儿链维护获取该锁
     */
    private StampedLock lock;

    /**
     * 记录通用日志
     */
    private NulsLogger commonLog;

    /**
     * 记录消息收发日志
     */
    private NulsLogger messageLog;

    /**
     * 分叉链、孤儿链中重复hash计数器
     */
    private Map<String, AtomicInteger> duplicateBlockMap;

    /**
     * 记录某个打包地址是否已经进行过分叉通知,每个地址只通知一次
     */
    private List<byte []> packingAddressList;

    /**
     * 缓存的hash与高度映射,用于设置节点高度
     */
    private Map<NulsHash, Long> cachedHashHeightMap;

    public Map<NulsHash, Long> getCachedHashHeightMap() {
        return cachedHashHeightMap;
    }

    public void setCachedHashHeightMap(Map<NulsHash, Long> cachedHashHeightMap) {
        this.cachedHashHeightMap = cachedHashHeightMap;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public boolean isDoSyn() {
        return doSyn;
    }

    public void setDoSyn(boolean doSyn) {
        this.doSyn = doSyn;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public List<Integer> getSystemTransactionType() {
        return systemTransactionType;
    }

    public void setSystemTransactionType(List<Integer> systemTransactionType) {
        this.systemTransactionType = systemTransactionType;
    }

    public Block getLatestBlock() {
        return latestBlock;
    }

    public void setLatestBlock(Block latestBlock) {
        this.latestBlock = latestBlock;
    }

    public Block getGenesisBlock() {
        return genesisBlock;
    }

    public void setGenesisBlock(Block genesisBlock) {
        this.genesisBlock = genesisBlock;
    }

    public Chain getMasterChain() {
        return masterChain;
    }

    public void setMasterChain(Chain masterChain) {
        this.masterChain = masterChain;
    }

    public ChainParameters getParameters() {
        return parameters;
    }

    public void setParameters(ChainParameters parameters) {
        this.parameters = parameters;
    }

    public StampedLock getLock() {
        return lock;
    }

    public void setLock(StampedLock lock) {
        this.lock = lock;
    }

    public NulsLogger getCommonLog() {
        return commonLog;
    }

    public void setCommonLog(NulsLogger commonLog) {
        this.commonLog = commonLog;
    }

    public NulsLogger getMessageLog() {
        return messageLog;
    }

    public void setMessageLog(NulsLogger messageLog) {
        this.messageLog = messageLog;
    }

    public Map<String, AtomicInteger> getDuplicateBlockMap() {
        return duplicateBlockMap;
    }

    public void setDuplicateBlockMap(Map<String, AtomicInteger> duplicateBlockMap) {
        this.duplicateBlockMap = duplicateBlockMap;
    }

    public List<byte[]> getPackingAddressList() {
        return packingAddressList;
    }

    public void setPackingAddressList(List<byte[]> packingAddressList) {
        this.packingAddressList = packingAddressList;
    }

    public void setStatus(StatusEnum status) {
        if (status.equals(getStatus())) {
            return;
        }
        synchronized (this) {
            commonLog.debug("status changed:" + this.status + "->" + status);
            this.status = status;
        }
    }

    public long getLatestHeight() {
        return latestBlock.getHeader().getHeight();
    }

    public void init() {
        LoggerUtil.init(chainId, parameters.getLogLevel());
        this.setStatus(StatusEnum.INITIALIZING);
        cachedHashHeightMap = CollectionUtils.getSynSizedMap(parameters.getSmallBlockCache());
        packingAddressList = new CopyOnWriteArrayList<>();
        duplicateBlockMap = new HashMap<>();
        systemTransactionType = new ArrayList<>();
        doSyn = true;
        lock = new StampedLock();
        //各类缓存初始化
        SmallBlockCacher.init(chainId);
        BlockCacher.init(chainId);
        BlockChainManager.init(chainId);
        TxGroupRequestor.init(chainId);
    }

    public void start() {

    }

    public void stop() {

    }

    public void destroy() {

    }

    public void printChains() {
        Chain masterChain = BlockChainManager.getMasterChain(chainId);
        commonLog.info("masterChain-" + masterChain);
        SortedSet<Chain> forkChains = BlockChainManager.getForkChains(chainId);
        forkChains.forEach(e -> commonLog.info("forkChain-" + e));
        SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
        orphanChains.forEach(e -> commonLog.info("orphanChain-" + e));
    }
}