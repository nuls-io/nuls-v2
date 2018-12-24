package io.nuls.transaction.service;

import io.nuls.base.data.BlockHeaderDigest;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;

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
     * @param chain
     * @param hash
     * @return Transaction
     */
    Transaction getTransaction(Chain chain, NulsDigestData hash);

    /**
     * 保存已确认交易
     * save confirmed transactions
     *
     * @param chain
     * @param transaction
     * @return Result
     */
    boolean saveTx(Chain chain, Transaction transaction);

    /**
     * 批量保存已确认交易
     * @param chain
     * @param txHashList
     * @return
     */
    boolean saveTxList(Chain chain, List<NulsDigestData> txHashList, BlockHeaderDigest blockHeaderDigest) throws NulsException;

    /**
     * 批量回滚已确认交易
     * @param chain
     * @param txHashList
     * @return
     */
//    boolean rollbackTxList(Chain chain, List<NulsDigestData> txHashList, BlockHeaderDigest blockHeaderDigest) throws NulsException;
}
