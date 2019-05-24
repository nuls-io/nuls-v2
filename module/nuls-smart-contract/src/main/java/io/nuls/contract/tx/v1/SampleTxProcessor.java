package io.nuls.contract.tx.v1;

import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.TransactionProcessor;

import java.util.List;

@Component("SampleTxProcessor")
public class SampleTxProcessor implements TransactionProcessor {
    @Override
    public int getType() {
        return 0;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, List<Transaction> allTxs, Object... objects) {
        return null;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, Object... objects) {
        return false;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, Object... objects) {
        return false;
    }
}
