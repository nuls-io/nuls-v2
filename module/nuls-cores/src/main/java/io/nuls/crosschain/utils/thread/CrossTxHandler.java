package io.nuls.crosschain.utils.thread;

import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.utils.TxUtil;

public class CrossTxHandler implements Runnable {
    private Chain chain;
    private Transaction transaction;
    private int syncStatus;

    public CrossTxHandler(Chain chain, Transaction transaction,int syncStatus){
        this.chain = chain;
        this.transaction = transaction;
        this.syncStatus = syncStatus;
    }

    @Override
    public void run() {
        /*
         * 1.If during the synchronization process, sign and broadcast, return
         * 2.Handling Cross Chain Transactions
         * */
        if(syncStatus == 0){
            TxUtil.signAndBroad(chain, transaction);
            return;
        }
        if(transaction.getType() == TxType.CROSS_CHAIN){
            TxUtil.localCtxByzantine(transaction,chain);
        }else{
            TxUtil.handleNewCtx(transaction,chain,null);
        }
    }
}
