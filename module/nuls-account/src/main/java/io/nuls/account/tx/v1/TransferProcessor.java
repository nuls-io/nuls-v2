package io.nuls.account.tx.v1;

import io.nuls.account.model.bo.Chain;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.TransactionProcessor;

import java.util.ArrayList;
import java.util.List;

@Component("TransferProcessorV1")
public class TransferProcessor implements TransactionProcessor {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TransactionService transactionService;
    @Override
    public int getType() {
        return TxType.TRANSFER;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, List<Transaction> allTxs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChain(chainId);
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : txs) {
            try {
                if (!transactionService.transferTxValidate(chain.getChainId(), tx)) {
                    result.add(tx);
                }
            } catch (Exception e) {
                chain.getLogger().error(e);
                result.add(tx);
            }
        }
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return true;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return true;
    }

}
