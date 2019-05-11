package io.nuls.api.model.po.db.mini;

import java.math.BigInteger;

public class MiniAccountInfo {

    private String address;

    private String alias;

    private int type;

    private BigInteger totalBalance;

    private BigInteger totalOut;

    private BigInteger totalIn;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public BigInteger getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigInteger totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigInteger getTotalOut() {
        return totalOut;
    }

    public void setTotalOut(BigInteger totalOut) {
        this.totalOut = totalOut;
    }

    public BigInteger getTotalIn() {
        return totalIn;
    }

    public void setTotalIn(BigInteger totalIn) {
        this.totalIn = totalIn;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
