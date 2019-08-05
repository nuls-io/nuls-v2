package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

@ApiModel(description = "单账户签名表单")
public class EncryptedPriKeySignForm {

    @ApiModelProperty(description = "交易序列化Hex字符串")
    private String txHex;
    @ApiModelProperty(description = "账户地址")
    private String address;
    @ApiModelProperty(description = "账户密文私钥")
    private String encryptedPriKey;
    @ApiModelProperty(description = "账户密码")
    private String password;

    public String getEncryptedPriKey() {
        return encryptedPriKey;
    }

    public void setEncryptedPriKey(String encryptedPriKey) {
        this.encryptedPriKey = encryptedPriKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

}
