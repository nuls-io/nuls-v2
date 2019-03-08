package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;

@Component
public class BlockService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private ChainService chainService;

    public BlockHeaderInfo getBestBlockHeader(int chainId) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache.getBestHeader() == null) {
            SyncInfo syncInfo = chainService.getSyncInfo(chainId);
            if (syncInfo == null) {
                return null;
            }
            apiCache.setBestHeader(getBlockHeader(chainId, syncInfo.getBestHeight()));
        }
        return apiCache.getBestHeader();
    }

    public BlockHeaderInfo getBlockHeader(int chainId, long height) {
        Document document = mongoDBService.findOne(MongoTableConstant.BLOCK_HEADER_TABLE + chainId, Filters.eq("_id", height));
        if (document == null) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "height", BlockHeaderInfo.class);
    }

    public void saveBLockHeaderInfo(int chainId, BlockHeaderInfo blockHeaderInfo) {
        Document document = DocumentTransferTool.toDocument(blockHeaderInfo, "height");
        mongoDBService.insertOne(MongoTableConstant.BLOCK_HEADER_TABLE + chainId, document);
    }
}
