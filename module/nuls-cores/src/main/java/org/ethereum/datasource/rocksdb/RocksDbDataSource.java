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

import io.nuls.common.NulsCoresConfig;
import io.nuls.contract.util.Log;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.manager.RocksDBManager;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rockdb.util.DBUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DbSettings;
import org.ethereum.datasource.DbSource;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    RocksDB rocksDB;

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

    public RocksDbDataSource(int chainId) {
        this.AREA = "contract_" + chainId;
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
            //logger.debug("~> RocksDbDataSource.init(): " + name);

            if (isAlive()) {
                return;
            }

            if (name == null) {
                throw new NullPointerException("no name set to the db");
            }


            String[] areas = RocksDBService.listTable();
            if (ArrayUtils.contains(areas, AREA)) {
                RocksDBManager.closeTable(AREA);
            }
            rocksDB = createTable(AREA);

            alive = true;

            //logger.debug("<~ RocksDbDataSource.init(): " + name);
        } catch (Exception e) {
            logger.error("RocksDbDataSource.init() error", e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    private RocksDB createTable(String area) {
        try {
            if (StringUtils.isBlank(area)) {
                throw new RuntimeException("empty area");
            }
            NulsCoresConfig contractConfig = SpringLiteContext.getBean(NulsCoresConfig.class);
            String dataPath = contractConfig.getDataPath();
            File pathDir = DBUtils.loadDataPath(dataPath);
            dataPath = pathDir.getPath();
            dataPath += File.separator + "smart-contract";
            File dir = new File(dataPath + File.separator + area);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dataPath = dataPath + File.separator + area + File.separator + "rocksdb";
            //Log.info("Contract dataPath is " + dataPath);

            Options options = new Options();
            options.setCreateIfMissing(true);
            /**
             * 优化读取性能方案
             */
            options.setAllowMmapReads(true);
            options.setCompressionType(CompressionType.NO_COMPRESSION);
            options.setMaxOpenFiles(-1);

            BlockBasedTableConfig tableOption = new BlockBasedTableConfig();
            tableOption.setBlockCache(new LRUCache(32 * 1024 * 1024));
            tableOption.setCacheIndexAndFilterBlocks(true);
            tableOption.setPinL0FilterAndIndexBlocksInCache(true);
            tableOption.setBlockRestartInterval(4);
            tableOption.setFilterPolicy(new BloomFilter(10, true));
            options.setTableFormatConfig(tableOption);

            options.setNewTableReaderForCompactionInputs(true);
            //为压缩的输入，打开RocksDB层的预读取
            options.setCompactionReadaheadSize(128 * SizeUnit.KB);
            return RocksDB.open(options, dataPath);
        } catch (Exception e) {
            Log.error("error create table: " + area, e);
            throw new RuntimeException("error create table: " + area);
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
        //long startTime = System.nanoTime();
        resetDbLock.readLock().lock();
        try {
            //if (Log.isTraceEnabled()) {
            //    Log.trace("~> RocksDbDataSource.get(): " + name + ", key: " + toHexString(key));
            //}
            try {

                byte[] ret = rocksDB.get(key);
                //if (Log.isInfoEnabled()) {
                //    Log.info("[{}]<~ db.get(): " + name + ", key: " + toHexString(key) + ", " + (ret == null ? "null" : ret.length) + ", cost {}", threadLocal.get(), System.nanoTime() - startTime);
                //}
                return ret;
            } catch (Exception e) {
                logger.warn("Exception. Retrying again...", e);
                byte[] ret = null;
                try {
                    ret = rocksDB.get(key);
                } catch (RocksDBException ex) {
                    // skip it
                }
                //if (Log.isTraceEnabled()) {
                //    Log.trace("<~ RocksDbDataSource.get(): " + name + ", key: " + toHexString(key) + ", " + (ret == null ? "null" : ret.length));
                //}
                return ret;
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(byte[] key, byte[] value) {
        resetDbLock.writeLock().lock();
        try {
            //if (Log.isTraceEnabled()) {
            //    Log.trace("~> RocksDbDataSource.put(): " + name + ", key: " + toHexString(key) + ", " + (value == null ? "null" : value.length));
            //}
            rocksDB.put(key, value);
            //if (Log.isInfoEnabled()) {
            //Log.info("<~ RocksDbDataSource.put(): " + name + ", key: " + toHexString(key) + ", " + (value == null ? "null" : value.length));
            //}
        } catch (Exception e) {
            logger.error("RocksDbDataSource.put() error", e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        resetDbLock.writeLock().lock();
        try {
            //if (Log.isTraceEnabled()) {
            //    Log.trace("~> RocksDbDataSource.delete(): " + name + ", key: " + toHexString(key));
            //}
            rocksDB.delete(key);
            //if (Log.isInfoEnabled()) {
            //    Log.info("<~ RocksDbDataSource.delete(): " + name + ", key: " + toHexString(key));
            //}
        } catch (Exception e) {
            Log.error("RocksDbDataSource.delete() error", e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public Set<byte[]> keys() {
        return null;
    }

    private void updateBatchInternal(Map<byte[], byte[]> rows) throws Exception {
        WriteBatch batch = null;
        try {
            batch = new WriteBatch();
            Set<Map.Entry<byte[], byte[]>> entrySet = rows.entrySet();
            for (Map.Entry<byte[], byte[]> entry : entrySet) {
                if (entry.getValue() == null) {
                    //Log.info("<~ RocksDbDataSource.delete(): " + name + ", key: " + toHexString(entry.getKey()));
                    batch.delete(entry.getKey());
                } else {
                    //Log.info("<~ RocksDbDataSource.put(): " + name + ", key: " + toHexString(entry.getKey()) + ", " + (entry.getValue() == null ? "null" : entry.getValue().length));
                    batch.put(entry.getKey(), entry.getValue());
                }
            }
            rocksDB.write(new WriteOptions(), batch);
        } catch (Exception e) {
            throw e;
        } finally {
            // Make sure you close the batch to avoid resource leaks.
            // 关闭批量操作对象释放资源
            if (batch != null) {
                batch.close();
            }
        }

    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        //long startTime = System.nanoTime();
        resetDbLock.writeLock().lock();
        try {
            //if (Log.isTraceEnabled()) {
            //    Log.trace("~> RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
            //}
            try {
                updateBatchInternal(rows);
                //if (Log.isInfoEnabled()) {
                //    Log.info("<~ RocksDbDataSource.updateBatch(): " + name + ", " + rows.size() + ", cost {}", System.nanoTime() - startTime + "\n");
                //}
            } catch (Exception e) {
                Log.error("Error, retrying one more time...", e);
                // try one more time
                try {
                    updateBatchInternal(rows);
                    //if (Log.isTraceEnabled()) {
                    //    Log.trace("<~ RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
                    //}
                } catch (Exception e1) {
                    Log.error("Error", e);
                    throw new RuntimeException(e);
                }
            }
        } finally {
            resetDbLock.writeLock().unlock();
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
