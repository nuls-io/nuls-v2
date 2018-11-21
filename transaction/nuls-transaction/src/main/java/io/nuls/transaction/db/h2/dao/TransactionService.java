package io.nuls.transaction.db.h2.dao;

import io.nuls.base.data.Page;
import io.nuls.transaction.model.po.TransactionPo;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public interface TransactionService {

    Page<TransactionPo> getTxs(String address, Integer type, Integer state,
                               Long startTime, Long endTime, int pageNumber, int pageSize);

    int saveTx(TransactionPo txPo);

    int saveTxs(List<TransactionPo> txPoList);

    int deleteTx(TransactionPo txPo);


    /**
     *
     *
     * 初始化创建存储交易的表
     * @param tableName  table name
     * @param indexName table index name
     * @param number number of tables 分表的数量
     */
    void createTable(String tableName, String indexName, int number);

    void createTxTables(String tableName, String indexName, int number);
}
