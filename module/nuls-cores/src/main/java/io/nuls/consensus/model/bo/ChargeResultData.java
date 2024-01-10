package io.nuls.consensus.model.bo;

import io.nuls.consensus.constant.ConsensusConstant;

import java.math.BigInteger;

/**
 * Transaction fee return result class
 * Transaction Fee Return Result Class
 *
 * @author tag
 * */
public class ChargeResultData {
    private BigInteger fee;
    private int assetChainId;
    private int assetId;

    public ChargeResultData(BigInteger fee, int assetChainId,int assetId) {
        this.fee = fee;
        this.assetChainId = assetChainId;
        this.assetId = assetId;
    }

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getKey(){
        return getAssetChainId() + ConsensusConstant.SEPARATOR + getAssetId();
    }
}
