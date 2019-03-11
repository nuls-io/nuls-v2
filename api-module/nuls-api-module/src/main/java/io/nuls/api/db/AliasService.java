package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AliasInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


@Component
public class AliasService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initCache() {
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            List<Document> documentList = mongoDBService.query(MongoTableConstant.ALIAS_TABLE + apiCache.getChainInfo().getChainId());
            for (Document document : documentList) {
                AliasInfo aliasInfo = DocumentTransferTool.toInfo(document, "alias", AliasInfo.class);
                apiCache.addAlias(aliasInfo);
            }
        }
    }

    public AliasInfo getAliasByAddress(int chainId, String address) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        AliasInfo aliasInfo = apiCache.getAlias(address);
        if (aliasInfo == null) {
            Document document = mongoDBService.findOne(MongoTableConstant.ALIAS_TABLE + chainId, Filters.eq("address", address));
            if (document == null) {
                return null;
            }
            aliasInfo = DocumentTransferTool.toInfo(document, "alias", AliasInfo.class);

            apiCache.addAlias(aliasInfo);
        }
        return aliasInfo;
    }

    public AliasInfo getByAlias(int chainId, String alias) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        AliasInfo aliasInfo = apiCache.getAlias(alias);
        if (aliasInfo == null) {
            Document document = mongoDBService.findOne(MongoTableConstant.ALIAS_TABLE + chainId, Filters.eq("_id", alias));
            if (document == null) {
                return null;
            }
            aliasInfo = DocumentTransferTool.toInfo(document, "alias", AliasInfo.class);

            apiCache.addAlias(aliasInfo);
        }
        return aliasInfo;
    }


    public void saveAliasList(int chainId, List<AliasInfo> aliasInfoList) {
        if (aliasInfoList.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (AliasInfo info : aliasInfoList) {
            Document document = DocumentTransferTool.toDocument(info, "address");
            documentList.add(document);
        }
        mongoDBService.insertMany(MongoTableConstant.ALIAS_TABLE + chainId, documentList);
    }

}
