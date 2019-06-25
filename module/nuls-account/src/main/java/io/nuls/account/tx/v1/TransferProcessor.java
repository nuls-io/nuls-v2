package io.nuls.account.tx.v1;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.util.ArrayList;
import java.util.HashMap;
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
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChain(chainId);
        Map<String, Object> result = new HashMap<>(AccountConstant.INIT_CAPACITY_4);
        String errorCode = null;
        if (chain == null) {
            errorCode = AccountErrorCode.CHAIN_NOT_EXIST.getCode();
            chain.getLogger().error("chain is not exist, -chainId:{}", chainId);
            result.put("txList", txs);
            result.put("errorCode", errorCode);
            return result;
        }
        List<Transaction> txList = new ArrayList<>();
        for (Transaction tx : txs) {
            try {
                Result rs =  transactionService.transferTxValidate(chain, tx);
                if (rs.isFailed()) {
                    errorCode = rs.getErrorCode().getCode();
                    txList.add(tx);
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
                txList.add(tx);
            }
        }
        result.put("txList", txs);
        result.put("errorCode", errorCode);

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
