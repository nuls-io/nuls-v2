package io.nuls.api.manager;

import io.nuls.api.cache.ApiCache;
import io.nuls.api.model.po.db.ChainInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    /**
     * 缓存每条链的数据
     */
    private static Map<Integer, ApiCache> apiCacheMap = new ConcurrentHashMap<>();

    public static void addApiCache(int chainID, ApiCache apiCache) {
        apiCacheMap.put(chainID, apiCache);
    }

    public static ApiCache getCache(int chainID) {
        return apiCacheMap.get(chainID);
    }

    public static void initCache(ChainInfo chainInfo) {
        ApiCache apiCache = new ApiCache();
        apiCache.setChainInfo(chainInfo);
        apiCacheMap.put(chainInfo.getChainId(), apiCache);
    }

    public static void addChainInfo(ChainInfo chainInfo) {
        apiCacheMap.get(chainInfo.getChainId()).setChainInfo(chainInfo);
    }

    public static ChainInfo getChainInfo(int chainId) {
        return apiCacheMap.get(chainId).getChainInfo();
    }

    public static Map<Integer, ApiCache> getApiCaches() {
        return apiCacheMap;
    }

}
