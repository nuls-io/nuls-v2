package io.nuls.base.api.provider.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.CreateCrossTxReq;
import io.nuls.base.api.provider.crosschain.facade.RegisterChainReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 17:01
 * @Description: 功能描述
 */
public interface CrossChainProvider {

    /**
     * 在主网注册一条友链，使其可以实现跨链交易
     * @param req
     * @return
     */
    Result<String> registerChain(RegisterChainReq req);

    Result<String> createCrossTx(CreateCrossTxReq req);

}
