package io.nuls.core.constant;

import org.junit.rules.Verifier;

/**
 * 交易类型
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/24 18:47
 */
public class TxType {

    /**
     * coinBase出块奖励
     */
    public static final int COIN_BASE = 1;
    /**
     * 转账
     * the type of the transfer transaction
     */
    public static final int TRANSFER = 2;

    /**
     * 设置账户别名
     * Set the transaction type of account alias.
     */
    public static final int ACCOUNT_ALIAS = 3;
    /**
     * 新建共识节点
     */
    public static final int REGISTER_AGENT = 4;
    /**
     * 委托参与共识
     */
    public static final int DEPOSIT = 5;
    /**
     * 取消委托
     */
    public static final int CANCEL_DEPOSIT = 6;
    /**
     * 黄牌
     */
    public static final int YELLOW_PUNISH = 7;
    /**
     * 红牌
     */
    public static final int RED_PUNISH = 8;
    /**
     * 注销共识节点
     */
    public static final int STOP_AGENT = 9;
    /**
     * 跨链转账
     */
    public static final int CROSS_CHAIN = 10;

    /**
     * 注册链
     */
    public static final int REGISTER_CHAIN_AND_ASSET = 11;
    /**
     * 注销链
     */
    public static final int DESTROY_CHAIN_AND_ASSET = 12;
    /**
     * 为链新增一种资产
     */
    public static final int ADD_ASSET_TO_CHAIN = 13;
    /**
     * 删除链上资产
     */
    public static final int REMOVE_ASSET_FROM_CHAIN = 14;
    /**
     * 创建智能合约
     */
    public static final int CREATE_CONTRACT = 15;
    /**
     * 调用智能合约
     */
    public static final int CALL_CONTRACT = 16;
    /**
     * 删除智能合约
     */
    public static final int DELETE_CONTRACT = 17;
    /**
     * 合约内部转账
     * contract transfer tx
     */
    public static final int CONTRACT_TRANSFER = 18;
    /**
     * 合约执行手续费返还
     * contract return gas tx
     */
    public static final int CONTRACT_RETURN_GAS = 19;
    /**
     * 合约新建共识节点
     * contract create agent tx
     */
    public static final int CONTRACT_CREATE_AGENT = 20;

    /**
     * 合约委托参与共识
     * contract deposit tx
     */
    public static final int CONTRACT_DEPOSIT = 21;

    /**
     * 合约取消委托共识
     * contract withdraw tx
     */
    public static final int CONTRACT_CANCEL_DEPOSIT = 22;

    /**
     * 合约注销共识节点
     * contract stop agent tx
     */
    public static final int CONTRACT_STOP_AGENT = 23;

    /**
     * 验证人变更
     * Verifier Change
     * */
    public static final int VERIFIER_CHANGE = 24;

    /**
     * 验证人初始化
     * Verifier init
     * */
    public static final int VERIFIER_INIT = 25;

    /**
     * 合约token跨链转账
     * contract token cross transfer tx
     */
    public static final int CONTRACT_TOKEN_CROSS_TRANSFER = 26;
    /**
     * 账本链内资产注册登记
     */
    public static final int LEDGER_ASSET_REG_TRANSFER = 27;

    /**
     * 注册跨链变更（新注册/注销链，新注册/注销资产）
     */
    public static final int REGISTERED_CHAIN_CHANGE = 60;

    /**
     * 重置跨链模块存储的本链验证人列表
     * 本交易必须由种子节点发出，节点收到本交易后从共识模块获取到最新的共识节点出块地址列表，覆盖跨链模块本地存储的本链验证人列表。
     */
    public static final int RESET_LOCAL_VERIFIER_LIST = 61;

}
