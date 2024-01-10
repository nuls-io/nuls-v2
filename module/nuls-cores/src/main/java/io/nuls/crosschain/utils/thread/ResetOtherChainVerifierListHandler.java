package io.nuls.crosschain.utils.thread;

import io.nuls.base.data.Transaction;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.utils.TxUtil;

/**
 * Reset the main chain validator list stored on the parallel chain
 */
public class ResetOtherChainVerifierListHandler implements Runnable {
    private Chain chain;
    private Transaction transaction;
    private int syncStatus;

    public ResetOtherChainVerifierListHandler(Chain chain, Transaction transaction, int syncStatus){
        this.chain = chain;
        this.transaction = transaction;
        this.syncStatus = syncStatus;
    }

    @Override
    public void run() {
        if(syncStatus == 0){
            TxUtil.signAndBroad(chain, transaction);
            return;
        }
        TxUtil.handleResetOtherVerifierListCtx(transaction,chain);
    }
}
