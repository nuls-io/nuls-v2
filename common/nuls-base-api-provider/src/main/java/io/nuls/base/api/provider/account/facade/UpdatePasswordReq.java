package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 11:57
 * @Description:
 * 修改密码
 * reset account password
 */
public class UpdatePasswordReq extends BaseReq {

    private String address;

    private String password;

    private String newPassword;

    public UpdatePasswordReq(String address, String password, String newPassword) {
        this.address = address;
        this.password = password;
        this.newPassword = newPassword;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}


