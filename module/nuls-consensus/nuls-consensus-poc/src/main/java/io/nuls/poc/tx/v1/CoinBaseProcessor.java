package io.nuls.poc.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Component;

import java.util.List;
import java.util.Map;

/**
 * CoinBase交易处理器
 * @author tag
 * @date 2019/6/1
 */
@Component("CoinBaseProcessorV1")
public class CoinBaseProcessor implements TransactionProcessor {
    @Override
    public int getType() {
        return TxType.COIN_BASE;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return null;
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
