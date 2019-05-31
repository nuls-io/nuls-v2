package io.nuls.api.task;


import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.manager.ChainManager;
import io.nuls.api.model.po.db.AssetInfo;
import io.nuls.core.basic.Result;

import java.util.Map;

public class QueryChainInfoTask implements Runnable {

    private int chainId;


    public QueryChainInfoTask(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void run() {
        Result<Map<String, AssetInfo>> result = WalletRpcHandler.getRegisteredChainInfoList();
        if (result.isSuccess()) {
            Map<String, AssetInfo> map = result.getData();
            CacheManager.setAssetInfoMap(map);
        }
    }
}
