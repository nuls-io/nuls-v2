/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.db;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.AbstractCachedSource;
import org.ethereum.datasource.AsyncFlushable;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public class DbFlushManager {
    private static final Logger logger = LoggerFactory.getLogger("db");

    List<AbstractCachedSource<byte[], ?>> writeCaches = new CopyOnWriteArrayList<>();
    List<Source<byte[], ?>> sources = new CopyOnWriteArrayList<>();
    Set<DbSource> dbSources = new HashSet<>();
    AbstractCachedSource<byte[], byte[]> stateDbCache;

    long sizeThreshold;
    int commitsCountThreshold;
    boolean syncDone = false;
    boolean flushAfterSyncDone;

    SystemProperties config;

    int commitCount = 0;

    private final BlockingQueue<Runnable> executorQueue = new ArrayBlockingQueue<>(1);
    private final ExecutorService flushThread = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            executorQueue, new ThreadFactoryBuilder().setNameFormat("DbFlushManagerThread-%d").build());
    Future<Boolean> lastFlush = Futures.immediateFuture(false);

    public DbFlushManager(SystemProperties config, Set<DbSource> dbSources, AbstractCachedSource<byte[], byte[]> stateDbCache) {
        this.config = config;
        this.dbSources = dbSources;
        sizeThreshold = config.getConfig().getInt("cache.flush.writeCacheSize") * 1024L * 1024;
        commitsCountThreshold = config.getConfig().getInt("cache.flush.blocks");
        flushAfterSyncDone = config.getConfig().getBoolean("cache.flush.shortSyncFlush");
        this.stateDbCache = stateDbCache;
    }

    public void setSizeThreshold(long sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    public void addCache(AbstractCachedSource<byte[], ?> cache) {
        writeCaches.add(cache);
    }

    public void addSource(Source<byte[], ?> src) {
        sources.add(src);
    }

    public long getCacheSize() {
        long ret = 0;
        for (AbstractCachedSource<byte[], ?> writeCache : writeCaches) {
            ret += writeCache.estimateCacheSize();
        }
        return ret;
    }

    public synchronized void commit(Runnable atomicUpdate) {
        atomicUpdate.run();
        commit();
    }

    public synchronized void commit() {
        long cacheSize = getCacheSize();
        if (sizeThreshold >= 0 && cacheSize >= sizeThreshold) {
            logger.debug("DbFlushManager: flushing db due to write cache size (" + cacheSize + ") reached threshold (" + sizeThreshold + ")");
            flush();
        } else if (commitsCountThreshold > 0 && commitCount >= commitsCountThreshold) {
            logger.debug("DbFlushManager: flushing db due to commits (" + commitCount + ") reached threshold (" + commitsCountThreshold + ")");
            flush();
            commitCount = 0;
        } else if (flushAfterSyncDone && syncDone) {
            logger.debug("DbFlushManager: flushing db due to short sync");
            flush();
        }
        commitCount++;
    }

    public synchronized void flushSync() {
        try {
            flush().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Future<Boolean> flush() {
        if (!lastFlush.isDone()) {
            logger.debug("Waiting for previous flush to complete...");
            try {
                lastFlush.get();
            } catch (Exception e) {
                logger.error("Error during last flush", e);
            }
        }
        logger.debug("Flipping async storages");
        for (AbstractCachedSource<byte[], ?> writeCache : writeCaches) {
            try {
                if (writeCache instanceof AsyncFlushable) {
                    ((AsyncFlushable) writeCache).flipStorage();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        logger.debug("Submitting flush task");
        return lastFlush = flushThread.submit(() -> {
            boolean ret = false;
            long s = System.nanoTime();
            logger.debug("Flush started");

            sources.forEach(Source::flush);

            for (AbstractCachedSource<byte[], ?> writeCache : writeCaches) {
                if (writeCache instanceof AsyncFlushable) {
                    try {
                        ret |= ((AsyncFlushable) writeCache).flushAsync().get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                } else {
                    ret |= writeCache.flush();
                }
            }
            if (stateDbCache != null) {
                logger.debug("Flushing to DB");
                stateDbCache.flush();
            }
            logger.debug("Flush completed in " + (System.nanoTime() - s) / 1000000 + " ms");

            return ret;
        });
    }

    /**
     * Flushes all caches and closes all databases
     */
    public synchronized void close() {
        logger.debug("Flushing DBs...");
        flushSync();
        logger.debug("Flush done.");
        for (DbSource dbSource : dbSources) {
            logger.debug("Closing DB: {}", dbSource.getName());
            try {
                dbSource.close();
            } catch (Exception ex) {
                logger.error(String.format("Caught error while closing DB: %s", dbSource.getName()), ex);
            }
        }
    }
}

