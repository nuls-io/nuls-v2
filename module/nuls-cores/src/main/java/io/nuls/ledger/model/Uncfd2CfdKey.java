package io.nuls.ledger.model;

public class Uncfd2CfdKey {

    private String assetKey;
    private String nonceKey;

    public Uncfd2CfdKey(String assetKey, String nonceKey) {
        this.assetKey = assetKey;
        this.nonceKey = nonceKey;
    }

    public String getAssetKey() {
        return assetKey;
    }

    public void setAssetKey(String assetKey) {
        this.assetKey = assetKey;
    }

    public String getNonceKey() {
        return nonceKey;
    }

    public void setNonceKey(String nonceKey) {
        this.nonceKey = nonceKey;
    }
}
