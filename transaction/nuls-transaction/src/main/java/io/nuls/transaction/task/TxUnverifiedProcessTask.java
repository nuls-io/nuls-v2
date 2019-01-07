package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;
import io.nuls.transaction.cache.TxVerifiedPool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
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

    private TxVerifiedPool txVerifiedPool = SpringLiteContext.getBean(TxVerifiedPool.class);
    private TransactionManager transactionManager = SpringLiteContext.getBean(TransactionManager.class);

    private TxUnverifiedStorageService txUnverifiedStorageService = SpringLiteContext.getBean(TxUnverifiedStorageService.class);
    private ConfirmedTransactionService confirmedTransactionService = SpringLiteContext.getBean(ConfirmedTransactionService.class);
    private TxVerifiedStorageService txVerifiedStorageService = SpringLiteContext.getBean(TxVerifiedStorageService.class);
    private TransactionH2Service transactionH2Service = SpringLiteContext.getBean(TransactionH2Service.class);

    private TransactionTimeComparator txComparator = SpringLiteContext.getBean(TransactionTimeComparator.class);
    private List<Transaction> orphanTxList = new ArrayList<>();

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
            chain.getLogger().error(e);
        }
        try {
            //处理孤儿交易
            doOrphanTxTask(chain);
        } catch (Exception e) {
            chain.getLogger().error(e);
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
            //只会有本地创建的跨链交易才会进入这里, 其他链广播到跨链交易, 由其他逻辑处理
            if (!rs) {
                return false;
            }
            //获取一笔交易(从已确认交易库中获取？)
            Transaction transaction = confirmedTransactionService.getConfirmedTransaction(chain, tx.getHash());
            if(null != transaction){
                return isOrphanTx;
            }
            Map<String, String> params = new HashMap<>();
            params.put("tx", tx.hex());
            Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "verifyCoinData",params);
            if(response.isSuccess()){
                txVerifiedPool.add(chain, tx,false);
                //保存到rocksdb
                txVerifiedStorageService.putTx(chainId, tx);
                //保存到h2数据库
                transactionH2Service.saveTxs(TxUtil.tx2PO(tx));
                //调账本记录未确认交易
                LegerCall.commitTxLeger(chain, tx, false);
                //广播交易hash
                NetworkCall.broadcastTxHash(chain.getChainId(),tx.getHash());
                return true;
            }
            Map map = (Map)response.getResponseData();
            ErrorCode errorCode = (ErrorCode)map.get("ErrorCode");
            if(errorCode.equals(TxErrorCode.ORPHAN_TX) && !isOrphanTx){
                processOrphanTx(tx);
            }else if(isOrphanTx){
                //todo 孤儿交易还是10分钟删, 如何处理nonce值??
                return tx.getTime() < (TimeService.currentTimeMillis() - 3600000L);
            }
        } catch (Exception e) {
            Log.error(e);
            e.printStackTrace();
        }
        return false;
    }


    private void doOrphanTxTask(Chain chain){
        //时间排序TransactionTimeComparator
        orphanTxList.sort(txComparator);

        Iterator<Transaction> it = orphanTxList.iterator();
        while (it.hasNext()) {
            Transaction tx = it.next();
            boolean success = processTx(chain, tx, true);
            if (success) {
                LegerCall.rollbackTxLeger(chain, tx, false);
                it.remove();
            }
        }
    }

    private void processOrphanTx(Transaction tx) throws NulsException {
        orphanTxList.add(tx);
    }

}
