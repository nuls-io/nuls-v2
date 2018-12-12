package io.nuls.transaction.db.h2.dao;

import io.nuls.base.data.Page;
import io.nuls.transaction.model.po.TransactionPO;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public interface TransactionH2Service {

    Page<TransactionPO> getTxs(String address, Integer type, Integer state,
                               Long startTime, Long endTime, int pageNumber, int pageSize);

    int saveTx(TransactionPO txPo);

    /**
     * 保存多个数据，组装单条插入语句，循环数据集合插入
     * @param txPoList
     * @return
     */
    int saveTxs(List<TransactionPO> txPoList);

    /**
     * 保存多个数据，按表组装批量插入语句，循环执行多个表的插入
     * @param txPoList
     * @return
     */
    int saveTxsTables(List<TransactionPO> txPoList);

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
