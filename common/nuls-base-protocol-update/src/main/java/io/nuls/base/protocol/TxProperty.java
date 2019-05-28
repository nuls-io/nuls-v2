package io.nuls.base.protocol;

import io.nuls.core.constant.TxType;

/**
 * 交易属性
 * Transaction attribute
 *
 * @author tag
 * 2019/1/16
 */
public enum TxProperty {
    /**
     * 转账
     * Create node transactions
     */
    TRANSFER(TxType.TRANSFER, false, false, true),

    /**
     * 设置别名
     * Stop node transactions
     */
    ACCOUNT_ALIAS(TxType.ACCOUNT_ALIAS, false, false, true),

    /**
     * 创建节点交易
     * Create node transactions
     */
    CREATE_AGENT(TxType.REGISTER_AGENT, false, false, true),

    /**
     * 停止节点交易
     * Stop node transactions
     */
    STOP_AGENT(TxType.STOP_AGENT, false, true, true),

    /**
     * 加入共识
     * Join the consensus
     */
    DEPOSIT(TxType.DEPOSIT, false, false, true),

    /**
     * 退出共识
     * Exit consensus
     */
    CANCEL_DEPOSIT(TxType.CANCEL_DEPOSIT, false, true, true),

    /**
     * CoinBase
     */
    COIN_BASE(TxType.COIN_BASE, true, false, false),

    /**
     * 红牌
     */
    RED_PUNISH(TxType.RED_PUNISH, true, false, false),

    /**
     * 黄牌
     */
    YELLOW_PUNISH(TxType.YELLOW_PUNISH, true, false, false);

    public final int txType;

    public final boolean systemTx;

    public final boolean unlockTx;

    public final boolean verifySignature;

    TxProperty(int txType, boolean systemTx, boolean unlockTx, boolean verifySignature) {
        this.txType = txType;
        this.systemTx = systemTx;
        this.unlockTx = unlockTx;
        this.verifySignature = verifySignature;
    }
}
