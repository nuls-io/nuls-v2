package io.nuls.base.api.provider.consensus.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:51
 * @Description:
 * 停止共识
 * stop  consensus
 */
public class GetStopAgentCoinDataReq extends BaseReq {

    /**
     * 共识地址
     */
    String agentHash;

    long lockHeight;

    public GetStopAgentCoinDataReq() {
    }

    public GetStopAgentCoinDataReq(String agentHash, long lockHeight) {
        this.agentHash = agentHash;
        this.lockHeight = lockHeight;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public long getLockHeight() {
        return lockHeight;
    }

    public void setLockHeight(long lockHeight) {
        this.lockHeight = lockHeight;
    }
}
