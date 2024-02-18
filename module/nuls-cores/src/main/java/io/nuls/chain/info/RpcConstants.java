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
package io.nuls.chain.info;

/**
 * @author lan
 * @description
 * @date 2019/01/19
 **/
public interface RpcConstants {

    /**
     * Internal instructions
     */
    String CMD_ASSET_DISABLE = "cm_assetDisable";
    String CMD_ASSET_REG = "cm_assetReg";
    String CMD_MAIN_NET_ASSET_REG = "cm_mainNetAssetReg";
    String CMD_GET_CHAIN_ASSET = "cm_getChainAsset";
    String CMD_ASSET = "cm_asset";
    String CMD_CHAIN_REG = "cm_chainReg";
    String CMD_CHAIN_ACTIVE = "cm_chainActive";
    String CMD_CHAIN = "cm_chain";
    String CMD_GET_CROSS_CHAIN_INFOS = "getCrossChainInfos";
    String CMD_GET_CROSS_CHAIN_SIMPLE_INFOS = "cm_getChainsSimpleInfo";


    String CMD_GET_CIRCULATE_CHAIN_ASSET = "cm_getCirculateChainAsset";
    String CMD_ASSET_CIRCULATE_VALIDATOR = "cm_assetCirculateValidator";
    String CMD_ASSET_CIRCULATE_COMMIT = "cm_assetCirculateCommit";
    String CMD_ASSET_CIRCULATE_ROLLBACK = "cm_assetCirculateRollBack";
    String CMD_UPDATE_CHAIN_ASSET = "updateChainAsset";


    /*CALL cmd Get network time*/
    String CMD_NW_GET_TIME_CALL = "nw_currentTimeMillis";
    /**
     * --------[call Transaction module RPC constants] -------
     */
    /**
     * Initiate new transaction interface
     */
    String CMD_TX_NEW = "tx_newTx";
    /**
     * transaction dataHEXcoding
     */
    String TX_DATA_HEX = "tx";
    /**
     * Exchange ChainID
     */
    String TX_CHAIN_ID = "chainId";


    String CMD_NW_CROSS_SEEDS = "nw_getSeeds";

    String CMD_NW_GET_MAIN_NET_MAGIC_NUMBER = "nw_getMainMagicNumber";

    /**
     * Create transaction
     */
    String CMD_NW_CREATE_NODEGROUP = "nw_createNodeGroup";

    String CMD_NW_DELETE_NODEGROUP = "nw_delNodeGroup";
    /**
     * Obtaining data from the ledger
     */
    String CMD_LG_GET_COINDATA = "getBalanceNonce";
    String CMD_LG_GET_ASSETS_BY_ID = "getAssetsById";
    String CMD_LG_GET_ASSETS_REG_INFO_BY_ID ="getAssetRegInfoByAssetId";
    /**
     * Account information verification section
     */
    String CMD_AC_GET_PRI_KEY = "ac_getPriKeyByAddress";
    String VALID_RESULT = "valid";

    String CMD_AC_SIGN_DIGEST = "ac_signDigest";
    String CMD_AC_ADDRESS_PREFIX = "ac_addAddressPrefix";




    /**
     * Cross chain protocol interface
     */
    String CMD_GET_FRIEND_CHAIN_CIRCULATE = "getFriendChainCirculate";

    String CMD_REG_CROSS_CHAIN = "registerCrossChain";
    String CMD_REG_CROSS_ASSET = "registerAsset";
    String CMD_CANCEL_CROSS_CHAIN = "cancelCrossChain";

    String CMD_CROSS_CHAIN_REGISTER_CHANGE = "crossChainRegisterChange";
    /**
     * Consensus module interface
     */
    String CMD_CS_GET_SEED_NODE_INFO = "cs_getSeedNodeInfo";
}
