package io.nuls.api.task;

import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.BlockService;
import io.nuls.api.db.StatisticalService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.BlockHeaderInfo;
import io.nuls.api.model.po.ChainStatisticalInfo;
import io.nuls.api.model.po.mini.MiniBlockHeaderInfo;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.math.BigInteger;
import java.util.List;

public class StatisticalRewardTask implements Runnable {

    private int chainId;

    private BlockService blockService;

    private StatisticalService statisticalService;

    public StatisticalRewardTask(int chainId) {
        this.chainId = chainId;
        blockService = SpringLiteContext.getBean(BlockService.class);
        statisticalService = SpringLiteContext.getBean(StatisticalService.class);
    }

    @Override
    public void run() {
        BigInteger reward = blockService.getLast24HourRewards(chainId);
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache != null) {
            apiCache.getStatisicalCache().setDailyReward(reward);
        }
        //查询当前最新block高度
        BlockHeaderInfo headerInfo = blockService.getBestBlockHeader(chainId);
        if (headerInfo == null) {
            return;
        }
        ChainStatisticalInfo statisticalInfo = statisticalService.getChainStatisticalInfo(chainId);
        if (statisticalInfo == null) {
            statisticalInfo = new ChainStatisticalInfo();
            statisticalInfo.setChainId(chainId);
            statisticalInfo.setLastStatisticalHeight(0);
        }
        List<MiniBlockHeaderInfo> headerInfoList = blockService.getBlockList(chainId, statisticalInfo.getLastStatisticalHeight(), headerInfo.getHeight());
        long count = 0;
        for (MiniBlockHeaderInfo headerInfo1 : headerInfoList) {
            count += headerInfo1.getTxCount();
        }
        statisticalInfo.setTxCount(statisticalInfo.getTxCount() + count);
        statisticalService.saveChainStatisticalInfo(statisticalInfo);
    }
}
