package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 17:50
 * @Description:
 * Delete specified account
 * remove account by address
 */
public class RemoveAccountReq extends BaseReq {

    private String password;

    private String address;

    public RemoveAccountReq(String password, String address) {
        this.password = password;
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
