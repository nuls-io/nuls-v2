package io.nuls.provider.api.model;

import java.util.ArrayList;
import java.util.List;

public class ChainInfo {

    private int chainId;

    private String chainName;

    private List<AssetInfo> assets;

    public ChainInfo() {
        assets = new ArrayList<>();
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public List<AssetInfo> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetInfo> assets) {
        this.assets = assets;
    }
}
