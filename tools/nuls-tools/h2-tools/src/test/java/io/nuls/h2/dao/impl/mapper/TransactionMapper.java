package io.nuls.h2.dao.impl.mapper;

import io.nuls.h2.common.BaseMapper;
import io.nuls.h2.entity.TransactionPo;
import io.nuls.h2.entity.TxTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public interface TransactionMapper extends BaseMapper<String, TransactionPo> {

    void createTable(@Param("tableName") String tableName, @Param("indexName") String indexName);

    void createTxTables(@Param("list") List<TxTable> list);

    int save(TransactionPo transactionPo);
}
