package io.nuls.base.basic;

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
     * @param transaction
     * @param objects
     * @return
     */
    FailedTxs verifyTxs(int chainId, List<Transaction> txs,List<Transaction> allTxs, Object... objects);

    /**
     * 保存接口
     *
     * @param chainId
     * @param transaction
     * @param objects
     * @return
     */
    boolean commit(int chainId, Transaction transaction, Object... objects);

    /**
     * 回滚接口
     *
     * @param chainId
     * @param transaction
     * @param objects
     * @return
     */
    boolean rollback(int chainId, Transaction transaction, Object... objects);

}
