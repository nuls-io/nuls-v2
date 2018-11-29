package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.tools.log.Log;
import io.nuls.transaction.cache.TxVerifiedPool;

/**
 * @author: Charlie
 * @date: 2018/11/28
 */
public class TxUnverifiedProcessTask implements Runnable {


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
       //System.out.println("count: " + count + " , size : " + size + " , orphan size : " + orphanTxList.size());

        System.out.println("TxUnverifiedProcessTask:ok");
    }

    private void doTask(){
        if (TxVerifiedPool.getInstance().getPoolSize() >= 1000000L) {
            return;
        }

        Transaction tx = null;
//        while ((tx = transactionQueueStorageService.pollTx()) != null && orphanTxList.size() < maxOrphanSize) {
//            size++;
//            processTx(tx, false);
//        }
    }

    private void processTx(Transaction tx, boolean isOrphanTx){

    }

    private void doOrphanTxTask(){

    }

}
