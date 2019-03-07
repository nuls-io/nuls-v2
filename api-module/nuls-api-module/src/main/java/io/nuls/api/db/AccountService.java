package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AccountInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AccountService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initCache() {
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            List<Document> documentList = mongoDBService.query(MongoTableConstant.ACCOUNT_TABLE + apiCache.getChainInfo().getChainId());
            for (Document document : documentList) {
                AccountInfo accountInfo = DocumentTransferTool.toInfo(document, "address", AccountInfo.class);
                apiCache.addAccountInfo(accountInfo);
            }
        }
    }

    public AccountInfo getAccountInfo(int chainId, String address) {
        return CacheManager.getCache(chainId).getAccountInfo(address);
    }

    public void saveAccounts(int chainId, Map<String, AccountInfo> accountInfoMap) {
        if (accountInfoMap.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (AccountInfo accountInfo : accountInfoMap.values()) {
            Document document = DocumentTransferTool.toDocument(accountInfo, "address");
            if (accountInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
                accountInfo.setNew(false);
                ApiCache apiCache = CacheManager.getCache(chainId);
                apiCache.addAccountInfo(accountInfo);
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", accountInfo.getAddress()), document));
            }
        }
        mongoDBService.bulkWrite(MongoTableConstant.ACCOUNT_TABLE + chainId, modelList);
    }
}
