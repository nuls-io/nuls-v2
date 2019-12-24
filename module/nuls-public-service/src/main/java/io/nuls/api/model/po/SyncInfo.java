package io.nuls.api.model.po;

public class SyncInfo {

    private int chainId;

    private long bestHeight;

    private int version;

    private int step;

    public SyncInfo() {
    }

    public SyncInfo(int chainId, long bestHeight, int version, int step) {
        this.chainId = chainId;
        this.bestHeight = bestHeight;
        this.version = version;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
