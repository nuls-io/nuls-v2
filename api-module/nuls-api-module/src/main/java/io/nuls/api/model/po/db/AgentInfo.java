package io.nuls.api.model.po.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigInteger;

public class AgentInfo extends TxDataInfo {

    private String txHash;

    private String agentId;

    private String agentAddress;

    private String packingAddress;

    private String rewardAddress;

    private String agentAlias;

    private BigInteger deposit;

    private double commissionRate;

    private long createTime;
    // 0:待共识，1:共识中，2:退出共识
    private int status;

    private BigInteger totalDeposit;

    private int depositCount;

    private double creditValue;

    private long totalPackingCount;

    private double lostRate;

    private long lastRewardHeight;

    private String deleteHash;

    private long blockHeight;

    private long deleteHeight;

    private BigInteger totalReward;

    private BigInteger commissionReward;

    private BigInteger agentReward;

    private long roundPackingTime;

    private int version;

    private int type;

    @JsonIgnore
    private boolean isNew;

    public AgentInfo() {

    }

    public void init() {
        totalReward = BigInteger.ZERO;
        commissionReward = BigInteger.ZERO;
        agentReward = BigInteger.ZERO;
        totalDeposit = BigInteger.ZERO;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
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

    public String getAgentAlias() {
        return agentAlias;
    }

    public void setAgentAlias(String agentAlias) {
        this.agentAlias = agentAlias;
    }

    public BigInteger getDeposit() {
        return deposit;
    }

    public void setDeposit(BigInteger deposit) {
        this.deposit = deposit;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BigInteger getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(BigInteger totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public int getDepositCount() {
        return depositCount;
    }

    public void setDepositCount(int depositCount) {
        this.depositCount = depositCount;
    }

    public double getCreditValue() {
        return creditValue;
    }

    public void setCreditValue(double creditValue) {
        this.creditValue = creditValue;
    }

    public long getTotalPackingCount() {
        return totalPackingCount;
    }

    public void setTotalPackingCount(long totalPackingCount) {
        this.totalPackingCount = totalPackingCount;
    }

    public double getLostRate() {
        return lostRate;
    }

    public void setLostRate(double lostRate) {
        this.lostRate = lostRate;
    }

    public long getLastRewardHeight() {
        return lastRewardHeight;
    }

    public void setLastRewardHeight(long lastRewardHeight) {
        this.lastRewardHeight = lastRewardHeight;
    }

    public String getDeleteHash() {
        return deleteHash;
    }

    public void setDeleteHash(String deleteHash) {
        this.deleteHash = deleteHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getDeleteHeight() {
        return deleteHeight;
    }

    public void setDeleteHeight(long deleteHeight) {
        this.deleteHeight = deleteHeight;
    }

    public BigInteger getTotalReward() {
        return totalReward;
    }

    public void setTotalReward(BigInteger totalReward) {
        this.totalReward = totalReward;
    }

    public BigInteger getCommissionReward() {
        return commissionReward;
    }

    public void setCommissionReward(BigInteger commissionReward) {
        this.commissionReward = commissionReward;
    }

    public BigInteger getAgentReward() {
        return agentReward;
    }

    public void setAgentReward(BigInteger agentReward) {
        this.agentReward = agentReward;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public long getRoundPackingTime() {
        return roundPackingTime;
    }

    public void setRoundPackingTime(long roundPackingTime) {
        this.roundPackingTime = roundPackingTime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
