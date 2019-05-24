package io.nuls.core.rpc.protocol;

import io.nuls.base.data.Transaction;

import java.util.List;

/**
 * 交易处理器
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/23 17:43
 */
public interface TransactionProcessor {

    int getType();

    /**
     * 验证接口
     *
     * @param chainId
     * @param txs
     * @param allTxs
     * @param objects
     * @return 未通过验证得交易，需要丢弃
     */
    List<Transaction> validate(int chainId, List<Transaction> txs, List<Transaction> allTxs, Object... objects);

    /**
     * 提交接口
     *
     * @param chainId
     * @param txs
     * @param objects
     * @return
     */
    boolean commit(int chainId, List<Transaction> txs, Object... objects);

    /**
     * 回滚接口
     *
     * @param chainId
     * @param txs
     * @param objects
     * @return
     */
    boolean rollback(int chainId, List<Transaction> txs, Object... objects);

}
