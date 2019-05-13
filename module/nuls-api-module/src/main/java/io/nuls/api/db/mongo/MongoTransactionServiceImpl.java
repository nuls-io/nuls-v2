package io.nuls.api.db.mongo;

import com.mongodb.client.model.*;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.TransactionService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.po.db.mini.MiniTransactionInfo;
import io.nuls.api.model.rpc.BalanceInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.util.TimeUtils;
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

    @Override
    public void afterPropertiesSet() {
        relationMap = new HashMap<>();
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List<Document> documentList = new ArrayList<>();
            relationMap.put("relation_" + i, documentList);
        }
    }

    public void saveTxList(int chainId, List<TransactionInfo> txList) {
        if (txList.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (TransactionInfo transactionInfo : txList) {
            documentList.add(transactionInfo.toDocument());
            deleteUnConfirmTx(chainId, transactionInfo.getHash());
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
        clear();

        for (TxRelationInfo relationInfo : relationInfos) {
            Document document = DocumentTransferTool.toDocument(relationInfo);
            int i = Math.abs(relationInfo.getAddress().hashCode()) % TX_RELATION_SHARDING_COUNT;
            List<Document> documentList = relationMap.get("relation_" + i);
            documentList.add(document);
        }
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List<Document> documentList = relationMap.get("relation_" + i);
            if (documentList.size() == 0) {
                continue;
            }
            InsertManyOptions options = new InsertManyOptions();
            options.ordered(false);
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
            txList.add(DocumentTransferTool.toInfo(document, "hash", MiniTransactionInfo.class));
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

    public PageInfo<TransactionInfo> getBlockTxList(int chainId, int pageIndex, int pageSize, long blockHeight, int type) {
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
        List<TransactionInfo> txList = new ArrayList<>();
        List<Document> docList = this.mongoDBService.pageQuery(TX_TABLE + chainId, filter, Sorts.descending("createTime"), pageIndex, pageSize);
        for (Document document : docList) {
            txList.add(TransactionInfo.fromDocument(document));
        }
        PageInfo<TransactionInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, count, txList);
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

    public void rollbackTxRelationList(int chainId, List<String> txHashList) {
        if (txHashList.isEmpty()) {
            return;
        }

        List<DeleteManyModel<Document>> list = new ArrayList<>();
        for (String hash : txHashList) {
            DeleteManyModel model = new DeleteManyModel(Filters.eq("txHash", hash));
            list.add(model);
        }
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            mongoDBService.bulkWrite(TX_RELATION_TABLE + chainId + "_" + i, list);
        }
    }

    public void rollbackTx(int chainId, List<String> txHashList) {
        if (txHashList.isEmpty()) {
            return;
        }
        List<DeleteOneModel<Document>> list = new ArrayList<>();
        for (String hash : txHashList) {
            DeleteOneModel<Document> model = new DeleteOneModel(Filters.eq("_id", hash));
            list.add(model);
        }
        mongoDBService.bulkWrite(COINDATA_TABLE + chainId, list);
        mongoDBService.bulkWrite(TX_TABLE + chainId, list);
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
            Document document = DocumentTransferTool.toDocument(relationInfo);
            documentList.add(document);
        }
        mongoDBService.insertMany(TX_UNCONFIRM_RELATION_TABLE + chainId, documentList);
        TxHexInfo hexInfo = new TxHexInfo();
        hexInfo.setTxHash(tx.getHash());
        hexInfo.setTxHex(txHex);
        hexInfo.setTime(TimeUtils.getCurrentTimeMillis());

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
            txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getAmount(), TRANSFER_TO_TYPE, balanceInfo.getTotalBalance()));
        }
    }

    private void processTransferTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), TRANSFER_FROM_TYPE, balanceInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getAmount(), TRANSFER_TO_TYPE, balanceInfo.getTotalBalance()));
            }
        }
    }

    private void processAliasTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        if (tx.getCoinFroms() != null) {
            for (CoinFromInfo input : tx.getCoinFroms()) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), TRANSFER_FROM_TYPE, balanceInfo.getTotalBalance()));
            }
        }
        if (tx.getCoinTos() != null) {
            for (CoinToInfo output : tx.getCoinTos()) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, output.getAddress(), output.getChainId(), output.getAssetsId());
                txRelationInfoSet.add(new TxRelationInfo(output.getAddress(), tx, output.getChainId(), output.getAssetsId(), output.getAmount(), TRANSFER_TO_TYPE, balanceInfo.getTotalBalance()));
            }
        }
    }

    private void processCreateAgentTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), TRANSFER_NO_TYPE, balanceInfo.getTotalBalance()));
    }

    private void processDepositTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), TRANSFER_NO_TYPE, balanceInfo.getTotalBalance()));
    }

    private void processCancelDepositTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), input.getAmount(), TRANSFER_NO_TYPE, balanceInfo.getTotalBalance()));
    }

    private void processStopAgentTx(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        Map<String, BigInteger> maps = new HashMap<>();
        for (int i = 0; i < tx.getCoinTos().size(); i++) {
            CoinToInfo output = tx.getCoinTos().get(i);
            BigInteger values = maps.get(output.getAddress());
            if (values == null) {
                values = BigInteger.ZERO;
            }
            values = values.add(output.getAmount());
            maps.put(output.getAddress(), values);
        }
        ApiCache apiCache = CacheManager.getCache(chainId);
        AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
        for (Map.Entry<String, BigInteger> entry : maps.entrySet()) {
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, entry.getKey(), defaultAsset.getChainId(), defaultAsset.getAssetId());
            txRelationInfoSet.add(new TxRelationInfo(entry.getKey(), tx, defaultAsset.getChainId(), defaultAsset.getAssetId(), entry.getValue(), TRANSFER_NO_TYPE, balanceInfo.getTotalBalance()));
        }
    }

    private void processCreateContract(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), BigInteger.ZERO, TRANSFER_NO_TYPE, balanceInfo.getTotalBalance()));
    }

    private void processCallContract(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        processTransferTx(chainId, tx, txRelationInfoSet);
    }

    private void processDeleteContract(int chainId, TransactionInfo tx, Set<TxRelationInfo> txRelationInfoSet) {
        CoinFromInfo input = tx.getCoinFroms().get(0);
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, input.getAddress(), input.getChainId(), input.getAssetsId());
        txRelationInfoSet.add(new TxRelationInfo(input.getAddress(), tx, input.getChainId(), input.getAssetsId(), BigInteger.ZERO, TRANSFER_NO_TYPE, balanceInfo.getTotalBalance()));
    }

    private void clear() {
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            List list = relationMap.get("relation_" + i);
            list.clear();
        }
    }
}
