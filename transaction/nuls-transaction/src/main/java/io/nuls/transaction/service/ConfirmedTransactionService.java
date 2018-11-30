package io.nuls.transaction.service;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;

/**
 * @author: Charlie
 * @date: 2018/11/30
 */
public interface ConfirmedTransactionService {

    /**
     * get a transaction
     *
     * 获取一笔交易
     * @param hash
     * @return Transaction
     */
    Transaction getTransaction(NulsDigestData hash);
}
