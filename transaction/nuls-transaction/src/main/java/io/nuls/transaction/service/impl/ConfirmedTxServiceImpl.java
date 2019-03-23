package io.nuls.transaction.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.constant.TxStatusEnum;
import io.nuls.base.data.*;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.bo.VerifyTxResult;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.rpc.call.TransactionCall;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.CtxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.h2.TransactionH2Service;
import io.nuls.transaction.storage.rocksdb.ConfirmedTxStorageService;
import io.nuls.transaction.storage.rocksdb.CtxStorageService;
import io.nuls.transaction.storage.rocksdb.UnconfirmedTxStorageService;
import io.nuls.transaction.utils.TxUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ChainManager chainManager;

    @Autowired
    private CtxStorageService ctxStorageService;

    @Autowired
    private PackablePool packablePool;

    @Autowired
    private CtxService ctxService;

    @Autowired
    private TransactionH2Service transactionH2Service;

    @Autowired
    private TxService txService;

    @Autowired
    private TxConfig txConfig;

    @Override
    public TransactionConfirmedPO getConfirmedTransaction(Chain chain, NulsDigestData hash) {
        if (null == hash) {
            return null;
        }
        return confirmedTxStorageService.getTx(chain.getChainId(), hash);
    }

    private boolean saveTx(Chain chain, TransactionConfirmedPO tx) {
        if (null == tx) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        return confirmedTxStorageService.saveTx(chain.getChainId(), tx);
    }

    @Override
    public boolean saveGengsisTxList(Chain chain, List<Transaction> txList, String blockHeaderHex) throws NulsException {
        if (null == chain || txList == null || txList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        LedgerCall.coinDataBatchNotify(chain);
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (Transaction tx : txList) {
            txHashList.add(tx.getHash());
            //todo 批量验证coinData，接口和单个的区别？
            VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, tx, true);
            if (!verifyTxResult.success()) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("*** Debug *** [保存创世块交易失败] " +
                        "coinData not success - code: {}, - reason:{}, type:{} - txhash:{}", verifyTxResult.getCode(), verifyTxResult.getDesc(), tx.getType(), tx.getHash().getDigestHex());
                return false;
            }
        }
        if (!saveBlockTxList(chain, txList, blockHeaderHex, true)) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("保存创世块交易失败");
            return false;
        }
        int debugCount = 0;
        for (Transaction tx : txList) {
            //保存到h2数据库
            debugCount += transactionH2Service.saveTxs(TxUtil.tx2PO(chain, tx));
        }
        CoinData coinData = TxUtil.getCoinData(txList.get(0));
        for (Coin coin : coinData.getTo()) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("address:{}, to:{}", AddressTool.getStringAddressByBytes(coin.getAddress()), coin.getAmount());
        }

        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("保存创世块交易成功, H2数据库生成{}条交易记录", debugCount);
        return true;
    }

    /**
     * 1.保存交易
     * 2.调提交易接口
     * 3.调账本
     * 4.从未打包交易库中删除交易
     */
    @Override
    public boolean saveTxList(Chain chain, List<NulsDigestData> txHashList, String blockHeaderHex) throws NulsException {
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("start save block txs.......");
        if (null == chain || txHashList == null || txHashList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        try {
            List<Transaction> txList = new ArrayList<>();
            for (int i = 0; i < txHashList.size(); i++) {
                NulsDigestData hash = txHashList.get(i);
                Transaction tx = unconfirmedTxStorageService.getTx(chain.getChainId(), hash);
                txList.add(tx);
            }
            return saveBlockTxList(chain, txList, blockHeaderHex, false);
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return false;
        }
    }

    private boolean saveBlockTxList(Chain chain, List<Transaction> txList, String blockHeaderHex, boolean gengsis) throws NulsException {
        List<String> txHexList = new ArrayList<>();
        int chainId = chain.getChainId();
        List<byte[]> txHashs = new ArrayList<>();
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        BlockHeader blockHeader = null;
        try {
            blockHeader = TxUtil.getInstance(blockHeaderHex, BlockHeader.class);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("saveBlockTxList block height:{}", blockHeader.getHeight());
            for (Transaction tx : txList) {
                if(TxManager.isSystemSmartContract(chain, tx.getType())) {
                    continue;
                }
                tx.setBlockHeight(blockHeader.getHeight());
                String txHex = tx.hex();
                txHexList.add(txHex);
                txHashs.add(tx.getHash().serialize());
                TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
            }
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return false;
        }
        if (!saveTxs(chain, txList, blockHeader.getHeight(), true)) {
            return false;
        }
        if (!gengsis && !commitTxs(chain, moduleVerifyMap, blockHeaderHex, true)) {
            removeTxs(chain, txList, blockHeader.getHeight(), false);
            return false;
        }
        if (!commitLedger(chain, txHexList, blockHeader.getHeight())) {
            if (!gengsis) {
                rollbackTxs(chain, moduleVerifyMap, blockHeaderHex, false);
            }
            removeTxs(chain, txList, blockHeader.getHeight(), false);
            return false;
        }
        //如果确认交易成功，则从未打包交易库中删除交易
        unconfirmedTxStorageService.removeTxList(chainId, txHashs);
        return true;
    }


    //保存交易
    private boolean saveTxs(Chain chain, List<Transaction> txList, long blockHeight, boolean atomicity) {
        List<Transaction> savedList = new ArrayList<>();
        boolean rs = true;
        for (Transaction tx : txList) {
            tx.setStatus(TxStatusEnum.CONFIRMED);
            TransactionConfirmedPO transactionConfirmedPO = new TransactionConfirmedPO(tx, blockHeight, TxStatusEnum.CONFIRMED.getStatus());
            if (saveTx(chain, transactionConfirmedPO)) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("success! saveTxs -type[{}], hash:{}", tx.getType(), tx.getHash().getDigestHex());
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("get hash:{}, txObj:{}",
                        tx.getType(), tx.getHash().getDigestHex(), confirmedTxStorageService.getTx(chain.getChainId(), tx.getHash().getDigestHex()).getTx());
                TxUtil.txInformationDebugPrint(chain, tx, chain.getLoggerMap().get(TxConstant.LOG_TX));
                savedList.add(tx);
            } else {
                if (atomicity) {
                    removeTxs(chain, savedList, blockHeight, false);
                }
                rs = false;
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("failed! saveTxs  -type[{}], hash:{}", tx.getType(), tx.getHash().getDigestHex());
                break;
            }
        }
        return rs;
    }

    //调提交易
    private boolean commitTxs(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, String blockHeaderHex, boolean atomicity) {
        //调用交易模块统一commit接口 批量
        Map<TxRegister, List<String>> successed = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        boolean result = true;
        for (Map.Entry<TxRegister, List<String>> entry : moduleVerifyMap.entrySet()) {
            boolean rs;
            if (entry.getKey().getModuleCode().equals(txConfig.getModuleCode())) {
                try {
                    rs = txService.crossTransactionCommit(chain, entry.getValue(), blockHeaderHex);
                } catch (NulsException e) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
                    rs = false;
                }
            } else {
                rs = TransactionCall.txProcess(chain, entry.getKey().getCommit(),
                        entry.getKey().getModuleCode(), entry.getValue(), blockHeaderHex);
            }
            if (!rs) {
                result = false;
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("save tx failed! commitTxs");
                break;
            }
            successed.put(entry.getKey(), entry.getValue());
        }
        if (!result && atomicity) {
            rollbackTxs(chain, successed, blockHeaderHex, false);
            return false;
        }
        return true;
    }

    private boolean commitLedger(Chain chain, List<String> txHexList, long blockHeight) {
        //提交账本
        try {
            boolean rs = LedgerCall.commitTxsLedger(chain, txHexList, blockHeight);
            if(!rs){
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("save block tx failed! commitLedger");
            }
            return rs;
        } catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("failed! commitLedger");
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return false;
        }
    }

    private boolean removeTxs(Chain chain, List<Transaction> txList, long blockheight, boolean atomicity) {
        boolean rs = true;
        if(!confirmedTxStorageService.removeTxList(chain.getChainId(), txList) && atomicity ){
            saveTxs(chain, txList, blockheight, false);
            rs = false;
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("failed! removeTxs");
        }
        return rs;
    }

    private boolean rollbackTxs(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, String blockHeaderHex, boolean atomicity) {
        Map<TxRegister, List<String>> successed = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        boolean result = true;
        for (Map.Entry<TxRegister, List<String>> entry : moduleVerifyMap.entrySet()) {
            boolean rs;
            if (entry.getKey().getModuleCode().equals(txConfig.getModuleCode())) {
                try {
                    rs = txService.crossTransactionRollback(chain, entry.getValue(), blockHeaderHex);
                } catch (NulsException e) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
                    rs = false;
                }
            } else {
                rs = TransactionCall.txProcess(chain, entry.getKey().getRollback(),
                        entry.getKey().getModuleCode(), entry.getValue(), blockHeaderHex);
            }
            if (!rs) {
                result = false;
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("failed! rollbackcommitTxs ");
                break;
            }
            successed.put(entry.getKey(), entry.getValue());
        }
        if (!result && atomicity) {
            commitTxs(chain, successed, blockHeaderHex, false);
            return false;
        }
        return true;
    }

    private boolean rollbackLedger(Chain chain, List<String> txHexList, Long blockHeight) {
        try {
            boolean rs =  LedgerCall.rollbackTxsLedger(chain, txHexList, blockHeight);
            if(!rs){
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("rollback block tx failed! rollbackLedger");
            }
            return rs;
        } catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return false;
        }
    }


    @Override
    public boolean rollbackTxList(Chain chain, List<NulsDigestData> txHashList, String blockHeaderHex) throws NulsException {
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("start rollbackTxList..............");
        if (null == chain || txHashList == null || txHashList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        int chainId = chain.getChainId();
        List<byte[]> txHashs = new ArrayList<>();
        List<Transaction> txList = new ArrayList<>();
        List<String> txHexList = new ArrayList<>();
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        try {
            for (int i = 0; i < txHashList.size(); i++) {
                NulsDigestData hash = txHashList.get(i);
                txHashs.add(hash.serialize());
                TransactionConfirmedPO txPO = confirmedTxStorageService.getTx(chainId, hash);
                Transaction tx = txPO.getTx();
                txList.add(tx);
                String txHex = tx.hex();
                txHexList.add(txHex);
                TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
            }
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return false;
        }

        BlockHeader blockHeader = TxUtil.getInstance(blockHeaderHex, BlockHeader.class);
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("rollbackTxList block height:{}", blockHeader.getHeight());
        if (!rollbackLedger(chain, txHexList, blockHeader.getHeight())) {
            return false;
        }

        if (!rollbackTxs(chain, moduleVerifyMap, blockHeaderHex, true)) {
            commitLedger(chain, txHexList, blockHeader.getHeight());
            return false;
        }
        if (!removeTxs(chain, txList, blockHeader.getHeight(), true)) {
            commitTxs(chain, moduleVerifyMap, blockHeaderHex, false);
            saveTxs(chain, txList, blockHeader.getHeight(), false);
            return false;
        }

        //倒序放入未确认库, 和待打包队列
        for (int i = txList.size() - 1; i >= 0; i--) {
            Transaction tx = txList.get(i);
            unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
            savePackable(chain, tx);
        }
        return true;
    }

    /**
     * 重新放回待打包队列的最前端
     *
     * @param chain chain
     * @param tx    Transaction
     * @return boolean
     */
    private boolean savePackable(Chain chain, Transaction tx) {
        //不是系统交易 并且节点是打包节点则重新放回待打包队列的最前端
        if (!TxManager.isSystemTx(chain, tx) && chain.getPackaging().get()) {
            return packablePool.addInFirst(chain, tx, false);
        }
        return true;
    }


    @Override
    public void processEffectCrossTx(Chain chain, long blockHeight) throws NulsException {
        int chainId = chain.getChainId();
        List<NulsDigestData> hashList = confirmedTxStorageService.getCrossTxEffectList(chainId, blockHeight);
        for (NulsDigestData hash : hashList) {
            TransactionConfirmedPO txPO = confirmedTxStorageService.getTx(chainId, hash);
            Transaction tx = txPO.getTx();
            if (null == tx) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).error(TxErrorCode.TX_NOT_EXIST.getMsg() + ": " + hash.toString());
                continue;
            }
            if (tx.getType() != TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).error(TxErrorCode.TX_TYPE_ERROR.getMsg() + ": " + hash.toString());
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
            if (chainId == txConfig.getMainChainId()) {
                if (toChainId == chainId) {
                    //todo 已到达目标链发送回执
                } else {
                    //广播给 toChainId 链的节点
                    NetworkCall.broadcastTxHash(toChainId, tx.getHash());
                }
            } else {
                //广播给 主网 链的节点
                NetworkCall.broadcastTxHash(txConfig.getMainChainId(), tx.getHash());
            }
        }
    }

    @Override
    public List<String> getTxList(Chain chain, List<String> hashList) {
        List<String> txList = new ArrayList<>();
        if (hashList == null || hashList.size() == 0) {
            return txList;
        }
        int chainId = chain.getChainId();
        for(String hashHex : hashList){
            TransactionConfirmedPO txCfmPO = confirmedTxStorageService.getTx(chainId, hashHex);
            try {
                txList.add(txCfmPO.getTx().hex());
            } catch (Exception e) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
                return new ArrayList<>();
            }
        }
        return txList;
    }

    @Override
    public List<String> getTxListExtend(Chain chain, List<String> hashList, boolean allHits) {
        List<String> txList = new ArrayList<>();
        if (hashList == null || hashList.size() == 0) {
            return txList;
        }
        int chainId = chain.getChainId();
        for(String hashHex : hashList){
            Transaction tx = unconfirmedTxStorageService.getTx(chain.getChainId(), hashHex);
            if(null == tx) {
                TransactionConfirmedPO txCfmPO = confirmedTxStorageService.getTx(chainId, hashHex);
                if(null == txCfmPO){
                    if(allHits) {
                        //allHits为true时一旦有一个没有获取到, 直接返回空list
                        return new ArrayList<>();
                    }
                    continue;
                }
                tx = txCfmPO.getTx();
            }
            try {
                txList.add(tx.hex());
            } catch (Exception e) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
                if(allHits) {
                    //allHits为true时直接返回空list
                    return new ArrayList<>();
                }
                continue;
            }
        }
        return txList;
    }
}
