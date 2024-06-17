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

package io.nuls.block.thread.monitor;

import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.common.ConfigBean;
import io.nuls.core.log.logback.NulsLogger;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.StampedLock;

/**
 * Forked chain、Orphan Chain Database Timer Cleaner
 * Because it was usedrocksDb,After cleaning the records,The size of the data file cannot change in real time,So we cannot use the database file size as a criterion for judgment,Clean up by block percentage each time
 * Trigger conditions:A certain chainIDThe total number of cached blocks in the database exceedscacheSize(Configurable)
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 afternoon3:54
 */
public class StorageSizeMonitor extends BaseMonitor {

    private static final StorageSizeMonitor INSTANCE = new StorageSizeMonitor();

    public static StorageSizeMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger commonLog) {
        //Get configuration items
        ConfigBean parameters = ContextManager.getContext(chainId).getParameters();
        int heightRange = parameters.getHeightRange();
        int orphanChainMaxAge = parameters.getOrphanChainMaxAge();
        context.setStatus(StatusEnum.STORAGE_CLEANING);
        forkChainsCleaner(chainId, heightRange, context);
        orphanChainsCleaner(chainId, heightRange, context, orphanChainMaxAge);
        int cacheSize = parameters.getCacheSize();
        dbSizeCleaner(chainId, context, cacheSize);
        context.setStatus(StatusEnum.RUNNING);
    }

    private void dbSizeCleaner(Integer chainId, ChainContext context, int cacheSize) {

        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        NulsLogger logger = context.getLogger();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                //1.Obtain a certain chainIDThe number of all blocks cached in the database
                int actualSize = BlockChainManager.getForkChains(chainId).stream().mapToInt(e -> e.getHashList().size()).sum();
                actualSize += BlockChainManager.getOrphanChains(chainId).stream().mapToInt(e -> e.getHashList().size()).sum();
                logger.debug("cacheSize:" + cacheSize + ", actualSize:" + actualSize);
                if (!lock.validate(stamp)) {
                    continue;
                }
                if (actualSize <= cacheSize) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                //Compared to threshold
                while (actualSize > cacheSize) {
                    logger.info("before clear, chainId:" + chainId + ", cacheSize:" + cacheSize + ", actualSize:" + actualSize);
                    //2.Cleaning up orphan chains
                    SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
                    if (!orphanChains.isEmpty()) {
                        Chain chain = orphanChains.first();
                        BlockChainManager.deleteOrphanChain(chainId, chain);
                    }
                    //3.Cleaning fork chains
                    SortedSet<Chain> forkChains = BlockChainManager.getForkChains(chainId);
                    if (!forkChains.isEmpty()) {
                        Chain chain = forkChains.first();
                        BlockChainManager.deleteForkChain(chainId, chain, true);
                    }
                    actualSize = BlockChainManager.getForkChains(chainId).stream().mapToInt(e -> e.getHashList().size()).sum();
                    actualSize += BlockChainManager.getOrphanChains(chainId).stream().mapToInt(e -> e.getHashList().size()).sum();
                    logger.info("after clear, chainId:" + chainId + ", cacheSize:" + cacheSize + ", actualSize:" + actualSize);
                }
                break;
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    /**
     * Cleaning fork chains
     *
     * @param chainId
     * @param heightRange
     * @param context
     */
    private void forkChainsCleaner(int chainId, int heightRange, ChainContext context) {
        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        NulsLogger logger = context.getLogger();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                //1.The starting height of the cleaning chain is located at the latest height increase or decrease of the main chain30(Configurable)Out of range forked chain
                SortedSet<Chain> forkChains = BlockChainManager.getForkChains(chainId);
                if (!lock.validate(stamp)) {
                    continue;
                }
                if (forkChains.isEmpty()) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                Chain masterChain = BlockChainManager.getMasterChain(chainId);
                long latestHeight = masterChain.getEndHeight();
                SortedSet<Chain> deleteSet = new TreeSet<>(Chain.COMPARATOR);
                //1.sign
                for (Chain forkChain : forkChains) {
                    if (latestHeight - forkChain.getStartHeight() > heightRange || masterChain.getHashList().contains(forkChain.getEndHash())) {
                        //clean uporphanChain,And recursively clean uporphanChainAll sub chains of
                        deleteSet.add(forkChain);
                    }
                }
                //2.clean up
                for (Chain chain : deleteSet) {
                    BlockChainManager.deleteForkChain(chainId, chain, true);
                    logger.info("remove fork chain, chain:" + chain);
                }
                break;
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    /**
     * Cleaning up orphan chains
     * @param chainId
     * @param heightRange
     * @param context
     * @param orphanChainMaxAge
     */
    private void orphanChainsCleaner(int chainId, int heightRange, ChainContext context, int orphanChainMaxAge) {
        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        NulsLogger logger = context.getLogger();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                //1.The starting height of the cleaning chain is located at the latest height increase or decrease of the main chain30(Configurable)Orphan chains outside of scope
                SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
                if (!lock.validate(stamp)) {
                    continue;
                }
                if (orphanChains.isEmpty()) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                Chain masterChain = BlockChainManager.getMasterChain(chainId);
                long latestHeight = masterChain.getEndHeight();
                SortedSet<Chain> deleteSet = new TreeSet<>(Chain.COMPARATOR);
                //1.sign
                for (Chain orphanChain : orphanChains) {
                    if (Math.abs(orphanChain.getStartHeight() - latestHeight) > heightRange || orphanChain.getAge().get() > orphanChainMaxAge) {
                        //clean uporphanChain,And recursively clean uporphanChainAll sub chains of
                        deleteSet.add(orphanChain);
                    }
                }
                //2.clean up
                for (Chain chain : deleteSet) {
                    BlockChainManager.deleteOrphanChain(chainId, chain);
                    logger.info("remove orphan chain, chain:" + chain);
                }
                break;
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

}
