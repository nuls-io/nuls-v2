package io.nuls.transaction.service;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;

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
}
