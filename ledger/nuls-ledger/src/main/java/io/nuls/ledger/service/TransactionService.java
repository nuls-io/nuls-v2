package io.nuls.ledger.service;

import io.nuls.ledger.model.Transaction;

/**
 * Created by wangkun23 on 2018/11/28.
 */
public interface TransactionService {

    /**
     * 已确认交易数据处理
     */
    public void txProcess(Transaction transaction);
}
