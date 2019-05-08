package io.nuls.base.api.provider.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.CrossChainRegisterInfo;
import io.nuls.base.api.provider.crosschain.facade.GetCrossChainInfoReq;
import io.nuls.base.api.provider.crosschain.facade.RegisterChainReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 16:07
 * @Description: 功能描述
 */
public interface ChainManageProvider {


    /**
     * 在主网注册一条友链，使其可以实现跨链交易
     * @param req
     * @return
     */
    Result<String> registerChain(RegisterChainReq req);


    /**
     * 获取注册了跨链交易的链的注册信息
     * @param req
     * @return
     */
    Result<CrossChainRegisterInfo> getCrossChainInfo(GetCrossChainInfoReq req);


}
