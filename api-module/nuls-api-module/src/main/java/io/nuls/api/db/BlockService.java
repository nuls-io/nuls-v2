package io.nuls.api.db;

import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.PageInfo;

public interface BlockService {

    BlockHeaderInfo getBestBlockHeader(int chainId);

    BlockHeaderInfo getBlockHeader(int chainId, long height);

    BlockHeaderInfo getBlockHeaderByHash(int chainId, String hash);

    void saveBLockHeaderInfo(int chainId, BlockHeaderInfo blockHeaderInfo);

    PageInfo<BlockHeaderInfo> pageQuery(int chainId, int pageIndex, int pageSize, String packingAddress, boolean filterEmptyBlocks);

    long getMaxHeight(int chainId, long endTime);

    void deleteBlockHeader(int chainId, long height);
}
