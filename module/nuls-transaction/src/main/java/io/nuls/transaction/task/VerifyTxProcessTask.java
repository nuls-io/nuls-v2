package io.nuls.transaction.task;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.message.handler.BroadcastTxMessageHandler;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyLedgerResult;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.threadpool.NetTxProcess;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 处理由其他节点广播的交易
 *
 * @author: Charlie
 * @date: 2018/11/28
 */
public class VerifyTxProcessTask implements Runnable {

    private final int processNumberonce = 2000;
    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);
    private Chain chain;
    private ExecutorService verifyExecutor = ThreadUtils.createThreadPool(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, new NulsThreadFactory(TxConstant.VERIFY_TX_THREAD));

    private NulsLogger logger;
    public VerifyTxProcessTask(Chain chain) {
        this.chain = chain;
        this.logger = chain.getLogger();
    }

    @Override
    public void run() {

        LOG.debug("累计接收完整新交易:{}", BroadcastTxMessageHandler.countRc.get());
        LOG.debug("网络交易加入待打包队列总数:{}", NetTxProcess.netTxToPackablePoolCount.get());
//        doTask();
//        try {
//            process();
//        } catch (RuntimeException e) {
//            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error(e);
//        }
    }

    /**
     * 优化待测
     *
     * @throws RuntimeException
     */
    private void process() throws RuntimeException {
        List<TransactionNetPO> txNetList = new ArrayList<>(processNumberonce);
        chain.getUnverifiedQueue().drainTo(txNetList, processNumberonce);
        if (txNetList.isEmpty()) {
            return;
        }
        Map<String, TransactionNetPO> txNetMap = new HashMap<>(processNumberonce);
        List<Transaction> txList = new LinkedList<>();
        List<Future<String>> futures = new ArrayList<>();
        for (TransactionNetPO txNet : txNetList) {
            Transaction tx = txNet.getTx();
            //多线程处理单个交易
            Future<String> res = verifyExecutor.submit(new Callable<String>() {
                @Override
                public String call() {
                    /**if(txService.isTxExists(chain, tx.getHash())){
                     return false;
                     }*/
                    if (!txService.verify(chain, tx).getResult()) {
                        logger.error("Net new tx verify fail.....hash:{}", tx.getHash().toHex());
                        return tx.getHash().toHex();
                    }
                    return null;
                }
            });
            futures.add(res);
            txList.add(tx);
            txNetMap.put(tx.getHash().toHex(), txNet);
        }
        txNetList = null;

        List<String> txFailList = new LinkedList<>();
        //多线程处理结果
        try {
            for (Future<String> future : futures) {
                if (null != future.get()) {
                    txFailList.add(future.get());
                }
            }
        } catch (InterruptedException e) {
            logger.error(e);
            return;
        } catch (ExecutionException e) {
            logger.error(e);
            return;
        }
        //有验证不通过的，则过滤掉
        if (!txFailList.isEmpty()) {
            Iterator<Transaction> it = txList.iterator();
            while (it.hasNext()) {
                Transaction tx = it.next();
                for (String hash : txFailList) {
                    if (hash.equals(tx.getHash().toHex())) {
                        it.remove();
                    }
                }
            }
        }

        if (txList.isEmpty()) {
            return;
        }
        try {
            verifyCoinData(chain, txList, txNetMap);
            for (Transaction tx : txList) {
                if (chain.getPackaging().get()) {
                    //当节点是出块节点时, 才将交易放入待打包队列
                    packablePool.add(chain, tx);
                    logger.debug("交易[加入待打包队列].....");
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
                //转发交易hash
                TransactionNetPO txNetPo = txNetMap.get(tx.getHash().toHex());
                NetworkCall.forwardTxHash(chain, tx.getHash(), txNetPo.getExcludeNode());
            }
        } catch (NulsException e) {
            logger.error("Net new tx process exception, -code:{}", e.getErrorCode().getCode());
        }


    }

    private void verifyCoinData(Chain chain, List<Transaction> txList, Map<String, TransactionNetPO> txNetMap) throws NulsException {
        try {
            Map verifyCoinDataResult = LedgerCall.commitBatchUnconfirmedTxs(chain, txList);
            List<String> failHashs = (List<String>) verifyCoinDataResult.get("fail");
            List<String> orphanHashs = (List<String>) verifyCoinDataResult.get("orphan");
            Iterator<Transaction> it = txList.iterator();
            while (it.hasNext()) {
                Transaction tx = it.next();
                //去除账本验证失败的交易
                for (String hash : failHashs) {
                    if (hash.equals(tx.getHash().toHex())) {
                        it.remove();
                        continue;
                    }
                }
                //去除孤儿交易, 同时把孤儿交易放入孤儿池
                for (String hash : orphanHashs) {
                    if (hash.equals(tx.getHash().toHex())) {
                        //孤儿交易
                        List<TransactionNetPO> chainOrphan = chain.getOrphanList();
                        synchronized (chainOrphan) {
                            chainOrphan.add(txNetMap.get(hash));
                        }
                        logger.debug("Net new tx coinData orphan, - type:{}, - txhash:{}",
                                tx.getType(), tx.getHash().toHex());
                        it.remove();
                        continue;
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    private void doTask() {
        TransactionNetPO tx = null;
        long s1 = System.currentTimeMillis();
        int count = 0;
        while ((tx = chain.getUnverifiedQueue().poll()) != null) {
            //chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("待处理队列交易数：{}", chain.getUnverifiedQueue().size());
            processTx(chain, tx);
            count++;
        }
        //chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("&&&&&&&&& time:{},&&&&&&&&&&&&& count:{}" ,(System.currentTimeMillis() - s1),count);
    }

    private void processTx(Chain chain, TransactionNetPO txNet) {
        long s1 = System.nanoTime();
        try {
            Transaction tx = txNet.getTx();
            int chainId = chain.getChainId();
            if (!txService.verify(chain, tx).getResult()) {
                logger.error("Net new tx verify fail.....hash:{}", tx.getHash().toHex());
                return;
            }
//            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("验证器花费时间:{}", System.currentTimeMillis() - s1);
//            long get = System.nanoTime();
//            if(txService.isTxExists(chain, tx.getHash())){
//                return;
//            }
            logger.debug("交易验证花费时间:{}", System.nanoTime() - s1);
            long timeCoinData = System.nanoTime();
            VerifyLedgerResult verifyLedgerResult = LedgerCall.commitUnconfirmedTx(chain, RPCUtil.encode(tx.serialize()));
            logger.debug("验证CoinData花费时间:{}", System.nanoTime() - timeCoinData);
            long s2 = System.nanoTime();
            if (verifyLedgerResult.businessSuccess()) {
                if (chain.getPackaging().get()) {
                    //当节点是出块节点时, 才将交易放入待打包队列
                    packablePool.add(chain, tx);
                    logger.debug("交易[加入待打包队列].....");
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chainId, tx);
                //转发交易hash
                NetworkCall.forwardTxHash(chain, tx.getHash(), txNet.getExcludeNode());
                long s3 = System.nanoTime();
                logger.debug("交易保存阶段花费时间:{}", s3 - s2);
                return;
            } else if (verifyLedgerResult.isOrphan()) {
                //孤儿交易
                List<TransactionNetPO> chainOrphan = chain.getOrphanList();
                synchronized (chainOrphan) {
                    chainOrphan.add(txNet);
                }
                logger.debug("Net new tx coinData orphan, - type:{}, - txhash:{}",
                        tx.getType(), tx.getHash().toHex());
                return;
            }
            logger.debug("Net new tx coinData fail - code:{}, type:{},  - txhash:{}",
                    verifyLedgerResult.getErrorCode() == null ? "" : verifyLedgerResult.getErrorCode().getCode(), tx.getType(), tx.getHash().toHex());
        } catch (Exception e) {
            logger.error(e);
        }
    }


}
