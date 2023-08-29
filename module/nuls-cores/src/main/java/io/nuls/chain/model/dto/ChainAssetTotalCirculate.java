package io.nuls.chain.model.dto;

import java.math.BigInteger;

public class ChainAssetTotalCirculate {
    int chainId;
    int assetId;
    BigInteger availableAmount = BigInteger.ZERO;
    BigInteger freeze = BigInteger.ZERO;

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

    public BigInteger getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigInteger availableAmount) {
        this.availableAmount = availableAmount;
    }

    public BigInteger getFreeze() {
        return freeze;
    }

    public void setFreeze(BigInteger freeze) {
        this.freeze = freeze;
    }
}
