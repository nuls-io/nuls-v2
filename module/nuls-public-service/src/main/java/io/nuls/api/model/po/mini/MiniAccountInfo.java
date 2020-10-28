package io.nuls.api.model.po.mini;

import io.nuls.api.model.po.AccountInfo;

import java.math.BigInteger;

public class MiniAccountInfo {

    private String address;

    private String alias;

    private int type;

    private BigInteger totalBalance;

    private BigInteger locked;

    private String proportion;

    private int decimal;

    public MiniAccountInfo() {

    }

    public MiniAccountInfo(AccountInfo accountInfo) {
        this.address = accountInfo.getAddress();
        this.alias = accountInfo.getAlias();
        this.type = accountInfo.getType();
    }

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public BigInteger getLocked() {
        return locked;
    }

    public void setLocked(BigInteger locked) {
        this.locked = locked;
    }

    public String getProportion() {
        return proportion;
    }

    public void setProportion(String proportion) {
        this.proportion = proportion;
    }
}
