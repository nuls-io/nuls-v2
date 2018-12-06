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
package io.nuls.ledger.constant;

/**
 * transaction type
 *
 * @seelink https://github.com/nuls-io/nuls_2.0_docs/blob/master/nulstar/%E4%BA%A4%E6%98%93%E7%B1%BB%E5%9E%8B%E5%AD%97%E5%85%B8.md
 * Created by wangkun23 on 2018/12/3.
 */
public enum TransactionType {

    /**
     * 公共交易
     */
    TX_TYPE_COINBASE(1),
    TX_TYPE_TRANSFER(2),
    TX_TYPE_ACCOUNT_ALIAS(3),

    /**
     * consensus module type
     */
    TX_TYPE_REGISTER_AGENT(4),
    TX_TYPE_JOIN_CONSENSUS(5),
    TX_TYPE_CANCEL_DEPOSIT(6),
    TX_TYPE_YELLOW_PUNISH(7),
    TX_TYPE_RED_PUNISH(8),
    TX_TYPE_STOP_AGENT(9),
    TX_TYPE_CROSS_CHAIN_TRANSFER(10),

    /**
     * 链管理
     * chain manager
     */
    TX_TYPE_REGISTER_CHAIN_AND_ASSET(11),
    TX_TYPE_DESTROY_ASSET_AND_CHAIN(12),
    TX_TYPE_ADD_ASSET_TO_CHAIN(13),
    TX_TYPE_REMOVE_ASSET_FROM_CHAIN(14),


    /**
     * 智能合约交易
     */
    TX_TYPE_CREATE_CONTRACT(100),
    TX_TYPE_CALL_CONTRACT(101),
    TX_TYPE_DELETE_CONTRACT(102),
    TX_TYPE_CONTRACT_TRANSFER(103),

    /**
     * default
     */
    OTHER(0);

    private int value;

    TransactionType(int value) {
        this.value = value;
    }

    public static TransactionType valueOf(int value) {
        for (TransactionType type : TransactionType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return OTHER;
    }

    public int getValue() {
        return this.value;
    }
}
