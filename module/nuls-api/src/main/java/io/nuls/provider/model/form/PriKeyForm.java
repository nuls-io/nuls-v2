package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

@ApiModel(description = "Account Private Key Form")
public class PriKeyForm {

    @ApiModelProperty(description = "Account plaintext private key")
    private String priKey;

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }
}
