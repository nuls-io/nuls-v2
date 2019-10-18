package io.nuls.api.db;

import io.nuls.api.model.po.BlockHeaderInfo;
import io.nuls.api.model.po.BlockHexInfo;
import io.nuls.api.model.po.PageInfo;
import io.nuls.api.model.po.mini.MiniBlockHeaderInfo;

import java.math.BigInteger;
import java.util.List;

public interface BlockService {

    BlockHeaderInfo getBestBlockHeader(int chainId);

    BlockHeaderInfo getBlockHeader(int chainId, long height);

    BlockHeaderInfo getBlockHeaderByHash(int chainId, String hash);

    void saveBLockHeaderInfo(int chainId, BlockHeaderInfo blockHeaderInfo);

    PageInfo<MiniBlockHeaderInfo> pageQuery(int chainId, int pageIndex, int pageSize, String packingAddress, boolean filterEmptyBlocks);

    List<MiniBlockHeaderInfo> getBlockList(int chainId, long startHeight, long endHeight);

    int getBlockPackageTxCount(int chainId, long startHeight, long endHeight);

    long getMaxHeight(int chainId, long endTime);

    void deleteBlockHeader(int chainId, long height);

    void saveBlockHexInfo(int chainId, BlockHexInfo hexInfo);

    BlockHexInfo getBlockHexInfo(int chainId, long height);

    BlockHexInfo getBlockHexInfo(int chainId, String hash);

    BigInteger getLast24HourRewards(int chainId);

}
