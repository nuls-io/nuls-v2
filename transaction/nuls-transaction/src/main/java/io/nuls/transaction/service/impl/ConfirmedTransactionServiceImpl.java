package io.nuls.transaction.service.impl;

import io.nuls.base.data.BlockHeaderDigest;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.transaction.cache.TxVerifiedPool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.TransactionStorageService;
import io.nuls.transaction.db.rocksdb.storage.TxVerifiedStorageService;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.rpc.call.LegerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.rpc.call.TransactionCall;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.service.CrossChainTxService;
import io.nuls.transaction.utils.TransactionIndexComparator;
import io.nuls.transaction.utils.TxUtil;

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

    @Autowired
    private TxVerifiedStorageService txVerifiedStorageService;

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private TransactionIndexComparator txIndexComparator;

    @Autowired
    private CrossChainTxStorageService crossChainTxStorageService;

    @Autowired
    private TxVerifiedPool txVerifiedPool;

    @Autowired
    private CrossChainTxService crossChainTxService;

    @Override
    public Transaction getConfirmedTransaction(Chain chain, NulsDigestData hash) {
        if (null == hash) {
            return null;
        }
        return transactionStorageService.getTx(chain.getChainId(), hash);
    }

    private boolean saveTx(Chain chain, Transaction tx) {
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
            List<NulsDigestData> ctxhashList = new ArrayList<>();
            for (int i = 0; i < txHashList.size(); i++) {
                NulsDigestData hash = txHashList.get(i);
                txHashs.add(hash.serialize());
                //从已验证但未打包的交易中取出交易
                Transaction tx = txVerifiedStorageService.getTx(chainId, hash);
                //将交易保存、提交、发送至账本
                boolean rs = saveCommitTx(chain, tx, ctxhashList);
                if (rs) {
                    savedList.add(tx);
                } else {
                    //删除当前交易,再回滚之前已处理完成的交易
                    transactionStorageService.removeTx(chain.getChainId(), tx.getHash());
                    this.rollbackTxList(chain, savedList, blockHeaderDigest, false);
                    // 保存区块交易失败, 回滚交易数
                    chain.getLogger().error("Save block transaction failed, rollback {} transactions", savedList.size());
                    return false;
                }
            }
            //保存生效高度
            long effectHeight = blockHeaderDigest.getHeight() + TxConstant.CTX_EFFECT_THRESHOLD;
            transactionStorageService.saveCrossTxEffectList(chainId, effectHeight, ctxhashList);
            //如果确认交易成功，则从未打包交易库中删除交易
            txVerifiedStorageService.removeTxList(chainId, txHashs);
            return true;
        } catch (NulsException e) {
            throw new NulsException(e.getErrorCode());
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    /**
     * 处理保存交易至数据库后,处理交易确认逻辑
     * 1.交易提交
     * 2.发送账本
     * 3.处理跨链交易, 更新交易验证流转状态, 添加至该区块的跨链交易hash集合
     *
     * @param chain       链
     * @param tx          要保存的交易
     * @param ctxhashList 该区块中所有的跨链交易hash集合
     * @return boolean
     */
    private boolean saveCommitTx(Chain chain, Transaction tx, List<NulsDigestData> ctxhashList) {
        boolean rs = false;
        try {
            rs = saveTx(chain, tx);
            if (!rs) {
                return false;
            }
            TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
            rs = TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx.hex());
            if (!rs) {
                chain.getLogger().error(tx.getHash().getDigestHex() + TxErrorCode.TX_COMMIT_FAIL);
                return false;
            }
            rs = LegerCall.commitTxLeger(chain.getChainId(), tx, true);
            if (!rs) {
                return false;
            }
            //记录跨链交易生效高度
            if (tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                //跨链交易变更状态
                rs = crossChainTxService.updateCrossTxState(chain, tx.getHash(), TxConstant.CTX_COMFIRM_4);
                ctxhashList.add(tx.getHash());
                return rs;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * 回滚区块交易时, 需要保留跨链交易至, 待打包队列?
     *
     * @param chain
     * @param savedList
     * @param blockHeaderDigest
     * @param atomicity         回滚是否具有原子性(是否是滚已确认过的区块), 回滚块时为true, 保存块时为false(保存交易失败时的回滚,不再处理回滚失败的情况)
     * @return
     * @throws NulsException
     */
    private boolean rollbackTxList(Chain chain, List<Transaction> savedList, BlockHeaderDigest blockHeaderDigest, boolean atomicity) throws NulsException {
        if (null == chain || savedList == null || savedList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        try {
            List<Transaction> rollbackedList = new ArrayList<>();
            for (int i = savedList.size() - 1; i >= 0; i--) {
                Transaction tx = savedList.get(i);
                boolean rs = false;
                if (tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                    //跨链交易变更状态
                    rs = crossChainTxService.updateCrossTxState(chain, tx.getHash(), TxConstant.CTX_NODE_STATISTICS_RESULT_3);
                    if (!rs) {
                        break;
                    }
                }

                rs = LegerCall.rollbackTxLeger(chain.getChainId(), tx, true);
                if (atomicity && !rs) {
                    //如果为原子操作并且账本回滚失败,则直接结束回滚,并重新commit已回滚成功的交易,并返回失败
                    break;
                }
                //执行交易rollback
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                rs = TransactionCall.txProcess(chain, txRegister.getRollback(), txRegister.getModuleCode(), tx.hex());
                if (atomicity) {
                    if (!rs) {
                        break;
                    } else if (!savePackable(chain, tx)) {

                        break;
                    } else {
                        rollbackedList.add(tx);
                    }
                }
            }

            boolean ctsRs = true;
            if (atomicity) {
                //如果是回滚已确认过的区块,则需要删除已记录的回滚块中跨链交易的生效高度
                long effectHeight = blockHeaderDigest.getHeight() + TxConstant.CTX_EFFECT_THRESHOLD;
                ctsRs = transactionStorageService.removeCrossTxEffectList(chain.getChainId(), effectHeight);
            }
            //有回滚失败的, 重新commit
            boolean faild = (atomicity && savedList.size() != rollbackedList.size()) || (atomicity && !ctsRs);
            if (faild) {
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

    /**
     * 重新放回待打包队列的最前端
     *
     * @param chain chain
     * @param tx    Transaction
     * @return boolean
     */
    private boolean savePackable(Chain chain, Transaction tx) {
        boolean result = true;
        //不是系统交易则重新放回待打包队列的最前端, 放回待打包DB数据库中
        if (!transactionManager.isSystemTx(chain, tx)) {
            result = txVerifiedStorageService.putTx(chain.getChainId(), tx);
            if (result) {
                result = txVerifiedPool.addInFirst(chain, tx, false);
                if (!result) {
                    txVerifiedStorageService.removeTx(chain.getChainId(), tx.getHash());
                }
            }
        }
        return result;
    }

    @Override
    public boolean rollbackTxList(Chain chain, List<NulsDigestData> txHashList, BlockHeaderDigest blockHeaderDigest) throws NulsException {
        if (null == chain || txHashList == null || txHashList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        try {
            int chainId = chain.getChainId();
            List<Transaction> txList = new ArrayList<>();
            for (int i = 0; i < txHashList.size(); i++) {
                NulsDigestData hash = txHashList.get(i);
                //从已验证但未打包的交易中取出交易
                Transaction tx = txVerifiedStorageService.getTx(chainId, hash);
                if (null == tx) {
                    throw new NulsException(TxErrorCode.TX_NOT_EXIST);
                }
            }
            return rollbackTxList(chain, txList, blockHeaderDigest, true);
        } catch (NulsException e) {
            throw new NulsException(e.getErrorCode());
        }
    }


    @Override
    public void processEffectCrossTx(Chain chain, long blockHeight) throws NulsException {
        int chainId = chain.getChainId();
        List<NulsDigestData> hashList = transactionStorageService.getCrossTxEffectList(chainId, blockHeight);
        for (NulsDigestData hash : hashList) {
            Transaction tx = transactionStorageService.getTx(chainId, hash);
            if (null == tx) {
                chain.getLogger().error(TxErrorCode.TX_NOT_EXIST.getMsg() + ": " + hash.toString());
                continue;
            }
            if (tx.getType() != TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                chain.getLogger().error(TxErrorCode.TX_TYPE_ERROR.getMsg() + ": " + hash.toString());
                continue;
            }
            int toChainId = TxUtil.getCrossTxTosOriginChainId(tx);
            if (toChainId == chainId && toChainId == TxConstant.NULS_CHAINID) {
                //todo 发送回执
            } else {
                //广播给 toChainId 链的节点
                NetworkCall.broadcastTxHash(toChainId, tx.getHash());
            }
        }
    }
}
