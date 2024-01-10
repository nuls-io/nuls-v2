package io.nuls.transaction.rpc.upgrade;

import io.nuls.base.data.Transaction;
import io.nuls.common.CommonVersionChangeInvoker;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.service.TxService;

/**
 * @author: Charlie
 * @date: 2019/05/20
 */
public class TxVersionChangeInvoker implements VersionChangeInvoker {

    private static TxVersionChangeInvoker txVersionChangeInvoker = new TxVersionChangeInvoker();
    private TxVersionChangeInvoker() {}
    public static TxVersionChangeInvoker instance() {
        return txVersionChangeInvoker;
    }

    @Override
    public void process(int chainId) {
        /**
         * 1.Turn on the flag for protocol upgrade in progress
         *  ** All transactions that need to be reprocessed are processed through online transaction channels
         *  . Orphan transactions are placed back to the front of the unprocessed transaction queue in reverse order
         *  . Take out all the transactions in the pending packaging queue and put them back at the front of the unprocessed transaction queue
         *  . The flag in packaging is enabled and placed directly back at the top of the unprocessed transaction queue(Excluding non system transactions in smart contracts)
         * 2.Close flag after completion
         */
        ChainManager chainManager = SpringLiteContext.getBean(ChainManager.class);
        Chain chain = chainManager.getChain(chainId);
        if (null == chain) {
            Log.error(TxErrorCode.CHAIN_NOT_FOUND.getCode());
            return;
        }
        //Set upgrade flags,Suspend packaging transactions(Empty block)Suspend new transaction processing
        chain.getProtocolUpgrade().set(true);
        try {
            Thread.sleep(3000L);
            //Waiting for the ongoing transaction processing to end(Transactions during packaging process、New transactions)
            while (!chain.getCanProtocolUpgrade().get()) {
                chain.getLogger().info("GetCanProtocolUpgrade waiting, chainId:[{}]", chainId);
                Thread.sleep(100L);
            }
        } catch (InterruptedException e) {
            chain.getLogger().error(e);
        }

        //Process the queue to be packaged
        PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
        boolean hasNext = true;
        while (hasNext) {
            //Starting from the end of the team
            Transaction tx = packablePool.pollLast(chain);
            if (null != tx) {
                addBack(chain, tx);
            } else {
                hasNext = false;
            }
        }

        LedgerCall.clearUnconfirmTxs(chain);
        //Processing completed reset flag
        chain.getProtocolUpgrade().set(false);
        chain.getLogger().info("Version Change process, chainId:[{}]", chainId);
    }

    private void addBack(Chain chain, Transaction tx) {
        addBack(chain, new TransactionNetPO(tx));
    }

    /**
     * Add back to the new transaction queue
     *
     * @param chain
     * @param txNet
     */
    private void addBack(Chain chain, TransactionNetPO txNet) {
        try {
            Transaction tx = txNet.getTx();
            //Perform basic transaction verification
            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
            if (null == txRegister) {
                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
            }
            TxService txService = SpringLiteContext.getBean(TxService.class);
            txService.baseValidateTx(chain, tx, txRegister);
            chain.getUnverifiedQueue().addFirst(txNet);
        } catch (NulsException e) {
            chain.getLogger().warn("TxVersionChangeInvoker verify failed", e);
        }
    }

}
