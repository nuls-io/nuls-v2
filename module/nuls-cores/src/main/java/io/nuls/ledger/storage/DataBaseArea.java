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
     *   Deposit unconfirmed transaction data and status
     */
    String TB_LEDGER_ACCOUNT_UNCONFIRMED = "account_unconfirmed";
    String TB_LEDGER_TX_UNCONFIRMED = "tx_unconfirmed";
    String TB_LEDGER_ACCOUNT_UNCFMD2CFMD = "account_uncfmd2cfmd";
    /**
     *   Perform the previous account status based on block height
     */

    String TB_LEDGER_ACCOUNT_BLOCK_SNAPSHOT = "account_block_snapshot";

    /**
     *   The current confirmed height of the storage block
     */
    String TB_LEDGER_BLOCK_HEIGHT = "chain_block_height";


    /**
     *   The current confirmed height of the storage block
     */
    String TB_SYNC_BLOCK = "chain_block_datas";
    /**
     *   Blocked transactions for storage and packagingnoncevalue
     */
    String TB_LEDGER_NONCES = "ledger_nonces";

    /**
     *   Store all transactions in the blockhashvalue
     */
    String TB_LEDGER_HASH = "ledger_tx_hashs";

    /**
     *   Chain Asset Index
     */
    String TB_LEDGER_ASSET_INDEX = "ledger_asset_index";

    /**
     *   Chain Asset Address Index
     */
    String TB_LEDGER_ASSET_ADDR_INDEX = "ledger_asset_addr_index";

    /**
     *   Chain Asset Registry
     */
    String TB_LEDGER_ASSET_REG_MNG = "ledger_asset_reg_mng";
    String TB_LEDGER_ASSET_REG_HASH_INDEX = "ledger_asset_reg_hash_index";
    String TB_LEDGER_ASSET_REG_CONTRACT_INDEX = "ledger_asset_reg_contract_index";

    /**
     *   Cross chain asset registration form
     */
    String TB_LEDGER_CROSS_CHAIN_ASSET = "ledger_cross_chain_asset";
}
