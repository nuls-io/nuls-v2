/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.transaction.db.rocksdb.storage.impl;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionManager;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.rocksdb.storage.TxUnverifiedStorageService;
import io.nuls.transaction.model.bo.TxWrapper;
import io.nuls.transaction.utils.queue.entity.PersistentQueue;

import java.io.IOException;

/**
 * 未验证交易存储
 * @author: qinyifeng
 * @date: 2018/11/29
 */
@Service
public class TxUnverifiedStorageServiceImpl implements TxUnverifiedStorageService {

    private PersistentQueue queue = new PersistentQueue(TxConstant.TX_UNVERIFIED_QUEUE, TxConstant.TX_UNVERIFIED_QUEUE_MAXSIZE);

    public TxUnverifiedStorageServiceImpl() throws Exception {
    }

    @Override
    public boolean putTx(TxWrapper txWrapper) {
        try {
            queue.offer(txWrapper.serialize());
            return true;
        } catch (IOException e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public TxWrapper pollTx() {
        byte[] bytes = queue.poll();
        if (null == bytes) {
            return null;
        }
        try {
            TxWrapper txWrapper=new TxWrapper();
            txWrapper.parse(new NulsByteBuffer(bytes));
            return txWrapper;
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }
}
