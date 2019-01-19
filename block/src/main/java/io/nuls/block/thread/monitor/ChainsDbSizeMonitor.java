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

package io.nuls.block.thread.monitor;

import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.constant.Constant.CLEAN_PARAM;
import static io.nuls.block.constant.RunningStatusEnum.RUNNING;
import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * 分叉链、孤儿链数据库定时清理器
 * 因为使用了rocksDb,清理记录后,数据文件大小不能实时变化,所以不能按数据库文件大小来做判断标准,每次按区块的百分比清理
 * 触发条件:某链ID的数据库缓存的区块总数超过超出cacheSize(可配置)
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class ChainsDbSizeMonitor implements Runnable {

    private static final ChainsDbSizeMonitor INSTANCE = new ChainsDbSizeMonitor();

    private ChainsDbSizeMonitor() {

    }

    public static ChainsDbSizeMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        for (Integer chainId : ContextManager.chainIds) {
            ChainContext context = ContextManager.getContext(chainId);
            try {
                //判断该链的运行状态,只有正常运行时才会有数据库的处理
                RunningStatusEnum status = context.getStatus();
                if (!status.equals(RUNNING)) {
                    Log.debug("skip process, status is " + status + ", chainId-" + chainId);
                    continue;
                }
                //获取配置项
                ChainParameters parameters = ContextManager.getContext(chainId).getParameters();
                int heightRange = parameters.getHeightRange();
                int orphanChainMaxAge = parameters.getOrphanChainMaxAge();
                context.setStatus(RunningStatusEnum.DATABASE_CLEANING);
                forkChainsCleaner(chainId, heightRange, context);
                orphanChainsCleaner(chainId, heightRange, context, orphanChainMaxAge);
                int cacheSize = parameters.getCacheSize();
                dbSizeCleaner(chainId, context, cacheSize);
                context.setStatus(RunningStatusEnum.RUNNING);
            } catch (Exception e) {
                context.setStatus(RunningStatusEnum.RUNNING);
                e.printStackTrace();
                Log.error(e);
            }
        }
    }

    private void dbSizeCleaner(Integer chainId, ChainContext context, int cacheSize) {

        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                //1.获取某链ID的数据库缓存的所有区块数量
                int actualSize = ChainManager.getForkChains(chainId).stream().mapToInt(e -> e.getHashList().size()).sum();
                actualSize += ChainManager.getOrphanChains(chainId).stream().mapToInt(e -> e.getHashList().size()).sum();
                Log.debug("chainId:" + chainId + ", cacheSize:" + cacheSize + ", actualSize:" + actualSize);
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
                    Log.info("before clear, chainId:" + chainId + ", cacheSize:" + cacheSize + ", actualSize:" + actualSize);
                    //2.清理孤儿链
                    SortedSet<Chain> orphanChains = ChainManager.getOrphanChains(chainId);
                    int orphanSize = orphanChains.size();
                    if (orphanSize > 0) {
                        int i = orphanSize / CLEAN_PARAM;
                        //最少清理一个链
                        i = i == 0 ? 1 : i;
                        for (int j = 0; j < i; j++) {
                            Chain chain = orphanChains.first();
                            int count = ChainManager.removeOrphanChain(chainId, chain);
                            if (count < 0) {
                                Log.error("remove orphan chain fail, chain:" + chain);
                                return;
                            } else {
                                Log.info("remove orphan chain, chain:" + chain);
                                actualSize -= count;
                            }
                        }
                    }
                    //3.清理分叉链
                    SortedSet<Chain> forkChains = ChainManager.getForkChains(chainId);
                    int forkSize = forkChains.size();
                    if (forkSize > 0) {
                        int i = forkSize / CLEAN_PARAM;
                        //最少清理一个链
                        i = i == 0 ? 1 : i;
                        for (int j = 0; j < i; j++) {
                            Chain chain = forkChains.first();
                            int count = ChainManager.removeForkChain(chainId, chain);
                            if (count < 0) {
                                Log.error("remove fork chain fail, chain:" + chain);
                                return;
                            } else {
                                Log.info("remove fork chain, chain:" + chain);
                                actualSize -= count;
                            }
                        }
                    }
                    Log.info("after clear, chainId:" + chainId + ", cacheSize:" + cacheSize + ", actualSize:" + actualSize);
                }
                break;
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private void forkChainsCleaner(int chainId, int heightRange, ChainContext context) {
        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                //1.清理链起始高度位于主链最新高度增减30(可配置)范围外的分叉链
                SortedSet<Chain> forkChains = ChainManager.getForkChains(chainId);
                if (!lock.validate(stamp)) {
                    continue;
                }
                if (forkChains.size() < 1) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                Chain masterChain = ChainManager.getMasterChain(chainId);
                long latestHeight = masterChain.getEndHeight();
                SortedSet<Chain> deleteSet = new TreeSet<>(Chain.COMPARATOR);
                //1.标记
                for (Chain forkChain : forkChains) {
                    if (latestHeight - forkChain.getStartHeight() > heightRange) {
                        //清理orphanChain,并递归清理orphanChain的所有子链
                        deleteSet.add(forkChain);
                        deleteSet.addAll(forkChain.getSons());
                    }
                }
                //2.清理
                for (Chain chain : deleteSet) {
                    ChainManager.deleteForkChain(chainId, chain);
                    Log.info("remove fork chain, chain:" + chain);
                }
                break;
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private void orphanChainsCleaner(int chainId, int heightRange, ChainContext context, int orphanChainMaxAge) {
        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                //1.清理链起始高度位于主链最新高度增减30(可配置)范围外的孤儿链
                SortedSet<Chain> orphanChains = ChainManager.getOrphanChains(chainId);
                if (!lock.validate(stamp)) {
                    continue;
                }
                if (orphanChains.size() < 1) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                Chain masterChain = ChainManager.getMasterChain(chainId);
                long latestHeight = masterChain.getEndHeight();
                SortedSet<Chain> deleteSet = new TreeSet<>(Chain.COMPARATOR);
                //1.标记
                for (Chain orphanChain : orphanChains) {
                    if (Math.abs(orphanChain.getStartHeight() - latestHeight) > heightRange || orphanChain.getAge().get() > orphanChainMaxAge) {
                        //清理orphanChain,并递归清理orphanChain的所有子链
                        deleteSet.add(orphanChain);
                        deleteSet.addAll(orphanChain.getSons());
                    }
                }
                //2.清理
                for (Chain chain : deleteSet) {
                    ChainManager.deleteOrphanChain(chainId, chain);
                    Log.info("remove orphan chain, chain:" + chain);
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
