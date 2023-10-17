package io.nuls.crosschain.utils.thread;

import io.nuls.base.data.Transaction;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.utils.TxUtil;

/**
 * 重置平行链上存储的主链验证人列表
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
