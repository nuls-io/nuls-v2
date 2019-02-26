package io.nuls.transaction.db.h2.dao;

import io.nuls.base.data.Page;
import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.po.TransactionPO;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public interface TransactionService {

    int saveTx(TransactionPO tx);

    Page<TransactionPO> getTxs(String address, Integer assetChainId, Integer assetId, Integer type, Integer state,
                               Long startTime, Long endTime, int pageNumber, int pageSize);

    void createTxTablesIfNotExists(String tableName, String indexName, String uniqueName, int number);
}
