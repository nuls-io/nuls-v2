package io.nuls.api.db;


import com.mongodb.client.model.Filters;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.manager.ChainManager;
import io.nuls.api.model.po.db.ChainInfo;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

@Component
public class ChainService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initCache() {
        List<Document> documentList = mongoDBService.query(MongoTableConstant.CHAIN_INFO_TABLE);
        for (Document document : documentList) {
            ChainInfo chainInfo = DocumentTransferTool.toInfo(document, "chainId", ChainInfo.class);
            CacheManager.initCache(chainInfo.getChainId());
            CacheManager.addChainInfo(chainInfo);
        }
    }

    public ChainInfo getChainInfo(int chainId) {
        return CacheManager.getChainInfo(chainId);
    }

    public SyncInfo getSyncInfo(int chainId) {
        Document document = mongoDBService.findOne(MongoTableConstant.SYNC_INFO_TABLE, Filters.eq("_id", chainId));
        if (document == null) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "chainId", SyncInfo.class);
    }

    public void saveNewSyncInfo(int chainId, long newHeight) {
        SyncInfo syncInfo = new SyncInfo(chainId, newHeight, false, 0);
        Document document = DocumentTransferTool.toDocument(syncInfo, "chainId");
        if (newHeight == 0) {
            mongoDBService.insertOne(MongoTableConstant.SYNC_INFO_TABLE, document);
        } else {
            Bson query = Filters.eq("_id", chainId);
            mongoDBService.updateOne(MongoTableConstant.SYNC_INFO_TABLE, query, document);
        }
    }

    public void updateStep(int chainId, long height, int step) {
        SyncInfo syncInfo = new SyncInfo(chainId, height, false, step);
        Document document = DocumentTransferTool.toDocument(syncInfo, "chainId");
        Bson query = Filters.eq("_id", chainId);
        mongoDBService.updateOne(MongoTableConstant.SYNC_INFO_TABLE, query, document);
    }

    public void syncComplete(int chainId, long height, int step) {
        SyncInfo syncInfo = new SyncInfo(chainId, height, true, step);
        Document document = DocumentTransferTool.toDocument(syncInfo, "chainId");
        Bson query = Filters.eq("_id", chainId);
        mongoDBService.updateOne(MongoTableConstant.SYNC_INFO_TABLE, query, document);
    }
}
