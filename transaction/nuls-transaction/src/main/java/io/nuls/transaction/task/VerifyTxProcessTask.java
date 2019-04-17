package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.rpc.util.TimeUtils;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyLedgerResult;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.storage.UnverifiedTxStorageService;
import io.nuls.transaction.utils.TransactionTimeComparator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 处理由其他节点广播的交易
 * @author: Charlie
 * @date: 2018/11/28
 */
public class VerifyTxProcessTask implements Runnable {

    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);

    private UnverifiedTxStorageService unverifiedTxStorageService = SpringLiteContext.getBean(UnverifiedTxStorageService.class);
    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);

    private TransactionTimeComparator txComparator = SpringLiteContext.getBean(TransactionTimeComparator.class);
    private List<Transaction> orphanTxList = new ArrayList<>();

    private Chain chain;

    public VerifyTxProcessTask(Chain chain){
        this.chain = chain;
    }


    @Override
    public void run() {
        try {
            doTask(chain);
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error(e);
        }
        try {
            //处理孤儿交易
          doOrphanTxTask(chain);
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error(e);
        }
    }

    private void doTask(Chain chain){
        if (packablePool.getPoolSize(chain) >= chain.getConfig().getTxUnverifiedQueueSize()) {
            return;
        }

        Transaction tx = null;
        long startTask = System.currentTimeMillis();
        int i = 0;
        while ((tx = unverifiedTxStorageService.pollTx(chain)) != null) {
            long start = System.currentTimeMillis();
            processTx(chain, tx, false);
            i++;
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("@@@@@@@@ 2 @@@@@@@@@@ one processTx:{}毫秒",System.currentTimeMillis()-start);
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("");
        }
        if(i>0) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("@@@@@@@@ 3 @@@@@@@@@@ one Task:{}毫秒, count:{}笔交易",
                    (System.currentTimeMillis() - startTask), i);
        }
    }

    private boolean processTx(Chain chain, Transaction tx, boolean isOrphanTx){
        try {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("新交易处理.....hash:{}", tx.getHash().getDigestHex());
            long s1 = System.currentTimeMillis();
            int chainId = chain.getChainId();
            //todo 跨链交易单独处理, 是否需要进行跨链验证？
            if (!txService.verify(chain, tx).getResult()) {
                return false;
            }
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("验证器花费时间:{}", System.currentTimeMillis() - s1);

            long get = System.currentTimeMillis();
            //获取一笔交易
            TransactionConfirmedPO existTx = txService.getTransaction(chain, tx.getHash());
            if(null != existTx){
                return isOrphanTx;
            }
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("交易获取花费时间:{}", System.currentTimeMillis() - get);
            long timeCoinData = System.currentTimeMillis();
            VerifyLedgerResult verifyLedgerResult = LedgerCall.commitUnconfirmedTx(chain, RPCUtil.encode(tx.serialize()));
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("验证CoinData花费时间:{}", System.currentTimeMillis() - timeCoinData);
            long s2 = System.currentTimeMillis();
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("交易验证阶段花费时间:{}", s2 - s1);
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("- - - - - -");
            if(verifyLedgerResult.success()){
                if(chain.getPackaging().get()) {
                    //当节点是出块节点时, 才将交易放入待打包队列
                    packablePool.add(chain, tx);
                    chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("交易[加入待打包队列].....");
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chainId, tx);

                //广播交易hash
                NetworkCall.broadcastTxHash(chain.getChainId(),tx.getHash());
                long s3 = System.currentTimeMillis();
                chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("交易保存阶段花费时间:{}", s3 - s2);
                return true;
            }
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug(
                    "coinData not success - code: {}, - reason:{}, type:{} - txhash:{}",
                    verifyLedgerResult.getCode(),  verifyLedgerResult.getDesc(), tx.getType(), tx.getHash().getDigestHex());
            if(verifyLedgerResult.getCode() == VerifyLedgerResult.ORPHAN && !isOrphanTx){
                processOrphanTx(tx);
            }else if(isOrphanTx){
                long currentTimeMillis = TimeUtils.getCurrentTimeMillis();
                return tx.getTime() < (currentTimeMillis - chain.getConfig().getOrphanTtl());
            }
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error(e);
            e.printStackTrace();
        }
        return false;
    }

    private void doOrphanTxTask(Chain chain) throws NulsException{
        if(orphanTxList.size() == 0){
            return;
        }
        try {
            //时间排序TransactionTimeComparator
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("处理孤儿交易 orphanTxList size:{}", orphanTxList.size());
            orphanTxList.sort(txComparator);
            Iterator<Transaction> it = orphanTxList.iterator();
            while (it.hasNext()) {
                Transaction tx = it.next();
                boolean success = processTx(chain, tx, true);
                if (success) {
                    it.remove();
                    chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("*** Debug *** [VerifyTxProcessTask - OrphanTx] " +
                            "OrphanTx remove - type:{} - txhash:{}, -orphanTxList size:{}", tx.getType(), tx.getHash().getDigestHex(), orphanTxList.size());
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
