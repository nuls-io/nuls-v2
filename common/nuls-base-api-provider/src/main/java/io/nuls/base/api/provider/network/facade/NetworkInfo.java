package io.nuls.base.api.provider.network.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 16:13
 * @Description:
 * network information
 * network info
 */
public class NetworkInfo {

    /**
     * Local latest block height
     */
    long localBestHeight;

    /**
     * The latest block height in the network
     */
    long netBestHeight;

    /**
     * Network time offset value Msec
     */
    long timeOffset;
    /**
     * Number of passive connection nodes
     */
    int inCount;
    /**
     * Number of active connection nodes
     */
    int outCount;

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"localBestHeight\":")
                .append(localBestHeight)
                .append(",\"netBestHeight\":")
                .append(netBestHeight)
                .append(",\"timeOffset\":")
                .append(timeOffset)
                .append(",\"inCount\":")
                .append(inCount)
                .append(",\"outCount\":")
                .append(outCount)
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkInfo)) return false;

        NetworkInfo that = (NetworkInfo) o;

        if (localBestHeight != that.localBestHeight) return false;
        if (netBestHeight != that.netBestHeight) return false;
        if (timeOffset != that.timeOffset) return false;
        if (inCount != that.inCount) return false;
        return outCount == that.outCount;
    }

    @Override
    public int hashCode() {
        int result = (int) (localBestHeight ^ (localBestHeight >>> 32));
        result = 31 * result + (int) (netBestHeight ^ (netBestHeight >>> 32));
        result = 31 * result + (int) (timeOffset ^ (timeOffset >>> 32));
        result = 31 * result + inCount;
        result = 31 * result + outCount;
        return result;
    }

    public long getLocalBestHeight() {
        return localBestHeight;
    }

    public void setLocalBestHeight(long localBestHeight) {
        this.localBestHeight = localBestHeight;
    }

    public long getNetBestHeight() {
        return netBestHeight;
    }

    public void setNetBestHeight(long netBestHeight) {
        this.netBestHeight = netBestHeight;
    }

    public long getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    public int getInCount() {
        return inCount;
    }

    public void setInCount(int inCount) {
        this.inCount = inCount;
    }

    public int getOutCount() {
        return outCount;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
    }
}
