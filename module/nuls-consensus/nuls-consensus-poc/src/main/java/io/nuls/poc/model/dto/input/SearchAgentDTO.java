package io.nuls.poc.model.dto.input;

/**
 * 查询指定节点参数列
 * Query the specified node parameter column
 *
 * @author tag
 * 2018/11/12
 * */
public class SearchAgentDTO {
    private int chainId;
    private String agentHash;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }
}
