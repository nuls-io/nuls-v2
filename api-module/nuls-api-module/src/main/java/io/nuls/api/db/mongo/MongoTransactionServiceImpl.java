package io.nuls.api.db.mongo;

import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.nuls.api.model.po.db.*;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.*;
import static io.nuls.api.constant.MongoTableConstant.*;


@Component
public class MongoTransactionServiceImpl {

    @Autowired
    private MongoDBService mongoDBService;

    @Autowired
    private MongoBlockServiceImpl mongoBlockServiceImpl;

    public void initSelect(int chainId) {

    }

    public void saveTxList(int chainId, List<TransactionInfo> txList) {
        if (txList.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (TransactionInfo transactionInfo : txList) {
            documentList.add(transactionInfo.toDocument());
        }
        mongoDBService.insertMany(TX_TABLE + chainId, documentList);
    }

    public void saveCoinDataList(int chainId, List<CoinDataInfo> coinDataList) {
        if (coinDataList.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (CoinDataInfo info : coinDataList) {
            documentList.add(info.toDocument());
        }
        mongoDBService.insertMany(COINDATA_TABLE + chainId, documentList);
    }

    public void saveTxRelationList(int chainId, Set<TxRelationInfo> relationInfos) {
        if (relationInfos.isEmpty()) {
            return;
        }

        List<Document> documentList = new ArrayList<>();
        for (TxRelationInfo relationInfo : relationInfos) {
            Document document = DocumentTransferTool.toDocument(relationInfo);
            documentList.add(document);
        }

        mongoDBService.insertMany(TX_RELATION_TABLE + chainId, documentList);
    }

    public PageInfo<TransactionInfo> getTxList(int chainId, int pageIndex, int pageSize, int type, boolean isHidden) {
        Bson filter = null;
        if (type > 0) {
            filter = eq("type", type);
        } else if (isHidden) {
            filter = ne("type", 1);
        }
        long totalCount = mongoDBService.getCount(TX_TABLE + chainId, filter);
        List<Document> docList = this.mongoDBService.pageQuery(TX_TABLE + chainId, filter, Sorts.descending("height", "createTime"), pageIndex, pageSize);
        List<TransactionInfo> txList = new ArrayList<>();
        for (Document document : docList) {
            txList.add(TransactionInfo.fromDocument(document));
        }

        PageInfo<TransactionInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, txList);
        return pageInfo;
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
        List<Document> docList = this.mongoDBService.pageQuery(TX_TABLE + chainId, filter, Sorts.descending("height", "time"), pageIndex, pageSize);
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
        mongoDBService.bulkWrite(TX_RELATION_TABLE + chainId, list);
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
}
