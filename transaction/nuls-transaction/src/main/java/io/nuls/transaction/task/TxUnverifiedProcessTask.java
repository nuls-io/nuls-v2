package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.transaction.cache.TxVerifiedPool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.rocksdb.storage.TxUnverifiedStorageService;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.utils.TransactionManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/28
 */
public class TxUnverifiedProcessTask implements Runnable {

    private TxVerifiedPool txVerifiedPool = TxVerifiedPool.getInstance();
    private TransactionManager transactionManager = TransactionManager.getInstance();

    private TxUnverifiedStorageService txUnverifiedStorageService = SpringLiteContext.getBean(TxUnverifiedStorageService.class);
    private ConfirmedTransactionService confirmedTransactionService = SpringLiteContext.getBean(ConfirmedTransactionService.class);

    private List<Transaction> orphanTxList = new ArrayList<>();

    //private static final int MAX_ORPHAN_SIZE = 200000;

    int count = 0;
    int size = 0;

    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception e) {
            Log.error(e);
        }
        try {
            doOrphanTxTask();
        } catch (Exception e) {
            Log.error(e);
        }
       System.out.println("count: " + count + " , size : " + size + " , orphan size : " + orphanTxList.size());
    }

    private void doTask(){
        if (txVerifiedPool.getPoolSize() >= TxConstant.TX_UNVERIFIED_QUEUE_MAXSIZE) {
            return;
        }

        Transaction tx = null;
        while ((tx = txUnverifiedStorageService.pollTx()) != null && orphanTxList.size() < TxConstant.ORPHAN_CONTAINER_MAX_SIZE) {
            size++;
            processTx(tx, false);
        }
    }

    private boolean processTx(Transaction tx, boolean isOrphanTx){
        try {
            Result result = transactionManager.verify(tx);
            if (result.isFailed()) {
                return false;
            }
            //获取一笔交易(从已确认交易库中获取？)
            Transaction transaction = confirmedTransactionService.getTransaction(tx.getHash());
            if(null != transaction){
                return isOrphanTx;
            }
            //验证coinData


        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void doOrphanTxTask(){
        //todo
        //时间排序TransactionTimeComparator
        Iterator<Transaction> it = orphanTxList.iterator();
        while (it.hasNext()) {
            Transaction tx = it.next();
            boolean success = processTx(tx, true);
            if (success) {
                it.remove();
            }
        }
    }

}
