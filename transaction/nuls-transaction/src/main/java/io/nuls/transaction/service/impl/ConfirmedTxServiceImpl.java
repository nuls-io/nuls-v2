package io.nuls.transaction.service.impl;

import io.nuls.base.data.BlockHeaderDigest;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.rocksdb.storage.CtxStorageService;
import io.nuls.transaction.db.rocksdb.storage.ConfirmedTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.bo.VerifyTxResult;
import io.nuls.transaction.rpc.call.ChainCall;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.rpc.call.TransactionCall;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.CtxService;
import io.nuls.transaction.utils.TransactionIndexComparator;
import io.nuls.transaction.utils.TxUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/30
 */
@Service
public class ConfirmedTxServiceImpl implements ConfirmedTxService {

    @Autowired
    private ConfirmedTxStorageService confirmedTxStorageService;

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private TransactionIndexComparator txIndexComparator;

    @Autowired
    private CtxStorageService ctxStorageService;

    @Autowired
    private PackablePool packablePool;

    @Autowired
    private CtxService ctxService;

    @Autowired
    private TransactionH2Service transactionH2Service;

    @Override
    public Transaction getConfirmedTransaction(Chain chain, NulsDigestData hash) {
        if (null == hash) {
            return null;
        }
         return confirmedTxStorageService.getTx(chain.getChainId(), hash);
    }

    private boolean saveTx(Chain chain, Transaction tx) {
        if (null == tx) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        chain.getLogger().debug("saveConfirmedTx: " + tx.getHash().getDigestHex());
        return confirmedTxStorageService.saveTx(chain.getChainId(), tx);
    }

