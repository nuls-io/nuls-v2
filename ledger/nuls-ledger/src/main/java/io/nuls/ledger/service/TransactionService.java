/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.service;


import io.nuls.base.data.Transaction;

/**
 * Created by wangkun23 on 2018/11/28.
 * update by lanjinsheng on 2018/12/31
 */
public interface TransactionService {



    /**
     * 未确认交易数据处理
     */
    boolean unConfirmTxProcess(int addressChainId,Transaction transaction);

    /**
     * 已确认交易数据处理
     */
    boolean confirmTxProcess(int addressChainId,Transaction transaction);
    /**
     * 确认交易回滚处理
     */
    boolean rollBackConfirmTx(int addressChainId,Transaction transaction);
    /**
     * 未确认交易回滚处理
     */
    boolean rollBackUnconfirmTx(int addressChainId,Transaction transaction);
}
