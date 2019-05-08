package io.nuls.api.model.po.db.mini;

import io.nuls.api.model.po.db.TxDataInfo;

public class MiniCoinBaseInfo extends TxDataInfo {

    private long roundIndex;

    private long packageIndex;

    private String agentId;

    public MiniCoinBaseInfo() {
    }

    public MiniCoinBaseInfo(long roundIndex, long packageIndex, String txHash) {
        this.roundIndex = roundIndex;
        this.packageIndex = packageIndex;
        this.agentId = txHash.substring(txHash.length() - 8, txHash.length());
    }

    public long getPackageIndex() {
        return packageIndex;
    }

    public void setPackageIndex(long packageIndex) {
        this.packageIndex = packageIndex;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }
}
