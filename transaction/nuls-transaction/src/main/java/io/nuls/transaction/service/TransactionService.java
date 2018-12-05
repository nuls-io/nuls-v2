package io.nuls.transaction.service;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.basic.Result;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.dto.CoinDTO;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
public interface TransactionService {

    Result register(TxRegister txRegister);

    Result newTx(Transaction transaction);

    /**
     * get a transaction
     *
     * 获取一笔交易
     * @param hash
     * @return Transaction
     */
    Result getTransaction(NulsDigestData hash);


    Result createCrossTransaction(List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark);
}
