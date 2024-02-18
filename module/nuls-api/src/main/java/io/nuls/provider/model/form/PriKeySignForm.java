package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

@ApiModel(description = "Single account signature form")
public class PriKeySignForm {

    @ApiModelProperty(description = "Transaction serializationHexcharacter string")
    private String txHex;
    @ApiModelProperty(description = "Account address")
    private String address;
    @ApiModelProperty(description = "Account plaintext private key")
    private String priKey;

    public String getTxHex() {
        return txHex;
    }

    public void setTxHex(String txHex) {
        this.txHex = txHex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }
}
