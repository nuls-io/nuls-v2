package io.nuls.base.api.provider.ledger.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 15:36
 * @Description:
 * 获取指定账户余额
 * get account balance
 */

public class GetBalanceReq extends BaseReq {

    Integer assetId;

    Integer assetChainId;

    String address;

    public GetBalanceReq(Integer assetId, Integer assetChainId, String address) {
        this.assetId = assetId;
        this.assetChainId = assetChainId;
        this.address = address;
    }

    public Integer getAssetId() {
        return assetId;
    }

    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }

    public Integer getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(Integer assetChainId) {
        this.assetChainId = assetChainId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
