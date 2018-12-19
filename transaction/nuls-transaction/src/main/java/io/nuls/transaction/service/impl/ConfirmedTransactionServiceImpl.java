package io.nuls.transaction.service.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.TransactionStorageService;
import io.nuls.transaction.db.rocksdb.storage.TxVerifiedStorageService;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.rpc.call.TransactionCmdCall;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TransactionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/11/30
 */
@Service
public class ConfirmedTransactionServiceImpl implements ConfirmedTransactionService {

    @Autowired
    private TransactionStorageService transactionStorageService;

    @Autowired
    private TxVerifiedStorageService txVerifiedStorageService;

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private ChainManager chainManager;

    @Override
    public Transaction getTransaction(int chainId, NulsDigestData hash) {
        if (null == hash) {
            return null;
        }
        return transactionStorageService.getTx(chainId, hash);
    }

    @Override
    public boolean saveTx(int chainId, Transaction tx) {
        //check params
        if (chainId <= 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        return transactionStorageService.saveTx(chainId, tx);
    }

    @Override
    public boolean saveTxList(int chainId, List<byte[]> txHashList) {
        //check params
        if (chainId <= 0 || txHashList == null || txHashList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        //根据交易hash查询已验证交易数据
        List<Transaction> txList = txVerifiedStorageService.getTxList(chainId, txHashList);
        //将已验证交易保存到已确认交易
        boolean saveResult = transactionStorageService.saveTxList(chainId, txList);
        if (saveResult) {
            //如果保存到已确认交易成功，则删除已验证交易
            return txVerifiedStorageService.removeTxList(chainId, txHashList);
        }
        return false;
    }

    @Override
    public boolean rollbackTxList(int chainId, List<byte[]> txHashList) {
        //check params
        if (chainId <= 0 || txHashList == null || txHashList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        boolean rollback = false;
        //根据交易hash查询已确认交易数据
        List<Transaction> confirmedTxList = transactionStorageService.getTxList(chainId, txHashList);
        Chain chain = chainManager.getChain(chainId);
        for (Transaction tx : confirmedTxList) {
            TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
            Map params = new HashMap();
            params.put("chainId", chainId);
            try {
                params.put("txHex", HexUtil.encode(tx.serialize()));
            } catch (IOException e) {
                Log.error(e);
            }
            HashMap response = TransactionCmdCall.request(txRegister.getRollback(), txRegister.getModuleCode(), params);
            rollback = (Boolean) response.get("value");
        }
        if (rollback) {
            //如果回滚其他模块交易成功，则删除已确认交易
            rollback = transactionStorageService.removeTxList(chainId, txHashList);
        }
        return rollback;
    }

}
