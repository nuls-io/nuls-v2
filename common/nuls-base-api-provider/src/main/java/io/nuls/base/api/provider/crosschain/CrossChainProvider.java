package io.nuls.base.api.provider.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 17:01
 * @Description: 功能描述
 */
public interface CrossChainProvider {

    /**
     * 创建一笔跨链交易
     * @param req
     * @return
     */
    Result<String> createCrossTx(CreateCrossTxReq req);


    /**
     * 查询跨链交易在其他链的处理状态
     * @param req
     * @return
     */
    Result<Integer> getCrossTxState(GetCrossTxStateReq req);

}
