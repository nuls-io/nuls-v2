package io.nuls.crosschain.base.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.service.RegisteredChainChangeService;

import java.util.List;
import java.util.Map;

@Component("RegisteredChainChangeProcessorV1")
public class RegisteredChainChangeProcessor implements TransactionProcessor {
    @Autowired
    private RegisteredChainChangeService service;

    @Override
    public int getType() {
        return TxType.REGISTERED_CHAIN_CHANGE;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return service.validate(chainId, txs, txMap, blockHeader);
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return service.commit(chainId, txs, blockHeader);
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return service.rollback(chainId, txs, blockHeader);
    }
}
