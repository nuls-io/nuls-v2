package io.nuls.base.api.provider.contract.facade;

import io.nuls.base.api.provider.BaseReq;

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

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"sender\":\"")
                .append(sender).append('\"')
                .append(",\"gasLimit\":")
                .append(gasLimit)
                .append(",\"price\":")
                .append(price)
                .append(",\"password\":\"")
                .append(password).append('\"')
                .append(",\"remark\":\"")
                .append(remark).append('\"')
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contract)) return false;

        Contract contract = (Contract) o;

        if (gasLimit != contract.gasLimit) return false;
        if (price != contract.price) return false;
        if (sender != null ? !sender.equals(contract.sender) : contract.sender != null) return false;
        if (password != null ? !password.equals(contract.password) : contract.password != null) return false;
        return remark != null ? remark.equals(contract.remark) : contract.remark == null;
    }

    @Override
    public int hashCode() {
        int result = sender != null ? sender.hashCode() : 0;
        result = 31 * result + (int) (gasLimit ^ (gasLimit >>> 32));
        result = 31 * result + (int) (price ^ (price >>> 32));
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        return result;
    }

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
