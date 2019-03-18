package io.nuls.api.db;


import com.mongodb.client.model.Filters;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.ChainInfo;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.api.constant.MongoTableConstant.CHAIN_INFO_TABLE;
import static io.nuls.api.constant.MongoTableConstant.SYNC_INFO_TABLE;

@Component
public class ChainService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initCache() {
        List<Document> documentList = mongoDBService.query(CHAIN_INFO_TABLE);
        for (Document document : documentList) {
            ChainInfo chainInfo = ChainInfo.toInfo(document);
            CacheManager.initCache(chainInfo);
        }
    }

    public List<ChainInfo> getChainInfoList() {
        List<Document> documentList = mongoDBService.query(CHAIN_INFO_TABLE);
        if (documentList.isEmpty()) {
            return null;
        }
        List<ChainInfo> chainList = new ArrayList<>();
        for (Document document : documentList) {
            chainList.add(ChainInfo.toInfo(document));
        }
        return chainList;
    }

    public void addChainInfo(ChainInfo chainInfo) {
        Document document = chainInfo.toDocument();
        mongoDBService.insertOne(CHAIN_INFO_TABLE, document);
        CacheManager.initCache(chainInfo);
    }

    public ChainInfo getChainInfo(int chainId) {
        return CacheManager.getChainInfo(chainId);
    }

    public SyncInfo getSyncInfo(int chainId) {
        Document document = mongoDBService.findOne(SYNC_INFO_TABLE, Filters.eq("_id", chainId));
        if (document == null) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "chainId", SyncInfo.class);
    }

    public void saveNewSyncInfo(int chainId, long newHeight) {
        SyncInfo syncInfo = new SyncInfo(chainId, newHeight, false, 0);
        Document document = DocumentTransferTool.toDocument(syncInfo, "chainId");
        if (newHeight == 0) {
            mongoDBService.insertOne(SYNC_INFO_TABLE, document);
        } else {
            Bson query = Filters.eq("_id", chainId);
            mongoDBService.updateOne(SYNC_INFO_TABLE, query, document);
        }
    }

    public void updateStep(int chainId, long height, int step) {
        SyncInfo syncInfo = new SyncInfo(chainId, height, false, step);
        Document document = DocumentTransferTool.toDocument(syncInfo, "chainId");
        Bson query = Filters.eq("_id", chainId);
        mongoDBService.updateOne(SYNC_INFO_TABLE, query, document);
    }

    public void syncComplete(int chainId, long height, int step) {
        SyncInfo syncInfo = new SyncInfo(chainId, height, true, step);
        Document document = DocumentTransferTool.toDocument(syncInfo, "chainId");
        Bson query = Filters.eq("_id", chainId);
        mongoDBService.updateOne(SYNC_INFO_TABLE, query, document);
    }

    public void rollbackComplete(int chainId) {
        Bson query = Filters.eq("_id", chainId);
        Document document = mongoDBService.findOne(SYNC_INFO_TABLE + chainId, Filters.eq("_id", chainId));
        document.put("height", document.getLong("height") - 1);
        document.put("finish", true);
        if (document.getLong("height") < 0) {
            mongoDBService.delete(SYNC_INFO_TABLE + chainId, query);
        } else {
            mongoDBService.updateOne(SYNC_INFO_TABLE + chainId, query, document);
        }
    }
}
