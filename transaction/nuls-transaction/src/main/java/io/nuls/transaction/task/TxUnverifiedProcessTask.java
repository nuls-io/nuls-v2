package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;
import io.nuls.transaction.cache.TxVerifiedPool;
import io.nuls.transaction.db.rocksdb.storage.TxUnverifiedStorageService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/28
 */
@Service
public class TxUnverifiedProcessTask implements Runnable {

    private TxVerifiedPool txVerifiedPool = TxVerifiedPool.getInstance();

    @Autowired
    private TxUnverifiedStorageService txUnverifiedStorageService;

    private List<Transaction> orphanTxList = new ArrayList<>();

    private static int maxOrphanSize = 200000;

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
        if (txVerifiedPool.getPoolSize() >= 1000000L) {
            return;
        }

        Transaction tx = null;
        while ((tx = txUnverifiedStorageService.pollTx()) != null && orphanTxList.size() < maxOrphanSize) {
            size++;
            processTx(tx, false);
        }
    }

    private void processTx(Transaction tx, boolean isOrphanTx){

    }

    private void doOrphanTxTask(){

    }

}
