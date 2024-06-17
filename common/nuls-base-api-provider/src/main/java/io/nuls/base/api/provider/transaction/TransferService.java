package io.nuls.base.api.provider.transaction;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.transaction.facade.*;
import io.nuls.base.data.Transaction;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 16:18
 * @Description: Function Description
 */
public interface TransferService {

    //todo
    /**
     * Test batch trading To delete
     */
    Result transferTest(int method, String addr1, String addr2,String amount);

    /**
     *  Initiate transaction
     *  transfer
     * @param req
     * @return
     */
    Result<String> transfer(TransferReq req);

    /**
     * Create multi signature transactions
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> multiSignTransfer(CreateMultiSignTransferReq req);

    /**
     * Multiple signature transactions
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> signMultiSignTransfer(SignMultiSignTransferReq req);


    /**
     * Transfer through alias
     * transfer by account alias
     * @param req
     * @return
     */
    Result<String> transferByAlias(TransferReq req);

    /**
     * adopthashObtain transaction information Including unconfirmed transactions
     * get transaction by hash
     * contains unconfirmed transaction
     * @param req
     * @return
     */
    Result<Transaction> getTxByHash(GetTxByHashReq req);

    /**
     * adopthashObtain confirmed transaction information
     * get confirmed transaction by hash
     * @param req
     * @return
     */
    Result<Transaction> getConfirmedTxByHash(GetConfirmedTxByHashReq req);

    /**
     * adopthashObtain brief data on confirmed transactions
     * @param req
     * @return
     */
    Result<TransactionData> getSimpleTxDataByHash(GetConfirmedTxByHashReq req);

}
