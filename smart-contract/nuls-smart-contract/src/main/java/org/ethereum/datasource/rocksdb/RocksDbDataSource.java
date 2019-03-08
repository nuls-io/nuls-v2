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
package org.ethereum.datasource.rocksdb;

import io.nuls.contract.model.bo.Chain;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.RocksDBService;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DbSettings;
import org.ethereum.datasource.DbSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.ethereum.util.ByteUtil.toHexString;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public class RocksDbDataSource implements DbSource<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger("db");

    private String AREA;

    SystemProperties config = SystemProperties.getDefault(); // initialized for standalone test

    String name;
    boolean alive;
    Chain chain;

    DbSettings settings = DbSettings.DEFAULT;

    // The native LevelDB insert/update/delete are normally thread-safe
    // However close operation is not thread-safe and may lead to a native crash when
    // accessing a closed DB.
    // The leveldbJNI lib has a protection over accessing closed DB but it is not synchronized
    // This ReadWriteLock still permits concurrent execution of insert/delete/update operations
    // however blocks them on init/close/delete operations
    private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    public RocksDbDataSource() {
    }

    public RocksDbDataSource(Chain chain) {
        this.AREA = "contract" + chain.getChainId();
        this.chain = chain;
    }

    public RocksDbDataSource(String name) {
        this.name = name;
        logger.debug("New RocksDbDataSource: " + name);
    }

    @Override
    public void init() {
        init(DbSettings.DEFAULT);
    }

    @Override
    public void init(DbSettings settings) {
        this.settings = settings;
        resetDbLock.writeLock().lock();
        try {
            logger.debug("~> RocksDbDataSource.init(): " + name);

            if (isAlive()) {
                return;
            }

            if (name == null) {
                throw new NullPointerException("no name set to the db");
            }


            String[] areas = RocksDBService.listTable();
            if (!ArrayUtils.contains(areas, AREA)) {
                RocksDBService.createTable(AREA);
            }

            alive = true;

            logger.debug("<~ RocksDbDataSource.init(): " + name);
        } catch (Exception e) {
            logger.error("RocksDbDataSource.init() error", e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public byte[] prefixLookup(byte[] key, int prefixBytes) {
        throw new RuntimeException("RocksDbDataSource.prefixLookup() is not supported");
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] get(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.get(): " + name + ", key: " + toHexString(key));
            }
            try {
                byte[] ret = RocksDBService.get(AREA, key);
                if (logger.isTraceEnabled()) {
                    logger.trace("<~ RocksDbDataSource.get(): " + name + ", key: " + toHexString(key) + ", " + (ret == null ? "null" : ret.length));
                }
                return ret;
            } catch (Exception e) {
                logger.warn("Exception. Retrying again...", e);
                byte[] ret = RocksDBService.get(AREA, key);
                if (logger.isTraceEnabled()) {
                    logger.trace("<~ RocksDbDataSource.get(): " + name + ", key: " + toHexString(key) + ", " + (ret == null ? "null" : ret.length));
                }
                return ret;
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(byte[] key, byte[] value) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.put(): " + name + ", key: " + toHexString(key) + ", " + (value == null ? "null" : value.length));
            }
            RocksDBService.put(AREA, key, value);
            if (logger.isTraceEnabled()) {
                logger.trace("<~ RocksDbDataSource.put(): " + name + ", key: " + toHexString(key) + ", " + (value == null ? "null" : value.length));
            }
        } catch (Exception e) {
            logger.error("RocksDbDataSource.put() error", e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.delete(): " + name + ", key: " + toHexString(key));
            }
            RocksDBService.delete(AREA, key);
            if (logger.isTraceEnabled()) {
                logger.trace("<~ RocksDbDataSource.delete(): " + name + ", key: " + toHexString(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("RocksDbDataSource.delete() error", e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> keys() {
        return null;
    }

    private void updateBatchInternal(Map<byte[], byte[]> rows) throws Exception {
        BatchOperation batchOperation = RocksDBService.createWriteBatch(AREA);
        for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
            if (entry.getValue() == null) {
                batchOperation.delete(entry.getKey());
            } else {
                batchOperation.put(entry.getKey(), entry.getValue());
            }
        }
        batchOperation.executeBatch();
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
            }
            try {
                updateBatchInternal(rows);
                if (logger.isTraceEnabled()) {
                    logger.trace("<~ RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
                }
            } catch (Exception e) {
                logger.error("Error, retrying one more time...", e);
                // try one more time
                try {
                    updateBatchInternal(rows);
                    if (logger.isTraceEnabled()) {
                        logger.trace("<~ RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
                    }
                } catch (Exception e1) {
                    logger.error("Error", e);
                    throw new RuntimeException(e);
                }
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public boolean flush() {
        return false;
    }

    @Override
    public void close() {
    }

}
