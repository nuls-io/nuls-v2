package io.nuls.poc.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Component;

import java.util.List;
import java.util.Map;

@Component("DelayStopAgentProcessorV1")
public class DelayStopAgentProcessor implements TransactionProcessor {
    @Override
    public int getType() {
        return TxType.DELAY_STOP_AGENT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        //todo 判断节点已经停止，但是资产却未解锁
        //验证签名
        //验证coindata
        //不准重复
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
