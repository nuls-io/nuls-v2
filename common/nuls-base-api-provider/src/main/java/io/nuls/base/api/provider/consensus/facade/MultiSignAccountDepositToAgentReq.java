package io.nuls.base.api.provider.consensus.facade;

import io.nuls.base.api.provider.BaseReq;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:53
 * @Description:
 * Commission consensus
 */
public class MultiSignAccountDepositToAgentReq extends BaseReq {

    String address;

    String agentHash;

    BigInteger deposit;

    String password;

    String signAddress;

    public MultiSignAccountDepositToAgentReq(String address, String agentHash, BigInteger deposit, String password) {
        this.address = address;
        this.agentHash = agentHash;
        this.deposit = deposit;
        this.password = password;
    }

    public MultiSignAccountDepositToAgentReq(String address, String agentHash, BigInteger deposit) {
        this.address = address;
        this.agentHash = agentHash;
        this.deposit = deposit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
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
