package io.nuls.api.model.po.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.data.Address;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class AccountInfo {

    private String address;

    private String alias;

    private int type;

    private int txCount;

    private BigInteger totalOut;

    private BigInteger totalIn;

    private BigInteger consensusLock;

    private BigInteger timeLock;

    private BigInteger balance;

    private BigInteger totalBalance;

    private BigInteger totalReward;

    private List<String> tokens;

    //是否是根据最新区块的交易新创建的账户，只为业务使用，不存储该字段
    @JsonIgnore
    private boolean isNew;

    public AccountInfo(){}

    public AccountInfo(String address) {
        this.address = address;
        Address address1 = new Address(address);
        this.type = address1.getAddressType();
        this.tokens = new ArrayList<>();
        this.isNew = true;
        this.totalOut = BigInteger.ZERO;
        this.totalIn = BigInteger.ZERO;
        this.consensusLock = BigInteger.ZERO;
        this.timeLock = BigInteger.ZERO;
        this.balance = BigInteger.ZERO;
        this.totalBalance = BigInteger.ZERO;
        this.totalReward = BigInteger.ZERO;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
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

    public BigInteger getConsensusLock() {
        return consensusLock;
    }

    public void setConsensusLock(BigInteger consensusLock) {
        this.consensusLock = consensusLock;
    }

    public BigInteger getTimeLock() {
        return timeLock;
    }

    public void setTimeLock(BigInteger timeLock) {
        this.timeLock = timeLock;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigInteger totalBalance) {
        this.totalBalance = totalBalance;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }
    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public BigInteger getTotalReward() {
        return totalReward;
    }

    public void setTotalReward(BigInteger totalReward) {
        this.totalReward = totalReward;
    }
}
