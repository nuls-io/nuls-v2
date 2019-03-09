package io.nuls.api.task;

import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.BlockInfo;
import io.nuls.api.model.po.db.SyncInfo;
import io.nuls.api.service.RollbackService;
import io.nuls.api.service.SyncService;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;

public class SyncBlockTask implements Runnable {

    private int chainId;

    private SyncService syncService;

    private RollbackService rollbackService;

    public SyncBlockTask(int chainId) {
        this.chainId = chainId;
        syncService = SpringLiteContext.getBean(SyncService.class);
        rollbackService = SpringLiteContext.getBean(RollbackService.class);
    }

    @Override
    public void run() {
        //每次同步数据前都查看一下最新的同步信息，如果最新块的数据并没有在一次事务中完全处理，需要对区块数据进行回滚
        //Check the latest synchronization information before each entity synchronization.
        //If the latest block entity is not completely processed in one transaction, you need to roll back the block entity.
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

    /**
     * 同步逻辑
     * 1.Take the record of the latest block saved from the local
     * 2.According to the height of the latest local block, to synchronize the next block of the wallet (local does not start from the 0th block)
     * 3.After syncing to the latest block, the task ends, waiting for the next 10 seconds, resynchronizing
     * 4.Each synchronization needs to be verified with the previous one. If the verification fails, it means local fork and needs to be rolled back.
     * <p>
     * 1. 从本地取出已保存的最新块的记录
     * 2. 根据本地最新块的高度，去同步钱包的下一个块（本地没有则从第0块开始）
     * 3. 同步到最新块后，任务结束，等待下个10秒，重新同步
     * 4. 每次同步都需要和上一块做连续性验证，如果验证失败，说明本地分叉，需要做回滚处理
     *
     * @return boolean 是否还继续同步
     */
    private boolean syncBlock() {
        long localBestHeight;
        BlockHeaderInfo localBestBlockHeader = syncService.getBestBlockHeader(chainId);
        if (localBestBlockHeader == null) {
            localBestHeight = -1;
        } else {
            localBestHeight = localBestBlockHeader.getHeight();
        }

        ApiContext.bestHeight = localBestHeight;

        try {
            return process(localBestBlockHeader);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    private boolean process(BlockHeaderInfo localBestBlockHeader) throws Exception {
        long nextHeight = 0;
        if (localBestBlockHeader != null) {
            nextHeight = localBestBlockHeader.getHeight() + 1;
        }
        BlockInfo newBlock = WalletRpcHandler.getBlockInfo(chainId, nextHeight);
        if (checkBlockContinuity(localBestBlockHeader, newBlock.getHeader())) {
            return syncService.syncNewBlock(chainId, newBlock);
        } else {
            return rollbackService.rollbackBlock();
        }
    }

    /**
     * 区块连续性验证
     * Block continuity verification
     *
     * @param localBest
     * @param newest
     * @return
     */
    private boolean checkBlockContinuity(BlockHeaderInfo localBest, BlockHeaderInfo newest) {
        if (localBest == null) {
            if (newest.getHeight() == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            if (newest.getHeight() == localBest.getHeight() + 1) {
                if (newest.getPreHash().equals(localBest.getHash())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
}
