package io.nuls.transaction.db.h2.dao.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import io.nuls.base.data.Page;
import io.nuls.h2.utils.SearchOperator;
import io.nuls.h2.utils.Searchable;
import io.nuls.tools.core.annotation.Service;
import io.nuls.transaction.constant.TransactionConstant;
import io.nuls.transaction.db.h2.dao.TransactionService;
import io.nuls.transaction.db.h2.dao.impl.mapper.TransactionMapper;
import io.nuls.transaction.model.po.TransactionPo;
import io.nuls.transaction.model.split.TxTable;
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
public class TransactionServiceImpl extends BaseService<TransactionMapper> implements TransactionService {



    @Override
    public Page<TransactionPo> getTxs(String address, Integer type, Integer state, Long startTime, Long endTime, int pageNumber, int pageSize) {
        //数据库交易查询结果集
        List<TransactionPo> transactionList = new ArrayList<>();
        Searchable searchable = new Searchable();
        if(!StringUtils.isNullOrEmpty(address)){
            searchable.addCondition("address", SearchOperator.eq, address);
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
        SqlSession sqlSession = sqlSessionFactory.openSession();
        TransactionMapper mapper = sqlSession.getMapper(TransactionMapper.class);
        String tableName = getTableName(address);
        long count = mapper.queryCount(searchable, tableName);
        if (count < (pageNumber - 1) * pageSize) {
            return new Page<>(pageNumber, pageSize);
        }
        //开启分页
        PageHelper.startPage(pageNumber, pageSize);
        PageHelper.orderBy(" address asc, time desc, type asc ");
        List<TransactionPo> list = mapper.getTxs(searchable, tableName);
        //sqlSession.commit();
        sqlSession.close();
        Page<TransactionPo> page = new Page<>();
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
     * @param address
     * @return
     */
    private String getTableName(String address){
        int tabNumber = (address.hashCode() & Integer.MAX_VALUE) % TransactionConstant.H2_TX_TABLE_NUMBER;
        return TransactionConstant.H2_TX_TABLE_NAME_PREFIX + tabNumber;
    }


    @Override
    public int saveTx(TransactionPo txPo) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        String tableName = TransactionConstant.H2_TX_TABLE_NAME_PREFIX + txPo.createTableIndex();
        int rs = sqlSession.getMapper(TransactionMapper.class).save(txPo, tableName);
        sqlSession.commit();
        sqlSession.close();
        return rs;
    }

    @Override
    public int saveTxs(List<TransactionPo> txPoList) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        int rs = 0;
        for (TransactionPo txPo : txPoList){
            String tableName = TransactionConstant.H2_TX_TABLE_NAME_PREFIX + txPo.createTableIndex();
            if(sqlSession.getMapper(TransactionMapper.class).save(txPo,tableName) == 1){
                rs++;
            }
        }
        sqlSession.commit();
        sqlSession.close();
        return rs;
    }

    @Override
    public int deleteTx(TransactionPo txPo) {
        return 0;
    }


    @Override
    public void createTable(String tableName, String indexName, int number) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        TransactionMapper mapper = sqlSession.getMapper(TransactionMapper.class);
        for (int i = 0; i <= number; i++) {
            mapper.createTable(tableName + i, indexName + i);
        }
        sqlSession.commit();
        sqlSession.close();
        System.out.println("OK");
    }

    @Override
    public void createTxTables(String tableName, String indexName, int number) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        TransactionMapper mapper = sqlSession.getMapper(TransactionMapper.class);
        List<TxTable> list = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            TxTable txTable = new TxTable(tableName + i, indexName + i);
            list.add(txTable);
        }
        mapper.createTxTables(list);
        sqlSession.commit();
        sqlSession.close();
        System.out.println("batch OK");
    }
}
