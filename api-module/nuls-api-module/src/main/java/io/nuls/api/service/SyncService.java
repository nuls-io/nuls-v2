package io.nuls.api.service;


import io.nuls.api.db.BlockService;
import io.nuls.api.db.ChainService;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.base.data.BlockHeader;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

@Component
public class SyncService {

    @Autowired
    private ChainService chainService;
    @Autowired
    private BlockService blockService;

    public SyncInfo getSyncInfo(int chainId) {
        return chainService.getSyncInfo(chainId);
    }


    public BlockHeaderInfo getBestBlockHeader(int chainId) {
        SyncInfo syncInfo = chainService.getSyncInfo(chainId);
        if (syncInfo == null) {
            return null;
        }
        return blockService.getBlockHeader(chainId, syncInfo.getBestHeight());
    }


}
