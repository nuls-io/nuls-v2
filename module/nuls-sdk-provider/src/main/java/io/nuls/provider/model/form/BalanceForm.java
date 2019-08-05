package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

@ApiModel(description = "获取账户余额表单信息")
public class BalanceForm {

    @ApiModelProperty(description = "资产的链ID")
    private int assetChainId;
    @ApiModelProperty(description = "资产ID")
    private int assetId;


    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }
}
