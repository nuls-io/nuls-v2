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
import io.nuls.transaction.rpc.call.LegerCall;
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
    public boolean saveTxList(Chain chain, List<NulsDigestData> txHashList, BlockHeaderDigest blockHeaderDigest) throws NulsException {

        if (null == chain || txHashList == null || txHashList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        try {
            int chainId = chain.getChainId();
            List<Transaction> savedList = new ArrayList<>();
            List<byte[]> txHashs = new ArrayList<>();
            for (int i = 0; i < txHashList.size(); i++) {
                NulsDigestData hash = txHashList.get(i);
                txHashs.add(hash.serialize());
                //从已验证但未打包的交易中取出交易
                Transaction tx = txVerifiedStorageService.getTx(chainId, hash);
                if (null == tx) {
                    throw new NulsException(TxErrorCode.TX_NOT_EXIST);
                }
                //保存交易
//                boolean rs = transactionStorageService.saveTx(chainId, tx);
//                if (rs) {
//                    //执行交易commit
//                    TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
//                    rs = TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx.hex());
//                    if (!rs) {
//                        //提交失败，之前删除当前交易
//                        transactionStorageService.removeTx(chainId, tx.getHash());
//                        chain.getLogger().error(tx.getHash().getDigestHex() + TxErrorCode.TX_COMMIT_FAIL);
//                    }else {
//                        //发送给账本模块
//                        rs = LegerCall.sendTx(chain.getChainId(), tx, true);
//                        if (!rs) {
//                            transactionStorageService.removeTx(chainId, tx.getHash());
//                            chain.getLogger().error(tx.getHash().getDigestHex() + TxErrorCode.TX_COMMIT_FAIL);
//                        }
//                    }
//
//                }
                boolean rs = saveCommitTx(chain, tx);
                if (rs) {
                    savedList.add(tx);
                } else {
                    this.rollbackTxList(chain, savedList, blockHeaderDigest, false);
                    throw new NulsException(TxErrorCode.SAVE_TX_ERROR);
                }
            }
            //如果确认交易成功，则从未打包交易库中删除交易
            txVerifiedStorageService.removeTxList(chainId, txHashs);
            return true;
        } catch (NulsException e) {
            throw new NulsException(e.getErrorCode());
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    private boolean saveCommitTx(Chain chain, Transaction tx) throws Exception{
        boolean rs = transactionStorageService.saveTx(chain.getChainId(), tx);
        if(!rs){
            return false;
        }
        TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
        rs = TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx.hex());
        if (!rs) {
            //提交失败，之前删除当前交易
            transactionStorageService.removeTx(chain.getChainId(), tx.getHash());
            chain.getLogger().error(tx.getHash().getDigestHex() + TxErrorCode.TX_COMMIT_FAIL);
            return false;
        }
        rs = LegerCall.sendTx(chain.getChainId(), tx, true);
        if (!rs) {
            transactionStorageService.removeTx(chain.getChainId(), tx.getHash());
            chain.getLogger().error(tx.getHash().getDigestHex() + TxErrorCode.TX_COMMIT_FAIL);
        }
        return rs;


       /* if (rs) {
            //执行交易commit
            TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
            rs = TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx.hex());
            if (!rs) {
                //提交失败，之前删除当前交易
                transactionStorageService.removeTx(chain.getChainId(), tx.getHash());
                chain.getLogger().error(tx.getHash().getDigestHex() + TxErrorCode.TX_COMMIT_FAIL);
            }else {
                //发送给账本模块

            }

        }*/
    }

    private boolean rollbackTxList(Chain chain, List<Transaction> savedList, BlockHeaderDigest blockHeaderDigest, boolean atomicity) throws NulsException {
        if (null == chain || savedList == null || savedList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        try {
            List<Transaction> rollbackedList = new ArrayList<>();
            for (int i = savedList.size() - 1; i >= 0; i--) {
                Transaction tx = savedList.get(i);
                //执行交易rollback
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                boolean rs = TransactionCall.txProcess(chain, txRegister.getRollback(), txRegister.getModuleCode(), tx.hex());
                if (atomicity) {
                    if (!rs) {
                        break;
                    } else {
                        rollbackedList.add(tx);
                    }
                }
            }
            //有回滚失败的 重新commit
            if (atomicity && savedList.size() != rollbackedList.size()) {
                for (int i = rollbackedList.size() - 1; i >= 0; i--) {
                    Transaction tx = rollbackedList.get(i);
                    TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                    TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx.hex());
                }
                return false;
            }
            return true;
        } catch (NulsException e) {
            throw new NulsException(e.getErrorCode());
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    @Override
    public boolean rollbackTxList(Chain chain, List<NulsDigestData> txHashList, BlockHeaderDigest blockHeaderDigest) throws NulsException {
        if (null == chain || txHashList == null || txHashList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        try {
            int chainId = chain.getChainId();
            List<Transaction> txList = new ArrayList<>();
            for(int i=0; i<txHashList.size(); i++){
                NulsDigestData hash = txHashList.get(i);
                //从已验证但未打包的交易中取出交易
                Transaction tx = txVerifiedStorageService.getTx(chainId, hash);
                if(null == tx){
                    throw new NulsException(TxErrorCode.TX_NOT_EXIST);
                }
            }
            return rollbackTxList(chain, txList, blockHeaderDigest, true);
        } catch (NulsException e) {
            throw new NulsException(e.getErrorCode());
        }
    }
}
