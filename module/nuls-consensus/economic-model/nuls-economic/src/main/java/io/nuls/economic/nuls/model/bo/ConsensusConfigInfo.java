package io.nuls.economic.nuls.model.bo;
import java.math.BigInteger;

/**
 * 共识与经济模型相关配置透传
 * Consensus and economic model-related allocation transmission
 *
 * @author tag
 * 2019/7/23
 * */
public class ConsensusConfigInfo {
    private int chainId;
    private int assertId;
    private long packingInterval;
    private BigInteger inflationAmount;
    private BigInteger totalInflationAmount;
    private long initTime;
    private double deflationRatio;
    private long deflationTimeInterval;
    private int awardAssetId;

    public ConsensusConfigInfo(){}

    public ConsensusConfigInfo(int chainId, int assertId, long packingInterval, BigInteger inflationAmount, BigInteger totalInflationAmount, long initTime, double deflationRatio, long deflationTimeInterval, int awardAssetId){
        this.chainId = chainId;
        this.assertId = assertId;
        this.packingInterval = packingInterval;
        this.inflationAmount = inflationAmount;
        this.totalInflationAmount = totalInflationAmount;
        this.initTime = initTime;
        this.deflationRatio = deflationRatio;
        this.deflationTimeInterval = deflationTimeInterval;
        this.awardAssetId = awardAssetId;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssertId() {
        return assertId;
    }

    public void setAssertId(int assertId) {
        this.assertId = assertId;
    }

    public long getPackingInterval() {
        return packingInterval;
    }

    public void setPackingInterval(long packingInterval) {
        this.packingInterval = packingInterval;
    }

    public BigInteger getInflationAmount() {
        return inflationAmount;
    }

    public void setInflationAmount(BigInteger inflationAmount) {
        this.inflationAmount = inflationAmount;
    }

    public long getInitTime() {
        return initTime;
    }

    public void setInitTime(long initTime) {
        this.initTime = initTime;
    }

    public double getDeflationRatio() {
        return deflationRatio;
    }

    public void setDeflationRatio(double deflationRatio) {
        this.deflationRatio = deflationRatio;
    }

    public long getDeflationTimeInterval() {
        return deflationTimeInterval;
    }

    public void setDeflationTimeInterval(long deflationTimeInterval) {
        this.deflationTimeInterval = deflationTimeInterval;
    }

    public int getAwardAssetId() {
        return awardAssetId;
    }

    public void setAwardAssetId(int awardAssetId) {
        this.awardAssetId = awardAssetId;
    }

    public BigInteger getTotalInflationAmount() {
        return totalInflationAmount;
    }

    public void setTotalInflationAmount(BigInteger totalInflationAmount) {
        this.totalInflationAmount = totalInflationAmount;
    }
}
