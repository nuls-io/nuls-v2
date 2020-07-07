package io.nuls.provider.api.model;

public class AssetInfo {


    private int chainId;

    private int assetId;

    private String symbol;

    private int decimals;


    public AssetInfo() {
    }

    public AssetInfo(int chainId, int assetId, String symbol, int decimals) {
        this.chainId = chainId;
        this.assetId = assetId;
        this.symbol = symbol;
        this.decimals = decimals;
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

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }
}
