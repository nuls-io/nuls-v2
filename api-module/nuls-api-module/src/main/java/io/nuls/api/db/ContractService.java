package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.model.po.db.ContractInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.parse.JSONUtils;
import org.bson.Document;

@Component
public class ContractService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private AccountService accountService;

    public ContractInfo getContractInfo(String contractAddress) throws Exception {
        Document document = mongoDBService.findOne(MongoTableConstant.CONTRACT_TABLE, Filters.eq("_id", contractAddress));
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
}