    @Override
    public boolean saveGengsisTxList(Chain chain, List<Transaction> txhexList, BlockHeaderDigest blockHeaderDigest) throws NulsException {
        if (null == chain || txhexList == null || txhexList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        LedgerCall.coinDataBatchNotify(chain);
        for(Transaction tx : txhexList){
            //todo 批量验证coinData，接口和单个的区别？
            VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, tx, true);
            if (!verifyTxResult.success()) {
                return false;
            }
        }
        List<Transaction> savedList = new ArrayList<>();
        for(Transaction tx : txhexList){
            //将交易保存、提交、发送至账本
            ResultSaveCommitTx resultSaveCommitTx = saveCommitTx(chain, tx, null, blockHeaderDigest);
            if (resultSaveCommitTx.rs) {
                savedList.add(tx);
            } else {
                //回滚当前交易已提交的步骤
                rollbackCurrentTx(chain, tx, resultSaveCommitTx, blockHeaderDigest);
                //回滚之前的交易
                this.rollbackTxList(chain, savedList, blockHeaderDigest, false);
                // 保存区块交易失败, 回滚交易数
                chain.getLogger().error("Save block transaction failed, rollback {} transactions", savedList.size());
                return false;
            }
        }
        for(Transaction tx : txhexList) {
            //保存到h2数据库
            transactionH2Service.saveTxs(TxUtil.tx2PO(tx));
        }
        chain.getLogger().debug("保存创世块交易完成");
        return true;
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
            List<NulsDigestData> ctxHashList = new ArrayList<>();
            for (int i = 0; i < txHashList.size(); i++) {
                NulsDigestData hash = txHashList.get(i);
                txHashs.add(hash.serialize());
                //从已验证但未打包的交易中取出交易
                Transaction tx = unconfirmedTxStorageService.getTx(chainId, hash);
                //将交易保存、提交、发送至账本
                ResultSaveCommitTx resultSaveCommitTx = saveCommitTx(chain, tx, ctxHashList, blockHeaderDigest);
                if (resultSaveCommitTx.rs) {
                    savedList.add(tx);
                } else {
                    //回滚当前交易已提交的步骤
                    rollbackCurrentTx(chain, tx, resultSaveCommitTx, blockHeaderDigest);
                    //回滚之前的交易
                    this.rollbackTxList(chain, savedList, blockHeaderDigest, false);
                    // 保存区块交易失败, 回滚交易数
                    chain.getLogger().error("Save block transaction failed, rollback {} transactions", savedList.size());
                    return false;
                }
            }
            //保存生效高度
            long effectHeight = blockHeaderDigest.getHeight() + TxConstant.CTX_EFFECT_THRESHOLD;
            boolean rs = confirmedTxStorageService.saveCrossTxEffectList(chainId, effectHeight, ctxHashList);
            if (!rs) {
                this.rollbackTxList(chain, savedList, blockHeaderDigest, false);
                return false;
            }
            //如果确认交易成功，则从未打包交易库中删除交易
            unconfirmedTxStorageService.removeTxList(chainId, txHashs);
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
     * @return int 返回成功了几个步骤, 如果未达成功的步骤数,则需要对应回滚之前的步骤
     */
    private ResultSaveCommitTx saveCommitTx(Chain chain, Transaction tx, List<NulsDigestData> ctxhashList, BlockHeaderDigest blockHeaderDigest) {
        boolean rs = false;
        int step = 0;
        //ResultSaveCommitTx rs = new ResultSaveCommitTx(false, 0);
        try {
            rs = saveTx(chain, tx);
            if (rs) {
                step = 1;
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                rs = TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx, HexUtil.encode(blockHeaderDigest.serialize()));
            }
            if (rs) {
                step = 2;
                if (tx.getType() != TxConstant.TX_TYPE_YELLOW_PUNISH) {
                    rs = LedgerCall.commitTxLedger(chain, tx, true);
                }
            }
            if (rs) {
                step = 3;
                if (tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER
                        && null != ctxService.getTx(chain, tx.getHash())) {
                    //不需要对该步骤结果设置step, 该步骤内部有处理回滚.
                    rs = ctxCommit(chain, tx, ctxhashList);
                }
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
        return new ResultSaveCommitTx(rs, step);
    }

    /**
     * 根据提交结果 回滚该交易对应的步骤
     * @param chain
     * @param tx
     * @param resultSaveCommitTx
     */
    private void rollbackCurrentTx(Chain chain, Transaction tx, ResultSaveCommitTx resultSaveCommitTx, BlockHeaderDigest blockHeaderDigest){
        int step = resultSaveCommitTx.step;
        try {
            if(step >= 3){
                //回滚账本
                LedgerCall.rollbackTxLedger(chain, tx, true);
            }
            if(step >= 2){
                //通过各模块回滚交易业务逻辑
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                TransactionCall.txProcess(chain, txRegister.getRollback(), txRegister.getModuleCode(), tx, HexUtil.encode(blockHeaderDigest.serialize()));
            }
            if(step >= 1){
                //从已确认库中删除该交易
                confirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
    }

    private class ResultSaveCommitTx {
        boolean rs;
        int step;

        public ResultSaveCommitTx(boolean rs, int step) {
            this.rs = rs;
            this.step = step;
        }
    }


    /**
     * 保存交易时对跨链交易进行处理, 包括跨链交易变更状态, 向链管理提交跨链交易coinData, 记录hash
     *
     * @param chain
     * @param tx
     * @return
     */
    private boolean ctxCommit(Chain chain, Transaction tx, List<NulsDigestData> ctxhashList){
        //跨链交易变更状态
        if(null != ctxhashList) {
            ctxhashList.add(tx.getHash());
        }
        boolean rs = ctxService.updateCrossTxState(chain, tx.getHash(), TxConstant.CTX_COMFIRM_4);
        if(!rs){
            return false;
        }
        String coinDataHex = HexUtil.encode(tx.getCoinData());
        try {
            rs = ChainCall.ctxChainLedgerCommit(coinDataHex);
            if(!rs){
               throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
            }
            return true;
        } catch (NulsException e) {
            if( null != ctxhashList) {
                ctxhashList.remove(tx.getHash());
            }
            ctxService.updateCrossTxState(chain, tx.getHash(), TxConstant.CTX_NODE_STATISTICS_RESULT_3);
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 回滚交易时对跨链交易进行处理, 包括跨链交易变更状态, 通知链管理回滚coinData
     * @param chain
     * @param tx
     * @return
     */
    private boolean ctxRollback(Chain chain, Transaction tx){
        boolean rs = ctxService.updateCrossTxState(chain, tx.getHash(), TxConstant.CTX_NODE_STATISTICS_RESULT_3);
        if (!rs) {
            return false;
        }
        String coinDataHex = HexUtil.encode(tx.getCoinData());
        try {
            rs = ChainCall.ctxChainLedgerRollback(coinDataHex);
            if(!rs){
                throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
            }
            return true;
        } catch (NulsException e) {
            ctxService.updateCrossTxState(chain, tx.getHash(), TxConstant.CTX_COMFIRM_4);
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 回滚区块交易时, 需要保留跨链交易至, 待打包队列?
     *
     * @param chain
     * @param savedList
     * @param blockHeaderDigest
     * @param atomicity         回滚是否具有原子性(是否是回滚已确认过的区块), 回滚块时为true, 需要处理回滚失败的情况, 保存块时为false(保存交易失败时的回滚,不再处理回滚失败的情况)
     * @return
     * @throws NulsException
     */
    private boolean rollbackTxList(Chain chain, List<Transaction> savedList, BlockHeaderDigest blockHeaderDigest, boolean atomicity) throws NulsException {
        if (null == chain || savedList == null || savedList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        /*
            atomicity: true 回滚过程失败时, 要处理回滚失败的情况
            atomicity: false 回滚过程失败时, 不再处理回滚失败的情况
         */
        int chainId = chain.getChainId();
        boolean rs = false;

        List<Transaction> rollbackedList = new ArrayList<>();
        for (int i = savedList.size() - 1; i >= 0; i--) {
            Transaction tx = savedList.get(i);
            /*
                放回待打包库
                如果是友链过来的跨链交易则需要处理跨链流程
                回滚账本
                交易业务回滚
                最后放回打包待队列
            */
            //放回待打包数据库
            rs = unconfirmedTxStorageService.putTx(chainId, tx);
            if (atomicity && !rs) {
                //reCommitCurrentTx(chain, tx, 1);
                break;
            }

            //如果是友链过来的跨链交易则需要处理跨链流程
            if (tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER
                    && null != ctxService.getTx(chain, tx.getHash())) {
                rs = ctxRollback(chain, tx);
                if (atomicity && !rs) {
                    reCommitCurrentTx(chain, tx, 1, blockHeaderDigest);
                    break;
                }
            }
            //回滚账本
            rs = LedgerCall.rollbackTxLedger(chain, tx, true);
            if (atomicity && !rs) {
                reCommitCurrentTx(chain, tx, 2, blockHeaderDigest);
                break;
            }
            //交易业务回滚
            try {
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                rs = TransactionCall.txProcess(chain, txRegister.getRollback(), txRegister.getModuleCode(), tx, HexUtil.encode(blockHeaderDigest.serialize()));
            } catch (Exception e) {
                rs = false;
                chain.getLogger().error(e);
            }
            if (atomicity && !rs) {
                reCommitCurrentTx(chain, tx, 3, blockHeaderDigest);
                break;
            }
            //从已确认交易数据库(主链)中删除
            rs = confirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
            if (atomicity && !rs) {
                reCommitCurrentTx(chain, tx, 4, blockHeaderDigest);
                break;
            }
            //从新放入待打包队列
            rs = savePackable(chain, tx);
            if (atomicity){
               if(!rs) {
                   rollbackedList.add(tx);
               }else{
                   reCommitCurrentTx(chain, tx, 5, blockHeaderDigest);
               }
            }
        }

        boolean ctsRs = true;
        //如果是回滚已确认过的区块,则需要删除已记录的回滚块中跨链交易的生效高度
        if(atomicity){
            long effectHeight = blockHeaderDigest.getHeight() + TxConstant.CTX_EFFECT_THRESHOLD;
            ctsRs = confirmedTxStorageService.removeCrossTxEffectList(chainId, effectHeight);
        }
        //有回滚失败的, 重新commit
        boolean failed = (atomicity && savedList.size() != rollbackedList.size()) || (atomicity && !ctsRs);
        if (failed) {
            try {
                for (int i = rollbackedList.size() - 1; i >= 0; i--) {
                    Transaction tx = rollbackedList.get(i);
                    confirmedTxStorageService.saveTx(chain.getChainId(), tx);
                    TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                    TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx, HexUtil.encode(blockHeaderDigest.serialize()));
                    if (tx.getType() != TxConstant.TX_TYPE_YELLOW_PUNISH) {
                        LedgerCall.commitTxLedger(chain, tx, true);
                    }
                    if (tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER
                            && null != ctxService.getTx(chain, tx.getHash())) {
                        //不需要对该步骤结果设置step, 该步骤内部有处理回滚.
                        ctxCommit(chain, tx, null);
                    }
                    unconfirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
                }
            } catch (Exception e) {
                chain.getLogger().error(e);
                throw new NulsException(e);
            }

            return false;
        }
        return true;
    }



    /**
     * 某个交易回滚失败时,需要重新提交已回滚成功的步骤
     * @param chain
     * @param tx
     * @param setp
     */
    private void reCommitCurrentTx(Chain chain, Transaction tx, int setp, BlockHeaderDigest blockHeaderDigest){
        try {
            if(setp >= 5){
                confirmedTxStorageService.saveTx(chain.getChainId(),tx);
            }
            if(setp >= 4){
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                TransactionCall.txProcess(chain, txRegister.getCommit(), txRegister.getModuleCode(), tx, HexUtil.encode(blockHeaderDigest.serialize()));
            }
            if(setp >= 3){
                if (tx.getType() != TxConstant.TX_TYPE_YELLOW_PUNISH) {
                    LedgerCall.commitTxLedger(chain, tx, true);
                }
            }
            if(setp >= 2){
                if (tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER
                        && null != ctxService.getTx(chain, tx.getHash())) {
                    //不需要对该步骤结果设置step, 该步骤内部有处理回滚.
                    ctxCommit(chain, tx, null);
                }
            }
            if(setp >= 1){
                unconfirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
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
        //不是系统交易则重新放回待打包队列的最前端
        if (!transactionManager.isSystemTx(chain, tx)) {
            return packablePool.addInFirst(chain, tx, false);

        }
        return true;
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
                Transaction tx = unconfirmedTxStorageService.getTx(chainId, hash);
                if (null == tx) {
                    throw new NulsException(TxErrorCode.TX_NOT_EXIST);
                }
                txList.add(tx);
            }
            return rollbackTxList(chain, txList, blockHeaderDigest, true);
        } catch (NulsException e) {
            throw new NulsException(e.getErrorCode());
        }
    }


    @Override
    public void processEffectCrossTx(Chain chain, long blockHeight) throws NulsException {
        int chainId = chain.getChainId();
        List<NulsDigestData> hashList = confirmedTxStorageService.getCrossTxEffectList(chainId, blockHeight);
        for (NulsDigestData hash : hashList) {
            Transaction tx = confirmedTxStorageService.getTx(chainId, hash);
            if (null == tx) {
                chain.getLogger().error(TxErrorCode.TX_NOT_EXIST.getMsg() + ": " + hash.toString());
                continue;
            }
            if (tx.getType() != TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                chain.getLogger().error(TxErrorCode.TX_TYPE_ERROR.getMsg() + ": " + hash.toString());
                continue;
            }
            //跨链转账交易接收者链id
            int toChainId = TxUtil.getCrossTxTosOriginChainId(tx);

            /*
                如果当前链是主网
                    1.需要对接收者链进行账目金额增加
                    2a.如果是交易收款方,则需要向发起链发送回执? todo
                    2b.如果不是交易收款方广播给收款方链
                如果当前链是交易发起链
                    1.广播给主网
             */
            if (chainId == TxConstant.NULS_CHAINID) {
                if (toChainId == chainId) {
                    //todo 已到达目标链发送回执
                }else {
                    //广播给 toChainId 链的节点
                    NetworkCall.broadcastTxHash(toChainId, tx.getHash());
                }
            }else{
                //广播给 主网 链的节点
                NetworkCall.broadcastTxHash(TxConstant.NULS_CHAINID, tx.getHash());
            }
        }
    }
}
