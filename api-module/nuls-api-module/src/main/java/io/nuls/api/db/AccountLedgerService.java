package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.manager.ChainManager;
import io.nuls.api.model.po.db.AccountInfo;
import io.nuls.api.model.po.db.AccountLedgerInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.annotation.Service;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AccountLedgerService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initCache() {
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            List<Document> documentList = mongoDBService.query(MongoTableConstant.ACCOUNT_LEDGER_TABLE + apiCache.getChainInfo().getChainId());
            for (Document document : documentList) {
                AccountLedgerInfo ledgerInfo = DocumentTransferTool.toInfo(document, "key", AccountLedgerInfo.class);
                apiCache.addAccountLedgerInfo(ledgerInfo);
            }
        }
    }

    public AccountLedgerInfo getAccountLedgerInfo(int chainId, String key) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        AccountLedgerInfo accountLedgerInfo = apiCache.getAccountLedgerInfo(key);
        if (accountLedgerInfo == null) {
            Document document = mongoDBService.findOne(MongoTableConstant.ACCOUNT_LEDGER_TABLE + chainId, Filters.eq("_id", key));
            if (document == null) {
                return null;
            }
            accountLedgerInfo = DocumentTransferTool.toInfo(document, "key", AccountLedgerInfo.class);
            apiCache.addAccountLedgerInfo(accountLedgerInfo);
        }
        return accountLedgerInfo;
    }

    public void saveLedgerList(int chainId, Map<String, AccountLedgerInfo> accountLedgerInfoMap) {
        if (accountLedgerInfoMap.isEmpty()) {
            return;
        }

        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (AccountLedgerInfo ledgerInfo : accountLedgerInfoMap.values()) {
            Document document = DocumentTransferTool.toDocument(ledgerInfo, "key");
            if (ledgerInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
                ledgerInfo.setNew(false);
                ApiCache cache = CacheManager.getCache(chainId);
                cache.addAccountLedgerInfo(ledgerInfo);
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", ledgerInfo.getKey()), document));
            }
        }
        mongoDBService.bulkWrite(MongoTableConstant.ACCOUNT_LEDGER_TABLE + chainId, modelList);
    }
}
