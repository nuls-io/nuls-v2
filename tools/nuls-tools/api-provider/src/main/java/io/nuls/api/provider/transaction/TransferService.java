package io.nuls.api.provider.transaction;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.transaction.facade.*;
import io.nuls.base.data.Transaction;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 16:18
 * @Description: 功能描述
 */
public interface TransferService {

    /**
     *  发起交易
     *  transfer
     * @param req
     * @return
     */
    Result<String> transfer(TransferReq req);


    /**
     * 通过别名转账
     * transfer by account alias
     * @param req
     * @return
     */
    Result<String> transferByAlias(TransferByAliasReq req);

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
