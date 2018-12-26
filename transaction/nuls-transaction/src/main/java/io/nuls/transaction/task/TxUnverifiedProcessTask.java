package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.transaction.cache.TxVerifiedPool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.rocksdb.storage.TxUnverifiedStorageService;
import io.nuls.transaction.db.rocksdb.storage.TxVerifiedStorageService;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.rpc.call.LegerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.utils.TxUtil;
import io.nuls.transaction.utils.TransactionTimeComparator;


import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/11/28
 */
public class TxUnverifiedProcessTask implements Runnable {

    //todo
    private TxVerifiedPool txVerifiedPool = SpringLiteContext.getBean(TxVerifiedPool.class);
    //todo
    private TransactionManager transactionManager = SpringLiteContext.getBean(TransactionManager.class);

    private TxUnverifiedStorageService txUnverifiedStorageService = SpringLiteContext.getBean(TxUnverifiedStorageService.class);
    private ConfirmedTransactionService confirmedTransactionService = SpringLiteContext.getBean(ConfirmedTransactionService.class);
    private TxVerifiedStorageService txVerifiedStorageService = SpringLiteContext.getBean(TxVerifiedStorageService.class);
    private TransactionH2Service transactionH2Service = SpringLiteContext.getBean(TransactionH2Service.class);

    private TransactionTimeComparator txComparator = SpringLiteContext.getBean(TransactionTimeComparator.class);
    private List<Transaction> orphanTxList = new ArrayList<>();

    //private static final int MAX_ORPHAN_SIZE = 200000;
    private Chain chain;

    public  TxUnverifiedProcessTask(Chain chain){
        this.chain = chain;
    }

    int count = 0;
    int size = 0;

    @Override
    public void run() {
        try {
            doTask(chain);
        } catch (Exception e) {
            Log.error(e);
        }
        try {
            doOrphanTxTask(chain);
        } catch (Exception e) {
            Log.error(e);
        }
        System.out.println("count: " + count + " , size : " + size + " , orphan size : " + orphanTxList.size());
    }

    private void doTask(Chain chain){
        if (txVerifiedPool.getPoolSize(chain) >= TxConstant.TX_UNVERIFIED_QUEUE_MAXSIZE) {
            return;
        }

        Transaction tx = null;
        while ((tx = txUnverifiedStorageService.pollTx(chain)) != null && orphanTxList.size() < TxConstant.ORPHAN_CONTAINER_MAX_SIZE) {
            size++;
            processTx(chain, tx, false);
        }
    }

    private boolean processTx(Chain chain, Transaction tx, boolean isOrphanTx){
        try {
            int chainId = chain.getChainId();
            boolean rs = transactionManager.verify(chain, tx);
            //todo 跨链交易单独处理, 是否需要进行跨链验证？

            if (!rs) {
                return false;
            }
            //获取一笔交易(从已确认交易库中获取？)
            Transaction transaction = confirmedTransactionService.getTransaction(chain,tx.getHash());
            if(null != transaction){
                return isOrphanTx;
            }
            //todo 验证coinData

            Map<String, String> params = new HashMap<>();
            params.put("tx", tx.hex());
            Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "verifyCoinData",params);
            if(response.isSuccess()){
                txVerifiedPool.add(chain, tx,false);
                //保存到rocksdb
                txVerifiedStorageService.putTx(chainId, tx);
                //保存到h2数据库
                transactionH2Service.saveTxs(TxUtil.tx2PO(tx));
                //todo 调账本记录未确认交易
                LegerCall.sendTx(chain.getChainId(), tx, false);
                //广播交易hash
                NetworkCall.broadcastTxHash(chain.getChainId(),tx.getHash());
            }
        } catch (Exception e) {
            Log.error(e);
            e.printStackTrace();

        }
        return false;
    }


    private void doOrphanTxTask(Chain chain){
        //todo
        //时间排序TransactionTimeComparator
        orphanTxList.sort(txComparator);

        Iterator<Transaction> it = orphanTxList.iterator();
        while (it.hasNext()) {
            Transaction tx = it.next();
            boolean success = processTx(chain, tx, true);
            if (success) {
                it.remove();
            }
        }
    }

}
