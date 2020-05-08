package io.nuls.crosschain.nuls.utils.thread;

import io.nuls.base.data.Transaction;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.utils.TxUtil;

public class VerifierInitTxHandler implements Runnable {

    private Chain chain;
    private Transaction transaction;

    public VerifierInitTxHandler(Chain chain, Transaction transaction){
        this.chain = chain;
        this.transaction = transaction;
    }
    @Override
    public void run() {
        TxUtil.handleNewCtx(transaction,chain,null);
    }
}
