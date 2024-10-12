package io.nuls.transaction.service.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.common.NulsCoresConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxContext;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.TransactionCall;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.ConfirmedTxStorageService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/11/30
 */
@Component
public class ConfirmedTxServiceImpl implements ConfirmedTxService {

    @Autowired
    private ConfirmedTxStorageService confirmedTxStorageService;

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private PackablePool packablePool;

    @Autowired
    private TxService txService;

    @Autowired
    private NulsCoresConfig txConfig;

    @Override
    public TransactionConfirmedPO getConfirmedTransaction(Chain chain, NulsHash hash) {
        if (null == hash) {
            return null;
        }
        return confirmedTxStorageService.getTx(chain.getChainId(), hash);
    }

    @Override
    public boolean saveGengsisTxList(Chain chain, List<String> txStrList, String blockHeader) throws NulsException {
        if (null == chain || txStrList == null || txStrList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        if (!saveBlockTxList(chain, txStrList, blockHeader, true)) {
            chain.getLogger().debug("Save gengsis txs fail");
            return false;
        }
        chain.getLogger().debug("Save gengsis txs success");
        return true;
    }

    @Override
    public boolean saveTxList(Chain chain, List<String> txStrList, List<String> contractList, String blockHeader) throws NulsException {
        if (null == chain || txStrList == null || txStrList.size() == 0) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        //Trading smart contracts(If there is any)Insert from the second to last transaction in the block transaction
        if(contractList.size() > 0){
            int ide = txStrList.size() - 1;
            String lastTxStr = txStrList.get(ide);
            //If the last transaction of the block is a smart contract returnGASTransaction, Then thecontractListtransaction, Before joining this transaction,Otherwise, join the end of the team directly
            if (TxUtil.extractTxTypeFromTx(lastTxStr) == TxType.CONTRACT_RETURN_GAS) {
                txStrList.remove(ide);
                txStrList.addAll(contractList);
                txStrList.add(lastTxStr);
            }else{
                txStrList.addAll(contractList);
            }
        }
        try {
            return saveBlockTxList(chain, txStrList, blockHeader, false);
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    private boolean saveBlockTxList(Chain chain, List<String> txStrList, String blockHeaderStr, boolean gengsis) {
        long start = NulsDateUtils.getCurrentTimeMillis();
        List<Transaction> txList = new ArrayList<>();
        int chainId = chain.getChainId();
        List<byte[]> txHashs = new ArrayList<>();
        //Assemble unified validation parameter data,keyUnify validators for each modulecmd
        Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        BlockHeader blockHeader;
        NulsLogger logger = chain.getLogger();
        List<String> crossChainTxList = new ArrayList<>();
        try {
            blockHeader = TxUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);
            logger.debug("[Save Block] start -----height:{} -----quantity:{}", blockHeader.getHeight(), txStrList.size());
            for (String txStr : txStrList) {
                Transaction tx =TxUtil.getInstanceRpcStr(txStr, Transaction.class);
                txList.add(tx);
                tx.setBlockHeight(blockHeader.getHeight());
                txHashs.add(tx.getHash().getBytes());
                if(TxManager.isSystemSmartContract(chain, tx.getType())) {
                    continue;
                }
                // add by pierre at 2019-12-01 holdtype10Transaction sent to contract module for filtering and processing
                if(TxType.CROSS_CHAIN == tx.getType()) {
                    crossChainTxList.add(txStr);
                }
                // end code by pierre
                TxUtil.moduleGroups(chain, moduleVerifyMap, tx.getType(), txStr);
            }
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
        logger.debug("[Save Block] Assembly data execution time:{}", NulsDateUtils.getCurrentTimeMillis() - start);

        long dbStart = NulsDateUtils.getCurrentTimeMillis();
        if (!saveTxs(chain, txList, blockHeader.getHeight(), true)) {
            return false;
        }
        logger.debug("[Save Block] Confirmed transactionsDB execution time:{}", NulsDateUtils.getCurrentTimeMillis()- dbStart);

        // add by pierre at 2019-12-01 holdtype10Transaction sent to contract module for filtering and processing requires protocol upgrade done
        /*if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_V250
                && !crossChainTxList.isEmpty() && txConfig.isCollectedSmartContractModule()) {
            List<String> contractList = moduleVerifyMap.computeIfAbsent(ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.SC.abbr), code -> new ArrayList<>());
            contractList.addAll(crossChainTxList);
        } After the module merge, all transactions are in onemoduleIn the middle, there is no need to add it separately, otherwise it will result in duplicate transactions*/
        // end code by pierre
        long commitStart = NulsDateUtils.getCurrentTimeMillis();
        if (!commitTxs(chain, moduleVerifyMap, blockHeaderStr, true)) {
            removeTxs(chain, txList, blockHeader.getHeight(), false);
            return false;
        }
        logger.debug("[Save Block] Transaction business submission execution time:{}", NulsDateUtils.getCurrentTimeMillis() - commitStart);

        long ledgerStart = NulsDateUtils.getCurrentTimeMillis();
        if (!commitLedger(chain, txStrList, blockHeader.getHeight())) {
            if (!gengsis) {
                rollbackTxs(chain, moduleVerifyMap, blockHeaderStr, false);
            }
            removeTxs(chain, txList, blockHeader.getHeight(), false);
            return false;
        }
        logger.debug("[Save Block] Ledger module submission execution time:{}", NulsDateUtils.getCurrentTimeMillis() - ledgerStart);

        //If the transaction is confirmed to be successful, delete the transaction from the unpackaged transaction library
        unconfirmedTxStorageService.removeTxList(chainId, txHashs);
        //From pending packagingmapDelete in
        packablePool.clearConfirmedTxs(chain, txHashs);
        logger.debug("[Save Block] Total execution time:{} - height:{}, - Transaction quantity:{}" + TxUtil.nextLine(),
                NulsDateUtils.getCurrentTimeMillis() - start, blockHeader.getHeight(), txList.size());
        return true;
    }

    private boolean saveTxs(Chain chain, List<Transaction> txList, long blockHeight, boolean atomicity) {
        boolean rs = true;
        List<TransactionConfirmedPO> toSaveList = new ArrayList<>();
        for (Transaction tx : txList) {
            tx.setStatus(TxStatusEnum.CONFIRMED);
            TransactionConfirmedPO txConfirmedPO = new TransactionConfirmedPO(tx, blockHeight, TxStatusEnum.CONFIRMED.getStatus());
            toSaveList.add(txConfirmedPO);
        }
        if(!confirmedTxStorageService.saveTxList(chain.getChainId(), toSaveList)){
            if (atomicity) {
                removeTxs(chain, txList, blockHeight, false);
            }
            rs = false;
            chain.getLogger().debug("save block Txs rocksdb failed! ");
        }
        return rs;
    }

    private boolean commitTxs(Chain chain, Map<String, List<String>> moduleVerifyMap, String blockHeader, boolean atomicity) {
        //Call the trading module uniformlycommitinterface batch
        Map<String, List<String>> successed = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        boolean result = true;
        for (Map.Entry<String, List<String>> entry : moduleVerifyMap.entrySet()) {
            boolean rs = TransactionCall.txProcess(chain, BaseConstant.TX_COMMIT,
                    entry.getKey(), entry.getValue(), blockHeader);
            if (!rs) {
                result = false;
                chain.getLogger().error("save tx failed! commitTxs");
                break;
            }
            successed.put(entry.getKey(), entry.getValue());
        }
        if (!result && atomicity) {
            rollbackTxs(chain, successed, blockHeader, false);
            return false;
        }
        return true;
    }

    private boolean commitLedger(Chain chain, List<String> txList, long blockHeight) {
        try {
            chain.getPackableState().set(false);
            boolean rs = LedgerCall.commitTxsLedger(chain, txList, blockHeight);
            if(!rs){
                chain.getLogger().error("save block tx failed! commitLedger");
            }
            return rs;
        } catch (NulsException e) {
            chain.getLogger().error("failed! commitLedger");
            chain.getLogger().error(e);
            return false;
        }finally {
            chain.getPackableState().set(true);
        }
    }

    private boolean removeTxs(Chain chain, List<Transaction> txList, long blockheight, boolean atomicity) {
        boolean rs = true;
        if(!confirmedTxStorageService.removeTxList(chain.getChainId(), txList) && atomicity ){
            saveTxs(chain, txList, blockheight, false);
            rs = false;
            chain.getLogger().debug("failed! removeTxs");
        }
        return rs;
    }

    private boolean rollbackTxs(Chain chain, Map<String, List<String>> moduleVerifyMap, String blockHeader, boolean atomicity) {
        Map<String, List<String>> successed = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        boolean result = true;
        for (Map.Entry<String, List<String>> entry : moduleVerifyMap.entrySet()) {
            chain.getLogger().info("[Rollback], module: {}, txCount: {}", entry.getKey(), entry.getValue() == null ? 0 : entry.getValue().size());
            boolean rs = TransactionCall.txProcess(chain, BaseConstant.TX_ROLLBACK,
                    entry.getKey(), entry.getValue(), blockHeader);
            if (!rs) {
                result = false;
                chain.getLogger().error("failed! rollbackcommitTxs ");
                break;
            }
            successed.put(entry.getKey(), entry.getValue());
        }
        if (!result && atomicity) {
            commitTxs(chain, successed, blockHeader, false);
            return false;
        }
        return true;
    }

    private boolean rollbackLedger(Chain chain, List<String> txList, Long blockHeight) {
        if (txList.isEmpty()) {
            return true;
        }
        try {
            chain.getPackableState().set(false);
            boolean rs =  LedgerCall.rollbackTxsLedger(chain, txList, blockHeight);
            if(!rs){
                chain.getLogger().error("rollback block tx failed! rollbackLedger");
            }
            return rs;
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return false;
        }finally {
            chain.getPackableState().set(true);
        }
    }

    @Override
    public boolean rollbackTxList(Chain chain, List<NulsHash> txHashList, String blockHeaderStr) throws NulsException {
        NulsLogger logger =  chain.getLogger();
        if (txHashList == null || txHashList.isEmpty()) {
            throw new NulsException(TxErrorCode.PARAMETER_ERROR);
        }
        int chainId = chain.getChainId();
        BlockHeader blockHeader = TxUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);
        long blockHeight = blockHeader.getHeight();
        logger.info("start rollbackTxList block height:{}", blockHeight);
        long start = NulsDateUtils.getCurrentTimeMillis();
        List<Transaction> txList = new ArrayList<>();
        List<String> txStrList = new ArrayList<>();
        List<String> crossChainTxList = new ArrayList<>();
        //Assemble unified validation parameter data,keyUnify validators for each modulecmd
        Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        try {
            for (NulsHash hash : txHashList) {
                TransactionConfirmedPO txPO = confirmedTxStorageService.getTx(chainId, hash);
                if (null == txPO) {
                    //Skipping the rolled back transaction without being detected may cause the block to be interrupted midway during save, resulting in incomplete save
                    continue;
                }
                Transaction tx = txPO.getTx();
                txList.add(tx);
                String txStr = RPCUtil.encode(tx.serialize());
                txStrList.add(txStr);
                // add by pierre at 2019-12-01 holdtype10Transaction sent to contract module for filtering and processing
                if(TxType.CROSS_CHAIN == tx.getType()) {
                    crossChainTxList.add(txStr);
                }
                // end code by pierre
                TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
            }
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
        if(txList.isEmpty()){
            logger.error("[rollback error] block txs is empty . -hight:{}", blockHeight);
            return false;
        }
        logger.debug("[Rolling back blocks] Assembly data execution time:{}", NulsDateUtils.getCurrentTimeMillis() - start);

        long ledgerStart = NulsDateUtils.getCurrentTimeMillis();
        if (!rollbackLedger(chain, txStrList, blockHeight)) {
            return false;
        }
        logger.debug("[Rolling back blocks] Rollback ledger execution time:{}", NulsDateUtils.getCurrentTimeMillis() - ledgerStart);

        // add by pierre at 2019-12-01 holdtype10Transaction sent to contract module for filtering and processing requires protocol upgrade done
        /*if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_V250
                && !crossChainTxList.isEmpty() && txConfig.isCollectedSmartContractModule()) {
            List<String> contractList = moduleVerifyMap.computeIfAbsent(ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.SC.abbr), code -> new ArrayList<>());
            contractList.addAll(crossChainTxList);
        } After the module merge, all transactions are in onemoduleIn the middle, there is no need to add it separately, otherwise it will result in duplicate transactions*/
        // end code by pierre
        long moduleStart = NulsDateUtils.getCurrentTimeMillis();
        if (!rollbackTxs(chain, moduleVerifyMap, blockHeaderStr, true)) {
            commitLedger(chain, txStrList, blockHeight);
            return false;
        }
        logger.debug("[Rolling back blocks] Rollback transaction business data execution time:{}", NulsDateUtils.getCurrentTimeMillis() - moduleStart);


        long dbStart = NulsDateUtils.getCurrentTimeMillis();
        if (!removeTxs(chain, txList, blockHeight, true)) {
            commitTxs(chain, moduleVerifyMap, blockHeaderStr, false);
            saveTxs(chain, txList, blockHeight, false);
            return false;
        }
        //Calculate the size of the queue to be packaged and place it in the unconfirmed library in reverse order, And the queue to be packaged

        int packableTxMapDataSize = 0;
        if(chain.getPackaging().get()) {
            //It is the packaging node that calculates the current capacity of the queue to be packaged
            for (Transaction tx : chain.getPackableTxMap().values()) {
                packableTxMapDataSize += tx.size();
            }
        }
        for (int i = txList.size() - 1; i >= 0; i--) {
            Transaction tx = txList.get(i);
            if(!TxManager.isSystemTx(chain, tx)) {
                unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
                //Not a system transaction,And the node is a packaging node,If the packaging queue does not reach its maximum value, it will be placed back at the forefront of the packaging queue
                if (chain.getPackaging().get() && packableTxMapDataSize < TxConstant.PACKABLE_TX_MAP_MAX_DATA_SIZE) {
                    packablePool.offerFirst(chain, tx);
                } 
            }
        }
        logger.debug("[Rolling back blocks] Rollback removalDBStored transactions, Put into unconfirmed library execution time:{}", NulsDateUtils.getCurrentTimeMillis() - dbStart);
        logger.info("rollbackTxList success block height:{}", blockHeight);
        return true;
    }

    /**
     * Batch implementation
     * @param chain
     * @param hashList
     * @return
     */
    @Override
    public List<String> getTxList(Chain chain, List<String> hashList) {
        List<String> txStrList = new ArrayList<>();
        if (hashList == null || hashList.size() == 0) {
            return txStrList;
        }
        int chainId = chain.getChainId();
        List<byte[]> keys = new ArrayList<>();
        for(String hashHex : hashList){
            keys.add(HexUtil.decode(hashHex));
        }
        List<Transaction> txList = confirmedTxStorageService.getTxList(chainId, keys);
        //Must hit all
        if(txList.size() != hashList.size()){
            return txStrList;
        }
        Map<String, String> map = new HashMap<>(txList.size() * 2);
        try {
            for(Transaction tx : txList){
                map.put(tx.getHash().toHex(), RPCUtil.encode(tx.serialize()));
            }
        } catch (IOException e) {
            chain.getLogger().error(e);
            return new ArrayList<>();
        }
        //Order and parameters returnedlistinhashThe order should be consistent
        for(String hash : hashList){
            txStrList.add(map.get(hash));
        }

        return txStrList;
    }

    @Override
    public List<String> getTxListExtend(Chain chain, List<String> hashList, boolean allHits) {
        List<String> txStrList = new ArrayList<>();
        if (hashList == null || hashList.size() == 0) {
            return txStrList;
        }
        int chainId = chain.getChainId();
        List<byte[]> keys = new ArrayList<>();
        for(String hashHex : hashList){
            keys.add(HexUtil.decode(hashHex));
        }
        List<Transaction> txConfirmedList = confirmedTxStorageService.getTxList(chainId,keys);
        List<Transaction> txUnconfirmedList = unconfirmedTxStorageService.getTxList(chainId,keys);
        Set<Transaction> allTx = new HashSet<>();
        allTx.addAll(txConfirmedList);
        allTx.addAll(txUnconfirmedList);
        if(allHits && allTx.size() != hashList.size()){
            //allHitsbytrueOnce there is one that has not been obtained, Directly return emptylist
            return new ArrayList<>();
        }
        //InsertmapValue used for sorting in the middle
        Map<String, String> map = new HashMap<>(allTx.size() * 2);
        try {
            for(Transaction tx : allTx){
                map.put(tx.getHash().toHex(), RPCUtil.encode(tx.serialize()));
                //txStrList.add(RPCUtil.encode(tx.serialize()));
            }
        } catch (IOException e) {
            chain.getLogger().error(e);
            if(allHits) {
                //allHitsbytrueDirectly return empty whenlist
                return new ArrayList<>();
            }
        }
        //Order and parameters returnedlistinhashThe order should be consistent
        for(String hash : hashList){
            String txHex = map.get(hash);
            if(null != txHex) {
                txStrList.add(txHex);
            }
        }
        return txStrList;
    }

    @Override
    public List<String> getNonexistentUnconfirmedHashList(Chain chain, List<String> hashList) {
        List<String> txHashList = new ArrayList<>();
        if (hashList == null || hashList.size() == 0) {
            return txHashList;
        }
        int chainId = chain.getChainId();
        List<byte[]> keys = new ArrayList<>();
        for(String hashHex : hashList){
            keys.add(HexUtil.decode(hashHex));
        }
        //Obtain detectable transactions
        List<String> txUnconfirmedList = unconfirmedTxStorageService.getExistKeysStr(chainId,keys);
        for(String hash : hashList){
            if(txUnconfirmedList.contains(hash)){
                continue;
            }
            //Only addtxUnconfirmedListNon-existent inhash
            txHashList.add(hash);
        }
        return txHashList;
    }

}
