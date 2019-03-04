package io.nuls.transaction.storage.h2.impl.mapper;

import io.nuls.h2.common.BaseMapper;
import io.nuls.h2.utils.Searchable;
import io.nuls.transaction.model.po.TransactionPO;
import io.nuls.transaction.model.split.TxTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public interface TransactionMapper extends BaseMapper<String, TransactionPO> {

    void createTxTables(@Param("list") List<TxTable> list);

    int insert(@Param("txPo") TransactionPO txPo, @Param("tableName") String tableName);

    int batchInsert(@Param("list") List<TransactionPO> list, @Param("tableName") String tableName);

    List<TransactionPO> getTxs(@Param("searchable") Searchable searchable, @Param("tableName") String tableName);

    long queryCount(@Param("searchable") Searchable searchable, @Param("tableName") String tableName);

    int delete(@Param("address")String address, @Param("hash")String hash, @Param("tableName") String tableName);
}
