package io.nuls.transaction.service.impl;

import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.service.TransactionService;
import io.nuls.transaction.utils.TransactionManager;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
public class TransactionServiceImpl implements TransactionService {

    @Override
    public void newTx(Transaction transaction) {
        /**
         * 1.基础数据库校验
         * 2.放入队列
         */


    }

    @Override
    public boolean register(TxRegister txRegister) {

        return TransactionManager.INSTANCE.register(txRegister);
    }
}
