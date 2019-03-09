package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.api.constant.MongoTableConstant.BLOCK_HEADER_TABLE;

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
        Document document = mongoDBService.findOne(BLOCK_HEADER_TABLE + chainId, Filters.eq("_id", height));
        if (document == null) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "height", BlockHeaderInfo.class);
    }

    public BlockHeaderInfo getBlockHeaderByHash(int chainId, String hash) {
        Document document = mongoDBService.findOne(BLOCK_HEADER_TABLE + chainId, Filters.eq("hash", hash));
        if (document == null) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "height", BlockHeaderInfo.class);
    }

    public void saveBLockHeaderInfo(int chainId, BlockHeaderInfo blockHeaderInfo) {
        Document document = DocumentTransferTool.toDocument(blockHeaderInfo, "height");
        mongoDBService.insertOne(BLOCK_HEADER_TABLE + chainId, document);
    }

    public PageInfo<BlockHeaderInfo> pageQuery(int chainId, int pageIndex, int pageSize, String packingAddress, boolean filterEmptyBlocks) {
        Bson filter = null;
        if (StringUtils.isNotBlank(packingAddress)) {
            filter = Filters.eq("packingAddress", packingAddress);
        }
        if (filterEmptyBlocks) {
            if (filter == null) {
                filter = Filters.gt("txCount", 1);
            } else {
                filter = Filters.and(filter, Filters.gt("txCount", 1));
            }
        }
        long totalCount = mongoDBService.getCount(BLOCK_HEADER_TABLE + chainId, filter);
        List<Document> docsList = this.mongoDBService.pageQuery(BLOCK_HEADER_TABLE + chainId, filter, Sorts.descending("_id"), pageIndex, pageSize);
        List<BlockHeaderInfo> list = new ArrayList<>();
        for (Document document : docsList) {
            list.add(DocumentTransferTool.toInfo(document, "height", BlockHeaderInfo.class));
        }
        PageInfo<BlockHeaderInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, list);
        return pageInfo;
    }
}
