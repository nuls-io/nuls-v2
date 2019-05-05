package io.nuls.base.api.provider.contract.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 16:17
 * @Description: 功能描述
 */
public class GetContractInfoReq extends BaseReq {

    private String contractAddress;

    public GetContractInfoReq(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}
