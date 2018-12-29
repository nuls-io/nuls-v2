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
 * Transaction data storage constants
 * @author: qinyifeng
 */
public interface TxDBConstant {

    /**
     * 系统语言表名 一个节点共用，不区分chain
     * system language table name
     */
    String DB_TX_LANGUAGE = "language";
    /**
     * 配置信息表名
     * chain configuration table name
     */
    String DB_MODULE_CONGIF = "config";
    /**
     * 接收到其他链发送的未处理的跨链交易，接收后直接储存
     * Cross-chain transaction in verification table name
     */
    String DB_UNPROCESSED_CROSSCHAIN = "transaction_unprocessed_crosschain";

    /**
     * 接收到其他链发送的跨链交易，已经在验证过程中，储存表名
     * Cross-chain transaction in verification table name
     */
    String DB_PROGRESS_CROSSCHAIN = "transaction_progress_crosschain";

    /**
     * 已确认交易表名
     * Confirmed transaction table name
     */
    String DB_TRANSACTION_CONFIRMED = "transaction_confirmed";

    /**
     * 验证通过但未打包的交易
     */
     String DB_TRANSACTION_CACHE = "transactions_cache";

}
