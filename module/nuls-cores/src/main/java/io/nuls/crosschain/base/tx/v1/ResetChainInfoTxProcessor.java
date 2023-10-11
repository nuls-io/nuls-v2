package io.nuls.crosschain.base.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.service.ResetChainService;

import java.util.*;

@Component("ResetChainInfoTxProcessor")
public class ResetChainInfoTxProcessor implements TransactionProcessor {

    @Autowired
    private ResetChainService resetChainService;

    @Override
    public int getType() {
        return TxType.RESET_CHAIN_INFO;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return resetChainService.validate(chainId, txs, txMap, blockHeader);
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return resetChainService.commit(chainId, txs, blockHeader);
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return resetChainService.rollback(chainId, txs, blockHeader);
    }
}
