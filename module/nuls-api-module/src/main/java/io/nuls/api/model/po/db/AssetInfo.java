package io.nuls.api.model.po.db;

import io.nuls.api.constant.ApiConstant;
import io.nuls.api.utils.DBUtil;

import java.math.BigInteger;

public class AssetInfo extends TxDataInfo {

    private String key;

    private int chainId;

    private int assetId;

    private String symbol;

    private int decimals;

    private BigInteger initCoins;

    private String address;

    private int status;

    public AssetInfo() {
        this.status = ApiConstant.ENABLE;
    }

    public AssetInfo(int chainId, int assetId, String symbol, int decimals) {
        this.key = DBUtil.getAssetKey(chainId, assetId);
        this.chainId = chainId;
        this.assetId = assetId;
        this.symbol = symbol;
        this.decimals = decimals;
        this.status = ApiConstant.ENABLE;
    }

    public String getKey() {
        if (key == null) {
            key = DBUtil.getAssetKey(chainId, assetId);
        }
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

    public BigInteger getInitCoins() {
        return initCoins;
    }

    public void setInitCoins(BigInteger initCoins) {
        this.initCoins = initCoins;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }
}
