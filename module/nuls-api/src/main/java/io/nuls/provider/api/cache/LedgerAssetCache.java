package io.nuls.provider.api.cache;


import io.nuls.base.api.provider.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.model.AssetInfo;
import io.nuls.provider.rpctools.CrossChainTools;
import io.nuls.provider.rpctools.LegderTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2021/5/31
 */
@Component
public class LedgerAssetCache {

    @Autowired
    private LegderTools legderTools;
    @Autowired
    private CrossChainTools crossChainTools;
    @Autowired
    private Config config;
    private long lastCrossAssetQueryRecordTime;// Last cross chain asset query time
    private long minitus_2 = 60 * 2 * 1000l;// Two minutes
    private Map<String, AssetInfo> CACHE_MAP = new HashMap<>();

    public void initial(int chainId) {
        Result<Map> allAsset = legderTools.getAllAsset(chainId);
        List<Map> list = allAsset.getList();
        for (Map map : list) {
            int assetChainId = Integer.parseInt(map.get("assetChainId").toString());
            int assetId = Integer.parseInt(map.get("assetId").toString());
            int decimalPlace = Integer.parseInt(map.get("decimalPlace").toString());
            String symbol = map.get("assetSymbol").toString();
            String key = assetChainId + "_" + assetId;
            CACHE_MAP.put(key, new AssetInfo(assetChainId, assetId, symbol, decimalPlace));
        }
        lastCrossAssetQueryRecordTime = System.currentTimeMillis();
    }

    public AssetInfo getAssetInfo(int chainId, int assetId) {
        String key = chainId + "_" + assetId;
        AssetInfo dto = CACHE_MAP.get(key);
        if (dto == null) {
            if (chainId == config.getChainId()) {
                Result<AssetInfo> result = legderTools.getAsset(chainId, assetId);
                if (result.isSuccess()) {
                    dto = result.getData();
                }
            } else {
                long now = System.currentTimeMillis();
                if (now - lastCrossAssetQueryRecordTime > minitus_2) {
                    List<AssetInfo> crossAssetInfos = crossChainTools.getRegisteredChainInfoList(config.getChainId());
                    if (crossAssetInfos != null) {
                        for (AssetInfo assetInfo : crossAssetInfos) {
                            String _key = assetInfo.getChainId() + "_" + assetInfo.getAssetId();
                            CACHE_MAP.put(_key, assetInfo);
                        }
                        dto = CACHE_MAP.get(key);
                        lastCrossAssetQueryRecordTime = now;
                    }
                }
            }
            if (dto == null) {
                return null;
            }
            CACHE_MAP.put(key, dto);
        }
        return dto;
    }

}
