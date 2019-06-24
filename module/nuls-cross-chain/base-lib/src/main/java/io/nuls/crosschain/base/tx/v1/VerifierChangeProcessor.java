package io.nuls.crosschain.base.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.service.CrossChainService;
import io.nuls.crosschain.base.service.VerifierChangeTxService;

import java.util.List;
import java.util.Map;

/**
 * 验证人变更处理类
 * Verifier Change Handling Class
 *
 * @author tag
 * 2019/5/20
 */
@Component("VerifierChangeProcessorV1")
public class VerifierChangeProcessor implements TransactionProcessor {
    @Autowired
    private VerifierChangeTxService verifierChangeTxService;

    @Override
    public int getType() {
        return TxType.VERIFIER_CHANGE;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return verifierChangeTxService.validate(chainId, txs, txMap, blockHeader);
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return verifierChangeTxService.commit(chainId, txs, blockHeader);
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return verifierChangeTxService.rollback(chainId, txs, blockHeader);
    }
}
