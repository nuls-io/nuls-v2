package io.nuls.transaction.db.h2.dao.impl.mapper;

import io.nuls.h2.common.BaseMapper;
import io.nuls.h2.utils.Searchable;
import io.nuls.transaction.model.po.TransactionPo;
import io.nuls.transaction.model.split.TxTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public interface TransactionMapper extends BaseMapper<String, TransactionPo> {

    void createTable(@Param("tableName") String tableName, @Param("indexName") String indexName);

    void createTxTables(@Param("list") List<TxTable> list);

    int save(@Param("txPo") TransactionPo txPo, @Param("tableName") String tableName);

    List<TransactionPo> getTxs(@Param("searchable") Searchable searchable, @Param("tableName") String tableName);

    long queryCount(@Param("searchable") Searchable searchable, @Param("tableName") String tableName);
}
