package io.nuls.api.provider.contract.facade;

import io.nuls.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 15:28
 * @Description: 功能描述
 */
public class Contract extends BaseReq {

    private String sender;

    private long gasLimit;

    private long price;

    private String password;

    private String remark;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
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
