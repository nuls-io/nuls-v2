package io.nuls.consensus.model.dto.input;
/**
 * 创建节点参数类
 * Creating Node Parameter Classes
 *
 * @author tag
 * 2018/11/12
 * */
public class CreateAgentDTO {
    private int chainId;
    private String agentAddress;
    private String packingAddress;
    private String rewardAddress;
    private byte commissionRate;
    private String deposit;
    private String password;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
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

    public byte getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(byte commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
