package io.nuls.api.db.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.*;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.db.AccountService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.AccountInfo;
import io.nuls.api.model.po.PageInfo;
import io.nuls.api.model.po.TxRelationInfo;
import io.nuls.api.model.po.mini.MiniAccountInfo;
import io.nuls.api.utils.DBUtil;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.BigIntegerUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.checkerframework.checker.units.qual.A;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.api.constant.DBTableConstant.*;

@Component
public class MongoAccountServiceImpl implements AccountService {

    @Autowired
    private MongoDBService mongoDBService;

    private static List<String> addressList = Collections.synchronizedList(new ArrayList<>());

    public static int cacheSize = 5000;

    public void initCache() {
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            List<Document> documentList = mongoDBService.pageQuery(ACCOUNT_TABLE + apiCache.getChainInfo().getChainId(), 0, cacheSize);
            for (int i = 0; i < documentList.size(); i++) {
                Document document = documentList.get(i);
                AccountInfo accountInfo = DocumentTransferTool.toInfo(document, "address", AccountInfo.class);
                apiCache.addAccountInfo(accountInfo);
                addressList.add(accountInfo.getAddress());
            }
        }
    }

    public AccountInfo getAccountInfo(int chainId, String address) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return null;
        }
        AccountInfo accountInfo = apiCache.getAccountInfo(address);
        if (accountInfo == null) {
            Document document = mongoDBService.findOne(ACCOUNT_TABLE + chainId, Filters.eq("_id", address));
            if (document == null) {
                return null;
            }
            accountInfo = DocumentTransferTool.toInfo(document, "address", AccountInfo.class);
            while (addressList.size() >= cacheSize) {
                address = addressList.remove(0);
                apiCache.getAccountMap().remove(address);
            }
            apiCache.addAccountInfo(accountInfo);
            addressList.add(accountInfo.getAddress());
        }
        return accountInfo.copy();
    }

    public MiniAccountInfo getMiniAccountInfo(int chainId, String address) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return null;
        }
        AccountInfo accountInfo = apiCache.getAccountInfo(address);
        if (accountInfo == null) {
            Document document = mongoDBService.findOne(ACCOUNT_TABLE + chainId, Filters.eq("_id", address));
            if (document == null) {
                return null;
            }
            accountInfo = DocumentTransferTool.toInfo(document, "address", AccountInfo.class);
            while (addressList.size() >= cacheSize) {
                address = addressList.remove(0);
                apiCache.getAccountMap().remove(address);
            }
            apiCache.addAccountInfo(accountInfo);
            addressList.add(accountInfo.getAddress());
        }

        return new MiniAccountInfo(accountInfo);
    }


    public void saveAccounts(int chainId, Map<String, AccountInfo> accountInfoMap) {
        if (accountInfoMap.isEmpty()) {
            return;
        }

        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        List<WriteModel<Document>> modelList = new ArrayList<>();
        int i = 0;
        for (AccountInfo accountInfo : accountInfoMap.values()) {
            Document document = DocumentTransferTool.toDocument(accountInfo, "address");
            document.put("totalBalance", BigIntegerUtils.bigIntegerToString(accountInfo.getTotalBalance(), 32));

            if (accountInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
                accountInfo.setNew(false);
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", accountInfo.getAddress()), document));
            }
            i++;
            if (i == 1000) {
                mongoDBService.bulkWrite(ACCOUNT_TABLE + chainId, modelList, options);
                modelList.clear();
                i = 0;
            }
        }
        if (modelList.size() > 0) {
            mongoDBService.bulkWrite(ACCOUNT_TABLE + chainId, modelList, options);
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        for (AccountInfo accountInfo : accountInfoMap.values()) {
            if (apiCache.getAccountMap().containsKey(accountInfo.getAddress())) {
                apiCache.addAccountInfo(accountInfo);
            }
//            else {
//                while (addressList.size() >= cacheSize) {
//                    String address = addressList.remove(0);
//                    apiCache.getAccountMap().remove(address);
//                }
//                apiCache.addAccountInfo(accountInfo);
//                addressList.add(accountInfo.getAddress());
//            }
        }
    }

    public PageInfo<AccountInfo> pageQuery(int chainId, int pageNumber, int pageSize) {
        List<Document> docsList = this.mongoDBService.pageQuery(ACCOUNT_TABLE + chainId, pageNumber, pageSize);
        List<AccountInfo> accountInfoList = new ArrayList<>();
        long totalCount = mongoDBService.getEstimateCount(ACCOUNT_TABLE + chainId);
        for (Document document : docsList) {
            accountInfoList.add(DocumentTransferTool.toInfo(document, "address", AccountInfo.class));
        }
        PageInfo<AccountInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, accountInfoList);
        return pageInfo;
    }

    public PageInfo<TxRelationInfo> getAccountTxs(int chainId, String address, int pageIndex, int pageSize, int type, long startHeight, long endHeight, int assetChainId, int assetId) {
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq("address", address));
        if (type > 0) {
            filters.add(Filters.eq("type", type));
        }
        if (assetChainId > 0 && assetId > 0) {
            filters.add(Filters.eq("chainId", assetChainId));
            filters.add(Filters.eq("assetId", assetId));
        }
        if (startHeight >= 0) {
            filters.add(Filters.gte("height", startHeight));
        }
        if (endHeight > 0) {
            filters.add(Filters.lte("height", endHeight));
        }

        int start = (pageIndex - 1) * pageSize;
        int end = pageIndex * pageSize;
        int index = DBUtil.getShardNumber(address);

        Bson filter = Filters.and(filters);
        long unConfirmCount = mongoDBService.getCount(TX_UNCONFIRM_RELATION_TABLE + chainId, filter);
        long confirmCount = mongoDBService.getCount(TX_RELATION_TABLE + chainId + "_" + index, filter);
        List<TxRelationInfo> txRelationInfoList;
        if (end <= unConfirmCount) {
            txRelationInfoList = unConfirmLimitQuery(chainId, filter, start, pageSize);
        } else if (start > unConfirmCount || unConfirmCount == 0) {
            start = (int) (start - unConfirmCount);
            txRelationInfoList = confirmLimitQuery(chainId, index, filter, start, pageSize);
        } else {
            txRelationInfoList = relationLimitQuery(chainId, index, filter, filter, start, pageSize);
        }

        PageInfo<TxRelationInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, unConfirmCount + confirmCount, txRelationInfoList);
        return pageInfo;
    }

    public PageInfo<TxRelationInfo> getAcctTxs(int chainId, int assetChainId, int assetId, String address,
                                               int type, long startTime, long endTime, int pageIndex, int pageSize) {

        List<Bson> filters = new ArrayList<>();
        Bson addressFilter = Filters.eq("address", address);
        filters.add(addressFilter);
        if (type > 0) {
            filters.add(Filters.eq("type", type));
        }
        if (assetChainId > 0 && assetId > 0) {
            filters.add(Filters.eq("chainId", assetChainId));
            filters.add(Filters.eq("assetId", assetId));
        }
        if (startTime > 0) {
            filters.add(Filters.gte("createTime", startTime));
        }
        if (endTime > 0) {
            filters.add(Filters.lte("createTime", endTime));
        }

        Bson filter = Filters.and(filters);
        int start = (pageIndex - 1) * pageSize;
        int end = pageIndex * pageSize;
        int index = DBUtil.getShardNumber(address);

        long unConfirmCount = mongoDBService.getCount(TX_UNCONFIRM_RELATION_TABLE + chainId, addressFilter);
        long confirmCount = mongoDBService.getCount(TX_RELATION_TABLE + chainId + "_" + index, filter);
        List<TxRelationInfo> txRelationInfoList;
        if (end <= unConfirmCount) {
            txRelationInfoList = unConfirmLimitQuery(chainId, filter, start, pageSize);
        } else if (start - 1 > unConfirmCount) {
            start = start - 1;
            start = (int) (start - unConfirmCount);
            txRelationInfoList = confirmLimitQuery(chainId, index, filter, start, pageSize);
        } else {
            txRelationInfoList = relationLimitQuery(chainId, index, addressFilter, filter, start, pageSize);
        }

        PageInfo<TxRelationInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, unConfirmCount + confirmCount, txRelationInfoList);
        return pageInfo;
    }

    private List<TxRelationInfo> unConfirmLimitQuery(int chainId, Bson filter, int start, int pageSize) {
        List<Document> docsList = this.mongoDBService.limitQuery(TX_UNCONFIRM_RELATION_TABLE + chainId, filter, Sorts.descending("createTime"), start, pageSize);
        List<TxRelationInfo> txRelationInfoList = new ArrayList<>();
        for (Document document : docsList) {
            TxRelationInfo txRelationInfo = TxRelationInfo.toInfo(document);
            txRelationInfo.setStatus(0);
            txRelationInfoList.add(txRelationInfo);
        }
        return txRelationInfoList;
    }

    private List<TxRelationInfo> confirmLimitQuery(int chainId, int index, Bson filter, int start, int pageSize) {
        List<Document> docsList = this.mongoDBService.limitQuery(TX_RELATION_TABLE + chainId + "_" + index, filter, Sorts.descending("createTime"), start, pageSize);
        List<TxRelationInfo> txRelationInfoList = new ArrayList<>();
        for (Document document : docsList) {
            TxRelationInfo txRelationInfo = TxRelationInfo.toInfo(document);
            txRelationInfo.setStatus(1);
            txRelationInfoList.add(txRelationInfo);
        }
        return txRelationInfoList;
    }

    private List<TxRelationInfo> relationLimitQuery(int chainId, int index, Bson filter1, Bson filter2, int start, int pageSize) {
        List<Document> docsList = this.mongoDBService.limitQuery(TX_UNCONFIRM_RELATION_TABLE + chainId, filter1, Sorts.descending("createTime"), start, pageSize);
        List<TxRelationInfo> txRelationInfoList = new ArrayList<>();
        for (Document document : docsList) {
            TxRelationInfo txRelationInfo = TxRelationInfo.toInfo(document);
            txRelationInfo.setStatus(ApiConstant.TX_UNCONFIRM);
            txRelationInfoList.add(txRelationInfo);
        }
        pageSize = pageSize - txRelationInfoList.size();
        docsList = this.mongoDBService.limitQuery(TX_RELATION_TABLE + chainId + "_" + index, filter2, Sorts.descending("createTime"), 0, pageSize);
        for (Document document : docsList) {
            TxRelationInfo txRelationInfo = TxRelationInfo.toInfo(document);
            txRelationInfo.setStatus(ApiConstant.TX_CONFIRM);
            txRelationInfoList.add(txRelationInfo);
        }
        return txRelationInfoList;
    }

    public PageInfo<MiniAccountInfo> getCoinRanking(int pageIndex, int pageSize, int chainId) {
        Bson sort = Sorts.descending("totalBalance");
        List<MiniAccountInfo> accountInfoList = new ArrayList<>();
        Bson filter = Filters.ne("totalBalance", 0);
        BasicDBObject fields = new BasicDBObject();
        fields.append("_id", 1).append("alias", 1).append("totalBalance", 1).append("totalOut", 1).append("totalIn", 1).append("type", 1);

        List<Document> docsList = this.mongoDBService.pageQuery(ACCOUNT_TABLE + chainId, filter, fields, sort, pageIndex, pageSize);
        long totalCount = mongoDBService.getCount(ACCOUNT_TABLE + chainId, filter);
        for (Document document : docsList) {
            MiniAccountInfo accountInfo = DocumentTransferTool.toInfo(document, "address", MiniAccountInfo.class);
//            List<Output> outputs = utxoService.getAccountUtxos(accountInfo.getAddress());
//            CalcUtil.calcBalance(accountInfo, outputs, blockHeaderService.getBestBlockHeight());
            accountInfoList.add(accountInfo);
        }
        PageInfo<MiniAccountInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, accountInfoList);
        return pageInfo;
    }

    public BigInteger getAllAccountBalance(int chainId) {
        boolean query = true;
        BigInteger totalBalance = BigInteger.ZERO;
        List<Document> documentList;
        int i = 1;
        BasicDBObject fields = new BasicDBObject();
        fields.append("totalBalance", 1);
        while (query) {
            documentList = mongoDBService.pageQuery(ACCOUNT_TABLE + chainId, null, fields, Sorts.descending("totalBalance"), i, 1000);
            for (Document document : documentList) {
                totalBalance = totalBalance.add(new BigInteger(document.getString("totalBalance")));
            }
            if (documentList.size() < 1000) {
                query = false;
            }
            i++;
        }
        return totalBalance;
    }

    public BigInteger getAccountTotalBalance(int chainId, String address) {
        AccountInfo accountInfo = getAccountInfo(chainId, address);
        if (accountInfo == null) {
            return BigInteger.ZERO;
        }
        return accountInfo.getTotalBalance();
    }

    @Override
    public void testBalance(int chainId) {

    }
}
