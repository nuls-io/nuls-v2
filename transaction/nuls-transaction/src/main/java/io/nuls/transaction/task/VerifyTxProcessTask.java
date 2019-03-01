package io.nuls.transaction.task;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.rocksdb.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.UnverifiedTxStorageService;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyTxResult;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.utils.TransactionTimeComparator;
import io.nuls.transaction.utils.TxUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.nuls.transaction.utils.LoggerUtil.Log;

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
//            System.out.println("count: " + count + " , size : " + size + " , orphan size : " + orphanTxList.size());
        }
    }

    private boolean processTx(Chain chain, Transaction tx, boolean isOrphanTx){
        try {
            chain.getLogger().debug("*** Debug *** [VerifyTxProcessTask] type:[{}], txhash:{}, ", tx.getType(), tx.getHash());
            CoinData coinData = TxUtil.getCoinData(tx);
            for(CoinFrom coinFrom : coinData.getFrom()){
                chain.getLogger().debug("*** Debug *** address:{}, nonce:{}, ", AddressTool.getChainIdByAddress(coinFrom.getAddress()), HexUtil.encode(coinFrom.getNonce()));
            }
            chain.getLogger().debug("");

            int chainId = chain.getChainId();
            boolean rs = transactionManager.verify(chain, tx);
            //todo 跨链交易单独处理, 是否需要进行跨链验证？
            //只会有本地创建的跨链交易才会进入这里, 其他链广播到跨链交易, 由其他逻辑处理
            if (!rs) {
                return false;
            }
            //获取一笔交易
            Transaction existTx = txService.getTransaction(chain, tx.getHash());
            if(null != existTx){
                return isOrphanTx;
            }
            VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, tx, false);
            if(verifyTxResult.success()){
                if(chain.getPackaging().get()) {
                    //当节点是出块节点时, 才将交易放入待打包队列
                    packablePool.add(chain, tx, false);
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chainId, tx);
                //保存到h2数据库
                transactionH2Service.saveTxs(TxUtil.tx2PO(tx));
                //调账本记录未确认交易
                LedgerCall.commitUnconfirmedTx(chain, tx.hex());
                //广播交易hash
                NetworkCall.broadcastTxHash(chain.getChainId(),tx.getHash());
                count++;
                return true;
            }
            chain.getLogger().debug("\n@@@@@@@@@@@@@@@@@");
            chain.getLogger().debug("*** Debug *** [VerifyTxProcessTask] " +
                    "coinData not success - code: {}, - reason:{}, type:{} - txhash:{}", verifyTxResult.getCode(),  verifyTxResult.getDesc(), tx.getType(), tx.getHash().getDigestHex());
            chain.getLogger().debug("@@@@@@@@@@@@@@@@@\n");
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
                    LedgerCall.rollBackUnconfirmTx(chain, tx.hex());
                    it.remove();
                    chain.getLogger().debug("*** Debug *** [VerifyTxProcessTask - OrphanTx] " +
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
