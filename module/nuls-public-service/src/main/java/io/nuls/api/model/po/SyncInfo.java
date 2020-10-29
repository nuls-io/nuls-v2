package io.nuls.api.model.po;

import java.math.BigInteger;

public class SyncInfo {

    private int chainId;

    private long bestHeight;

    private int version;

    private BigInteger totalSupply = BigInteger.ZERO;

    private int step;

    public SyncInfo() {
    }

    public SyncInfo(int chainId, long bestHeight, BlockHeaderInfo headerInfo) {
        this.chainId = chainId;
        this.bestHeight = bestHeight;
        this.version = headerInfo.getMainVersion();
        this.totalSupply = headerInfo.getReward();
        this.step = 0;
    }

    public boolean isFinish() {
        return this.step == 100;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public long getBestHeight() {
        return bestHeight;
    }

    public void setBestHeight(long bestHeight) {
        this.bestHeight = bestHeight;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public BigInteger getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(BigInteger totalSupply) {
        this.totalSupply = totalSupply;
    }
}
