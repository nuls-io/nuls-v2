package io.nuls.poc.model.bo;

import io.nuls.poc.constant.ConsensusConstant;

import java.math.BigInteger;

/**
 * 交易手续费返回结果类
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
