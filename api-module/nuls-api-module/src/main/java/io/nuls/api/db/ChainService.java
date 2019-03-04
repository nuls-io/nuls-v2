package io.nuls.api.db;


import com.mongodb.client.model.Filters;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.api.utils.DocumentTransferTool;
import org.bson.Document;

public class ChainService {

    private MongoDBService mongoDBService;

    public ChainService() {
    }

    public ChainService(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
    }

    public SyncInfo getSyncInfo(int chainId) {
        Document document = mongoDBService.findOne(MongoTableConstant.SYNC_INFO_TABLE, Filters.eq("_id", chainId));
        return DocumentTransferTool.toInfo(document, "chainId", SyncInfo.class);
    }

}
