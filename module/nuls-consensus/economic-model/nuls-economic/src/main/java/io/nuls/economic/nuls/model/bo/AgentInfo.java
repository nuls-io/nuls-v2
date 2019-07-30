package io.nuls.economic.nuls.model.bo;

import java.math.BigInteger;
import java.util.List;

/**
 * 节点信息
 * agent info
 *
 * @author tag
 * 2019/7/23
 * */
public class AgentInfo {
    private byte commissionRate;
    private BigInteger deposit;
    private byte[] rewardAddress;
    private BigInteger totalDeposit;
    private double creditVal;
    private List<DepositInfo> depositList;

    public AgentInfo(){}

    public  AgentInfo(byte commissionRate,BigInteger deposit,byte[] rewardAddress,BigInteger totalDeposit,double creditVal,List<DepositInfo> depositList){
        this.commissionRate = commissionRate;
        this.deposit = deposit;
        this.rewardAddress = rewardAddress;
        this.totalDeposit = totalDeposit;
        this.creditVal = creditVal;
        this.depositList = depositList;
    }

    public byte getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(byte commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigInteger getDeposit() {
        return deposit;
    }

    public void setDeposit(BigInteger deposit) {
        this.deposit = deposit;
    }

    public byte[] getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(byte[] rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public BigInteger getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(BigInteger totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public double getCreditVal() {
        return creditVal;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public List<DepositInfo> getDepositList() {
        return depositList;
    }

    public void setDepositList(List<DepositInfo> depositList) {
        this.depositList = depositList;
    }
}
