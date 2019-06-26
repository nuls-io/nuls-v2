package io.nuls.api.task;


import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AssetInfo;
import io.nuls.api.model.po.db.ChainInfo;
import io.nuls.core.basic.Result;

import java.util.HashMap;
import java.util.Map;

public class QueryChainInfoTask implements Runnable {

    private int chainId;


    public QueryChainInfoTask(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void run() {
        Map<Integer, ChainInfo> chainInfoMap;
        Map<String, AssetInfo> assetInfoMap;
        if (ApiContext.isRunCrossChain) {
            Result<Map<String, Object>> result = WalletRpcHandler.getRegisteredChainInfoList();
            Map<String, Object> map = result.getData();
            CacheManager.setChainInfoMap((Map<Integer, ChainInfo>) map.get("chainInfoMap"));
            CacheManager.setAssetInfoMap((Map<String, AssetInfo>) map.get("assetInfoMap"));
            if (CacheManager.getChainInfoMap().isEmpty() && CacheManager.getAssetInfoMap().isEmpty()) {
                ApiContext.isReady = false;
            } else {
                ApiContext.isReady = true;
            }

        } else {
            chainInfoMap = new HashMap<>();
            assetInfoMap = new HashMap<>();
            ApiCache apiCache = CacheManager.getCache(chainId);
            ChainInfo chainInfo = apiCache.getChainInfo();
            chainInfoMap.put(chainInfo.getChainId(), chainInfo);
            assetInfoMap.put(chainInfo.getDefaultAsset().getKey(), chainInfo.getDefaultAsset());

            CacheManager.setChainInfoMap(chainInfoMap);
            CacheManager.setAssetInfoMap(assetInfoMap);

            ApiContext.isReady = true;
        }
    }
}
