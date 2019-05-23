package io.nuls.account.tx.v2;

import io.nuls.base.basic.TransactionProcessor;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;

@Component("TransferImplV2")
public class TransferImpl extends io.nuls.account.tx.v1.TransferImpl implements TransactionProcessor {
    @Override
    public boolean validate(int chainId, Transaction transaction, Object... obj) {
        Log.info("validate v2");
        return false;
    }

}
