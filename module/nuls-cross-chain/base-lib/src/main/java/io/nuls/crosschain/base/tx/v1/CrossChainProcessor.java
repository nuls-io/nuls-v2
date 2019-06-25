package io.nuls.crosschain.base.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.service.CrossChainService;

import java.util.List;
import java.util.Map;

/**
 * 跨链交易处理类
 * Cross chain transaction Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("CrossChainProcessorV1")
public class CrossChainProcessor implements TransactionProcessor {

    @Autowired
    private CrossChainService crossChainService;

    @Override
    public int getType() {
        return crossChainService.getCrossChainTxType();
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return crossChainService.crossTxBatchValid(chainId, txs, txMap, blockHeader);
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return crossChainService.commitCrossTx(chainId, txs, blockHeader);
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return crossChainService.commitCrossTx(chainId, txs, blockHeader);
    }
}
