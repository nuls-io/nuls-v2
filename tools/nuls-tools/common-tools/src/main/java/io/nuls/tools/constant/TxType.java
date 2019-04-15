package io.nuls.tools.constant;

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
     * 合约转账
     */
    public final static int CONTRACT_TRANSFER = 103;
    /**
     * 合约费用返还
     */
    public final static int CONTRACT_RETURN_GAS = 104;

}
