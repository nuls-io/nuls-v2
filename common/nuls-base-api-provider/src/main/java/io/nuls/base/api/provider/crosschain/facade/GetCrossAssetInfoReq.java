package io.nuls.base.api.provider.crosschain.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: ljs
 * @Time: 2019-08-07 11:49
 * @Description: 功能描述
 */
public class GetCrossAssetInfoReq extends BaseReq {
    private int assetId;

    public GetCrossAssetInfoReq(Integer chainId, Integer assetId) {
        this.setChainId(chainId);
        this.assetId = assetId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }
}
