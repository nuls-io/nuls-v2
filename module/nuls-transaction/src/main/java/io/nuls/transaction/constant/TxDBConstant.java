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

package io.nuls.transaction.constant;

/**
 * 交易数据存储常量
 * Transaction entity storage constants
 * @author: qinyifeng
 */
public interface TxDBConstant {
    /**
     * 配置信息表名
     * chain configuration table name
     */
    String DB_MODULE_CONGIF = "config";

    /**
     * 已确认交易表名
     * Confirmed transaction table name
     */
    String DB_TRANSACTION_CONFIRMED_PREFIX = "tx_table_confirmed_";

    /**
     * 验证通过但未打包的交易(未确认)
     */
     String DB_TRANSACTION_UNCONFIRMED_PREFIX = "tx_table_unconfirmed_";


    /** 接收新交易的文件队列名**/
    String TX_UNVERIFIED_QUEUE_PREFIX = "tx_queue_unverified_";

}
