package io.nuls.h2.dao.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.nuls.h2.dao.TransactionService;
import io.nuls.h2.dao.impl.mapper.TransactionMapper;
import io.nuls.h2.entity.TransactionPo;
import io.nuls.h2.utils.SearchOperator;
import io.nuls.h2.utils.Searchable;
import io.nuls.tools.core.annotation.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
@Service
public class TransactionServiceImpl extends BaseService<TransactionMapper> implements TransactionService {
    public TransactionServiceImpl() {
        super(TransactionMapper.class);
    }

    @Override
    public Page<TransactionPo> getTxs(String address, Integer type, Integer state, Long startTime, Long endTime, int pageNumber, int pageSize, String orderBy) {
        //数据库交易查询结果集
        List<TransactionPo> transactionList = new ArrayList<>();
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
    public int saveTx(TransactionPo txPo) {
        return this.getMapper().insert(txPo);
    }

    @Override
    public int saveTxs(List<TransactionPo> txPoList) {
        return 0;
    }

    @Override
    public int deleteTx(TransactionPo txPo) {
        return 0;
    }


    @Override
    public void createTable(String tableName, String indexName, int number) {
        for (int i = 0; i <= number; i++) {
            this.getMapper().createTable(tableName + "_" + i, indexName + "_" + i);
        }
    }
}
