package io.nuls.api.task;

import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.api.service.SyncService;
import io.nuls.tools.core.ioc.SpringLiteContext;

public class SyncBlockTask implements Runnable {

    private int chainId;

    private SyncService syncService;

    public SyncBlockTask(int chainId) {
        this.chainId = chainId;
        syncService = SpringLiteContext.getBean(SyncService.class);
    }

    @Override
    public void run() {
        SyncInfo syncInfo = syncService.getSyncInfo(chainId);
        if (syncInfo != null && !syncInfo.isFinish()) {
            //rollback();
        }

        boolean running = true;
        while (running) {
            try {
                running = syncBlock();
            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }

        }
    }

    private boolean syncBlock() {
        BlockHeaderInfo localBestBlockHeader = syncService.getBestBlockHeader(chainId);
        return false;
    }
}
