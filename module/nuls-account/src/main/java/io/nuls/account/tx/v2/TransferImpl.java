package io.nuls.account.tx.v2;

import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.protocol.TransactionProcessor;

import java.util.List;

@Component("TransferImplV2")
public class TransferImpl extends io.nuls.account.tx.v1.TransferImpl implements TransactionProcessor {

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, List<Transaction> allTxs, Object... objects) {
        Log.info("validate v2");
        return List.of();
    }
}
