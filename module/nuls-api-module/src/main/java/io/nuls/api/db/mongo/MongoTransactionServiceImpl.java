package io.nuls.api.db.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.*;
import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.TransactionService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.po.db.mini.MiniTransactionInfo;
import io.nuls.api.model.rpc.BalanceInfo;
import io.nuls.api.utils.DBUtil;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.util.NulsDateUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigInteger;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static io.nuls.api.constant.ApiConstant.*;
import static io.nuls.api.constant.DBTableConstant.*;

@Component
public class MongoTransactionServiceImpl implements TransactionService, InitializingBean {

    @Autowired
    private MongoDBService mongoDBService;

    @Autowired
    private MongoBlockServiceImpl mongoBlockServiceImpl;

    Map<String, List<Document>> relationMap;
    Map<String, List<String>> deleteRelationMap;
//    Map<String, List<DeleteManyModel<Document>>> deleteRelationMap;

    @Override
    public void afterPropertiesSet() {
        relationMap = new HashMap<>();
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List<Document> documentList = new ArrayList<>();
            relationMap.put("relation_" + i, documentList);
        }

        deleteRelationMap = new HashMap<>();
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List<String> modelList = new ArrayList<>();
            deleteRelationMap.put("relation_" + i, modelList);
        }

//        deleteRelationMap = new HashMap<>();
//        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
//            List<DeleteManyModel<Document>> modelList = new ArrayList<>();
//            deleteRelationMap.put("relation_" + i, modelList);
//        }
    }

    //tx_table只存储最近100万条数据
    public void saveTxList(int chainId, List<TransactionInfo> txList) {
        if (txList.isEmpty()) {
            return;
        }

        List<Document> documentList = new ArrayList<>();
        for (TransactionInfo transactionInfo : txList) {
            documentList.add(transactionInfo.toDocument());
            deleteUnConfirmTx(chainId, transactionInfo.getHash());
        }

        long totalCount = mongoDBService.getCount(TX_TABLE + chainId);
        totalCount += documentList.size();
        if (totalCount > 1000000) {
            int deleteCount = (int) (totalCount - 1000000);
            BasicDBObject fields = new BasicDBObject();
            fields.append("_id", 1);
            List<Document> docList = this.mongoDBService.pageQuery(TX_TABLE + chainId, null, fields, Sorts.ascending("createTime"), 1, deleteCount);
            List<String> hashList = new ArrayList<>();
            for (Document document : docList) {
                hashList.add(document.getString("_id"));
            }
            mongoDBService.delete(TX_TABLE + chainId, Filters.in("_id", hashList));
        }
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(TX_TABLE + chainId, documentList, options);
    }

    public void saveCoinDataList(int chainId, List<CoinDataInfo> coinDataList) {
        if (coinDataList.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (CoinDataInfo info : coinDataList) {
            documentList.add(info.toDocument());
        }
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(COINDATA_TABLE + chainId, documentList, options);
    }

    public void saveTxRelationList(int chainId, Set<TxRelationInfo> relationInfos) {
        if (relationInfos.isEmpty()) {
            return;
        }
        relationMapClear();

        for (TxRelationInfo relationInfo : relationInfos) {
            Document document = relationInfo.toDocument();
            int i = Math.abs(relationInfo.getAddress().hashCode()) % TX_RELATION_SHARDING_COUNT;
            List<Document> documentList = relationMap.get("relation_" + i);
            documentList.add(document);
        }

        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List<Document> documentList = relationMap.get("relation_" + i);
            if (documentList.size() == 0) {
                continue;
            }
            mongoDBService.insertMany(TX_RELATION_TABLE + chainId + "_" + i, documentList, options);
        }
    }

    public PageInfo<MiniTransactionInfo> getTxList(int chainId, int pageIndex, int pageSize, int type, boolean isHidden) {
        Bson filter = null;
        if (type > 0) {
            filter = eq("type", type);
        } else if (isHidden) {
            filter = ne("type", 1);
        }
        long totalCount = mongoDBService.getCount(TX_TABLE + chainId, filter);
        List<Document> docList = this.mongoDBService.pageQuery(TX_TABLE + chainId, filter, Sorts.descending("createTime"), pageIndex, pageSize);
        List<MiniTransactionInfo> txList = new ArrayList<>();
        for (Document document : docList) {
            txList.add(MiniTransactionInfo.toInfo(document));
        }

        PageInfo<MiniTransactionInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, txList);
        return pageInfo;
    }

    @Override
    public List<TxHexInfo> getUnConfirmList(int chainId) {
        List<Document> docList = mongoDBService.query(TX_UNCONFIRM_TABLE + chainId);
        List<TxHexInfo> txHexInfoList = new ArrayList<>();
        for (Document document : docList) {
            TxHexInfo txHexInfo = DocumentTransferTool.toInfo(document, "txHash", TxHexInfo.class);
            txHexInfoList.add(txHexInfo);
        }
        return txHexInfoList;
    }

    public PageInfo<MiniTransactionInfo> getBlockTxList(int chainId, int pageIndex, int pageSize, long blockHeight, int type) {
        Bson filter = null;
        if (type == 0) {
            filter = eq("height", blockHeight);
        } else {
            filter = and(eq("type", type), eq("height", blockHeight));
        }
        BlockHeaderInfo blockInfo = mongoBlockServiceImpl.getBlockHeader(chainId, blockHeight);
        if (blockInfo == null) {
            return null;
        }
        long count = mongoDBService.getCount(TX_TABLE + chainId, filter);
        List<MiniTransactionInfo> txList = new ArrayList<>();
        List<Document> docList = this.mongoDBService.pageQuery(TX_TABLE + chainId, filter, Sorts.descending("createTime"), pageIndex, pageSize);
        for (Document document : docList) {
            txList.add(MiniTransactionInfo.toInfo(document));
        }
        PageInfo<MiniTransactionInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, count, txList);
        return pageInfo;
    }

    public TransactionInfo getTx(int chainId, String txHash) {
        Document document = mongoDBService.findOne(TX_TABLE + chainId, eq("_id", txHash));
        if (null == document) {
            return null;
        }
        TransactionInfo txInfo = TransactionInfo.fromDocument(document);
        document = mongoDBService.findOne(COINDATA_TABLE + chainId, eq("_id", txHash));
        CoinDataInfo coinDataInfo = CoinDataInfo.toInfo(document);
        txInfo.setCoinTos(coinDataInfo.getToList());
        txInfo.setCoinFroms(coinDataInfo.getFromList());
        return txInfo;
    }

