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
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.thread.monitor.TxGroupRequestor;
import io.nuls.block.utils.LoggerUtil;
import io.nuls.block.utils.SingleBlockCacher;
import io.nuls.block.utils.SmallBlockCacher;
import io.nuls.common.ConfigBean;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

/**
 * Each chainIDCorresponding to one{@link ChainContext},Maintain information during chain operation,And responsible for initializing the chain、start-up、cease、Destruction operation
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 morning10:46
 */
public class ChainContext {
    /**
     * Represents the operational status of the chain
     */
    private StatusEnum status;

    /**
     * Whether to perform block synchronization,If an abnormality occurs midway, set it asfalse,Terminate synchronization
     */
    private boolean needSyn;

    /**
     * chainID
     */
    private int chainId;

    /**
     * The system transaction type of this chain
     */
    private List<Integer> systemTransactionType;

    /**
     * The latest height of the internet
     */
    private long networkHeight;

    /**
     * Latest Block
     */
    private Block latestBlock;

    /**
     * Genesis Block
     */
    private Block genesisBlock;

    /**
     * The runtime parameters of the chain
     */
    private ConfigBean parameters;

    /**
     * Get lock object
     * Clean up the database,Block synchronization,Forked chain maintenance,Orphan chain maintenance obtains the lock
     */
    private StampedLock lock;

    /**
     * Record General Logs
     */
    private NulsLogger logger;

    /**
     * Forked chain、Repeated in orphan chainhashcounter
     */
    private Map<String, AtomicInteger> duplicateBlockMap;

    /**
     * Record whether a packaging address has undergone fork notification,Notify each address only once
     */
    private List<String> packingAddressList;

    /**
     * CachedhashMapping to height,Used to set node height
     */
    private Map<NulsHash, Long> cachedHashHeightMap;

    /**
     * Number of cached block bytes
     */
    private AtomicInteger cachedBlockSize;

    /**
     * Parameters used during a block download process
     */
    private BlockDownloaderParams downloaderParams;

    /**
     * Synchronize block cache
     */
    private Map<Long, Block> blockMap = new ConcurrentHashMap<>(100);

    /**
     * Orphan block associated nodes,Prioritize downloading orphan blocks from these nodes when maintaining them
     */
    private Map<NulsHash, List<String>> orphanBlockRelatedNodes;

    public Map<NulsHash, List<String>> getOrphanBlockRelatedNodes() {
        return orphanBlockRelatedNodes;
    }

    public void setOrphanBlockRelatedNodes(Map<NulsHash, List<String>> orphanBlockRelatedNodes) {
        this.orphanBlockRelatedNodes = orphanBlockRelatedNodes;
    }

    public Map<Long, Block> getBlockMap() {
        return blockMap;
    }

    public void setBlockMap(Map<Long, Block> blockMap) {
        this.blockMap = blockMap;
    }

    public BlockDownloaderParams getDownloaderParams() {
        return downloaderParams;
    }

    public void setDownloaderParams(BlockDownloaderParams downloaderParams) {
        this.downloaderParams = downloaderParams;
    }

    public AtomicInteger getCachedBlockSize() {
        return cachedBlockSize;
    }

    public void setCachedBlockSize(AtomicInteger cachedBlockSize) {
        this.cachedBlockSize = cachedBlockSize;
    }

    public Map<NulsHash, Long> getCachedHashHeightMap() {
        return cachedHashHeightMap;
    }

    public void setCachedHashHeightMap(Map<NulsHash, Long> cachedHashHeightMap) {
        this.cachedHashHeightMap = cachedHashHeightMap;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public boolean isNeedSyn() {
        return needSyn;
    }

    public void setNeedSyn(boolean needSyn) {
        this.needSyn = needSyn;
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

    public ConfigBean getParameters() {
        return parameters;
    }

    public void setParameters(ConfigBean parameters) {
        this.parameters = parameters;
    }

    public StampedLock getLock() {
        return lock;
    }

    public void setLock(StampedLock lock) {
        this.lock = lock;
    }

    public NulsLogger getLogger() {
        return logger;
    }

    public void setLogger(NulsLogger logger) {
        this.logger = logger;
    }

    public Map<String, AtomicInteger> getDuplicateBlockMap() {
        return duplicateBlockMap;
    }

    public void setDuplicateBlockMap(Map<String, AtomicInteger> duplicateBlockMap) {
        this.duplicateBlockMap = duplicateBlockMap;
    }

    public long getNetworkHeight() {
        return networkHeight;
    }

    public void setNetworkHeight(long networkHeight) {
        this.networkHeight = networkHeight;
    }

    public List<String> getPackingAddressList() {
        return packingAddressList;
    }

    public void setPackingAddressList(List<String> packingAddressList) {
        this.packingAddressList = packingAddressList;
    }

    public void setStatus(StatusEnum status) {
        if (status.equals(getStatus())) {
            return;
        }
        synchronized (this) {
            logger.debug("status changed:" + this.status + "->" + status);
            this.status = status;
        }
    }

    public long getLatestHeight() {
        if (null == latestBlock || null == latestBlock.getHeader()) {
            return -1;
        }
        return latestBlock.getHeader().getHeight();
    }

    public void init() {
        LoggerUtil.init(chainId);
        cachedBlockSize = new AtomicInteger(0);
        this.setStatus(StatusEnum.INITIALIZING);
        cachedHashHeightMap = CollectionUtils.getSynSizedMap(parameters.getSmallBlockCache());
        orphanBlockRelatedNodes = CollectionUtils.getSynSizedMap(parameters.getHeightRange());
        packingAddressList = CollectionUtils.getSynList();
        duplicateBlockMap = new HashMap<>();
        systemTransactionType = new ArrayList<>();
        needSyn = true;
        lock = new StampedLock();
        //Various types of cache initialization
        SmallBlockCacher.init(chainId);
        SingleBlockCacher.init(chainId);
        BlockChainManager.init(chainId);
        TxGroupRequestor.init(chainId);
    }

    public void start() {

    }

    public void stop() {

    }

    public void destroy() {

    }

    /**
     * Print current chain information
     */
    public void printChains() {
        Chain masterChain = BlockChainManager.getMasterChain(chainId);
        logger.info("-------------------------------------master chain-------------------------------------");
        logger.info("-" + masterChain);
        SortedSet<Chain> forkChains = BlockChainManager.getForkChains(chainId);
        if (!forkChains.isEmpty()) {
            logger.info("-------------------------------------fork chains-------------------------------------");
            for (Chain forkChain : forkChains) {
                logger.info("-" + forkChain);
            }
        }
        SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
        if (!orphanChains.isEmpty()) {
            logger.info("-------------------------------------orphan chains-------------------------------------");
            for (Chain orphanChain : orphanChains) {
                logger.info("-" + orphanChain);
            }
        }
    }

}
