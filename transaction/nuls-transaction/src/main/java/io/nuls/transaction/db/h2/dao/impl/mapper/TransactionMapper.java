package io.nuls.transaction.db.h2.dao.impl.mapper;

import io.nuls.h2.common.BaseMapper;
import io.nuls.h2.utils.Searchable;
import io.nuls.transaction.model.po.TransactionsPO;
import io.nuls.transaction.model.split.TxTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public interface TransactionMapper extends BaseMapper<String, TransactionsPO> {

    void createTxTables(@Param("list") List<TxTable> list);

    int insert(@Param("txPo") TransactionsPO txPo, @Param("tableName") String tableName);

    int batchInsert(@Param("list") List<TransactionsPO> list, @Param("tableName") String tableName);

    List<TransactionsPO> getTxs(@Param("searchable") Searchable searchable, @Param("tableName") String tableName);

    long queryCount(@Param("searchable") Searchable searchable, @Param("tableName") String tableName);

    int delete(@Param("address")String address, @Param("hash")String hash, @Param("tableName") String tableName);
}