//    public void rollbackTxRelationList(int chainId, Set<TxRelationInfo> relationInfos) {
//        if (relationInfos.isEmpty()) {
//            return;
//        }
//
//        long time1, time2;
//        time1 = System.currentTimeMillis();
//        rollbackClear();
//
//        for (TxRelationInfo relationInfo : relationInfos) {
//            DeleteManyModel model = new DeleteManyModel(Filters.eq("txHash", relationInfo.getTxHash()));
//            int i = Math.abs(relationInfo.getAddress().hashCode()) % TX_RELATION_SHARDING_COUNT;
//            List<DeleteManyModel<Document>> list = deleteRelationMap.get("relation_" + i);
//            list.add(model);
//        }
//
//        BulkWriteOptions options = new BulkWriteOptions();
//        options.ordered(false);
//        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
//            List<DeleteManyModel<Document>> list = deleteRelationMap.get("relation_" + i);
//            if (list.size() == 0) {
//                continue;
//            }
//            mongoDBService.bulkWrite(TX_RELATION_TABLE + chainId + "_" + i, list, options);
//        }
//
//        time2 = System.currentTimeMillis();
//        System.out.println("----------rollbackTxRelationList, count:" + relationInfos.size() + "-----------use:" + (time2 - time1));
//    }


    public void rollbackTxRelationList(int chainId, Set<TxRelationInfo> relationInfos) {
        if (relationInfos.isEmpty()) {
            return;
        }
        relationRollbackClear();

        for (TxRelationInfo relationInfo : relationInfos) {
            int i = Math.abs(relationInfo.getAddress().hashCode()) % TX_RELATION_SHARDING_COUNT;
            List<String> list = deleteRelationMap.get("relation_" + i);
            list.add(relationInfo.getTxHash());
        }

        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List<String> list = deleteRelationMap.get("relation_" + i);
            if (list.size() == 0) {
                continue;
            }
            mongoDBService.delete(TX_RELATION_TABLE + chainId + "_" + i, Filters.in("txHash", list));
        }
    }


    /**
     * 这种实现方式，效率低些
     * @param chainId
     * @param txHashList
     */
