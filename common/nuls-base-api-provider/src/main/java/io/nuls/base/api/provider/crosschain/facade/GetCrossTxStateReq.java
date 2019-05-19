package io.nuls.base.api.provider.crosschain.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 13:35
 * @Description: 功能描述
 */
public class GetCrossTxStateReq extends BaseReq {

    private String txHash;

    public GetCrossTxStateReq(Integer chainId,String txHash) {
        this.setChainId(chainId);
        this.txHash = txHash;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
}
