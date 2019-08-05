package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

@ApiModel(description = "离线获取明文私钥表单")
public class ResetPasswordForm {

    @ApiModelProperty(description = "账户地址")
    private String address;
    @ApiModelProperty(description = "账户密文私钥")
    private String encryptedPriKey;
    @ApiModelProperty(description = "账户原密码")
    private String oldPassword;
    @ApiModelProperty(description = "账户新密码")
    private String newPassword;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEncryptedPriKey() {
        return encryptedPriKey;
    }

    public void setEncryptedPriKey(String encryptedPriKey) {
        this.encryptedPriKey = encryptedPriKey;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }


}
