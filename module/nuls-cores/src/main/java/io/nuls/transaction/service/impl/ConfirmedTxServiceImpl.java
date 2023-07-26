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
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConfig;
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
    private TxConfig txConfig;

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
        //将智能合约交易(如果有)，从区块交易倒数第二个交易后插入
        if(contractList.size() > 0){
            int ide = txStrList.size() - 1;
            String lastTxStr = txStrList.get(ide);
            //如果区块最后一笔交易是智能合约返还GAS的交易, 则将contractList交易, 加入该交易之前,否则直接加入队尾
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
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        BlockHeader blockHeader;
        NulsLogger logger = chain.getLogger();
        List<String> crossChainTxList = new ArrayList<>();
        try {
            blockHeader = TxUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);
            logger.debug("[保存区块] 开始 -----高度:{} -----数量:{}", blockHeader.getHeight(), txStrList.size());
            for (String txStr : txStrList) {
                Transaction tx =TxUtil.getInstanceRpcStr(txStr, Transaction.class);
                txList.add(tx);
                tx.setBlockHeight(blockHeader.getHeight());
                txHashs.add(tx.getHash().getBytes());
                if(TxManager.isSystemSmartContract(chain, tx.getType())) {
                    continue;
                }
                // add by pierre at 2019-12-01 把type10交易发送到合约模块筛选处理
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
        logger.debug("[保存区块] 组装数据 执行时间:{}", NulsDateUtils.getCurrentTimeMillis() - start);

        long dbStart = NulsDateUtils.getCurrentTimeMillis();
        if (!saveTxs(chain, txList, blockHeader.getHeight(), true)) {
            return false;
        }
        logger.debug("[保存区块] 存已确认交易DB 执行时间:{}", NulsDateUtils.getCurrentTimeMillis()- dbStart);

        // add by pierre at 2019-12-01 把type10交易发送到合约模块筛选处理，需要协议升级 done
        if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_V250
                && !crossChainTxList.isEmpty() && txConfig.isCollectedSmartContractModule()) {
            List<String> contractList = moduleVerifyMap.computeIfAbsent(ModuleE.SC.abbr, code -> new ArrayList<>());
            contractList.addAll(crossChainTxList);
        }
        // end code by pierre
        long commitStart = NulsDateUtils.getCurrentTimeMillis();
        if (!commitTxs(chain, moduleVerifyMap, blockHeaderStr, true)) {
            removeTxs(chain, txList, blockHeader.getHeight(), false);
            return false;
        }
        logger.debug("[保存区块] 交易业务提交 执行时间:{}", NulsDateUtils.getCurrentTimeMillis() - commitStart);

        long ledgerStart = NulsDateUtils.getCurrentTimeMillis();
        if (!commitLedger(chain, txStrList, blockHeader.getHeight())) {
            if (!gengsis) {
                rollbackTxs(chain, moduleVerifyMap, blockHeaderStr, false);
            }
            removeTxs(chain, txList, blockHeader.getHeight(), false);
            return false;
        }
        logger.debug("[保存区块] 账本模块提交 执行时间:{}", NulsDateUtils.getCurrentTimeMillis() - ledgerStart);

        //如果确认交易成功，则从未打包交易库中删除交易
        unconfirmedTxStorageService.removeTxList(chainId, txHashs);
        //从待打包map中删除
        packablePool.clearConfirmedTxs(chain, txHashs);
        logger.debug("[保存区块] 合计执行时间:{} - 高度:{}, - 交易数量:{}" + TxUtil.nextLine(),
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
        //调用交易模块统一commit接口 批量
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
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        try {
            for (NulsHash hash : txHashList) {
                TransactionConfirmedPO txPO = confirmedTxStorageService.getTx(chainId, hash);
                if (null == txPO) {
                    //回滚的交易没有查出来就跳过，保存时该块可能中途中断，导致保存不全
                    continue;
                }
                Transaction tx = txPO.getTx();
                txList.add(tx);
                String txStr = RPCUtil.encode(tx.serialize());
                txStrList.add(txStr);
                // add by pierre at 2019-12-01 把type10交易发送到合约模块筛选处理
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
        logger.debug("[回滚区块] 组装数据 执行时间:{}", NulsDateUtils.getCurrentTimeMillis() - start);

        long ledgerStart = NulsDateUtils.getCurrentTimeMillis();
        if (!rollbackLedger(chain, txStrList, blockHeight)) {
            return false;
        }
        logger.debug("[回滚区块] 回滚账本 执行时间:{}", NulsDateUtils.getCurrentTimeMillis() - ledgerStart);

        // add by pierre at 2019-12-01 把type10交易发送到合约模块筛选处理，需要协议升级 done
        if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_V250
                && !crossChainTxList.isEmpty() && txConfig.isCollectedSmartContractModule()) {
            List<String> contractList = moduleVerifyMap.computeIfAbsent(ModuleE.SC.abbr, code -> new ArrayList<>());
            contractList.addAll(crossChainTxList);
        }
        // end code by pierre
        long moduleStart = NulsDateUtils.getCurrentTimeMillis();
        if (!rollbackTxs(chain, moduleVerifyMap, blockHeaderStr, true)) {
            commitLedger(chain, txStrList, blockHeight);
            return false;
        }
        logger.debug("[回滚区块] 回滚交易业务数据 执行时间:{}", NulsDateUtils.getCurrentTimeMillis() - moduleStart);


        long dbStart = NulsDateUtils.getCurrentTimeMillis();
        if (!removeTxs(chain, txList, blockHeight, true)) {
            commitTxs(chain, moduleVerifyMap, blockHeaderStr, false);
            saveTxs(chain, txList, blockHeight, false);
            return false;
        }
        //计算待打包队列大小倒序放入未确认库, 和待打包队列

        int packableTxMapDataSize = 0;
        if(chain.getPackaging().get()) {
            //是打包节点才计算待打包队列的当前容量
            for (Transaction tx : chain.getPackableTxMap().values()) {
                packableTxMapDataSize += tx.size();
            }
        }
        for (int i = txList.size() - 1; i >= 0; i--) {
            Transaction tx = txList.get(i);
            if(!TxManager.isSystemTx(chain, tx)) {
                unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
                //不是系统交易,并且节点是打包节点,待打包队列没到最大值则重新放回待打包队列的最前端
                if (chain.getPackaging().get() && packableTxMapDataSize < TxConstant.PACKABLE_TX_MAP_MAX_DATA_SIZE) {
                    packablePool.offerFirst(chain, tx);
                } 
            }
        }
        logger.debug("[回滚区块] 回滚移除DB已存储的交易, 放入未确认库 执行时间:{}", NulsDateUtils.getCurrentTimeMillis() - dbStart);
        logger.info("rollbackTxList success block height:{}", blockHeight);
        return true;
    }

    /**
     * 批量实现
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
        //必须全部命中
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
        //返回的顺序和参数list中hash顺序要一致
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
            //allHits为true时一旦有一个没有获取到, 直接返回空list
            return new ArrayList<>();
        }
        //放入map中用于排序时取值
        Map<String, String> map = new HashMap<>(allTx.size() * 2);
        try {
            for(Transaction tx : allTx){
                map.put(tx.getHash().toHex(), RPCUtil.encode(tx.serialize()));
                //txStrList.add(RPCUtil.encode(tx.serialize()));
            }
        } catch (IOException e) {
            chain.getLogger().error(e);
            if(allHits) {
                //allHits为true时直接返回空list
                return new ArrayList<>();
            }
        }
        //返回的顺序和参数list中hash顺序要一致
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
        //获取能查出来的交易
        List<String> txUnconfirmedList = unconfirmedTxStorageService.getExistKeysStr(chainId,keys);
        for(String hash : hashList){
            if(txUnconfirmedList.contains(hash)){
                continue;
            }
            //只添加txUnconfirmedList中不存在的hash
            txHashList.add(hash);
        }
        return txHashList;
    }

}
