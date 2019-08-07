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
import io.nuls.block.model.ChainParameters;
import io.nuls.core.log.logback.NulsLogger;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.StampedLock;

/**
 * 分叉链、孤儿链数据库定时清理器
 * 因为使用了rocksDb,清理记录后,数据文件大小不能实时变化,所以不能按数据库文件大小来做判断标准,每次按区块的百分比清理
 * 触发条件:某链ID的数据库缓存的区块总数超过超出cacheSize(可配置)
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class StorageSizeMonitor extends BaseMonitor {

    private static final StorageSizeMonitor INSTANCE = new StorageSizeMonitor();

    public static StorageSizeMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger commonLog) {
        //获取配置项
        ChainParameters parameters = ContextManager.getContext(chainId).getParameters();
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
                //1.获取某链ID的数据库缓存的所有区块数量
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
                //与阈值比较
                while (actualSize > cacheSize) {
                    logger.info("before clear, chainId:" + chainId + ", cacheSize:" + cacheSize + ", actualSize:" + actualSize);
                    //2.清理孤儿链
                    SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
                    if (!orphanChains.isEmpty()) {
                        Chain chain = orphanChains.first();
                        BlockChainManager.deleteOrphanChain(chainId, chain);
                    }
                    //3.清理分叉链
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
     * 清理分叉链
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
                //1.清理链起始高度位于主链最新高度增减30(可配置)范围外的分叉链
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
                //1.标记
                for (Chain forkChain : forkChains) {
                    if (latestHeight - forkChain.getStartHeight() > heightRange || masterChain.getHashList().contains(forkChain.getEndHash())) {
                        //清理orphanChain,并递归清理orphanChain的所有子链
                        deleteSet.add(forkChain);
                    }
                }
                //2.清理
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
     * 清理孤儿链
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
                //1.清理链起始高度位于主链最新高度增减30(可配置)范围外的孤儿链
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
                //1.标记
                for (Chain orphanChain : orphanChains) {
                    if (Math.abs(orphanChain.getStartHeight() - latestHeight) > heightRange || orphanChain.getAge().get() > orphanChainMaxAge) {
                        //清理orphanChain,并递归清理orphanChain的所有子链
                        deleteSet.add(orphanChain);
                    }
                }
                //2.清理
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
