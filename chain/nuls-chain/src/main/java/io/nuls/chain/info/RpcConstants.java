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
     * --------[call Transaction module RPC constants] -------
     */
    /**
     * tx_register注册交易接口版本号
     */
    String TX_REGISTER_VERSION = "1.0";
    /**
     * 注册交易接口
     */
    String TX_REGISTER_CMD = "tx_register";
    /**
     * 注册交易的模块编码
     */
    String TX_MODULE_CODE = "moduleCode";
    /**
     * 模块统一交易验证器接口
     */
    String TX_MODULE_VALIDATE_CMD = "moduleValidator";
    String TX_MODULE_VALIDATE_CMD_VALUE  = "chainModuleTxValidate";

    /**
     * 交易类型
     */
    String TX_TYPE = "txType";
    /**
     * 交易提交接口
     */
    String TX_COMMIT_CMD = "commit";

    String TX_COMMIT_CMD_VALUE = "moduleTxsCommit";

    /**
     * 交易回滚接口
     */
    String TX_ROLLBACK_CMD = "rollback";
    String TX_ROLLBACK_CMD_VALUE = "moduleTxsRollBack";


    /**
     * 单个交易验证器接口
     */
    String TX_VALIDATE_CMD = "validateCmd";
    String TX_VALIDATE_CMD_VALUE_CHAIN_REG = "cm_chainRegValidator";
    String TX_VALIDATE_CMD_VALUE_CHAIN_DESTROY = "cm_chainDestroyValidator";
    String TX_VALIDATE_CMD_VALUE_ASSET_REG = "cm_chainDestroyValidator";
    String TX_VALIDATE_CMD_VALUE_ASSET_DESTROY = "cm_assetDisableValidator";


    /**
     * 是否是系统产生的交易（打包节点产生，用于出块奖励结算、红黄牌惩罚）
     */
    String TX_IS_SYSTEM_CMD = "systemTx";
    /**
     * 是否是解锁交易
     */
    String TX_UNLOCK_CMD = "unlockTx";
    /**
     * 该交易是否需要在账本中验证签名
     */
    String TX_VERIFY_SIGNATURE_CMD = "verifySignature";

    /**
     * newTx发起新交易接口版本号
     */
    String TX_NEW_VERSION = "1.0";
    /**
     * 发起新交易接口
     */
    String CMD_TX_NEW ="tx_newTx";
    /**
     * 交易数据HEX编码
     */
    String TX_DATA_HEX = "txHex";
    /**
     * 交易所属链ID
     */
    String TX_CHAIN_ID = "chainId";

    /**
     * 交易注册
     */
    public static final String CMD_TX_REGISTER = "tx_register";
    /**
     * 创建交易
     */
    public static final String CMD_NW_CROSS_SEEDS = "nw_getSeeds";

    public static final String CMD_NW_CREATE_NODEGROUP = "nw_createNodeGroup";

    public static final String CMD_NW_DELETE_NODEGROUP = "nw_delNodeGroup";
    /**
     * 账本获取数据
     */
    public static final String CMD_LG_GET_COINDATA = "getCoinData";

}
