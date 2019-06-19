package io.nuls.api.db.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.*;
import io.nuls.api.db.ContractService;
import io.nuls.api.model.po.db.ContractInfo;
import io.nuls.api.model.po.db.ContractResultInfo;
import io.nuls.api.model.po.db.ContractTxInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.mini.MiniContractInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.api.constant.DBTableConstant.*;

@Component
public class MongoContractServiceImpl implements ContractService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private MongoAccountServiceImpl mongoAccountServiceImpl;

    public ContractInfo getContractInfo(int chainId, String contractAddress) {
        Document document = mongoDBService.findOne(CONTRACT_TABLE + chainId, Filters.eq("_id", contractAddress));
        if (document == null) {
            return null;
        }
        ContractInfo contractInfo = ContractInfo.toInfo(document);
        return contractInfo;
    }

    public ContractInfo getContractInfoByHash(int chainId, String txHash) {
        Document document = mongoDBService.findOne(CONTRACT_TABLE + chainId, Filters.eq("createTxHash", txHash));
        if (document == null) {
            return null;
        }
        ContractInfo tokenInfo = DocumentTransferTool.toInfo(document, "contractAddress", ContractInfo.class);
        return tokenInfo;
    }

    public void saveContractInfos(int chainId, Map<String, ContractInfo> contractInfoMap) {
        if (contractInfoMap.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (ContractInfo contractInfo : contractInfoMap.values()) {
            Document document = contractInfo.toDocument();
            if (contractInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", contractInfo.getContractAddress()), document));
            }
        }
        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        mongoDBService.bulkWrite(CONTRACT_TABLE + chainId, modelList, options);
    }

    public void rollbackContractInfos(int chainId, Map<String, ContractInfo> contractInfoMap) {
        if (contractInfoMap.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (ContractInfo contractInfo : contractInfoMap.values()) {
            Document document = contractInfo.toDocument();

            if (contractInfo.isNew()) {
                modelList.add(new DeleteOneModel<>(Filters.eq("_id", contractInfo.getContractAddress())));
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", contractInfo.getContractAddress()), document));
            }
        }
        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        mongoDBService.bulkWrite(CONTRACT_TABLE + chainId, modelList, options);
    }

    public void saveContractTxInfos(int chainId, List<ContractTxInfo> contractTxInfos) {
        if (contractTxInfos.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (ContractTxInfo txInfo : contractTxInfos) {
            Document document = DocumentTransferTool.toDocument(txInfo);
            documentList.add(document);
        }
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(CONTRACT_TX_TABLE + chainId, documentList, options);
    }

    public void rollbackContractTxInfos(int chainId, List<String> contractTxHashList) {
        if (contractTxHashList.isEmpty()) {
            return;
        }
        mongoDBService.delete(CONTRACT_TX_TABLE + chainId, Filters.in("txHash", contractTxHashList));
    }


    public void saveContractResults(int chainId, List<ContractResultInfo> contractResultInfos) {
        if (contractResultInfos.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (ContractResultInfo resultInfo : contractResultInfos) {
            Document document = resultInfo.toDocument();
            documentList.add(document);
        }
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(CONTRACT_RESULT_TABLE + chainId, documentList, options);
    }

    public void rollbackContractResults(int chainId, List<String> contractTxHashList) {
        if (contractTxHashList.isEmpty()) {
            return;
        }
        mongoDBService.delete(CONTRACT_RESULT_TABLE + chainId, Filters.in("_id", contractTxHashList));
    }

    public PageInfo<ContractTxInfo> getContractTxList(int chainId, String contractAddress, int type, int pageNumber, int pageSize) {
        Bson filter;
        if (type == 0) {
            filter = Filters.eq("contractAddress", contractAddress);
        } else {
            filter = Filters.and(Filters.eq("contractAddress", contractAddress), Filters.eq("type", type));
        }
        Bson sort = Sorts.descending("time");
        List<Document> docsList = this.mongoDBService.pageQuery(CONTRACT_TX_TABLE + chainId, filter, sort, pageNumber, pageSize);
        List<ContractTxInfo> contractTxInfos = new ArrayList<>();
        long totalCount = mongoDBService.getCount(CONTRACT_TX_TABLE + chainId, filter);
        for (Document document : docsList) {
            contractTxInfos.add(DocumentTransferTool.toInfo(document, ContractTxInfo.class));
        }
        PageInfo<ContractTxInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, contractTxInfos);
        return pageInfo;
    }

    public PageInfo<MiniContractInfo> getContractList(int chainId, int pageNumber, int pageSize, boolean onlyNrc20, boolean isHidden) {
        Bson filter = null;
        if (onlyNrc20) {
            filter = Filters.eq("isNrc20", true);
        } else if (isHidden) {
            filter = Filters.ne("isNrc20", true);
        }
        Bson sort = Sorts.descending("createTime");
        BasicDBObject fields = new BasicDBObject();
        fields.append("_id", 1).append("remark", 1).append("txCount", 1).append("status", 1)
                .append("createTime", 1).append("balance", 1).append("tokenName", 1).append("symbol", 1)
                .append("decimals", 1).append("totalSupply", 1).append("creater", 1);

        List<Document> docsList = this.mongoDBService.pageQuery(CONTRACT_TABLE + chainId, filter, fields, sort, pageNumber, pageSize);
        List<MiniContractInfo> contractInfos = new ArrayList<>();
        long totalCount = mongoDBService.getCount(CONTRACT_TABLE + chainId, filter);

        for (Document document : docsList) {
            MiniContractInfo contractInfo = DocumentTransferTool.toInfo(document, "contractAddress", MiniContractInfo.class);
            contractInfos.add(contractInfo);
        }
        PageInfo<MiniContractInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, contractInfos);
        return pageInfo;
    }

    @Override
    public PageInfo<MiniContractInfo> getContractList(int chainId, int pageNumber, int pageSize, String address, boolean onlyNrc20, boolean isHidden) {
        Bson filter = null;
        if (onlyNrc20) {
            filter = Filters.and(Filters.eq("isNrc20", true), Filters.eq("creater", address));
        } else if (isHidden) {
            filter = Filters.and(Filters.ne("isNrc20", true), Filters.eq("creater", address));
        } else {
            filter = Filters.eq("creater", address);
        }
        Bson sort = Sorts.descending("createTime");
        BasicDBObject fields = new BasicDBObject();
        fields.append("_id", 1).append("remark", 1).append("txCount", 1).append("status", 1)
                .append("createTime", 1).append("balance", 1).append("tokenName", 1).append("symbol", 1)
                .append("decimals", 1).append("totalSupply", 1).append("creater", 1);

        List<Document> docsList = this.mongoDBService.pageQuery(CONTRACT_TABLE + chainId, filter, fields, sort, pageNumber, pageSize);
        List<MiniContractInfo> contractInfos = new ArrayList<>();
        long totalCount = mongoDBService.getCount(CONTRACT_TABLE + chainId, filter);

        for (Document document : docsList) {
            MiniContractInfo contractInfo = DocumentTransferTool.toInfo(document, "contractAddress", MiniContractInfo.class);
            contractInfos.add(contractInfo);
        }
        PageInfo<MiniContractInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, contractInfos);
        return pageInfo;
    }

    @Override
    public List<MiniContractInfo> getContractList(int chainId, List<String> addressList) {
        Bson filter = Filters.in("_id", addressList);
        BasicDBObject fields = new BasicDBObject();
        fields.append("_id", 1).append("remark", 1).append("txCount", 1).append("status", 1)
                .append("createTime", 1).append("balance", 1).append("tokenName", 1).append("symbol", 1)
                .append("decimals", 1).append("totalSupply", 1).append("creater", 1);
        List<Document> docsList = this.mongoDBService.query(CONTRACT_TABLE + chainId, fields);
        List<MiniContractInfo> contractInfos = new ArrayList<>();
        for (Document document : docsList) {
            MiniContractInfo contractInfo = DocumentTransferTool.toInfo(document, "contractAddress", MiniContractInfo.class);
            contractInfos.add(contractInfo);
        }
        return contractInfos;
    }

    public ContractResultInfo getContractResultInfo(int chainId, String txHash) {
        Document document = mongoDBService.findOne(CONTRACT_RESULT_TABLE + chainId, Filters.eq("_id", txHash));
        if (document == null) {
            return null;
        }
        ContractResultInfo contractResultInfo = ContractResultInfo.toInfo(document);
        return contractResultInfo;
    }
}
