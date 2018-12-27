/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2018 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.block.test;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;

@Service
public class TransactionStorageServiceImpl implements TransactionStorageService {
    @Override
    public boolean save(int chainId, Transaction transaction) {
        byte[] bytes;
        try {
            bytes = transaction.serialize();
            return RocksDBService.put("tx" + chainId, transaction.getHash().serialize(), bytes);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public Transaction query(int chainId, NulsDigestData hash) {
        try {
            Transaction po = new Transaction();
            byte[] bytes = RocksDBService.get("tx" + chainId, hash.serialize());
            po.parse(new NulsByteBuffer(bytes));
            return po;
        }catch (Exception e){
            Log.error(e);
            return null;
        }
    }

    @Override
    public boolean remove(int chainId, NulsDigestData hash) {
        try {
            return RocksDBService.delete("tx" + chainId, hash.serialize());
        }catch (Exception e){
            Log.error(e);
            return false;
        }
    }
}
