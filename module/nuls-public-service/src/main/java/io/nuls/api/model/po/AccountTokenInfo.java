package io.nuls.api.model.po;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigInteger;

public class AccountTokenInfo {

    private String key;

    private String address;

    private String tokenName;

    private String tokenSymbol;

    private String contractAddress;

    private BigInteger balance;

    private int status;

    private int decimals;
    @JsonIgnore
    private boolean isNew;

    public AccountTokenInfo() {

    }

    public AccountTokenInfo(String address, String contractAddress, String tokenName, String tokenSymbol, int decimals) {
        this.key = address + contractAddress;
        this.address = address;
        this.tokenName = tokenName;
        this.tokenSymbol = tokenSymbol;
        this.contractAddress = contractAddress;
        this.balance = BigInteger.ZERO;
        this.decimals = decimals;
        this.isNew = true;
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

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
