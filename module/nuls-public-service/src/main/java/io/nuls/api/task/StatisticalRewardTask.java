package io.nuls.api.task;

import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.BlockService;
import io.nuls.api.db.ChainService;
import io.nuls.api.db.StatisticalService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.BlockHeaderInfo;
import io.nuls.api.model.po.ChainStatisticalInfo;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;

import java.math.BigInteger;

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
        try {
            BigInteger reward = blockService.getLast24HourRewards(chainId);
            ApiCache apiCache = CacheManager.getCache(chainId);
            if (apiCache != null) {
                apiCache.getCoinContextInfo().setDailyReward(reward);
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
            //统计已打包区块的交易数量
            //获取上一次统计截止的区块高度，获取当前最新区块高度，累计之间所有区块的交易数量
            //超过1000条数据后，每次循环统计1000条
            long startHeight = statisticalInfo.getLastStatisticalHeight();
            long endHeight = headerInfo.getHeight();
            while (endHeight - startHeight > 1000) {
                long count = blockService.getBlockPackageTxCount(chainId, startHeight, startHeight + 1000);
                statisticalInfo.setLastStatisticalHeight(startHeight + 1000);
                statisticalInfo.setTxCount(statisticalInfo.getTxCount() + count);
                statisticalService.saveChainStatisticalInfo(statisticalInfo);
                apiCache.getCoinContextInfo().setTxCount(statisticalInfo.getTxCount());
                startHeight += 1000;
                Thread.sleep(100);
            }
            long count = blockService.getBlockPackageTxCount(chainId, startHeight, endHeight);
            statisticalInfo.setLastStatisticalHeight(endHeight);
            statisticalInfo.setTxCount(statisticalInfo.getTxCount() + count);
            statisticalService.saveChainStatisticalInfo(statisticalInfo);

            apiCache.getCoinContextInfo().setTxCount(statisticalInfo.getTxCount());
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
