/**
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
package io.nuls.db.manager;

import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.constant.KernelErrorCode;
import io.nuls.db.log.Log;
import io.nuls.db.model.Entry;
import io.nuls.db.model.Result;
import io.nuls.db.util.DBUtils;
import io.nuls.db.util.StringUtils;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.Filter;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Statistics;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.rocksdb.util.SizeUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.db.constant.DBConstant.BASE_TABLE_NAME;

public class RocksDBManager {

    static {
        RocksDB.loadLibrary();
    }

    private static final ConcurrentHashMap<String, RocksDB> TABLES = new ConcurrentHashMap<>();

    private static final String BASE_DB_NAME = "rocksdb";

    private static volatile boolean isInit = false;

    private static ReentrantLock lock = new ReentrantLock();

    private static String dataPath;

    public static String getBaseTableName() {
        return BASE_TABLE_NAME;
    }

    /**
     * 根据传入的数据库路径将已存在的数据库连接打开，并缓存DB连接
     *
     * @param path
     * @throws Exception
     */
    public static void init(String path) throws Exception {
        synchronized (RocksDBManager.class) {
            if (!isInit) {
                isInit = true;
                File dir = DBUtils.loadDataPath(path);
                dataPath = dir.getPath();
                Log.info("RocksDBManager dataPath is " + dataPath);

                //initBaseDB(dataPath);

                File[] tableFiles = dir.listFiles();
                RocksDB db = null;
                String dbPath = null;
                for (File tableFile : tableFiles) {
                    if (BASE_TABLE_NAME.equals(tableFile.getName())) {
                        continue;
                    }
                    if (!tableFile.isDirectory()) {
                        continue;
                    }
                    try {
                        dbPath = tableFile.getPath() + File.separator + BASE_DB_NAME;
                        db = initOpenDB(dbPath);
                        if (db != null) {
                            TABLES.put(tableFile.getName(), db);
                        }
                    } catch (Exception e) {
                        Log.warn("load table failed, tableName: " + tableFile.getName() + ", dbPath: " + dbPath, e);
                    }

                }
            }
        }

    }

    /**
     * @param dbPath
     * @return
     * @throws IOException
     */
    private static RocksDB initOpenDB(String dbPath) throws RocksDBException {
        File checkFile = new File(dbPath + File.separator + "CURRENT");
        if (!checkFile.exists()) {
            return null;
        }
        Options options = getCommonOptions(false);
        return RocksDB.open(options, dbPath);
    }

    /**
     * 装载数据库
     * load database
     *
     * @param dbPath
     * @param createIfMissing
     * @return
     * @throws IOException
     */
    private static RocksDB openDB(String dbPath, boolean createIfMissing) throws RocksDBException {
        Options options = getCommonOptions(createIfMissing);
        return RocksDB.open(options, dbPath);
    }

    /**
     * 根据名称创建对应的数据库
     *
     * @param tableName
     * @return
     */
    public static Result createTable(String tableName) {
        lock.lock();
        try {
            if (StringUtils.isBlank(tableName)) {
                return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
            }
            if (TABLES.containsKey(tableName)) {
                return Result.getFailed(DBErrorCode.DB_TABLE_EXIST);
            }
            if (StringUtils.isBlank(dataPath) || !DBUtils.checkPathLegal(tableName)) {
                return Result.getFailed(DBErrorCode.DB_TABLE_CREATE_PATH_ERROR);
            }
            Result result;
            try {
                File dir = new File(dataPath + File.separator + tableName);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                String filePath = dataPath + File.separator + tableName + File.separator + BASE_DB_NAME;
                RocksDB db = openDB(filePath, true);
                TABLES.put(tableName, db);
                result = Result.getSuccess();
            } catch (Exception e) {
                Log.error("error create table: " + tableName, e);
                result = Result.getFailed(DBErrorCode.DB_TABLE_CREATE_ERROR);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据名称获得对应的数据库对象
     *
     * @param tableName
     * @return
     */
    public static RocksDB getTable(String tableName) {
        return TABLES.get(tableName);
    }

    /**
     * 根据名称删除对应的数据库
     *
     * @param tableName
     * @return
     */
    public static Result destroyTable(String tableName) {
        if (!baseCheckTable(tableName)) {
            return Result.getFailed(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (StringUtils.isBlank(dataPath) || !DBUtils.checkPathLegal(tableName)) {
            return Result.getFailed(DBErrorCode.DB_TABLE_CREATE_PATH_ERROR);
        }
        Result result;
        try {
            RocksDB db = TABLES.remove(tableName);
            db.close();
            File dir = new File(dataPath + File.separator + tableName);
            if (!dir.exists()) {
                return Result.getFailed(DBErrorCode.DB_TABLE_NOT_EXIST);
            }
            String filePath = dataPath + File.separator + tableName + File.separator + BASE_DB_NAME;
            destroyDB(filePath);
            //TABLES_COMPARATOR.remove(tableName);
            //delete(BASE_TABLE_NAME, bytes(tableName + "-comparator"));
            //delete(BASE_TABLE_NAME, bytes(tableName + "-cacheSize"));
            result = Result.getSuccess();
        } catch (Exception e) {
            Log.error("error destroy table: " + tableName, e);
            result = Result.getFailed(DBErrorCode.DB_TABLE_DESTROY_ERROR);
        }
        return result;
    }

    private static void destroyDB(String dbPath) throws RocksDBException {
        Options options = new Options();
        RocksDB.destroyDB(dbPath, options);
    }

    /**
     * close all table
     * 关闭所有数据区域
     */
    public static void close() {
        Set<Map.Entry<String, RocksDB>> entries = TABLES.entrySet();
        for (Map.Entry<String, RocksDB> entry : entries) {
            try {
                TABLES.remove(entry.getKey());
                //TABLES_COMPARATOR.remove(entry.getKey());
                entry.getValue().close();
            } catch (Exception e) {
                Log.warn("close rocksdb error", e);
            }
        }
    }

    /**
     * close a table
     * 关闭指定数据区域
     */
    public static void closeTable(String tableName) {
        try {
            //TABLES_COMPARATOR.remove(table);
            RocksDB db = TABLES.remove(tableName);
            db.close();
        } catch (Exception e) {
            Log.warn("close rocksdb tableName error:" + tableName, e);
        }
    }

    private static boolean baseCheckTable(String tableName) {
        if (StringUtils.isBlank(tableName) || !TABLES.containsKey(tableName)) {
            return false;
        }
        return true;
    }

    /**
     * 查询所有的数据库名称
     * query all table names
     *
     * @return
     */
    public static String[] listTable() {
        int i = 0;
        Enumeration<String> keys = TABLES.keys();
        String[] tables = new String[TABLES.size()];
        int length = tables.length;
        while (keys.hasMoreElements()) {
            tables[i++] = keys.nextElement();
            // thread safe, prevent java.lang.ArrayIndexOutOfBoundsException
            if (i == length) {
                break;
            }
        }
        return tables;
    }

    /**
     * 新增或者修改数据
     * Add or modify data to specified table
     *
     * @param table 表名
     * @param key   数据键
     * @param value 数据值
     * @return
     */
    public static Result put(String table, byte[] key, byte[] value) {
        if (!baseCheckTable(table)) {
            return Result.getFailed(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (key == null || value == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            RocksDB db = TABLES.get(table);
            db.put(key, value);
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 删除数据
     * delete data from specified table
     *
     * @param table
     * @param key
     * @return
     */
    public static Result delete(String table, byte[] key) {
        if (!baseCheckTable(table)) {
            return Result.getFailed(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (key == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        try {
            RocksDB db = TABLES.get(table);
            db.delete(key);
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    public static Result batchPut(String table, Map<byte[], byte[]> kvs) {
        if (!baseCheckTable(table)) {
            return Result.getFailed(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (kvs == null || kvs.size() == 0) {
            return null;
        }

        try (WriteBatch writeBatch = new WriteBatch()) {
            RocksDB db = TABLES.get(table);
            for (Map.Entry<byte[], byte[]> entry : kvs.entrySet()) {
                writeBatch.put(entry.getKey(), entry.getValue());
            }
            db.write(new WriteOptions(), writeBatch);
            return Result.getSuccess();
        } catch (Exception ex) {
            Log.error(ex);
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    public static Result deleteKeys(String table, List<byte[]> keys) {
        if (!baseCheckTable(table)) {
            return Result.getFailed(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (keys == null || keys.size() == 0) {
            return null;
        }
        try (WriteBatch writeBatch = new WriteBatch()) {
            RocksDB db = TABLES.get(table);
            for (byte[] key : keys) {
                writeBatch.delete(key);
            }
            db.write(new WriteOptions(), writeBatch);
            return Result.getSuccess();
        } catch (Exception ex) {
            Log.error(ex);
            return Result.getFailed(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 根据key查询数据
     * query data in a specified table by key
     *
     * @param table
     * @param key
     * @return
     */
    public static byte[] get(String table, byte[] key) {
        if (!baseCheckTable(table)) {
            return null;
        }
        if (key == null) {
            return null;
        }
        try {
            RocksDB db = TABLES.get(table);
            return db.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 批量查询指定keys的Map集合
     * batch query the Map set of the specified keys.
     *
     * @param table
     * @param keys
     * @return
     */
    public static Map<byte[], byte[]> multiGet(String table, List<byte[]> keys) {
        if (!baseCheckTable(table)) {
            return null;
        }
        if (keys == null || keys.size() == 0) {
            return null;
        }
        try {
            RocksDB db = TABLES.get(table);
            return db.multiGet(keys);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 批量查询指定keys的Map集合
     * batch query the List set of the specified keys.
     *
     * @param table
     * @param keys
     * @return
     */
    public static List<byte[]> multiGetValueList(String table, List<byte[]> keys) {
        if (!baseCheckTable(table)) {
            return null;
        }
        if (keys == null || keys.size() == 0) {
            return null;
        }
        try {
            RocksDB db = TABLES.get(table);
            Map<byte[], byte[]> map = db.multiGet(keys);
            if (map != null && map.size() > 0) {
                return new ArrayList<>(map.values());
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 查询指定表的key-List集合
     * query the key-List collection of the specified table
     *
     * @param table
     * @return
     */
    public static List<byte[]> keyList(String table) {
        if (!baseCheckTable(table)) {
            return null;
        }
        List<byte[]> list = new ArrayList<>();
        try {
            RocksDB db = TABLES.get(table);
            try (final RocksIterator iterator = db.newIterator()) {
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    list.add(iterator.key());
                }
            }
            return list;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 查询指定表的value-List集合
     * query the value-List collection of the specified table
     *
     * @param table
     * @return
     */
    public static List<byte[]> valueList(String table) {
        if (!baseCheckTable(table)) {
            return null;
        }
        List<byte[]> list = new ArrayList<>();
        try {
            RocksDB db = TABLES.get(table);
            try (final RocksIterator iterator = db.newIterator()) {
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    list.add(iterator.value());
                }
            }
            return list;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 查询指定表的entry-List集合
     * query the entry-List collection of the specified table
     *
     * @param table
     * @return
     */
    public static List<Entry<byte[], byte[]>> entryList(String table) {
        if (!baseCheckTable(table)) {
            return null;
        }
        List<Entry<byte[], byte[]>> entryList = new ArrayList<>();
        try {
            RocksDB db = TABLES.get(table);
            try (final RocksIterator iterator = db.newIterator()) {
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    entryList.add(new Entry(iterator.key(), iterator.value()));
                }
            }
            return entryList;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 获得公共的数据库连接属性
     *
     * @param createIfMissing
     * @return
     */
    private synchronized static Options getCommonOptions(boolean createIfMissing) {
        Options options = new Options();
        final Filter bloomFilter = new BloomFilter(10);
        final Statistics stats = new Statistics();
        //final RateLimiter rateLimiter = new RateLimiter(10000000, 10000, 10);

        options.setCreateIfMissing(createIfMissing).setAllowMmapReads(true).setCreateMissingColumnFamilies(true)
                .setStatistics(stats).setMaxWriteBufferNumber(3).setMaxBackgroundCompactions(10);

        final BlockBasedTableConfig tableOptions = new BlockBasedTableConfig();
        tableOptions.setBlockCacheSize(64 * SizeUnit.KB).setFilter(bloomFilter)
                .setCacheNumShardBits(6).setBlockSizeDeviation(5).setBlockRestartInterval(10)
                .setCacheIndexAndFilterBlocks(true).setHashIndexAllowCollision(false)
                .setBlockCacheCompressedSize(64 * SizeUnit.KB)
                .setBlockCacheCompressedNumShardBits(10);

        options.setTableFormatConfig(tableOptions);
        return options;
    }

}
