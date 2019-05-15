package io.nuls.api.db.mongo;


import com.mongodb.client.model.Filters;
import io.nuls.api.db.ChainService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.ChainInfo;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.api.constant.DBTableConstant.CHAIN_INFO_TABLE;
import static io.nuls.api.constant.DBTableConstant.SYNC_INFO_TABLE;

@Component
public class MongoChainServiceImpl implements ChainService {

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

    @Override
    public void saveChainList(List<ChainInfo> chainInfoList) {
        if (chainInfoList.isEmpty()) {
            return;
        }
        for (ChainInfo chainInfo : chainInfoList) {
            addChainInfo(chainInfo);
        }
    }

    @Override
    public void rollbackChainList(List<ChainInfo> chainInfoList) {
        if (chainInfoList.isEmpty()) {
            return;
        }
        for (ChainInfo chainInfo : chainInfoList) {
            Bson filter = Filters.eq("_id", chainInfo.getChainId());
            mongoDBService.delete(CHAIN_INFO_TABLE, filter);
            CacheManager.removeChain(chainInfo.getChainId());
        }
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

    public SyncInfo saveNewSyncInfo(int chainId, long newHeight) {
        SyncInfo syncInfo = new SyncInfo(chainId, newHeight, 0);
        Document document = DocumentTransferTool.toDocument(syncInfo, "chainId");
        if (newHeight == 0) {
            mongoDBService.insertOne(SYNC_INFO_TABLE, document);
        } else {
            Bson query = Filters.eq("_id", chainId);
            mongoDBService.updateOne(SYNC_INFO_TABLE, query, document);
        }
        return syncInfo;
    }

    public void updateStep(SyncInfo syncInfo) {
        if (syncInfo.getBestHeight() >= 0) {
            Document document = DocumentTransferTool.toDocument(syncInfo, "chainId");
            Bson query = Filters.eq("_id", syncInfo.getChainId());
            mongoDBService.updateOne(SYNC_INFO_TABLE, query, document);
        } else {
            Bson query = Filters.eq("_id", syncInfo.getChainId());
            mongoDBService.delete(SYNC_INFO_TABLE, query);
        }
    }
}
