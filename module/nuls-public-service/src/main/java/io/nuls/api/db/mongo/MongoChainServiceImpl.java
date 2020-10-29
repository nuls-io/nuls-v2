package io.nuls.api.db.mongo;


import com.mongodb.client.model.Filters;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.ChainService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.BlockHeaderInfo;
import io.nuls.api.model.po.ChainConfigInfo;
import io.nuls.api.model.po.ChainInfo;
import io.nuls.api.model.po.SyncInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.nuls.api.constant.DBTableConstant.*;

@Component
public class MongoChainServiceImpl implements ChainService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initCache() {
        List<Document> documentList = mongoDBService.query(CHAIN_CONFIG_TABLE);
        for (Document document : documentList) {
            ChainConfigInfo configInfo = DocumentTransferTool.toInfo(document, "chainId", ChainConfigInfo.class);
            Document chainDocument = mongoDBService.findOne(CHAIN_INFO_TABLE, Filters.eq("_id", configInfo.getChainId()));
            ChainInfo chainInfo = ChainInfo.toInfo(chainDocument);
            CacheManager.initCache(chainInfo, configInfo);
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

    public List<ChainInfo> getOtherChainInfoList(int chainId) {
        Bson filter = Filters.ne("_id", chainId);
        List<Document> documentList = mongoDBService.query(CHAIN_INFO_TABLE, filter);
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
        if (getChainInfo(chainInfo.getChainId()) == null) {
            Document document = chainInfo.toDocument();
            mongoDBService.insertOne(CHAIN_INFO_TABLE, document);
            chainInfo.setNew(false);
        }
    }

    @Override
    public void addCacheChain(ChainInfo chainInfo, ChainConfigInfo configInfo) {
        Document document = DocumentTransferTool.toDocument(configInfo, "chainId");
        mongoDBService.insertOne(CHAIN_CONFIG_TABLE, document);

        ApiCache apiCache = CacheManager.getCache(chainInfo.getChainId());
        if (apiCache == null) {
            addChainInfo(chainInfo);
            CacheManager.initCache(chainInfo, configInfo);
        } else {
            apiCache.setConfigInfo(configInfo);
        }
    }

    public void updateChainInfo(ChainInfo chainInfo) {
        Bson filter = Filters.eq("_id", chainInfo.getChainId());
        Document document = chainInfo.toDocument();
        mongoDBService.updateOne(CHAIN_INFO_TABLE, filter, document);
    }

    @Override
    public void saveChainList(List<ChainInfo> chainInfoList) {
        if (chainInfoList.isEmpty()) {
            return;
        }
        for (ChainInfo chainInfo : chainInfoList) {
            if (chainInfo.isNew()) {
                //有可能新注册的链会占用已注销链的链ID，因此在这里需要重新查询一下
                ChainInfo chain = getChainInfo(chainInfo.getChainId());
                if (chain == null) {
                    addChainInfo(chainInfo);
                } else {
                    updateChainInfo(chainInfo);
                }
            } else {
                updateChainInfo(chainInfo);
            }
        }
    }

    @Override
    public void rollbackChainList(List<ChainInfo> chainInfoList) {
        if (chainInfoList.isEmpty()) {
            return;
        }
        for (ChainInfo chainInfo : chainInfoList) {
            //缓存的链,数据不清空
            if (CacheManager.getCacheChain(chainInfo.getChainId()) != null) {
                continue;
            }
            if (chainInfo.isNew()) {
                Bson filter = Filters.eq("_id", chainInfo.getChainId());
                mongoDBService.delete(CHAIN_INFO_TABLE, filter);
                CacheManager.removeApiCache(chainInfo.getChainId());
            } else {
                updateChainInfo(chainInfo);
            }
        }
    }

    public ChainInfo getChainInfo(int chainId) {
        ChainInfo chainInfo = CacheManager.getCacheChain(chainId);
        if (chainInfo == null) {
            Document document = mongoDBService.findOne(CHAIN_INFO_TABLE, Filters.eq("_id", chainId));
            if (document != null) {
                chainInfo = ChainInfo.toInfo(document);
            }
        }
        return chainInfo;
    }

    public SyncInfo getSyncInfo(int chainId) {
        Document document = mongoDBService.findOne(SYNC_INFO_TABLE, Filters.eq("_id", chainId));
        if (document == null) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "chainId", SyncInfo.class);
    }

    public SyncInfo saveNewSyncInfo(int chainId, long newHeight, BlockHeaderInfo headerInfo) {
        Bson query = Filters.eq("_id", chainId);
        Document document = mongoDBService.findOne(SYNC_INFO_TABLE, query);
        SyncInfo syncInfo;
        boolean isNew = false;
        if (document == null) {
            isNew = true;
            syncInfo = new SyncInfo(chainId, newHeight, headerInfo);
        } else {
            syncInfo = DocumentTransferTool.toInfo(document, "chainId", SyncInfo.class);
            syncInfo.setVersion(headerInfo.getMainVersion());
            syncInfo.setBestHeight(headerInfo.getHeight());
            BigInteger reward = headerInfo.getReward() == null ? BigInteger.ZERO : headerInfo.getReward();
            BigInteger totalFee = headerInfo.getTotalFee() == null ? BigInteger.ZERO : headerInfo.getTotalFee();
            syncInfo.setTotalSupply(syncInfo.getTotalSupply().add(reward).subtract(totalFee));
        }
        document = DocumentTransferTool.toDocument(syncInfo, "chainId");
        if (isNew) {
            mongoDBService.insertOne(SYNC_INFO_TABLE, document);

        } else {
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
