package io.nuls.core.rockdb.manager;

import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.util.DBUtils;
import org.rocksdb.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionDBManager {

    static {
        TransactionDB.loadLibrary();
    }

    /**
     * Database opened connection cache.
     */
    private static final ConcurrentHashMap<String, TransactionDB> TABLES = new ConcurrentHashMap<>();

    /**
     * Data Table Basic Folder Name.
     */
    private static final String BASE_DB_NAME = "rocksdb";

    /**
     * Has the database been initialized.
     */
    private static volatile boolean isInit = false;

    /**
     * Data operation synchronization lock.
     */
    private static ReentrantLock lock = new ReentrantLock();

    /**
     * Absolute path to database root directory.
     */
    private static String dataPath;

    /**
     * Open existing database connections based on the incoming database path and cache themDBconnect.
     * If a data table connection is closed and needs to be reopened, it is also possible to initialize the connection
     *
     * @param path Database address
     * @throws Exception Database open connection exception
     */
    public static void init(final String path) throws Exception {
        synchronized (TransactionDBManager.class) {
            isInit = true;
            File dir = DBUtils.loadDataPath(path);
            dataPath = dir.getPath();
            Log.info("TransactionDBManager dataPath is " + dataPath);

            File[] tableFiles = dir.listFiles();
            TransactionDB db;
            String dbPath = null;
            for (File tableFile : tableFiles) {
                //Database connections that already exist in the cache will no longer be opened repeatedly
                if (!tableFile.isDirectory() || TABLES.get(tableFile.getName()) != null) {
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
                    throw e;
                }
            }
        }
    }

    /**
     * @param dbPath Database address
     * @return TransactionDB Database Connection Object
     * @throws RocksDBException Database connection exception
     */
    private static TransactionDB initOpenDB(final String dbPath) throws RocksDBException {
        File checkFile = new File(dbPath + File.separator + "CURRENT");
        if (!checkFile.exists()) {
            return null;
        }

        Options options = getCommonOptions(false);
        return TransactionDB.open(options, new TransactionDBOptions(), dbPath);
    }

    /**
     * Create a corresponding database based on the name.
     * Create database based by name
     *
     * @param tableName Database Table Name
     * @return Result Create Results
     */
    public static boolean createTable(final String tableName) throws Exception {
        lock.lock();
        try {
            if (StringUtils.isBlank(tableName)) {
                throw new Exception(DBErrorCode.NULL_PARAMETER);
            }
            if (TABLES.containsKey(tableName)) {
                throw new Exception(DBErrorCode.DB_TABLE_EXIST);
            }
            if (StringUtils.isBlank(dataPath) || !DBUtils.checkPathLegal(tableName)) {
                throw new Exception(DBErrorCode.DB_TABLE_CREATE_PATH_ERROR);
            }
            try {
                File dir = new File(dataPath + File.separator + tableName);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                String filePath = dataPath + File.separator + tableName + File.separator + BASE_DB_NAME;
                Options options = getCommonOptions(true);
                TransactionDB db = TransactionDB.open(options, new TransactionDBOptions(), filePath);
                TABLES.put(tableName, db);
            } catch (Exception e) {
                Log.error("error create table: " + tableName, e);
                throw new Exception(DBErrorCode.DB_TABLE_CREATE_ERROR);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }


    /**
     * Obtain the corresponding database object based on its name.
     * Get database objects by name
     *
     * @param tableName Database Table Name
     * @return RocksDB
     */
    public static TransactionDB getTable(final String tableName) {
        return TABLES.get(tableName);
    }

    /**
     * Delete the corresponding database by name.
     * Delete database by name
     *
     * @param tableName Database Table Name
     * @return Result
     */
    public static boolean destroyTable(final String tableName) throws Exception {
        if (StringUtils.isBlank(dataPath) || !DBUtils.checkPathLegal(tableName)) {
            throw new Exception(DBErrorCode.DB_TABLE_CREATE_PATH_ERROR);
        }
        if (!baseCheckTable(tableName)) {
            throw new Exception(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        try {
            TransactionDB db = TABLES.remove(tableName);
            db.close();
            File dir = new File(dataPath + File.separator + tableName);
            if (!dir.exists()) {
                throw new Exception(DBErrorCode.DB_TABLE_NOT_EXIST);
            }
            String filePath = dataPath + File.separator + tableName + File.separator + BASE_DB_NAME;
            destroyDB(filePath);
        } catch (Exception e) {
            Log.error("error destroy table: " + tableName, e);
            throw new Exception(DBErrorCode.DB_TABLE_DESTROY_ERROR);
        }
        return true;
    }

    /**
     * Delete Data Table.
     *
     * @param dbPath Database Name
     * @throws RocksDBException Database connection exception
     */
    private static void destroyDB(final String dbPath) throws RocksDBException {
        Options options = new Options();
        TransactionDB.destroyDB(dbPath, options);
    }


    /**
     * Query all database names.
     * query all table names
     *
     * @return All data table names
     */
    public static String[] listTable() {
        int i = 0;
        Enumeration<String> keys = TABLES.keys();
        String[] tables = new String[TABLES.size()];
        int length = tables.length;
        while (keys.hasMoreElements()) {
            tables[i++] = keys.nextElement();
            if (i == length) {
                break;
            }
        }
        return tables;
    }


    /**
     * Close all database connections.
     * close all table
     */
    public static void close() {
        Set<Map.Entry<String, TransactionDB>> entries = TABLES.entrySet();
        for (Map.Entry<String, TransactionDB> entry : entries) {
            try {
                TABLES.remove(entry.getKey());
                entry.getValue().close();
            } catch (Exception e) {
                Log.warn("close rocksdb error", e);
            }
        }
    }


    /**
     * Add or modify data.
     * Add or modify entity to specified table
     *
     * @param table Table Name
     * @param key   Data keys
     * @param value Data value
     * @return Whether the save was successful
     */
    public static boolean put(final String table, final byte[] key, final byte[] value) throws Exception {
        if (!baseCheckTable(table)) {
            throw new Exception(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (key == null || value == null) {
            throw new Exception(DBErrorCode.NULL_PARAMETER);
        }
        try {
            TransactionDB db = TABLES.get(table);
            db.put(key, value);
            return true;
        } catch (Exception e) {
            Log.error(e);
            throw new Exception(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * Batch Save Data.
     * batch save entity
     *
     * @param table Database Table Name
     * @param kvs   Save key value pairs for data
     * @return Whether batch saving was successful
     */
    public static boolean batchPut(final String table, final Map<byte[], byte[]> kvs) throws Exception {
        if (!baseCheckTable(table)) {
            throw new Exception(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (kvs == null || kvs.size() == 0) {
            throw new Exception(DBErrorCode.NULL_PARAMETER);
        }
        try (WriteBatch writeBatch = new WriteBatch()) {
            TransactionDB db = TABLES.get(table);
            for (Map.Entry<byte[], byte[]> entry : kvs.entrySet()) {
                writeBatch.put(entry.getKey(), entry.getValue());
            }
            db.write(new WriteOptions(), writeBatch);
            return true;
        } catch (Exception ex) {
            Log.error(ex);
            throw new Exception(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * Delete data.
     * delete entity from specified table
     *
     * @param table Database Table Name
     * @param key   Delete identification
     * @return Whether the deletion was successful
     */
    public static boolean delete(final String table, final byte[] key) throws Exception {
        if (!baseCheckTable(table)) {
            throw new Exception(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (key == null) {
            throw new Exception(DBErrorCode.NULL_PARAMETER);
        }
        try {
            TransactionDB db = TABLES.get(table);
            db.delete(key);
            return true;
        } catch (Exception e) {
            Log.error(e);
            throw new Exception(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }


    /**
     * Batch deletion of data.
     * batch delete entity
     *
     * @param table Database Table Name
     * @param keys  Batch deletion of identification
     * @return Whether batch deletion was successful
     */
    public static boolean deleteKeys(final String table, final List<byte[]> keys) throws Exception {
        if (!baseCheckTable(table)) {
            throw new Exception(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (keys == null || keys.size() == 0) {
            throw new Exception(DBErrorCode.NULL_PARAMETER);
        }
        try (WriteBatch writeBatch = new WriteBatch()) {
            TransactionDB db = TABLES.get(table);
            for (byte[] key : keys) {
                writeBatch.delete(key);
            }
            db.write(new WriteOptions(), writeBatch);
            return true;
        } catch (Exception ex) {
            Log.error(ex);
            throw new Exception(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
    }

    /**
     * according tokeyQuery data.
     * query entity in a specified table by key
     *
     * @param table Database Table Name
     * @param key   Query keywords
     * @return Query Results
     */
    public static byte[] get(final String table, final byte[] key) {
        if (!baseCheckTable(table)) {
            return null;
        }
        if (key == null) {
            return null;
        }
        try {
            TransactionDB db = TABLES.get(table);
            return db.get(key);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Batch query specificationkeysofMapaggregate.
     * batch query the Map set of the specified keys.
     *
     * @param table Database Table Name
     * @param keys  Batch query keywords
     * @return Batch query result key value pair set
     */
    public static Map<byte[], byte[]> multiGet(final String table, final List<byte[]> keys) {
        if (!baseCheckTable(table)) {
            return null;
        }
        if (keys == null || keys.size() == 0) {
            return null;
        }
        try {
            TransactionDB db = TABLES.get(table);
            return db.multiGet(keys);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Batch query specificationkeysofListaggregate
     * batch query the List set of the specified keys.
     *
     * @param table Database Table Name
     * @param keys  Batch query keywords
     * @return Batch query result value byte array collection
     */
    public static List<byte[]> multiGetValueList(final String table, final List<byte[]> keys) {
        List<byte[]> list = new ArrayList<>();
        if (!baseCheckTable(table)) {
            return list;
        }
        if (keys == null || keys.size() == 0) {
            return list;
        }
        try {
            TransactionDB db = TABLES.get(table);
            Map<byte[], byte[]> map = db.multiGet(keys);
            if (map != null && map.size() > 0) {
                list.addAll(map.values());
            }
            return list;
        } catch (Exception ex) {
            return list;
        }
    }

    /**
     * Batch query specificationkeysofListaggregate
     * batch query the List set of the specified keys.
     *
     * @param table Database Table Name
     * @param keys  Batch query keywords
     * @return Batch query result value byte array collection
     */
    public static List<byte[]> multiGetKeyList(final String table, final List<byte[]> keys) {
        List<byte[]> list = new ArrayList<>();
        if (!baseCheckTable(table)) {
            return list;
        }
        if (keys == null || keys.size() == 0) {
            return list;
        }
        try {
            TransactionDB db = TABLES.get(table);
            Map<byte[], byte[]> map = db.multiGet(keys);
            if (map != null && map.size() > 0) {
                list.addAll(map.keySet());
            }
            return list;
        } catch (Exception ex) {
            return list;
        }
    }


    /**
     * Query the specified tablekey-Listaggregate.
     * query the key-List collection of the specified table
     *
     * @param table Database Table Name
     * @return All keys of this table
     */
    public static List<byte[]> keyList(final String table) {
        if (!baseCheckTable(table)) {
            return null;
        }
        List<byte[]> list = new ArrayList<>();
        try {
            TransactionDB db = TABLES.get(table);
            try (RocksIterator iterator = db.newIterator()) {
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
     * Query the specified tablevalue-Listaggregate.
     * query the value-List collection of the specified table
     *
     * @param table Database Table Name
     * @return All values in this table
     */
    public static List<byte[]> valueList(final String table) {
        if (!baseCheckTable(table)) {
            return null;
        }
        List<byte[]> list = new ArrayList<>();
        try {
            TransactionDB db = TABLES.get(table);
            try (RocksIterator iterator = db.newIterator()) {
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
     * Query the specified tableentry-Listaggregate.
     * query the entry-List collection of the specified table
     *
     * @param table Database Table Name
     * @return The set of all key value pairs in this table
     */
    public static List<Entry<byte[], byte[]>> entryList(final String table) {
        if (!baseCheckTable(table)) {
            return null;
        }
        List<Entry<byte[], byte[]>> entryList = new ArrayList<>();
        try {
            TransactionDB db = TABLES.get(table);
            try (RocksIterator iterator = db.newIterator()) {
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    entryList.add(new Entry(iterator.key(), iterator.value()));
                }
            }
            return entryList;
        } catch (Exception ex) {
            return null;
        }
    }

    public static Transaction openSession(final String table) {
        WriteOptions options = new WriteOptions();
       // options.setSync(false);
        TransactionDB db = TABLES.get(table);
        return db.beginTransaction(options);
    }

    public static void commit(Transaction tx) throws RocksDBException {
        try {
            tx.commit();
        }finally {
            tx.close();
        }
    }

    public static void rollBack(Transaction tx) throws RocksDBException {
        try {
            tx.rollback();
        }finally {
            tx.close();
        }
    }

    /**
     * Basic Database Verification.
     * Basic database check
     *
     * @param tableName Database Table Name
     * @return boolean Verify if successful
     */
    private static boolean baseCheckTable(final String tableName) {
        if (StringUtils.isBlank(tableName) || !TABLES.containsKey(tableName)) {
            return false;
        }
        return true;
    }


    /**
     * Obtain public database connection properties.
     *
     * @param createIfMissing Default Table
     * @return Database Connection Properties
     */
    private static synchronized Options getCommonOptions(final boolean createIfMissing) {
        Options options = new Options();
        options.setCreateIfMissing(createIfMissing);
        /**
         * Optimize reading performance plan
         */
        options.setAllowMmapReads(true);
        options.setCompressionType(CompressionType.NO_COMPRESSION);
        options.setMaxOpenFiles(-1);

        BlockBasedTableConfig tableOption = new BlockBasedTableConfig();
        tableOption.setNoBlockCache(true);
        tableOption.setBlockRestartInterval(4);
        tableOption.setFilterPolicy(new BloomFilter(10, true));
        options.setTableFormatConfig(tableOption);

        return options;
    }
}
