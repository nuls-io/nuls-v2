package io.nuls.api.db.mongo;

import com.mongodb.client.model.*;
import io.nuls.api.ApiContext;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.DBTableConstant;
import io.nuls.api.db.AccountLedgerService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AccountLedgerInfo;
import io.nuls.api.model.po.db.AssetInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MongoAccountLedgerServiceImpl implements AccountLedgerService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initCache() {
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            List<Document> documentList = mongoDBService.query(DBTableConstant.ACCOUNT_LEDGER_TABLE + apiCache.getChainInfo().getChainId());
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
            Document document = mongoDBService.findOne(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, Filters.eq("_id", key));
            if (document == null) {
                return null;
            }
            accountLedgerInfo = DocumentTransferTool.toInfo(document, "key", AccountLedgerInfo.class);
            apiCache.addAccountLedgerInfo(accountLedgerInfo);
        }
        return accountLedgerInfo.copy();
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
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", ledgerInfo.getKey()), document));
            }
        }
        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        mongoDBService.bulkWrite(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, modelList, options);

        ApiCache cache = CacheManager.getCache(chainId);
        for (AccountLedgerInfo ledgerInfo : accountLedgerInfoMap.values()) {
            cache.addAccountLedgerInfo(ledgerInfo);
        }
    }

    @Override
    public List<AccountLedgerInfo> getAccountLedgerInfoList(int chainId, String address) {
        Bson filter = Filters.eq("address", address);
        List<Document> documentList = mongoDBService.query(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, filter);
        List<AccountLedgerInfo> accountLedgerInfoList = new ArrayList<>();

        for (Document document : documentList) {
            if (document.getInteger("chainId") != chainId) {
                continue;
            }
            AccountLedgerInfo ledgerInfo = DocumentTransferTool.toInfo(document, "key", AccountLedgerInfo.class);
            accountLedgerInfoList.add(ledgerInfo);
        }
        if (accountLedgerInfoList.isEmpty()) {
            AccountLedgerInfo accountLedgerInfo = new AccountLedgerInfo(address, ApiContext.defaultChainId, ApiContext.defaultAssetId);
            accountLedgerInfoList.add(accountLedgerInfo);
        }
        return accountLedgerInfoList;
    }

    @Override
    public List<AccountLedgerInfo> getAccountCrossLedgerInfoList(int chainId, String address) {
        Bson filter = Filters.eq("address", address);
        List<Document> documentList = mongoDBService.query(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, filter);
        List<AccountLedgerInfo> accountLedgerInfoList = new ArrayList<>();

        for (Document document : documentList) {
            if (document.getInteger("chainId") == chainId) {
                continue;
            }
            AccountLedgerInfo ledgerInfo = DocumentTransferTool.toInfo(document, "key", AccountLedgerInfo.class);
            accountLedgerInfoList.add(ledgerInfo);
        }
        return accountLedgerInfoList;
    }
}
