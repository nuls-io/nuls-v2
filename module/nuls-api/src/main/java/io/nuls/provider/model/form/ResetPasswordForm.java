package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

@ApiModel(description = "Offline acquisition of plaintext private key form")
public class ResetPasswordForm {

    @ApiModelProperty(description = "Account address")
    private String address;
    @ApiModelProperty(description = "Account ciphertext private key")
    private String encryptedPriKey;
    @ApiModelProperty(description = "Account original password")
    private String oldPassword;
    @ApiModelProperty(description = "Account New Password")
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
