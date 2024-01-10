package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 13:42
 * @Description:
 * Obtain account information based on address
 * get account info by address
 */
public class GetAccountByAddressReq extends BaseReq {

    private String address;

    public GetAccountByAddressReq(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
