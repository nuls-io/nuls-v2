package io.nuls.h2.dao.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.nuls.h2.dao.TransactionService;
import io.nuls.h2.dao.impl.mapper.TransactionMapper;
import io.nuls.h2.entity.TransactionPO;
import io.nuls.h2.entity.TxTable;
import io.nuls.h2.utils.SearchOperator;
import io.nuls.h2.utils.Searchable;
import io.nuls.tools.core.annotation.Service;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
@Service
public class TransactionServiceImpl extends BaseService<TransactionMapper> implements TransactionService {

    @Override
    public Page<TransactionPO> getTxs(String address, Integer type, Integer state, Long startTime, Long endTime, int pageNumber, int pageSize, String orderBy) {
        //数据库交易查询结果集
        List<TransactionPO> transactionList = new ArrayList<>();
        Searchable searchable = new Searchable();
        if (null != type) {
            searchable.addCondition("type", SearchOperator.eq, type);
        }
        if (null != state) {
            searchable.addCondition("state", SearchOperator.eq, state);
        }

        //开启分页
        PageHelper.startPage(pageNumber, pageSize);


        return null;
    }

    @Override
    public int saveTx(TransactionPO txPo) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        int rs = sqlSession.getMapper(TransactionMapper.class).save(txPo);
        sqlSession.commit();
        sqlSession.close();
        return rs;
    }

    @Override
    public int saveTxs(List<TransactionPO> txPoList) {
        return 0;
    }

    @Override
    public int deleteTx(TransactionPO txPo) {
        return 0;
    }


    @Override
    public void createTable(String tableName, String indexName, int number) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        TransactionMapper mapper = sqlSession.getMapper(TransactionMapper.class);
        for (int i = 0; i <= number; i++) {
            mapper.createTable(tableName + "_" + i, indexName + "_" + i);
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
        for (int i = 0; i <= number; i++) {
            TxTable txTable = new TxTable(tableName + "_" + i, indexName + "_" + i);
            list.add(txTable);
        }
        mapper.createTxTables(list);
        sqlSession.commit();
        sqlSession.close();
        System.out.println("batch OK");
    }
}
