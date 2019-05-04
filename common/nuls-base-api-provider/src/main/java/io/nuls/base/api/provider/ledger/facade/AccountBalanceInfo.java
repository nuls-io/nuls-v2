package io.nuls.base.api.provider.ledger.facade;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 15:39
 * @Description: 功能描述
 */
public class AccountBalanceInfo {

    private BigInteger freeze;

    private BigInteger total;

    private BigInteger available;

    public BigInteger getFreeze() {
        return freeze;
    }

    public void setFreeze(BigInteger freeze) {
        this.freeze = freeze;
    }

    public BigInteger getTotal() {
        return total;
    }

    public void setTotal(BigInteger total) {
        this.total = total;
    }

    public BigInteger getAvailable() {
        return available;
    }

    public void setAvailable(BigInteger available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"freeze\":")
                .append(freeze)
                .append(",\"total\":")
                .append(total)
                .append(",\"available\":")
                .append(available)
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountBalanceInfo)) return false;

        AccountBalanceInfo that = (AccountBalanceInfo) o;

        if (freeze != null ? !freeze.equals(that.freeze) : that.freeze != null) return false;
        if (total != null ? !total.equals(that.total) : that.total != null) return false;
        return available != null ? available.equals(that.available) : that.available == null;
    }

    @Override
    public int hashCode() {
        int result = freeze != null ? freeze.hashCode() : 0;
        result = 31 * result + (total != null ? total.hashCode() : 0);
        result = 31 * result + (available != null ? available.hashCode() : 0);
        return result;
    }
}
