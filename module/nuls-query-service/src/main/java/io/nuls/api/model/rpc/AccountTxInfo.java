package io.nuls.api.model.rpc;

import io.nuls.api.model.po.db.FeeInfo;
import io.nuls.api.model.po.db.TxRelationInfo;

import java.math.BigInteger;

public class AccountTxInfo {

    private String txHash;

    private String address;

    private int type;

    private long createTime;

    private long height;

    private int chainId;

    private int assetId;

    private BigInteger values;

    private FeeInfo fee;

    private BigInteger balance;

    // -1 : from , 1: to
    private int transferType;

    private int status;

    private String symbol;

    public AccountTxInfo() {

    }

    public AccountTxInfo(TxRelationInfo relationInfo, int status, String symbol) {
        this.txHash = relationInfo.getTxHash();
        this.address = relationInfo.getAddress();
        this.type = relationInfo.getType();
        this.createTime = relationInfo.getCreateTime();
        this.height = relationInfo.getHeight();
        this.chainId = relationInfo.getChainId();
        this.assetId = relationInfo.getAssetId();
       // this.fee = relationInfo.getFee();
        this.values = relationInfo.getValues();
        this.balance = relationInfo.getBalance();
        this.transferType = relationInfo.getTransferType();
        this.status = status;
        this.symbol = symbol;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
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

    public BigInteger getValues() {
        return values;
    }

    public void setValues(BigInteger values) {
        this.values = values;
    }

    public FeeInfo getFee() {
        return fee;
    }

    public void setFee(FeeInfo fee) {
        this.fee = fee;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public int getTransferType() {
        return transferType;
    }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
