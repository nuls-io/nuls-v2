package io.nuls.api.service;


import io.nuls.api.db.ChainService;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

@Component
public class SyncService {

    @Autowired
    private ChainService chainService;

    public SyncInfo getSyncInfo(int chainId) {
        return chainService.getSyncInfo(chainId);
    }




}
