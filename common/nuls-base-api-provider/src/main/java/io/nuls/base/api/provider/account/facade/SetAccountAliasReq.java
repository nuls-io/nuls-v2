package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 14:39
 * @Description:
 * 设置账户别名
 * set account alias
 */
public class SetAccountAliasReq extends BaseReq {

    private String password;

    private String address;

    private String alias;

    public SetAccountAliasReq(String password, String address, String alias) {
        this.password = password;
        this.address = address;
        this.alias = alias;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
