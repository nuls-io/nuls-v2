package io.nuls.account.tx.v2;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;

import java.util.List;
import java.util.Map;

@Component("TransferProcessorV2")
public class TransferProcessor extends io.nuls.account.tx.v1.TransferProcessor implements TransactionProcessor {

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Log.info("TransferProcessorV2 take effect");
        return super.validate(chainId, txs, txMap, blockHeader);
    }
}
