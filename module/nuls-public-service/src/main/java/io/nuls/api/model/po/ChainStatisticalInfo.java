package io.nuls.api.model.po;

public class ChainStatisticalInfo {

    private int chainId;

    private long txCount;

    private long lastStatisticalHeight;

    public long getTxCount() {
        return txCount;
    }

    public void setTxCount(long txCount) {
        this.txCount = txCount;
    }

    public long getLastStatisticalHeight() {
        return lastStatisticalHeight;
    }

    public void setLastStatisticalHeight(long lastStatisticalHeight) {
        this.lastStatisticalHeight = lastStatisticalHeight;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }
}
