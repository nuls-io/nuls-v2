package io.nuls.base.api.provider.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.*;
import io.nuls.base.data.Transaction;

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


    /**
     * 查询跨链交易在其他链的处理状态
     * @param req
     * @return
     */
    Result<Transaction> getCrossTx(GetCrossTxStateReq req);

    /**
     * 给全网发信号，对指定跨链交易重新进行拜赞庭验证
     * @param req
     * @return
     */
    Result<String> rehandleCtx(RehandleCtxReq req);

    /**
     * 创建一个通知所有节点重置本地验证人列表的交易
     * @param req
     * @return
     */
    Result<String> resetLocalVerifier(CreateResetLocalVerifierTxReq req);

}
