package io.nuls.base.api.provider.transaction.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-07-18 14:26
 * @Description: Create multi signature transactions
 */
public class CreateMultiSignTransferReq extends TransferReq {

    private String signAddress;

    private String signPassword;


    public CreateMultiSignTransferReq() {
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
