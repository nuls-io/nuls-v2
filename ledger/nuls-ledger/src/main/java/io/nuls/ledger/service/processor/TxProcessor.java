package io.nuls.ledger.service.processor;

import io.nuls.base.data.Transaction;

/**
 * Created by wangkun23 on 2018/11/29.
 */
public interface TxProcessor {

    public void process(Transaction transaction);
}
