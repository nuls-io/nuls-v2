package io.nuls.account.tx.v1;

import io.nuls.base.basic.TransactionProcessor;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;

@Component("TransferImplV1")
public class TransferImpl implements TransactionProcessor {
    @Override
    public int getType() {
        return TxType.TRANSFER;
    }

    @Override
    public boolean validate(int chainId, Transaction transaction, Object... obj) {
        Log.info("validate v1");
        return false;
    }

    @Override
    public boolean save(int chainId, Transaction transaction, Object... obj) {
        Log.info("save v1");
        return false;
    }

    @Override
    public boolean rollback(int chainId, Transaction transaction, Object... obj) {
        Log.info("rollback v1");
        return false;
    }
}
