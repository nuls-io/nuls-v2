package io.nuls.transaction.service;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.basic.Result;

import java.util.List;

/**
 * 已确认交易的服务接口
 * @author: Charlie
 * @date: 2018/11/30
 */
public interface ConfirmedTransactionService {

    /**
     * get a transaction
     *
     * 获取一笔交易
     * @param chainId
     * @param hash
     * @return Transaction
     */
    Transaction getTransaction(int chainId, NulsDigestData hash);

    /**
     * 保存已确认交易
     * save confirmed transactions
     *
     * @param chainId
     * @param transaction
     * @return Result
     */
    boolean saveTx(int chainId, Transaction transaction);

    /**
     * 批量保存交易
     * @param chainId
     * @param txList
     * @return
     */
    boolean saveTxList(int chainId, List<Transaction> txList);
}
