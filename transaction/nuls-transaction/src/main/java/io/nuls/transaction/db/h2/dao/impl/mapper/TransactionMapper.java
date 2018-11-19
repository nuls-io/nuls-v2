package io.nuls.transaction.db.h2.dao.impl.mapper;

import io.nuls.h2.common.BaseMapper;
import io.nuls.transaction.model.po.TransactionPo;
import io.nuls.transaction.model.split.TxTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public interface TransactionMapper extends BaseMapper<String, TransactionPo> {

    void createTable(@Param("tableName") String tableName, @Param("indexName") String indexName);

    void createTxTables(@Param("list") List<TxTable> list);

    int save(@Param("txPo") TransactionPo txPo, @Param("tableName") String tableName);
}
