package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;

@Component
public class BlockService {

    @Autowired
    private MongoDBService mongoDBService;

    public BlockHeaderInfo getBlockHeader(int chainId, long height) {
        Document document = mongoDBService.findOne(MongoTableConstant.BLOCK_HEADER_TABLE, Filters.eq("_id", height));
        if (document == null) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "height", BlockHeaderInfo.class);
    }
}
