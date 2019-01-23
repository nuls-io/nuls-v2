package io.nuls.poc.utils.enumeration;

import io.nuls.poc.constant.ConsensusConstant;

/**
 * 交易属性
 * Transaction attribute
 * @author tag
 * 2019/1/16
 */
public enum TxProperty {
    /**
     * 创建节点交易
     * Create node transactions
     * */
    CREATE_AGENT(ConsensusConstant.TX_TYPE_REGISTER_AGENT,false,false,true),

    /**
     * 停止节点交易
     * Stop node transactions
     * */
    STOP_AGENT(ConsensusConstant.TX_TYPE_STOP_AGENT,false,true,true),

    /**
     * 加入共识
     * Join the consensus
     * */
    JOIN_DEPOSIT(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS,false,false,true),

    /**
     * 退出共识
     * Exit consensus
     * */
    CANCEL_DEPOSIT(ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT,false,true,true),

    /**
     * CoinBase
     * */
    COIN_BASE(ConsensusConstant.TX_TYPE_COINBASE,true,false,false),

    /**
     * 红牌
     * */
    RED_PUNISH(ConsensusConstant.TX_TYPE_RED_PUNISH,true,false,false),

    /**
     * 黄牌
     * */
    YELLOW_PUNISH(ConsensusConstant.TX_TYPE_YELLOW_PUNISH,true,false,false);

    public final int txType;

    public final boolean systemTx;

    public final boolean unlockTx;

    public final boolean verifySignature;

    TxProperty(int txType,boolean systemTx,boolean unlockTx,boolean verifySignature){
        this.txType = txType;
        this.systemTx = systemTx;
        this.unlockTx = unlockTx;
        this.verifySignature = verifySignature;
    }
}
