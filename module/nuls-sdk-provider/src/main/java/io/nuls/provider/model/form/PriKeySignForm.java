package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

@ApiModel(description = "单账户签名表单")
public class PriKeySignForm {

    @ApiModelProperty(description = "交易序列化Hex字符串")
    private String txHex;
    @ApiModelProperty(description = "账户地址")
    private String address;
    @ApiModelProperty(description = "账户明文私钥")
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
