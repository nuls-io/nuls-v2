package io.nuls.base.api.provider.crosschain.facade;

import io.nuls.base.api.provider.BaseReq;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 16:56
 * @Description: Function Description
 */
public class CreateResetLocalVerifierTxReq extends BaseReq {

    String address;

    String password;

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

    public CreateResetLocalVerifierTxReq(String address, String password) {
        this.address = address;
        this.password = password;
    }
}
