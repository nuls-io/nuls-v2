package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.model.po.db.ContractInfo;
import io.nuls.api.model.po.db.ContractResultInfo;
import io.nuls.api.model.po.db.ContractTxInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.parse.JSONUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.api.constant.MongoTableConstant.*;

@Component
public class ContractService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private AccountService accountService;

    public ContractInfo getContractInfo(int chainId, String contractAddress) {
        Document document = mongoDBService.findOne(CONTRACT_TABLE + chainId, Filters.eq("_id", contractAddress));
        if (document == null) {
            return null;
        }
        ContractInfo tokenInfo = DocumentTransferTool.toInfo(document, "contractAddress", ContractInfo.class);
//        if (tokenInfo != null && tokenInfo.getMethodStr() != null) {
//            tokenInfo.setMethods(JSONUtils.json2list(tokenInfo.getMethodStr(), ContractMethod.class));
//        }
//        tokenInfo.setMethodStr(null);
        return tokenInfo;
    }

    public ContractInfo getContractInfoByHash(int chainId, String txHash) {
        Document document = mongoDBService.findOne(CONTRACT_TABLE + chainId, Filters.eq("createTxHash", txHash));
        if (document == null) {
            return null;
        }
        ContractInfo tokenInfo = DocumentTransferTool.toInfo(document, "contractAddress", ContractInfo.class);
//        tokenInfo.setMethods(JSONUtils.json2list(tokenInfo.getMethodStr(), ContractMethod.class));
//        tokenInfo.setMethodStr(null);
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
        mongoDBService.bulkWrite(CONTRACT_TABLE + chainId, modelList);
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
        mongoDBService.insertMany(CONTRACT_TX_TABLE + chainId, documentList);
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
        mongoDBService.insertMany(CONTRACT_RESULT_TABLE + chainId, documentList);
    }
}
