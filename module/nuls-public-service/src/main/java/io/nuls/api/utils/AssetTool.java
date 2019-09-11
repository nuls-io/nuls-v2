package io.nuls.api.utils;

import io.nuls.api.ApiContext;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.AgentService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.CoinContextInfo;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.util.HashMap;
import java.util.Map;

public class AssetTool {

    public static Map getNulsAssets() {
        ApiCache apiCache = CacheManager.getCache(ApiContext.defaultChainId);
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("trades", coinContextInfo.getTxCount());
        map.put("totalAssets", coinContextInfo.getTotal());
        map.put("circulation", coinContextInfo.getCirculation());
        map.put("deposit", coinContextInfo.getConsensusTotal());
        map.put("circulation", coinContextInfo.getCirculation());
        map.put("business", coinContextInfo.getBusiness());
        map.put("team", coinContextInfo.getTeam());
        map.put("community", coinContextInfo.getCommunity());
        map.put("unmapped", coinContextInfo.getUnmapped());
        map.put("dailyReward", coinContextInfo.getDailyReward());
        int consensusCount = apiCache.getCurrentRound().getMemberCount() - apiCache.getChainInfo().getSeeds().size();
        if (consensusCount < 0) {
            consensusCount = 0;
        }
        map.put("consensusNodes", consensusCount);
        long count = 0;
        if (apiCache.getBestHeader() != null) {
            AgentService agentService = SpringLiteContext.getBean(AgentService.class);
            if (agentService != null) {
                count = agentService.agentsCount(ApiContext.defaultChainId, apiCache.getBestHeader().getHeight());
            }
        }
        map.put("totalNodes", count);
        return map;
    }
}
