/**
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
package io.nuls.core.rockdb.service;

import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.manager.RocksDBManager;
import io.nuls.core.rockdb.model.Entry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RocksDBService {

    public static void init(String path) {
        try {
            RocksDBManager.init(path);
        } catch (Exception e) {
            Log.error(e.getMessage(),e);
        }
    }

    public static boolean createTable(String tableName) throws Exception {
        if (!RocksDBService.existTable(tableName)) {
            return RocksDBManager.createTable(tableName);
        }
        return false;
    }

    public static boolean destroyTable(String table) throws Exception {
        return RocksDBManager.destroyTable(table);
    }

    public static String[] listTable() {
        return RocksDBManager.listTable();
    }

    /**
     * 如果表不存在就创建表
     * if table not exist then create this;
     * @param table
     * @return
     * @throws Exception
     */
    public static boolean createTableIfNotExist(String table) throws Exception {
        boolean exist = existTable(table);
        if(!exist){
            createTable(table);
        }
        return exist;
    }

    /**
     * 判断表是否存在
     *
     * @param table
     * @return
     */
    public static boolean existTable(String table) {
        return RocksDBManager.getTable(table) != null;
    }


    public static boolean put(String table, byte[] key, byte[] value) throws Exception {
        return RocksDBManager.put(table, key, value);
    }

    public static boolean delete(String table, byte[] key) throws Exception {
        return RocksDBManager.delete(table, key);
    }

    public static boolean batchPut(String table, Map<byte[], byte[]> kvs) throws Exception {
        return RocksDBManager.batchPut(table, kvs);
    }

    public static boolean deleteKeys(String table, List<byte[]> keys) throws Exception {
        return RocksDBManager.deleteKeys(table, keys);
    }

    public static byte[] get(String table, byte[] key) {
        return RocksDBManager.get(table, key);
    }

    public static boolean keyMayExist(final String table, final byte[] key) {
        return RocksDBManager.keyMayExist(table, key);
    }

    public static Map<byte[], byte[]> multiGet(String table, List<byte[]> keys) {
        return RocksDBManager.multiGet(table, keys);
    }

    public static List<byte[]> multiGetAsList(String table, List<byte[]> keys) {
        return RocksDBManager.multiGetAsList(table, keys);
    }

    public static List<byte[]> multiGetValueList(String table, List<byte[]> keys) {
        return RocksDBManager.multiGetValueList(table, keys);
    }
    public static List<byte[]> multiGetKeyList(String table, List<byte[]> keys) {
        return RocksDBManager.multiGetKeyList(table, keys);
    }

    public static List<byte[]> keyList(String table) {
        return RocksDBManager.keyList(table);
    }

    public static List<byte[]> valueList(String table) {
        return RocksDBManager.valueList(table);
    }

    public static List<Entry<byte[], byte[]>> entryList(String table) {
        return RocksDBManager.entryList(table);
    }

    public static BatchOperation createWriteBatch(String table) {
        if (StringUtils.isBlank(table)) {
            return null;
        }
        RocksDBBatchOperation batchOperation = new RocksDBBatchOperation(table);
        boolean result = false;
        try {
            result = batchOperation.checkBatch();
        } catch (Exception e) {
            Log.error("DB batch create error: " + e.getMessage());
        }
        if (!result) {
            return null;
        }
        return batchOperation;
    }
}
