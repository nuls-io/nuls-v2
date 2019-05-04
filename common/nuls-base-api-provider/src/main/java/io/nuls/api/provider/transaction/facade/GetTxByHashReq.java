package io.nuls.api.provider.transaction.facade;

import io.nuls.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:01
 * @Description: 功能描述
 */
public class GetTxByHashReq extends BaseReq {

    private String txHash;

    private GetTxByHashReq(String txHash) {
        this.txHash = txHash;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
}
