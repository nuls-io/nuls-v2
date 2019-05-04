package io.nuls.core.constant;

public class TxType {

    /**
     * 造币
     */
    public final static int COIN_BASE = 1;
    /**
     * 转账
     * the type of the transfer transaction
     */
    public final static int TRANSFER = 2;

    /**
     * 设置账户别名
     * Set the transaction type of account alias.
     */
    public final static int ACCOUNT_ALIAS = 3;
    /**
     * 新建节点
     */
    public final static int REGISTER_AGENT = 4;
    /**
     * 委托
     */
    public final static int DEPOSIT = 5;
    /**
     * 取消委托
     */
    public final static int CANCEL_DEPOSIT = 6;
    /**
     * 黄牌
     */
    public final static int YELLOW_PUNISH = 7;
    /**
     * 红牌
     */
    public final static int RED_PUNISH = 8;
    /**
     * 停止节点
     */
    public final static int STOP_AGENT = 9;
    /**
     * 跨链
     */
    public final static int CROSS_CHAIN = 10;

    /**
     * 注册链交易
     */
    public final static int REGISTER_CHAIN_AND_ASSET = 11;
    /**
     * 销毁链
     */
    public final static int DESTROY_CHAIN_AND_ASSET = 12;
    /**
     * 为链新增一种资产
     */
    public final static int ADD_ASSET_TO_CHAIN = 13;
    /**
     * 删除链上资产
     */
    public final static int REMOVE_ASSET_FROM_CHAIN = 14;
    /**
     * 创建智能合约交易
     */
    public final static int CREATE_CONTRACT = 15;
    /**
     * 调用智能合约交易
     */
    public final static int CALL_CONTRACT = 16;
    /**
     * 删除智能合约交易
     */
    public final static int DELETE_CONTRACT = 17;
    /**
     * 合约内部转账
     * contract transfer tx
     */
    public final static int CONTRACT_TRANSFER = 18;
    /**
     * 合约执行手续费返还
     * contract return gas tx
     */
    public final static int CONTRACT_RETURN_GAS = 19;


}
