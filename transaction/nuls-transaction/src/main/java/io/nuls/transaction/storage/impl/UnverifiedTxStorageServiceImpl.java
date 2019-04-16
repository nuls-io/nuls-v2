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

package io.nuls.transaction.storage.impl;


import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.storage.UnverifiedTxStorageService;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;

import static io.nuls.transaction.utils.LoggerUtil.Log;

/**
 * 未验证交易存储
 *
 * @author: qinyifeng
 * @date: 2018/11/29
 */
@Component
public class UnverifiedTxStorageServiceImpl implements UnverifiedTxStorageService {

    @Override
    public boolean putTx(Chain chain, Transaction tx) {
        try {
            chain.getUnverifiedQueue().offer(tx.serialize());
            return true;
        } catch (IOException e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public Transaction pollTx(Chain chain) {
        try {
            return TxUtil.getTransaction(chain.getUnverifiedQueue().poll());
        } catch (NulsException e) {
            Log.error(e);
        }
        return null;
    }

    @Override
    public long size(Chain chain) {
        return chain.getUnverifiedQueue().size();
    }
}
