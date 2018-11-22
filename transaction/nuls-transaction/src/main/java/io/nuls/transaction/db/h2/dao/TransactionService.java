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

    int saveTxsTables(List<TransactionPo> txPoList);

    int deleteTx(String address, String txhash);

    /**
     * 初始化创建存储账户交易的表
     * 主要为了存储账户和交易的关系,
     * 采用分表机制，创建表同时创建联合索引(address,time,type)主要用于查询,
     * 以及唯一索引(address,hash)主要用于删除.
     *
     * @param tableName  table name
     * @param indexName table index name
     * @param number number of tables 分表的数量
     */
    void createTxTables(String tableName, String indexName, String uniqueName, int number);
}
