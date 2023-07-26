package io.nuls.crosschain.nuls.utils.enumeration;

import io.nuls.crosschain.base.constant.CrossChainConstant;

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
    CROSS_TX(CrossChainConstant.TX_TYPE_CROSS_CHAIN,false,false,true);


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
