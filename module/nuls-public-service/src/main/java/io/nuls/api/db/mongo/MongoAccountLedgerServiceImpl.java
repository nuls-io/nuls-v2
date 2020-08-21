package io.nuls.api.db.mongo;

import com.mongodb.client.model.*;
import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.DBTableConstant;
import io.nuls.api.db.AccountLedgerService;
import io.nuls.api.db.AccountService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.*;
import io.nuls.api.model.po.mini.MiniAccountInfo;
import io.nuls.api.model.rpc.BalanceInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.DoubleUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class MongoAccountLedgerServiceImpl implements AccountLedgerService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private AccountService accountService;

    private List<String> keyList = new LinkedList<>();

    private static int cacheSize = 20000;

    public void initCache() {
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            List<Document> documentList = mongoDBService.pageQuery(DBTableConstant.ACCOUNT_LEDGER_TABLE + apiCache.getChainInfo().getChainId(), 0, cacheSize);
            for (Document document : documentList) {
                AccountLedgerInfo ledgerInfo = DocumentTransferTool.toInfo(document, "key", AccountLedgerInfo.class);
                apiCache.addAccountLedgerInfo(ledgerInfo);
                keyList.add(ledgerInfo.getKey());
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
            while (keyList.size() >= cacheSize) {
                key = keyList.remove(0);
                apiCache.getLedgerMap().remove(key);
            }
            apiCache.addAccountLedgerInfo(accountLedgerInfo);
            keyList.add(accountLedgerInfo.getKey());
        }
        return accountLedgerInfo.copy();
    }

    public void saveLedgerList(int chainId, Map<String, AccountLedgerInfo> accountLedgerInfoMap) {
        if (accountLedgerInfoMap.isEmpty()) {
            return;
        }

        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        List<WriteModel<Document>> modelList = new ArrayList<>();
        int i = 0;
        for (AccountLedgerInfo ledgerInfo : accountLedgerInfoMap.values()) {
            Document document = DocumentTransferTool.toDocument(ledgerInfo, "key");
            document.put("totalBalance", BigIntegerUtils.bigIntegerToString(ledgerInfo.getTotalBalance(), 32));
            if (ledgerInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
                ledgerInfo.setNew(false);
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", ledgerInfo.getKey()), document));
            }
            i++;
            if (i == 1000) {
                mongoDBService.bulkWrite(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, modelList, options);
                modelList.clear();
                i = 0;
            }
        }
        if (modelList.size() > 0) {
            mongoDBService.bulkWrite(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, modelList, options);
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        for (AccountLedgerInfo ledgerInfo : accountLedgerInfoMap.values()) {
            if (apiCache.getLedgerMap().containsKey(ledgerInfo.getKey())) {
                apiCache.addAccountLedgerInfo(ledgerInfo);
            }
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
            AssetInfo assetInfo = CacheManager.getCacheChain(chainId).getDefaultAsset();
            AccountLedgerInfo accountLedgerInfo = new AccountLedgerInfo(address, assetInfo.getChainId(), assetInfo.getAssetId());
            accountLedgerInfoList.add(accountLedgerInfo);
        }
        return accountLedgerInfoList;
    }

    DecimalFormat format = new DecimalFormat("###.#####");

    public PageInfo<MiniAccountInfo> getAssetRanking(int chainId, int assetChainId, int assetId, int pageNumber, int pageSize) {
        AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(assetChainId + "-" + assetId);
        if (assetInfo == null) {
            return new PageInfo<>();
        } else if (assetInfo.getChainId() == ApiContext.defaultChainId && assetInfo.getAssetId() == ApiContext.defaultAssetId) {
            ApiCache apiCache = CacheManager.getCache(chainId);
            CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
            assetInfo.setLocalTotalCoins(coinContextInfo.getCirculation());
        }
        Bson filter = Filters.and(Filters.eq("chainId", assetChainId), Filters.eq("assetId", assetId));
        long totalCount = mongoDBService.getCount(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, filter);
        Bson sort = Sorts.descending("totalBalance");
        List<Document> documentList = mongoDBService.pageQuery(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, filter, sort, pageNumber, pageSize);
        List<MiniAccountInfo> list = new ArrayList<>();
        for (int i = 0; i < documentList.size(); i++) {
            AccountLedgerInfo ledgerInfo = DocumentTransferTool.toInfo(documentList.get(i), "key", AccountLedgerInfo.class);
            MiniAccountInfo accountInfo = accountService.getMiniAccountInfo(chainId, ledgerInfo.getAddress());
            accountInfo.setTotalBalance(ledgerInfo.getTotalBalance());
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, accountInfo.getAddress(), ledgerInfo.getChainId(), ledgerInfo.getAssetId());
            accountInfo.setLocked(balanceInfo.getConsensusLock().add(balanceInfo.getTimeLock()));
            accountInfo.setDecimal(assetInfo.getDecimals());

            BigDecimal b1 = new BigDecimal(accountInfo.getTotalBalance());
            BigDecimal b2 = new BigDecimal(assetInfo.getLocalTotalCoins());
            double prop = 0;
            if (b2.compareTo(BigDecimal.ZERO) > 0) {
                prop = b1.divide(b2, 5, RoundingMode.HALF_UP).doubleValue() * 100;
            }
            accountInfo.setProportion(format.format(prop) + "%");
            list.add(accountInfo);
        }

        PageInfo<MiniAccountInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, list);
        return pageInfo;
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

    @Override
    public List<AccountLedgerInfo> getAccountLedgerInfoList(int assetChainId, int assetId) {
        Bson filter = Filters.and(Filters.eq("chainId", assetChainId), Filters.eq("assetId", assetId));
        List<Document> documentList = mongoDBService.query(DBTableConstant.ACCOUNT_LEDGER_TABLE + ApiContext.defaultChainId, filter);
        List<AccountLedgerInfo> accountLedgerInfoList = new ArrayList<>();

        for (Document document : documentList) {
            AccountLedgerInfo ledgerInfo = DocumentTransferTool.toInfo(document, "key", AccountLedgerInfo.class);
            accountLedgerInfoList.add(ledgerInfo);
        }
        return accountLedgerInfoList;
    }
}
