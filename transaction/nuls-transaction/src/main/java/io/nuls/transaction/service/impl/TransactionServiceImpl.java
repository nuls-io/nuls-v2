package io.nuls.transaction.service.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Service;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.service.TransactionService;
import io.nuls.transaction.utils.TransactionManager;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionManager transactionManager = TransactionManager.getInstance();

    @Override
    public void newTx(Transaction transaction) {
        /**
         * 1.基础数据库校验
         * 2.放入队列
         */
    }

    @Override
    public boolean register(TxRegister txRegister) {
        return transactionManager.register(txRegister);
    }

    @Override
    public Transaction getTransaction(NulsDigestData hash){
        return null;
    }
}
