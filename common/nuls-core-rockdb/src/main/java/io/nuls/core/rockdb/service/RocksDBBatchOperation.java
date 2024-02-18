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

import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.manager.RocksDBManager;
import io.nuls.core.log.Log;
import org.rocksdb.RocksDB;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

public class RocksDBBatchOperation implements BatchOperation {

    private String table;
    private RocksDB db;
    private WriteBatch batch;
    private volatile boolean isClose = false;

    RocksDBBatchOperation(String table) {
        this.table = table;
        db = RocksDBManager.getTable(table);
        if (db != null) {
            batch = new WriteBatch();
        }
    }

    public boolean checkBatch() throws Exception {
        if (db == null) {
            throw new Exception(DBErrorCode.DB_TABLE_NOT_EXIST);
        }
        if (batch == null) {
            throw new Exception(DBErrorCode.DB_UNKOWN_EXCEPTION);
        }
        return true;
    }

    @Override
    public boolean put(byte[] key, byte[] value) throws Exception {
        if (key == null || value == null) {
            throw new Exception(DBErrorCode.NULL_PARAMETER);
        }
        batch.put(key, value);
        return true;
    }

    @Override
    public boolean delete(byte[] key) throws Exception {
        if (key == null) {
            throw new Exception(DBErrorCode.NULL_PARAMETER);
        }
        batch.delete(key);
        return true;
    }

    private void close() {
        this.isClose = true;
    }

    private boolean checkClose() {
        return isClose;
    }

    @Override
    public boolean executeBatch() throws Exception {
        // Check logic shutdown
        if (checkClose()) {
            throw new Exception(DBErrorCode.DB_TABLE_FAILED_BATCH_CLOSE);
        }
        try {
            db.write(new WriteOptions(), batch);
        } catch (Exception e) {
            Log.error(e);
            throw new Exception(DBErrorCode.DB_UNKOWN_EXCEPTION);
        } finally {
            // Make sure you close the batch to avoid resource leaks.
            // Close batch operation objects to release resources
            if (batch != null) {
                this.close();
                batch.close();
            }
        }
        return true;
    }
}
