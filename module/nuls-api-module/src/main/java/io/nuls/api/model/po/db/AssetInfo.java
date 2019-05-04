package io.nuls.api.model.po.db;

public class AssetInfo {

    private String key;

    private int chainId;

    private int assetId;

    private String symbol;

    public AssetInfo(){}

    public AssetInfo(int chainId, int assetId, String symbol) {
        this.key = chainId + "-" + assetId;
        this.chainId = chainId;
        this.assetId = assetId;
        this.symbol = symbol;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
}
