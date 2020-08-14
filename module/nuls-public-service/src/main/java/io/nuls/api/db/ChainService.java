package io.nuls.api.db;

import io.nuls.api.model.po.BlockHeaderInfo;
import io.nuls.api.model.po.ChainConfigInfo;
import io.nuls.api.model.po.ChainInfo;
import io.nuls.api.model.po.SyncInfo;

import java.util.List;

public interface ChainService {

    void initCache();

    List<ChainInfo> getChainInfoList();

    List<ChainInfo> getOtherChainInfoList(int chainId);

    SyncInfo getSyncInfo(int chainId);

    void addChainInfo(ChainInfo chainInfo);

    void addCacheChain(ChainInfo chainInfo, ChainConfigInfo configInfo);

    void saveChainList(List<ChainInfo> chainInfoList);

    void rollbackChainList(List<ChainInfo> chainInfoList);

    ChainInfo getChainInfo(int chainId);

    SyncInfo saveNewSyncInfo(int chainId, long newHeight, BlockHeaderInfo headerInfo);

    void updateStep(SyncInfo syncInfo);
}
