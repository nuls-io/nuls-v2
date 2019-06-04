package io.nuls.api.model.po.db;

import java.math.BigInteger;

public class TxRelationInfo {

    private String txHash;

    private String address;

    private int type;

    private long createTime;

    private long height;

    private int chainId;

    private int assetId;

    private String symbol;

    private BigInteger values;

    private BigInteger fee;

    private BigInteger balance;

    // -1 : from , 1: to
    private int transferType;

    private int status;

    public TxRelationInfo() {

    }

    public TxRelationInfo(String address, String txHash) {
        this.address = address;
        this.txHash = txHash;
    }

    public TxRelationInfo(String address, TransactionInfo info, int chainId, int assetId, String symbol, BigInteger values, int transferType, BigInteger balance) {
        this.address = address;
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.chainId = chainId;
        this.assetId = assetId;
        this.symbol = symbol;
        this.fee = info.getFee();
        this.values = values;
        this.balance = balance;
        this.transferType = transferType;
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

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
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
