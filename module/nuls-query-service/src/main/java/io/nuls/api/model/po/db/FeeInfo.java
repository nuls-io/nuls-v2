package io.nuls.api.model.po.db;

import java.math.BigInteger;

public class FeeInfo {

    private int chainId;

    private int assetId;

    private String symbol;

    private BigInteger value;

    public FeeInfo() {

    }

    public FeeInfo(int chainId, int assetId, String symbol) {
        this.chainId = chainId;
        this.assetId = assetId;
        this.symbol = symbol;
        this.value = BigInteger.ZERO;
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }
}
