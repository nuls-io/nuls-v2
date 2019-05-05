package io.nuls.base.api.provider.consensus.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-26 15:46
 * @Description: 功能描述
 */
public class GetAgentInfoReq extends BaseReq {

    String agentHash;

    public GetAgentInfoReq(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }
}
