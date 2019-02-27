package io.nuls.transaction.db.h2.dao.impl;

import com.github.pagehelper.PageHelper;
import io.nuls.base.data.Page;
import io.nuls.base.data.Transaction;
import io.nuls.h2.utils.MybatisDbHelper;
import io.nuls.h2.utils.SearchOperator;
import io.nuls.h2.utils.Searchable;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.h2.dao.impl.mapper.TransactionMapper;
import io.nuls.transaction.model.po.TransactionPO;
import io.nuls.transaction.model.split.TxTable;
import io.nuls.transaction.utils.TxUtil;
import org.apache.ibatis.session.SqlSession;
import org.h2.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
@Service
public class TransactionH2ServiceImpl implements TransactionH2Service {

    @Override
    public Page<TransactionPO> getTxs(String address, Integer assetChainId, Integer assetId, Integer type, int pageNumber, int pageSize) {
        return getTxs(address, assetChainId, assetId, type, null, null, null, pageNumber, pageSize);
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
        SqlSession sqlSession = MybatisDbHelper.getSession();
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

    /**
     * 根据地址获取对应存储位置的表名
     *
     * @param address
     * @return
     */
    private String getTableName(String address) {
        int tabNumber = (address.hashCode() & Integer.MAX_VALUE) % TxConstant.H2_TX_TABLE_NUMBER;
        return TxConstant.H2_TX_TABLE_NAME_PREFIX + tabNumber;
    }


    @Override
    @io.nuls.h2.transactional.annotation.Transaction
    public int saveTx(TransactionPO txPo) {
        SqlSession sqlSession = MybatisDbHelper.getSession();
        String tableName = TxConstant.H2_TX_TABLE_NAME_PREFIX + txPo.createTableIndex();
        int rs = sqlSession.getMapper(TransactionMapper.class).insert(txPo, tableName);
        return rs;
    }


    private Map<String, List<TransactionPO>> assembleMap(List<TransactionPO> txPoList) {
        Map<String, List<TransactionPO>> map = new HashMap<>();
        for (TransactionPO txPo : txPoList) {
            String tableName = TxConstant.H2_TX_TABLE_NAME_PREFIX + txPo.createTableIndex();
            if (!map.containsKey(tableName)) {
                List<TransactionPO> list = new ArrayList<>();
                list.add(txPo);
                map.put(tableName, list);
            } else {
                map.get(tableName).add(txPo);
            }
        }
        return map;
    }

    @Override
    @io.nuls.h2.transactional.annotation.Transaction
    public int saveTxsTables(List<TransactionPO> txPoList) {
        SqlSession sqlSession = MybatisDbHelper.getSession();
        Map<String, List<TransactionPO>> map = assembleMap(txPoList);
        int rs = 0;
        for (Map.Entry<String, List<TransactionPO>> entry : map.entrySet()) {
            if (sqlSession.getMapper(TransactionMapper.class).batchInsert(entry.getValue(), entry.getKey()) == 1) {
                rs += entry.getValue().size();
            }
        }
        return rs;
    }

    @Override
    @io.nuls.h2.transactional.annotation.Transaction
    public int saveTxs(List<TransactionPO> txPoList) {
        SqlSession sqlSession = MybatisDbHelper.getSession();
        int rs = 0;
        for (TransactionPO txPo : txPoList) {
            String tableName = TxConstant.H2_TX_TABLE_NAME_PREFIX + txPo.createTableIndex();
            if (sqlSession.getMapper(TransactionMapper.class).insert(txPo, tableName) == 1) {
                rs++;
            }
        }
        return rs;
    }

    @Override
    @io.nuls.h2.transactional.annotation.Transaction
    public int deleteTx(String address, String txhash) {
        SqlSession sqlSession = MybatisDbHelper.getSession();
        int rs = sqlSession.getMapper(TransactionMapper.class).delete(address, txhash, getTableName(address));
        return rs;
    }

    @Override
    @io.nuls.h2.transactional.annotation.Transaction
    public int deleteTx(Transaction tx) {
        int count = 0;
        try {
            List<TransactionPO> list = TxUtil.tx2PO(tx);
            for (TransactionPO transactionPO : list) {
                count += deleteTx(transactionPO.getAddress(), transactionPO.getHash());
            }
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public void createTxTablesIfNotExists(String tableName, String indexName, String uniqueName, int number) {
        SqlSession sqlSession = MybatisDbHelper.getSession();
        TransactionMapper mapper = sqlSession.getMapper(TransactionMapper.class);
        List<TxTable> list = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            TxTable txTable = new TxTable(tableName + i, indexName + i, uniqueName + i);
            list.add(txTable);
        }
        mapper.createTxTables(list);
    }
}
