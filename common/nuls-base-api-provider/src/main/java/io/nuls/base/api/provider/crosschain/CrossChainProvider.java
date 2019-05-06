package io.nuls.base.api.provider.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.RegisterChainReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 17:01
 * @Description: 功能描述
 */
public interface CrossChainProvider {

    Result<String> registerChain(RegisterChainReq req);

}
