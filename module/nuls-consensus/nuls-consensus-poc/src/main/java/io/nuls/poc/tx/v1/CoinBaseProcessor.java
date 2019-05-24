package io.nuls.poc.tx.v1;

import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.TransactionProcessor;

import java.util.List;

@Component("CoinBaseProcessorV1")
public class CoinBaseProcessor implements TransactionProcessor {
    @Override
    public int getType() {
        return TxType.COIN_BASE;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, List<Transaction> allTxs, Object... objects) {
        return List.of();
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, Object... objects) {
        return true;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, Object... objects) {
        return true;
    }
}
