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
package io.nuls.ledger.storage;

/**
 * database table name constant
 * Created by wangkun23 on 2018/11/19.
 * @author lanjinsheng
 */
public interface DataBaseArea {
    String TB_LEDGER_ACCOUNT = "account";

    String TB_LEDGER_LOCK_TX = "locked_ledger";
    /**
     *   存未确认交易数据及状态
     */
    String TB_LEDGER_ACCOUNT_UNCONFIRMED = "account_unconfirmed";
    String TB_LEDGER_TX_UNCONFIRMED = "tx_unconfirmed";
    String TB_LEDGER_ACCOUNT_UNCFMD2CFMD = "account_uncfmd2cfmd";
    /**
     *   按区块高度来进行上一个账号状态的
     */

    String TB_LEDGER_ACCOUNT_BLOCK_SNAPSHOT = "account_block_snapshot";

    /**
     *   存区块当前确认的高度
     */
    String TB_LEDGER_BLOCK_HEIGHT = "chain_block_height";


    /**
     *   存区块当前确认的高度
     */
    String TB_SYNC_BLOCK = "chain_block_datas";
    /**
     *   存打包的区块交易nonce值
     */
    String TB_LEDGER_NONCES = "ledger_nonces";

    /**
     *   存区块所有交易的hash值
     */
    String TB_LEDGER_HASH = "ledger_tx_hashs";

    /**
     *   链资产索引
     */
    String TB_LEDGER_ASSET_INDEX = "ledger_asset_index";

    /**
     *   链资产地址索引
     */
    String TB_LEDGER_ASSET_ADDR_INDEX = "ledger_asset_addr_index";
}
