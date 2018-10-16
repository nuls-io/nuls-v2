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
package io.nuls.db.service;

import io.nuls.db.manager.RocksDBManager;
import io.nuls.db.model.Entry;
import io.nuls.tools.basic.Result;

import java.util.List;
import java.util.Map;

public class DBService {

    public static void init(String path) throws Exception {
        RocksDBManager.init(path);
    }

    public static Result createTable(String tableName) {
        return RocksDBManager.createTable(tableName);
    }

    public static Result destroyTable(String table) {
        return RocksDBManager.destroyTable(table);
    }

    public static String[] listTable() {
        return RocksDBManager.listTable();
    }

    public static Result put(String table, byte[] key, byte[] value) {
        return RocksDBManager.put(table, key, value);
    }

    public static Result delete(String table, byte[] key) {
        return RocksDBManager.delete(table, key);
    }

    public static Result batchPut(String table, Map<byte[], byte[]> kvs) {
        return RocksDBManager.batchPut(table, kvs);
    }

    public static Result deleteKeys(String table, List<byte[]> keys) {
        return RocksDBManager.deleteKeys(table, keys);
    }

    public static byte[] get(String table, byte[] key) {
        return RocksDBManager.get(table, key);
    }

    public static Map<byte[], byte[]> multiGet(String table, List<byte[]> keys) {
        return RocksDBManager.multiGet(table, keys);
    }

    public static List<byte[]> multiGetValueList(String table, List<byte[]> keys) {
        return RocksDBManager.multiGetValueList(table, keys);
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


}
