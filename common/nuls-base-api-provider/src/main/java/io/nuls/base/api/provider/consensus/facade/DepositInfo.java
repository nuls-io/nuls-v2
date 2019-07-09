package io.nuls.base.api.provider.consensus.facade;

public class DepositInfo {
    private String deposit;

    private String agentHash;

    private String address;

    private Long time;

    private String txHash;

    private Long blockHeight;

    private Long delHeight;

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(Long delHeight) {
        this.delHeight = delHeight;
    }
}
