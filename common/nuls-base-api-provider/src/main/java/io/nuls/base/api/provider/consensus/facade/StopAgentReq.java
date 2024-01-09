package io.nuls.base.api.provider.consensus.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:51
 * @Description:
 * Stop consensus
 * stop  consensus
 */
public class StopAgentReq extends BaseReq {

    /**
     * Consensus Address
     */
    String address;

    String password;

    public StopAgentReq(String address, String password) {
        this.address = address;
        this.password = password;
    }

    public StopAgentReq(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
