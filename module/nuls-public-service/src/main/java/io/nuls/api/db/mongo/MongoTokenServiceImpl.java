package io.nuls.api.db.mongo;

import com.mongodb.client.model.*;
import io.nuls.api.db.TokenService;
import io.nuls.api.model.po.AccountTokenInfo;
import io.nuls.api.model.po.PageInfo;
import io.nuls.api.model.po.TokenTransfer;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.api.constant.DBTableConstant.*;

@Component
public class MongoTokenServiceImpl implements TokenService {

    @Autowired
    private MongoDBService mongoDBService;

    public AccountTokenInfo getAccountTokenInfo(int chainId, String key) {
        Bson query = Filters.eq("_id", key);

        Document document = mongoDBService.findOne(ACCOUNT_TOKEN_TABLE + chainId, query);
        if (document == null) {
            return null;
        }
        AccountTokenInfo tokenInfo = DocumentTransferTool.toInfo(document, "key", AccountTokenInfo.class);
        return tokenInfo;
    }

    public void saveAccountTokens(int chainId, Map<String, AccountTokenInfo> accountTokenInfos) {
        if (accountTokenInfos.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (AccountTokenInfo tokenInfo : accountTokenInfos.values()) {
            Document document = DocumentTransferTool.toDocument(tokenInfo, "key");
            if (tokenInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", tokenInfo.getKey()), document));
            }
        }
        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        mongoDBService.bulkWrite(ACCOUNT_TOKEN_TABLE + chainId, modelList, options);
    }

    public PageInfo<AccountTokenInfo> getAccountTokens(int chainId, String address, int pageNumber, int pageSize) {
        Bson query = Filters.eq("address", address);
        Bson sort = Sorts.descending("balance");
        List<Document> docsList = this.mongoDBService.pageQuery(ACCOUNT_TOKEN_TABLE + chainId, query, sort, pageNumber, pageSize);
        List<AccountTokenInfo> accountTokenList = new ArrayList<>();
        long totalCount = mongoDBService.getCount(ACCOUNT_TOKEN_TABLE + chainId, query);

        for (Document document : docsList) {
            AccountTokenInfo tokenInfo = DocumentTransferTool.toInfo(document, "key", AccountTokenInfo.class);
            accountTokenList.add(tokenInfo);
            document = mongoDBService.findOne(CONTRACT_TABLE + chainId, Filters.eq("_id", tokenInfo.getContractAddress()));
            tokenInfo.setStatus(document.getInteger("status"));
        }

        PageInfo<AccountTokenInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, accountTokenList);
        return pageInfo;
    }

    public PageInfo<AccountTokenInfo> getContractTokens(int chainId, String contractAddress, int pageNumber, int pageSize) {
        Bson query = Filters.eq("contractAddress", contractAddress);
        Bson sort = Sorts.descending("balance");
        List<Document> docsList = this.mongoDBService.pageQuery(ACCOUNT_TOKEN_TABLE + chainId, query, sort, pageNumber, pageSize);
        List<AccountTokenInfo> accountTokenList = new ArrayList<>();
        long totalCount = mongoDBService.getCount(ACCOUNT_TOKEN_TABLE + chainId, query);
        for (Document document : docsList) {
            accountTokenList.add(DocumentTransferTool.toInfo(document, "key", AccountTokenInfo.class));
        }
        PageInfo<AccountTokenInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, accountTokenList);
        return pageInfo;
    }


    public void saveTokenTransfers(int chainId, List<TokenTransfer> tokenTransfers) {
        if (tokenTransfers.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (TokenTransfer tokenTransfer : tokenTransfers) {
            Document document = DocumentTransferTool.toDocument(tokenTransfer);
            documentList.add(document);
        }
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(TOKEN_TRANSFER_TABLE + chainId, documentList, options);
    }

    public void rollbackTokenTransfers(int chainId, List<String> tokenTxHashs, long height) {
        if (tokenTxHashs.isEmpty()) {
            return;
        }
        mongoDBService.delete(TOKEN_TRANSFER_TABLE + chainId, Filters.eq("height", height));
    }

    public PageInfo<TokenTransfer> getTokenTransfers(int chainId, String address, String contractAddress, int pageIndex, int pageSize) {
        Bson filter;
        if (StringUtils.isNotBlank(address) && StringUtils.isNotBlank(contractAddress)) {
            Bson addressFilter = Filters.or(Filters.eq("fromAddress", address), Filters.eq("toAddress", address));
            filter = Filters.and(Filters.eq("contractAddress", contractAddress), addressFilter);
        } else if (StringUtils.isNotBlank(contractAddress)) {
            filter = Filters.eq("contractAddress", contractAddress);
        } else {
            filter = Filters.or(Filters.eq("fromAddress", address), Filters.eq("toAddress", address));
        }
        Bson sort = Sorts.descending("time");
        List<Document> docsList = this.mongoDBService.pageQuery(TOKEN_TRANSFER_TABLE + chainId, filter, sort, pageIndex, pageSize);
        List<TokenTransfer> tokenTransfers = new ArrayList<>();
        long totalCount = mongoDBService.getCount(TOKEN_TRANSFER_TABLE + chainId, filter);
        for (Document document : docsList) {
            tokenTransfers.add(DocumentTransferTool.toInfo(document, TokenTransfer.class));
        }

        PageInfo<TokenTransfer> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, tokenTransfers);
        return pageInfo;
    }
}
