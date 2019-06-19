package io.nuls.api.model.po.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.api.utils.DBUtil;
import org.checkerframework.checker.units.qual.A;

import java.math.BigInteger;

public class AccountLedgerInfo {

    private String key;

    private String address;

    private int chainId;

    private int assetId;

    private String symbol;

    private BigInteger totalBalance;

    private BigInteger balance;

    private BigInteger timeLock;

    private BigInteger consensusLock;

    @JsonIgnore
    private boolean isNew;

    public AccountLedgerInfo() {
    }

    public AccountLedgerInfo(String address, int chainId, int assetId) {
        this.key = DBUtil.getAccountAssetKey(address, chainId, assetId);
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

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getTimeLock() {
        return timeLock;
    }

    public void setTimeLock(BigInteger timeLock) {
        this.timeLock = timeLock;
    }

    public BigInteger getConsensusLock() {
        return consensusLock;
    }

    public void setConsensusLock(BigInteger consensusLock) {
        this.consensusLock = consensusLock;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public AccountLedgerInfo copy() {
        AccountLedgerInfo ledgerInfo = new AccountLedgerInfo();
        ledgerInfo.key = this.key;
        ledgerInfo.address = this.address;
        ledgerInfo.chainId = this.chainId;
        ledgerInfo.assetId = this.assetId;
        ledgerInfo.symbol = this.symbol;
        ledgerInfo.balance = this.balance;
        ledgerInfo.timeLock = this.timeLock;
        ledgerInfo.consensusLock = this.consensusLock;
        ledgerInfo.totalBalance = new BigInteger(this.totalBalance.toString());
        return ledgerInfo;
    }

    public String getAssetKey() {
        return chainId + "-" + assetId;
    }
}