//    public void rollbackTx(int chainId, List<String> txHashList) {
//        if (txHashList.isEmpty()) {
//            return;
//        }
//        List<DeleteOneModel<Document>> list = new ArrayList<>();
//        for (String hash : txHashList) {
//            DeleteOneModel<Document> model = new DeleteOneModel(Filters.eq("_id", hash));
//            list.add(model);
//        }
//        BulkWriteOptions options = new BulkWriteOptions();
//        options.ordered(false);
//
//        long time1, time2;
//        time1 = System.currentTimeMillis();
//        mongoDBService.bulkWrite(COINDATA_TABLE + chainId, list, options);
//        mongoDBService.bulkWrite(TX_TABLE + chainId, list, options);
//        time2 = System.currentTimeMillis();
//
//        System.out.println("---------rollbackTx count:" + list.size() + ",----use:" + (time2 - time1));
//    }

    /**
     * 这种实现方式，效率高些
     *
     * @param chainId
     * @param txHashList
     */
    public void rollbackTx(int chainId, List<String> txHashList) {
        if (txHashList.isEmpty()) {
            return;
        }
        //   mongoDBService.delete(COINDATA_TABLE + chainId, Filters.in("_id", txHashList));
        mongoDBService.delete(TX_TABLE + chainId, Filters.in("_id", txHashList));
    }

    @Override
    public void saveUnConfirmTx(int chainId, TransactionInfo tx, String txHex) {
        Set<TxRelationInfo> txRelationInfoSet = new HashSet<>();
        if (tx.getType() == TxType.COIN_BASE) {
            processCoinBaseTx(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.TRANSFER) {
            processTransferTx(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.ACCOUNT_ALIAS) {
            processAliasTx(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.REGISTER_AGENT) {
            processCreateAgentTx(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.DEPOSIT) {
            processDepositTx(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.CANCEL_DEPOSIT) {
            processCancelDepositTx(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.STOP_AGENT) {
            processStopAgentTx(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.CREATE_CONTRACT) {
            processCreateContract(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.CALL_CONTRACT) {
            processCallContract(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.DELETE_CONTRACT) {
            processDeleteContract(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.CONTRACT_TRANSFER) {
            processTransferTx(chainId, tx, txRelationInfoSet);
        } else if (tx.getType() == TxType.CONTRACT_RETURN_GAS) {
            processCoinBaseTx(chainId, tx, txRelationInfoSet);
        }

        List<Document> documentList = new ArrayList<>();
        for (TxRelationInfo relationInfo : txRelationInfoSet) {
            documentList.add(relationInfo.toDocument());
        }
        mongoDBService.insertMany(TX_UNCONFIRM_RELATION_TABLE + chainId, documentList);
        TxHexInfo hexInfo = new TxHexInfo();
        hexInfo.setTxHash(tx.getHash());
        hexInfo.setTxHex(txHex);
        hexInfo.setTime(NulsDateUtils.getCurrentTimeMillis());

        Document document = DocumentTransferTool.toDocument(hexInfo, "txHash");
        mongoDBService.insertOne(TX_UNCONFIRM_TABLE + chainId, document);
    }

    @Override
    public void deleteUnConfirmTx(int chainId, String txHash) {
        Bson filter = Filters.eq("txHash", txHash);
        mongoDBService.delete(TX_UNCONFIRM_TABLE + chainId, filter);
        mongoDBService.delete(TX_UNCONFIRM_RELATION_TABLE + chainId, filter);
    }

    private void processCoinBaseTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        if (tx.getCoinTos() == null || tx.getCoinTos().isEmpty()) {
            return;
        }
        for (CoinToInfo output : tx.getCoinTos()) {
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
            txRelationInfoSet.add(new TxRelationInfo(output, tx, balanceInfo.getTotalBalance()));
        }
    }

    private void processTransferTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(input, tx, balanceInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(output, tx, balanceInfo.getTotalBalance()));
            }
        }
    }

    private void processAliasTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(input, tx, balanceInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(output, tx, balanceInfo.getTotalBalance()));
            }
        }
    }

    private void processCreateAgentTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input, tx, tx.getFee().getValue(), balanceInfo.getTotalBalance()));
    }

    private void processDepositTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input, tx, tx.getFee().getValue(), balanceInfo.getTotalBalance()));
    }

    private void processCancelDepositTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input, tx, tx.getFee().getValue(), balanceInfo.getTotalBalance()));
    }

    private void processStopAgentTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        AgentInfo agentInfo = (AgentInfo) tx.getTxData();
        CoinFromInfo agentInput = null;
        //处理代理节点地址相关数据
        for (CoinFromInfo input : tx.getCoinFroms()) {
            if (input.getAddress().equals(agentInfo.getAgentAddress())) {
                agentInput = input;
                break;
            }
        }
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, agentInput.getAddress(), agentInput.getChainId(), agentInput.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(agentInput, tx, tx.getFee().getValue(), balanceInfo.getTotalBalance()));
        //处理其他委托的地址相关数据
        for (int i = 0; i < tx.getCoinTos().size(); i++) {
            CoinToInfo output = tx.getCoinTos().get(i);
            if (!output.getAddress().equals(agentInfo.getAgentAddress())) {
                balanceInfo = WalletRpcHandler.getAccountBalance(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(output, tx, BigInteger.ZERO, balanceInfo.getTotalBalance()));
            }
        }
    }

    private void processCreateContract(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input, tx, tx.getFee().getValue(), balanceInfo.getTotalBalance()));
    }

    private void processCallContract(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        processTransferTx(chainId, tx, txRelationInfoSet);
    }

    private void processDeleteContract(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input, tx, tx.getFee().getValue(), balanceInfo.getTotalBalance()));
    }

    private void relationMapClear() {
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List list = relationMap.get("relation_" + i);
            list.clear();
        }
    }

    private void relationRollbackClear() {
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List list = deleteRelationMap.get("relation_" + i);
            list.clear();
        }
    }

}
