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
package io.nuls.ledger.constant;

/**
 * @author lan
 * @description
 * @date 2019/03/10
 **/
public interface CmdConstant {
    /*CALL cmd */
    /**
     * Obtain blocks based on block height
     */
    String CMD_GET_BLOCK_BY_HEIGHT = "getBlockByHeight";

    /**
     * Get the latest altitude
     */
    String CMD_LATEST_HEIGHT = "latestHeight";
    /**
     * Account signature verification
     */
    String CMD_AC_SIGN_DIGEST = "ac_signDigest";
    /**
     * Initiate new transaction interface
     */
    String CMD_TX_NEW = "tx_newTx";

    /*RPC CMD*/


    String CMD_CHAIN_ASSET_REG_INFO = "getAssetRegInfo";
    String CMD_GET_ALL_ASSET = "lg_get_all_asset";
    String CMD_CHAIN_ASSET_REG_INFO_BY_HASH = "getAssetRegInfoByHash";
    String CMD_CHAIN_ASSET_REG_INFO_BY_ASSETID = "getAssetRegInfoByAssetId";
    String CMD_CHAIN_ASSET_TX_REG = "chainAssetTxReg";

    String CMD_CHAIN_ASSET_CONTRACT_REG = "chainAssetContractReg";
    String CMD_CHAIN_ASSET_CONTRACT_ROLL_BACK = "chainAssetContractRollBack";
    String CMD_CHAIN_ASSET_CONTRACT_ADDRESS = "getAssetContractAddress";
    String CMD_CHAIN_ASSET_CONTRACT_ASSETID = "getAssetContractAssetId";
    String CMD_CHAIN_ASSET_CONTRACT = "getAssetContract";
    /**
     * Obtain confirmed transaction balance
     */
    String CMD_GET_BALANCE = "getBalance";


    /**
     * Obtain information containing unconfirmed transactions
     */
    String CMD_GET_BALANCE_NONCE = "getBalanceNonce";
    /**
     * Obtain accountnoncevalue
     */
    String CMD_GET_NONCE = "getNonce";
    /**
     * Get frozen list
     */
    String CMD_GET_FREEZE_LIST = "getFreezeList";
    /**
     * Submit unconfirmed transactions
     */
    String CMD_COMMIT_UNCONFIRMED_TX = "commitUnconfirmedTx";
    /**
     * Batch submit unconfirmed transactions
     */
    String CMD_COMMIT_UNCONFIRMED_TXS = "commitBatchUnconfirmedTxs";
    /**
     * Rolling back unconfirmed transactions
     */
    String CMD_ROLLBACK_UNCONFIRMED_TX = "rollBackUnconfirmTx";

    /**
     * Rolling back unconfirmed transactions
     */
    String CMD_CLEAR_UNCONFIRMED_TXS = "clearUnconfirmTxs";


    /**
     * Submit block transaction
     */
    String CMD_COMMIT_BLOCK_TXS = "commitBlockTxs";

    /**
     * Rolling back block transactions
     */
    String CMD_ROLLBACK_BLOCK_TXS = "rollBackBlockTxs";

    /**
     * Overall verification of block packaging
     */
    String CMD_VERIFY_COINDATA_PACKAGED = "verifyCoinDataPackaged";

    /**
     * Overall verification of block packaging
     */
    String CMD_VERIFY_COINDATA_BATCH_PACKAGED = "verifyCoinDataBatchPackaged";

    /**
     * Single transaction verification
     */
    String CMD_VERIFY_COINDATA = "verifyCoinData";

    /**
     * Rolling back the status of packaged transactions
     */
    String CMD_ROLLBACKTX_VALIDATE_STATUS = "rollbackTxValidateStatus";
    /**
     * Batch verification start
     */
    String CMD_BATCH_VALIDATE_BEGIN = "batchValidateBegin";

    /**
     * Whole block verification
     */
    String CMD_BLOCK_VALIDATE = "blockValidate";
    /**
     * Obtain asset information
     */
    String CMD_GET_ASSETS_BY_ID = "getAssetsById";

    String CMD_GET_ASSET_BY_ID = "getAssetById";
}
