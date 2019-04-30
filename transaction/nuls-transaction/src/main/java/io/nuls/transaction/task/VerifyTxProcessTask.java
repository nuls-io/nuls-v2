package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyLedgerResult;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;

import java.util.List;

/**
 * 处理由其他节点广播的交易
 * @author: Charlie
 * @date: 2018/11/28
 */
public class VerifyTxProcessTask implements Runnable {

    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);

    private Chain chain;

    public VerifyTxProcessTask(Chain chain){
        this.chain = chain;
    }

    @Override
    public void run() {
        doTask(chain);
    }

    private void doTask(Chain chain){
        TransactionNetPO tx = null;
        long s1 = System.currentTimeMillis();
        int count = 0;
        while ((tx = chain.getUnverifiedQueue().poll()) != null) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("待处理队列交易数：{}", chain.getUnverifiedQueue().size());
            processTx(chain, tx);
            count++;
        }
        chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("&&&&&&&&& time:{},&&&&&&&&&&&&& count:{}" ,
                (System.currentTimeMillis() - s1),count);
    }

    private void processTx(Chain chain, TransactionNetPO txNet){
        long s1 = System.nanoTime();
        try {
            Transaction tx = txNet.getTx();
            int chainId = chain.getChainId();
            if (!txService.verify(chain, tx).getResult()) {
                chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error("Net new tx verify fail.....hash:{}", tx.getHash().getDigestHex());
                return;
            }
//            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("验证器花费时间:{}", System.currentTimeMillis() - s1);
//            long get = System.nanoTime();
            if(txService.isTxExists(chain, tx.getHash())){
                return;
            }
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("交易验证花费时间:{}", System.nanoTime() - s1);
            long timeCoinData = System.nanoTime();
            VerifyLedgerResult verifyLedgerResult = LedgerCall.commitUnconfirmedTx(chain, RPCUtil.encode(tx.serialize()));
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("验证CoinData花费时间:{}", System.nanoTime() - timeCoinData);
            long s2 = System.nanoTime();
            if(verifyLedgerResult.businessSuccess()){
                if(chain.getPackaging().get()) {
                    //当节点是出块节点时, 才将交易放入待打包队列
                    packablePool.add(chain, tx);
                    chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("交易[加入待打包队列].....");
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chainId, tx);
                //转发交易hash
                NetworkCall.forwardTxHash(chain.getChainId(),tx.getHash(), txNet.getExcludeNode());
                long s3 = System.nanoTime();
                chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("交易保存阶段花费时间:{}", s3 - s2);
                return;
            }else if(verifyLedgerResult.isOrphan()){
                //孤儿交易
                List<TransactionNetPO> chainOrphan = chain.getOrphanList();
                synchronized (chainOrphan){
                    chainOrphan.add(txNet);
                }
                chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("Net new tx coinData orphan, - type:{}, - txhash:{}",
                        tx.getType(), tx.getHash().getDigestHex());
                return;
            }
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("Net new tx coinData fail - code:{}, type:{},  - txhash:{}",
                    verifyLedgerResult.getErrorCode() == null ? "" : verifyLedgerResult.getErrorCode().getCode(), tx.getType(), tx.getHash().getDigestHex());
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error(e);
        }
    }


}
