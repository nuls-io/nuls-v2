package io.nuls.base.api.provider.consensus.facade;

import io.nuls.base.api.provider.BaseReq;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:44
 * @Description:
 *  创建共识节点
 *  create consensus agent node
 */
public class CreateAgentReq extends BaseReq {

    private String agentAddress;

    private String packingAddress;

    private String rewardAddress;

    private Integer commissionRate;

    private BigInteger deposit;

    private String password;

    private String signAddress;

    public CreateAgentReq(String agentAddress, String packingAddress, String rewardAddress, Integer commissionRate, BigInteger deposit, String password) {
        this.agentAddress = agentAddress;
        this.packingAddress = packingAddress;
        this.rewardAddress = rewardAddress;
        this.commissionRate = commissionRate;
        this.deposit = deposit;
        this.password = password;
    }

    public CreateAgentReq(String agentAddress, String packingAddress, String rewardAddress, Integer commissionRate, BigInteger deposit) {
        this.agentAddress = agentAddress;
        this.packingAddress = packingAddress;
        this.rewardAddress = rewardAddress;
        this.commissionRate = commissionRate;
        this.deposit = deposit;
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

    public Integer getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(Integer commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigInteger getDeposit() {
        return deposit;
    }

    public void setDeposit(BigInteger deposit) {
        this.deposit = deposit;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }
}
