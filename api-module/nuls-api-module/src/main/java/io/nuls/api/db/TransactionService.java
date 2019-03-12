package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.*;
import static io.nuls.api.constant.MongoTableConstant.TX_RELATION_TABLE;
import static io.nuls.api.constant.MongoTableConstant.TX_TABLE;


@Component
public class TransactionService {

    @Autowired
    private MongoDBService mongoDBService;

    @Autowired
    private BlockService blockService;

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
        BlockHeaderInfo blockInfo = blockService.getBlockHeader(chainId, blockHeight);
        if (blockInfo == null) {
            return null;
        }
        List<TransactionInfo> txList = new ArrayList<>();
        List<Document> docList = this.mongoDBService.pageQuery(TX_TABLE + chainId, filter, Sorts.descending("height", "time"), pageIndex, pageSize);
        for (Document document : docList) {
            txList.add(TransactionInfo.fromDocument(document));
        }
        PageInfo<TransactionInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, blockInfo.getHeight(), txList);
        return pageInfo;
    }
}
