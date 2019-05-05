package io.nuls.base.api.provider.contract.facade;

import io.nuls.base.api.provider.BaseReq;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 16:26
 * @Description: 功能描述
 */
public class TransferToContractReq  extends BaseReq {

    private String address;

    private String toAddress;

    private BigInteger amount;

    private String password;

    private String remark;

    public TransferToContractReq(String address, String toAddress, BigInteger amount, String password, String remark) {
        this.address = address;
        this.toAddress = toAddress;
        this.amount = amount;
        this.password = password;
        this.remark = remark;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
