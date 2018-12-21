package io.nuls.transaction.service.impl;

import io.nuls.base.data.BlockHeaderDigest;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.TransactionStorageService;
import io.nuls.transaction.db.rocksdb.storage.TxVerifiedStorageService;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.rpc.call.TransactionCall;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.utils.TransactionIndexComparator;

import java.io.IOException;
import java.util.*;

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

    @Autowired
    private TransactionIndexComparator txIndexComparator;

    @Override
    public Transaction getTransaction(Chain chain, NulsDigestData hash) {
        if (null == hash) {
            return null;
        }
        return transactionStorageService.getTx(chain.getChainId(), hash);
    }

    @Override
    public boolean saveTx(Chain chain, Transaction tx) {
        //check params
        if (null == tx) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        return transactionStorageService.saveTx(chain.getChainId(), tx);
    }

    @Override
    public boolean saveTxList(Chain chain, List<NulsDigestData> txHashList, BlockHeaderDigest blockHeaderDigest) throws NulsException{

        if (null == chain || txHashList == null || txHashList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        try {
            int chainId = chain.getChainId();
            List<Transaction> txList = new ArrayList<>();
            List<byte[]> txHashs = new ArrayList<>();
            for(int i=0; i<txHashList.size(); i++){
                NulsDigestData hash = txHashList.get(i);
                txHashs.add(hash.serialize());
                //从已验证但未打包的交易中取出交易
                Transaction tx = txVerifiedStorageService.getTx(chainId, hash);
                if(null == tx){
                    throw new NulsException(TxErrorCode.TX_NOT_EXIST);
                }
                //设置交易在block中的顺序
                tx.setInBlockIndex(i);
                txList.add(tx);
            }
            //将已验证交易保存到DB已确认交易中
            boolean saveResult = transactionStorageService.saveTxList(chainId, txList);
            if (!saveResult) {
                throw new NulsException(TxErrorCode.SAVE_TX_ERROR);
            }
            for(Transaction tx : txList) {
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                boolean rs = TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx.hex());
                if(!rs){
                    //失败则回滚已保存的交易
                    transactionStorageService.removeTxList(chainId, txHashs);
                    throw new NulsException(TxErrorCode.TX_COMMIT_FAIL);
                }
            }
            //如果确认交易成功，则从未打包交易库中删除交易
            txVerifiedStorageService.removeTxList(chainId, txHashs);
            return true;
        } catch (IOException e) {
            throw new NulsException(TxErrorCode.IO_ERROR);
        } catch (NulsException e) {
            throw new NulsException(e.getErrorCode());
        }catch (Exception e) {
            throw new NulsException(e);
        }
    }

   /* @Override
    public boolean rollbackTxList(Chain chain, List<NulsDigestData> txHashList, BlockHeaderDigest blockHeaderDigest) throws NulsException {
        if (null == chain || txHashList == null || txHashList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        try {
            int chainId = chain.getChainId();
            List<Transaction> txList = new ArrayList<>();
            List<byte[]> txHashs = new ArrayList<>();
            for(int i=0; i<txHashList.size(); i++){
                NulsDigestData hash = txHashList.get(i);
                txHashs.add(hash.serialize());
               *//* //从已验证但未打包的交易中取出交易
                Transaction tx = txVerifiedStorageService.getTx(chainId, hash);
                if(null == tx){
                    throw new NulsException(TxErrorCode.TX_NOT_EXIST);
                }
                //设置交易在block中的顺序
                tx.setInBlockIndex(i);
                txList.add(tx);*//*
            }
            List<Transaction> confirmedTxList = transactionStorageService.getTxList(chainId, txHashs);
            //调回滚

            //将已验证交易保存到DB已确认交易中
            boolean saveResult = transactionStorageService.saveTxList(chainId, txList);
            if (!saveResult) {
                throw new NulsException(TxErrorCode.SAVE_TX_ERROR);
            }
            for(Transaction tx : txList) {
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                boolean rs = TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx.hex());
                if(!rs){
                    //失败则回滚已保存的交易
                    transactionStorageService.removeTxList(chainId, txHashs);
                    throw new NulsException(TxErrorCode.TX_COMMIT_FAIL);
                }
            }
            //如果确认交易成功，则从未打包交易库中删除交易
            txVerifiedStorageService.removeTxList(chainId, txHashs);
            return true;
        } catch (IOException e) {
            throw new NulsException(TxErrorCode.IO_ERROR);
        } catch (NulsException e) {
            throw new NulsException(e.getErrorCode());
        }catch (Exception e) {
            throw new NulsException(e);
        }




        int chainId = chain.getChainId();
        //check params
        if (chainId <= 0 || txHashList == null || txHashList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        boolean rollback = false;
        //根据交易hash查询已确认交易数据
        List<Transaction> confirmedTxList = transactionStorageService.getTxList(chainId, txHashList);
        for (Transaction tx : confirmedTxList) {
            TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
            Map params = new HashMap();
            params.put("chainId", chainId);
            try {
                params.put("txHex", HexUtil.encode(tx.serialize()));
            } catch (IOException e) {
                Log.error(e);
            }
            HashMap response = (HashMap)TransactionCall.request(txRegister.getRollback(), txRegister.getModuleCode(), params);
            rollback = (Boolean) response.get("value");
        }
        if (rollback) {
            //如果回滚其他模块交易成功，则删除已确认交易
            rollback = transactionStorageService.removeTxList(chainId, txHashList);
        }
        return rollback;
    }
*/
}
