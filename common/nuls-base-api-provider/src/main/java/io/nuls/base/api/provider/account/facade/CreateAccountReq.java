package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 15:36
 * @Description: 功能描述
 */
public class CreateAccountReq extends BaseReq {

    private int count;

    private String password;

    public CreateAccountReq(int count, String password) {
        this.count = count;
        this.password = password;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
