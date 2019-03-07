package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AliasInfo;
import io.nuls.api.model.po.db.TransactionInfo;
import io.nuls.api.model.po.db.TxRelationInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Component
public class TransactionService {

    @Autowired
    private MongoDBService mongoDBService;

    public void initSelect(int chainId) {

    }

    public void saveTxList(int chainId, List<TransactionInfo> txList) {
        if (txList.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (TransactionInfo transactionInfo : txList) {
            transactionInfo.calcValue();
            documentList.add(transactionInfo.toDocument());
        }
        mongoDBService.insertMany(MongoTableConstant.TX_TABLE + chainId, documentList);
    }

    public void saveTxRelationList(int chainId, Set<TxRelationInfo> relationInfos) {
        if (relationInfos.isEmpty()) {
            return;
        }

        List<Document> documentList = new ArrayList<>();
        for (TxRelationInfo relationInfo : relationInfos) {
            Document document = DocumentTransferTool.toDocument(relationInfo);
            documentList.add(document);
        }

        mongoDBService.insertMany(MongoTableConstant.TX_RELATION_TABLE + chainId, documentList);
    }
}
