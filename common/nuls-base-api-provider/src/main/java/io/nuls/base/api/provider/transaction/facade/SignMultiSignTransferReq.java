package io.nuls.base.api.provider.transaction.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-07-18 15:24
 * @Description: Multiple transaction signatures
 */
public class SignMultiSignTransferReq extends BaseReq {

    private String tx;

    private String signAddress;

    private String signPassword;


    public SignMultiSignTransferReq(String tx, String signAddress, String signPassword) {
        this.tx = tx;
        this.signAddress = signAddress;
        this.signPassword = signPassword;
    }

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }

    public String getSignPassword() {
        return signPassword;
    }

    public void setSignPassword(String signPassword) {
        this.signPassword = signPassword;
    }
}
