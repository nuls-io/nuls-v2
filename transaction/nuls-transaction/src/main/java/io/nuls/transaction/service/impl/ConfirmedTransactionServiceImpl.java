package io.nuls.transaction.service.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.TransactionStorageService;
import io.nuls.transaction.model.bo.TxWrapper;
import io.nuls.transaction.service.ConfirmedTransactionService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/30
 */
@Service
public class ConfirmedTransactionServiceImpl implements ConfirmedTransactionService {

    @Autowired
    private TransactionStorageService transactionStorageService;

    @Override
    public Transaction getTransaction(int chainId, NulsDigestData hash) {
        if (null == hash) {
            return null;
        }
        return transactionStorageService.getTx(chainId, hash);
    }

    @Override
    public boolean saveTx(int chainId, Transaction transaction) {
        //check params
        if (chainId <= 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        TxWrapper txWrapper = new TxWrapper(chainId, transaction);
        return transactionStorageService.saveTx(txWrapper);
    }

    @Override
    public boolean saveTxList(int chainId, List<String> txHashList) {
        //check params
        if (chainId <= 0 || txHashList == null || txHashList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        //todo 保存交易 rocksdb批量获取交易(加个方法) 再匹配
        return transactionStorageService.saveTxList(chainId, new ArrayList<>());
    }

}
