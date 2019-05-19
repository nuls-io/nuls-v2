package io.nuls.base.api.provider.crosschain.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 11:49
 * @Description: 功能描述
 */
public class GetCrossChainInfoReq extends BaseReq {

    public GetCrossChainInfoReq(Integer chainId){
        this.setChainId(chainId);
    }

}
