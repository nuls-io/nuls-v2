package io.nuls.account.tx.v1;

import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.protocol.TransactionProcessor;

import java.util.List;

@Component("TransferImplV1")
public class TransferImpl implements TransactionProcessor {
    @Override
    public int getType() {
        return TxType.TRANSFER;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, List<Transaction> allTxs, Object... objects) {
        Log.info("validate v1");
        return List.of();
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, Object... objects) {
        Log.info("save v1");
        return false;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, Object... objects) {
        Log.info("rollback v1");
        return false;
    }

}
