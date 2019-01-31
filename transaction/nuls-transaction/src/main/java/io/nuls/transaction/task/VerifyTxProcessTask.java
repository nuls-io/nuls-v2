package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.rocksdb.storage.UnverifiedTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyTxResult;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.utils.TxUtil;
import io.nuls.transaction.utils.TransactionTimeComparator;


import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/11/28
 */
public class VerifyTxProcessTask implements Runnable {

    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
    private TransactionManager transactionManager = SpringLiteContext.getBean(TransactionManager.class);

    private UnverifiedTxStorageService unverifiedTxStorageService = SpringLiteContext.getBean(UnverifiedTxStorageService.class);
    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);
    private TransactionH2Service transactionH2Service = SpringLiteContext.getBean(TransactionH2Service.class);

    private TransactionTimeComparator txComparator = SpringLiteContext.getBean(TransactionTimeComparator.class);
    private List<Transaction> orphanTxList = new ArrayList<>();

    private Chain chain;

    public VerifyTxProcessTask(Chain chain){
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
//        System.out.println("count: " + count + " , size : " + size + " , orphan size : " + orphanTxList.size());
    }

    private void doTask(Chain chain){
        if (packablePool.getPoolSize(chain) >= TxConstant.TX_UNVERIFIED_QUEUE_MAXSIZE) {
            return;
        }

        Transaction tx = null;
        while ((tx = unverifiedTxStorageService.pollTx(chain)) != null && orphanTxList.size() < TxConstant.ORPHAN_CONTAINER_MAX_SIZE) {
            size++;
            processTx(chain, tx, false);
            System.out.println("count: " + count + " , size : " + size + " , orphan size : " + orphanTxList.size());
        }
    }

    private boolean processTx(Chain chain, Transaction tx, boolean isOrphanTx){
        try {
            chain.getLogger().debug("\n*** Debug *** [VerifyTxProcessTask] txhash:{}, type:{}", tx.getHash(), tx.getType());
            int chainId = chain.getChainId();
            boolean rs = transactionManager.verify(chain, tx);
            //todo 跨链交易单独处理, 是否需要进行跨链验证？
            //只会有本地创建的跨链交易才会进入这里, 其他链广播到跨链交易, 由其他逻辑处理
            if (!rs) {
                return false;
            }
            //获取一笔交易(从已确认交易库中获取？)
            //Transaction transaction = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
            Transaction existTx = txService.getTransaction(chain, tx.getHash());
            if(null != existTx){
                return isOrphanTx;
            }
            VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, tx, false);
            if(verifyTxResult.success()){
                packablePool.add(chain, tx,false);
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chainId, tx);
                //保存到h2数据库
                transactionH2Service.saveTxs(TxUtil.tx2PO(tx));
                //调账本记录未确认交易
                List<String> txHexList = new ArrayList<>();
                txHexList.add(tx.hex());
                LedgerCall.commitTxLedger(chain, txHexList, false);
                //广播交易hash
                //todo 调试暂时注释
                NetworkCall.broadcastTxHash(chain.getChainId(),tx.getHash());
                count++;
                return true;
            }
            chain.getLogger().debug("\n*** Debug *** [VerifyTxProcessTask] " +
                    "coinData not success - code: {}, - reason:{}, type:{} - txhash:{}", verifyTxResult.getCode(),  verifyTxResult.getDesc(), tx.getType(), tx.getHash().getDigestHex());
            if(verifyTxResult.getCode() == VerifyTxResult.ORPHAN && !isOrphanTx){
                processOrphanTx(tx);
            }else if(isOrphanTx){
                //todo 孤儿交易还是10分钟删, 如何处理nonce值??
                long currentTimeMillis = NetworkCall.getCurrentTimeMillis();
//                return tx.getTime() < (currentTimeMillis - 3600000L);
                return tx.getTime() < (currentTimeMillis - 60000L);//TODO 调试暂时改为60秒
            }
        } catch (Exception e) {
            Log.error(e);
            e.printStackTrace();
        }
        return false;
    }


    private void doOrphanTxTask(Chain chain) throws NulsException{
        //时间排序TransactionTimeComparator
        try {
            orphanTxList.sort(txComparator);
            Iterator<Transaction> it = orphanTxList.iterator();
            while (it.hasNext()) {
                Transaction tx = it.next();
                boolean success = processTx(chain, tx, true);
                if (success) {
                    List<String> txHexList = new ArrayList<>();
                    txHexList.add(tx.hex());
                    LedgerCall.rollbackTxLedger(chain, txHexList, false);
                    it.remove();
                }
            }
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    private void processOrphanTx(Transaction tx) throws NulsException {
        orphanTxList.add(tx);
    }

}
