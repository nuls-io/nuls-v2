package io.nuls.base.api.provider.transaction.facade;

import io.nuls.base.api.provider.BaseReq;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:25
 * @Description:
 * 通过别名转账
 * transfer by account alias
 */
public class TransferByAliasReq extends BaseReq {

    /**
     * 发出资产账户别名
     */
    private String alias;

    /**
     * 接收地址
     */
    private String address;

    /**
     * 转账金额
     */
    private BigInteger amount;

    /**
     * 账户密码
     *
     */
    private String password;

    /**
     * 备注
     */
    private String remark;

    public TransferByAliasReq(String alias, String address, BigInteger amount, String password, String remark) {
        this.alias = alias;
        this.address = address;
        this.amount = amount;
        this.password = password;
        this.remark = remark;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
