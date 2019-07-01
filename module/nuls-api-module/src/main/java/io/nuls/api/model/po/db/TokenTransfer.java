package io.nuls.api.model.po.db;

public class TokenTransfer extends TxDataInfo {

    private String txHash;

    private long height;

    private String contractAddress;

    private String name;

    private String symbol;

    private int decimals;

    private String fromAddress;

    private String toAddress;

    private String value;

    private Long time;

    private String fromBalance;

    private String toBalance;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getFromBalance() {
        return fromBalance;
    }

    public void setFromBalance(String fromBalance) {
        this.fromBalance = fromBalance;
    }

    public String getToBalance() {
        return toBalance;
    }

    public void setToBalance(String toBalance) {
        this.toBalance = toBalance;
    }
}
