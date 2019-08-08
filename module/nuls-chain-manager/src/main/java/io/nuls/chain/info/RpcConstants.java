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
package io.nuls.chain.info;

/**
 * @author lan
 * @description
 * @date 2019/01/19
 **/
public interface RpcConstants {

    /**
     * 内部指令
     */
    String CMD_ASSET_DISABLE = "cm_assetDisable";
    String CMD_ASSET_REG = "cm_assetReg";
    String CMD_GET_CHAIN_ASSET = "cm_getChainAsset";
    String CMD_ASSET = "cm_asset";
    String CMD_CHAIN_REG = "cm_chainReg";
    String CMD_CHAIN_ACTIVE = "cm_chainActive";
    String CMD_CHAIN = "cm_chain";
    String CMD_GET_CROSS_CHAIN_INFOS = "getCrossChainInfos";
    String CMD_GET_CIRCULATE_CHAIN_ASSET = "cm_getCirculateChainAsset";
    String CMD_ASSET_CIRCULATE_VALIDATOR = "cm_assetCirculateValidator";
    String CMD_ASSET_CIRCULATE_COMMIT = "cm_assetCirculateCommit";
    String CMD_ASSET_CIRCULATE_ROLLBACK = "cm_assetCirculateRollBack";
    String CMD_UPDATE_CHAIN_ASSET = "updateChainAsset";


    /*CALL cmd 获取网络时间*/
    String CMD_NW_GET_TIME_CALL = "nw_currentTimeMillis";
    /**
     * --------[call Transaction module RPC constants] -------
     */
    /**
     * 发起新交易接口
     */
    String CMD_TX_NEW = "tx_newTx";
    /**
     * 交易数据HEX编码
     */
    String TX_DATA_HEX = "tx";
    /**
     * 交易所属链ID
     */
    String TX_CHAIN_ID = "chainId";


    String CMD_NW_CROSS_SEEDS = "nw_getSeeds";

    String CMD_NW_GET_MAIN_NET_MAGIC_NUMBER = "nw_getMainMagicNumber";

    /**
     * 创建交易
     */
    String CMD_NW_CREATE_NODEGROUP = "nw_createNodeGroup";

    String CMD_NW_DELETE_NODEGROUP = "nw_delNodeGroup";
    /**
     * 账本获取数据
     */
    String CMD_LG_GET_COINDATA = "getBalanceNonce";
    String CMD_LG_GET_ASSETS_BY_ID = "getAssetsById";

    /**
     * 账户信息校验部分
     */
    String CMD_AC_GET_PRI_KEY = "ac_getPriKeyByAddress";
    String VALID_RESULT = "valid";

    String CMD_AC_SIGN_DIGEST = "ac_signDigest";
    String CMD_AC_ADDRESS_PREFIX = "ac_addAddressPrefix";


    /**
     * 跨链协议接口
     */
    String CMD_GET_FRIEND_CHAIN_CIRCULATE = "getFriendChainCirculate";

    String CMD_REG_CROSS_CHAIN = "registerCrossChain";
    String CMD_REG_CROSS_ASSET = "registerAsset";
    String CMD_CANCEL_CROSS_CHAIN = "cancelCrossChain";

    String CMD_CROSS_CHAIN_REGISTER_CHANGE = "crossChainRegisterChange";
    /**
     * 共识模块接口
     */
    String CMD_CS_GET_SEED_NODE_INFO = "cs_getSeedNodeInfo";
}
