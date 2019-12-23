package io.nuls.api.task;

import io.nuls.api.db.TransactionService;
import io.nuls.api.db.mongo.MongoTransactionServiceImpl;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;

public class DeleteTxsTask implements Runnable {

    private int chainId;

    private TransactionService transactionService;

    public DeleteTxsTask(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void run() {
        try {
            transactionService = SpringLiteContext.getBean(MongoTransactionServiceImpl.class);
            transactionService.deleteTxs(chainId);
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
