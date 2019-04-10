package io.nuls.api.provider.network.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 16:13
 * @Description:
 * 网络信息
 * network info
 */
public class NetworkInfo {

    /**
     * 本地最新区块高度
     */
    long localBestHeight;

    /**
     * 网络最新区块高度
     */
    long netBestHeight;

    /**
     * 网络时间偏移值 毫秒数
     */
    long timeOffset;
    /**
     * 被动连接节点数量
     */
    int inCount;
    /**
     * 主动连接节点数量
     */
    int outCount;

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
