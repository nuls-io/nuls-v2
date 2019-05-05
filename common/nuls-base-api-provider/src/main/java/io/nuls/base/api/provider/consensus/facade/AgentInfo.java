package io.nuls.base.api.provider.consensus.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-26 15:44
 * @Description: 功能描述
 */
public class AgentInfo {

    private String agentHash;

    private String agentAddress;

    private String packingAddress;

    private String rewardAddress;

    private String deposit;

    private double commissionRate;

    private String agentName;

    private String agentId;

    private long time;

    private long blockHeight = -1L;

    private long delHeight = -1L;

    private int status;

    private double creditVal;

    private String totalDeposit;

    private String txHash;

    private int memberCount;

    private String version;


    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"agentHash\":\"")
                .append(agentHash).append('\"')
                .append(",\"agentAddress\":\"")
                .append(agentAddress).append('\"')
                .append(",\"packingAddress\":\"")
                .append(packingAddress).append('\"')
                .append(",\"rewardAddress\":\"")
                .append(rewardAddress).append('\"')
                .append(",\"deposit\":\"")
                .append(deposit).append('\"')
                .append(",\"commissionRate\":")
                .append(commissionRate)
                .append(",\"agentName\":\"")
                .append(agentName).append('\"')
                .append(",\"agentId\":\"")
                .append(agentId).append('\"')
                .append(",\"time\":")
                .append(time)
                .append(",\"blockHeight\":")
                .append(blockHeight)
                .append(",\"delHeight\":")
                .append(delHeight)
                .append(",\"status\":")
                .append(status)
                .append(",\"creditVal\":")
                .append(creditVal)
                .append(",\"totalDeposit\":\"")
                .append(totalDeposit).append('\"')
                .append(",\"txHash\":\"")
                .append(txHash).append('\"')
                .append(",\"memberCount\":")
                .append(memberCount)
                .append(",\"version\":\"")
                .append(version).append('\"')
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentInfo)) return false;

        AgentInfo agentInfo = (AgentInfo) o;

        if (Double.compare(agentInfo.commissionRate, commissionRate) != 0) return false;
        if (time != agentInfo.time) return false;
        if (blockHeight != agentInfo.blockHeight) return false;
        if (delHeight != agentInfo.delHeight) return false;
        if (status != agentInfo.status) return false;
        if (Double.compare(agentInfo.creditVal, creditVal) != 0) return false;
        if (memberCount != agentInfo.memberCount) return false;
        if (agentHash != null ? !agentHash.equals(agentInfo.agentHash) : agentInfo.agentHash != null) return false;
        if (agentAddress != null ? !agentAddress.equals(agentInfo.agentAddress) : agentInfo.agentAddress != null)
            return false;
        if (packingAddress != null ? !packingAddress.equals(agentInfo.packingAddress) : agentInfo.packingAddress != null)
            return false;
        if (rewardAddress != null ? !rewardAddress.equals(agentInfo.rewardAddress) : agentInfo.rewardAddress != null)
            return false;
        if (deposit != null ? !deposit.equals(agentInfo.deposit) : agentInfo.deposit != null) return false;
        if (agentName != null ? !agentName.equals(agentInfo.agentName) : agentInfo.agentName != null) return false;
        if (agentId != null ? !agentId.equals(agentInfo.agentId) : agentInfo.agentId != null) return false;
        if (totalDeposit != null ? !totalDeposit.equals(agentInfo.totalDeposit) : agentInfo.totalDeposit != null)
            return false;
        if (txHash != null ? !txHash.equals(agentInfo.txHash) : agentInfo.txHash != null) return false;
        return version != null ? version.equals(agentInfo.version) : agentInfo.version == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = agentHash != null ? agentHash.hashCode() : 0;
        result = 31 * result + (agentAddress != null ? agentAddress.hashCode() : 0);
        result = 31 * result + (packingAddress != null ? packingAddress.hashCode() : 0);
        result = 31 * result + (rewardAddress != null ? rewardAddress.hashCode() : 0);
        result = 31 * result + (deposit != null ? deposit.hashCode() : 0);
        temp = Double.doubleToLongBits(commissionRate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (agentName != null ? agentName.hashCode() : 0);
        result = 31 * result + (agentId != null ? agentId.hashCode() : 0);
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (blockHeight ^ (blockHeight >>> 32));
        result = 31 * result + (int) (delHeight ^ (delHeight >>> 32));
        result = 31 * result + status;
        temp = Double.doubleToLongBits(creditVal);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (totalDeposit != null ? totalDeposit.hashCode() : 0);
        result = 31 * result + (txHash != null ? txHash.hashCode() : 0);
        result = 31 * result + memberCount;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public String getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(String rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(long delHeight) {
        this.delHeight = delHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getCreditVal() {
        return creditVal;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public String getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(String totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

