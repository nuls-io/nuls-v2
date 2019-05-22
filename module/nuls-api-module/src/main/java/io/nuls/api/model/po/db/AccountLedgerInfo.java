package io.nuls.api.model.po.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.units.qual.A;

import java.math.BigInteger;

public class AccountLedgerInfo {

    private String key;

    private String address;

    private int chainId;

    private int assetId;

    private BigInteger totalBalance;
    @JsonIgnore
    private boolean isNew;

    public AccountLedgerInfo(){}

    public AccountLedgerInfo(String address, int chainId, int assetId) {
        this.key = address + chainId + assetId;
        this.address = address;
        this.chainId = chainId;
        this.assetId = assetId;
        this.totalBalance = BigInteger.ZERO;
        isNew = true;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public BigInteger getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigInteger totalBalance) {
        this.totalBalance = totalBalance;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }


    public AccountLedgerInfo copy() {
        AccountLedgerInfo ledgerInfo = new AccountLedgerInfo();
        ledgerInfo.key = this.key;
        ledgerInfo.address = this.address;
        ledgerInfo.chainId = this.chainId;
        ledgerInfo.assetId = this.assetId;
        ledgerInfo.totalBalance = new BigInteger(this.totalBalance.toString());
        return ledgerInfo;
    }
}
