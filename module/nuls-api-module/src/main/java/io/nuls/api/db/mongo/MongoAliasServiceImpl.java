package io.nuls.api.db.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.AliasService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AliasInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.api.constant.DBTableConstant.ALIAS_TABLE;


@Component
public class MongoAliasServiceImpl implements AliasService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initCache() {
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            List<Document> documentList = mongoDBService.query(ALIAS_TABLE + apiCache.getChainInfo().getChainId());
            for (Document document : documentList) {
                AliasInfo aliasInfo = DocumentTransferTool.toInfo(document, "address", AliasInfo.class);
                apiCache.addAlias(aliasInfo);
            }
        }
    }

    public AliasInfo getAliasByAddress(int chainId, String address) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        AliasInfo aliasInfo = apiCache.getAlias(address);
        if (aliasInfo == null) {
            Document document = mongoDBService.findOne(ALIAS_TABLE + chainId, Filters.eq("_id", address));
            if (document == null) {
                return null;
            }
            aliasInfo = DocumentTransferTool.toInfo(document, "address", AliasInfo.class);

            apiCache.addAlias(aliasInfo);
        }
        return aliasInfo;
    }

    public AliasInfo getByAlias(int chainId, String alias) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        AliasInfo aliasInfo = apiCache.getAlias(alias);
        if (aliasInfo == null) {
            Document document = mongoDBService.findOne(ALIAS_TABLE + chainId, Filters.eq("alias", alias));
            if (document == null) {
                return null;
            }
            aliasInfo = DocumentTransferTool.toInfo(document, "address", AliasInfo.class);

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
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(ALIAS_TABLE + chainId, documentList, options);
    }


    public void rollbackAliasList(int chainId, List<AliasInfo> aliasInfoList) {
        if (aliasInfoList.isEmpty()) {
            return;
        }
        ApiCache apiCache = CacheManager.getCache(chainId);
        List<String> list = new ArrayList<>();
        for (AliasInfo aliasInfo : aliasInfoList) {
            list.add(aliasInfo.getAddress());
            apiCache.getAliasMap().remove(aliasInfo.getAddress());
            apiCache.getAliasMap().remove(aliasInfo.getAlias());
        }
        mongoDBService.delete(ALIAS_TABLE + chainId, Filters.in("_id", list));
    }
}
