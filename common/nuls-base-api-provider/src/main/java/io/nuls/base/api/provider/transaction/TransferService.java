package io.nuls.base.api.provider.transaction;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.transaction.facade.*;
import io.nuls.base.data.Transaction;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 16:18
 * @Description: 功能描述
 */
public interface TransferService {

    //todo
    /**
     * 测试批量发交易 要删
     */
    Result transferTest(int method, String addr1, String addr2);

    /**
     *  发起交易
     *  transfer
     * @param req
     * @return
     */
    Result<String> transfer(TransferReq req);

    /**
     * 创建多签交易
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> multiSignTransfer(CreateMultiSignTransferReq req);

    /**
     * 签名多签交易
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> signMultiSignTransfer(SignMultiSignTransferReq req);


    /**
     * 通过别名转账
     * transfer by account alias
     * @param req
     * @return
     */
    Result<String> transferByAlias(TransferReq req);

    /**
     * 通过hash获取交易信息 包含未确认的交易
     * get transaction by hash
     * contains unconfirmed transaction
     * @param req
     * @return
     */
    Result<Transaction> getTxByHash(GetTxByHashReq req);

    /**
     * 通过hash获取已确认交易信息
     * get confirmed transaction by hash
     * @param req
     * @return
     */
    Result<Transaction> getConfirmedTxByHash(GetConfirmedTxByHashReq req);

    /**
     * 通过hash获取已确认的交易的简要数据
     * @param req
     * @return
     */
    Result<TransactionData> getSimpleTxDataByHash(GetConfirmedTxByHashReq req);

}
