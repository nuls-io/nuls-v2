/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
 * Transaction data storage constant
 * Transaction entity storage constants
 * @author: qinyifeng
 */
public interface TxDBConstant {
    /**
     * Configuration Information Table Name
     * chain configuration table name
     */
    String DB_MODULE_CONGIF = "config";

    /**
     * Confirmed transaction table name
     * Confirmed transaction table name
     */
    String DB_TRANSACTION_CONFIRMED_PREFIX = "tx_table_confirmed_";

    /**
     * Transactions that have been validated but not packaged(Unconfirmed)
     */
     String DB_TRANSACTION_UNCONFIRMED_PREFIX = "tx_table_unconfirmed_";
}
