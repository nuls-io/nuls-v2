package io.nuls.base.api.provider.network.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-28 14:45
 * @Description: 功能描述
 */
public class RemoteNodeInfo {

    private String blockHash;

    private long blockHeight;

    private String peer;

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getPeer() {
        return peer;
    }

    public void setPeer(String peer) {
        this.peer = peer;
    }
}
