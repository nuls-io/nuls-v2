package io.nuls.api.model.po.db;

public class ChainConfigInfo extends TxDataInfo {

    private int chainId;

    private int agentChainId;

    private int agentAssetId;

    private int awardAssetId;

    public ChainConfigInfo() {
    }

    public ChainConfigInfo(int chainId, int agentChainId, int agentAssetId, int awardAssetId) {
        this.chainId = chainId;
        this.agentChainId = agentChainId;
        this.agentAssetId = agentAssetId;
        this.awardAssetId = awardAssetId;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAgentChainId() {
        return agentChainId;
    }

    public void setAgentChainId(int agentChainId) {
        this.agentChainId = agentChainId;
    }

    public int getAgentAssetId() {
        return agentAssetId;
    }

    public void setAgentAssetId(int agentAssetId) {
        this.agentAssetId = agentAssetId;
    }

    public int getAwardAssetId() {
        return awardAssetId;
    }

    public void setAwardAssetId(int awardAssetId) {
        this.awardAssetId = awardAssetId;
    }
}
