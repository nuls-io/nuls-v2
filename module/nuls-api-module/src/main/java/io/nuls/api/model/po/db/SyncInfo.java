package io.nuls.api.model.po.db;

public class SyncInfo {

    private int chainId;

    private long bestHeight;

    private int step;

    public SyncInfo() {
    }

    public SyncInfo(int chainId, long bestHeight, int step) {
        this.chainId = chainId;
        this.bestHeight = bestHeight;
        this.step = step;
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
}
