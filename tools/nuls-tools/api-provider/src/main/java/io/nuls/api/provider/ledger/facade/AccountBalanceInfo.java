package io.nuls.api.provider.ledger.facade;

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
}
