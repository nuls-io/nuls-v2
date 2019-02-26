package io.nuls.transaction.db.h2.dao.impl;

import com.github.pagehelper.PageHelper;
import io.nuls.base.data.Page;
import io.nuls.h2.transactional.annotation.Transaction;
import io.nuls.h2.utils.MybatisDbHelper;
import io.nuls.h2.utils.SearchOperator;
import io.nuls.h2.utils.Searchable;
import io.nuls.tools.core.annotation.Service;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.h2.dao.TransactionService;
import io.nuls.transaction.db.h2.dao.impl.mapper.TransactionMapper;
import io.nuls.transaction.model.po.TransactionPO;
import io.nuls.transaction.model.split.TxTable;
import org.apache.ibatis.session.SqlSession;
import org.h2.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Override
    @Transaction
    public int saveTx(TransactionPO tx) {
        SqlSession sqlSession = MybatisDbHelper.get();
        String tableName = TxConstant.H2_TX_TABLE_NAME_PREFIX + tx.createTableIndex();
        int rs = sqlSession.getMapper(TransactionMapper.class).insert(tx, tableName);
        return rs;
    }

    @Override
    public Page<TransactionPO> getTxs(String address, Integer assetChainId, Integer assetId, Integer type, Integer state, Long startTime, Long endTime, int pageNumber, int pageSize) {
        Searchable searchable = new Searchable();
        if (!StringUtils.isNullOrEmpty(address)) {
            searchable.addCondition("address", SearchOperator.eq, address);
        }
        if (null != assetChainId) {
            searchable.addCondition("assetChainId", SearchOperator.eq, assetChainId);
        }
        if (null != assetId) {
            searchable.addCondition("assetId", SearchOperator.eq, assetId);
        }
        if (null != startTime) {
            searchable.addCondition("time", SearchOperator.gte, startTime);
        }
        if (null != endTime) {
            searchable.addCondition("time", SearchOperator.lte, endTime);
        }
        if (null != type) {
            searchable.addCondition("type", SearchOperator.eq, type);
        }
        if (null != state) {
            searchable.addCondition("state", SearchOperator.eq, state);
        }
        SqlSession sqlSession = MybatisDbHelper.get();
        TransactionMapper mapper = sqlSession.getMapper(TransactionMapper.class);
        String tableName = getTableName(address);
        long count = mapper.queryCount(searchable, tableName);
        if (count < (pageNumber - 1) * pageSize) {
            return new Page<>(pageNumber, pageSize);
        }
        //开启分页
        PageHelper.startPage(pageNumber, pageSize);
        PageHelper.orderBy(" address asc, time desc, type asc ");
        List<TransactionPO> list = mapper.getTxs(searchable, tableName);
        //sqlSession.commit();
        sqlSession.close();
        Page<TransactionPO> page = new Page<>();
        if (pageSize > 0) {
            page.setPageNumber(pageNumber);
            page.setPageSize(pageSize);
        } else {
            page.setPageNumber(1);
            page.setPageSize((int) count);
        }
        page.setTotal(count);
        page.setList(list);

        return page;
    }

    private String getTableName(String address) {
        int tabNumber = (address.hashCode() & Integer.MAX_VALUE) % TxConstant.H2_TX_TABLE_NUMBER;
        return TxConstant.H2_TX_TABLE_NAME_PREFIX + tabNumber;
    }

    @Override
    public void createTxTablesIfNotExists(String tableName, String indexName, String uniqueName, int number) {
        SqlSession sqlSession = MybatisDbHelper.get();
        TransactionMapper mapper = sqlSession.getMapper(TransactionMapper.class);
        List<TxTable> list = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            TxTable txTable = new TxTable(tableName + i, indexName + i, uniqueName + i);
            list.add(txTable);
        }
        mapper.createTxTables(list);
    }
}
