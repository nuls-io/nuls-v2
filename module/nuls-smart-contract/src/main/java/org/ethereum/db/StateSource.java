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

import io.nuls.contract.util.Log;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.*;

/**
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class StateSource extends SourceChainBox<byte[], byte[], byte[], byte[]>
        implements HashedKeySource<byte[], byte[]> {

    JournalSource<byte[]> journalSource;
    NoDeleteSource<byte[], byte[]> noDeleteSource;

    ReadCache<byte[], byte[]> readCache;
    AbstractCachedSource<byte[], byte[]> writeCache;

    public StateSource(Source<byte[], byte[]> src, boolean pruningEnabled) {
        super(src);
        long memorySize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        Log.info("Total RAM：{} MB", memorySize);
        int maxCapacity = 16 * 1024 * 2;
        if (memorySize >= 7500) {
            maxCapacity = maxCapacity * 12;
        } else if (memorySize >= 4500) {
            maxCapacity = maxCapacity * 6;
        } else if (memorySize >= 2500) {
            maxCapacity = maxCapacity * 3;
        }
        add(readCache = new ReadCache.BytesKey<>(src).withMaxCapacity(maxCapacity)); // 512 - approx size of a node
        readCache.setFlushSource(true);
        writeCache = new AsyncWriteCache<byte[], byte[]>(readCache) {
            @Override
            protected WriteCache<byte[], byte[]> createCache(Source<byte[], byte[]> source) {
                WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<byte[]>(source, WriteCache.CacheType.SIMPLE);
                ret.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
                ret.setFlushSource(true);
                return ret;
            }
        }.withName("state");

        add(writeCache);

        if (pruningEnabled) {
            add(journalSource = new JournalSource<>(writeCache));
        } else {
            add(noDeleteSource = new NoDeleteSource<>(writeCache));
        }
    }

    public void setConfig(SystemProperties config) {
        int size = config.getConfig().getInt("cache.stateCacheSize");
        long memorySize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        Log.info("Total RAM：{} MB", memorySize);
        int maxCapacity = size * 1024 * 2;
        if (memorySize >= 7500) {
            maxCapacity = maxCapacity * 10;
        } else if (memorySize >= 4500) {
            maxCapacity = maxCapacity * 5;
        } else if (memorySize >= 2500) {
            maxCapacity = maxCapacity * 2;
        }
        readCache.withMaxCapacity(maxCapacity); // 512 - approx size of a node
    }

    public void setCommonConfig(CommonConfig commonConfig) {
        if (journalSource != null) {
            journalSource.setJournalStore(commonConfig.cachedDbSource("journal"));
        }
    }

    public JournalSource<byte[]> getJournalSource() {
        return journalSource;
    }

    /**
     * Returns the source behind JournalSource
     */
    public Source<byte[], byte[]> getNoJournalSource() {
        return writeCache;
    }

    public AbstractCachedSource<byte[], byte[]> getWriteCache() {
        return writeCache;
    }

    public ReadCache<byte[], byte[]> getReadCache() {
        return readCache;
    }
}
