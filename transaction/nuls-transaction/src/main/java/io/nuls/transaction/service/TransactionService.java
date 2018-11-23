package io.nuls.transaction.service;

import io.nuls.base.data.Transaction;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
public interface TransactionService {

    void newTx(Transaction transaction);
}
