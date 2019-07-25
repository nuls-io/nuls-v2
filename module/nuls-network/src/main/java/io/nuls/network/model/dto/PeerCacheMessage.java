package io.nuls.network.model.dto;

import io.nuls.network.manager.TimeManager;

/**
 * @author lanjinsheng
 * @date 2019-07-16
 */
public class PeerCacheMessage {
    byte[] message;
    long createTime = 0;

    public PeerCacheMessage(byte[] message) {
        this.message = message;
        this.createTime = TimeManager.currentTimeMillis();
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
