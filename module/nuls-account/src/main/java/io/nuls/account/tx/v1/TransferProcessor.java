package io.nuls.account.tx.v1;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChain(chainId);
        if (chain == null) {
            chain.getLogger().error(AccountErrorCode.CHAIN_NOT_EXIST.getCode());
            return txs;
        }
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : txs) {
            try {
                if (!transactionService.transferTxValidate(chain, tx)) {
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
