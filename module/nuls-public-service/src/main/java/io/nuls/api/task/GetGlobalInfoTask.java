package io.nuls.api.task;

import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.core.basic.Result;

import java.util.Map;

public class GetGlobalInfoTask implements Runnable {

    private int chainId;

    public GetGlobalInfoTask(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void run() {
        Result<Map<String, Object>> result = WalletRpcHandler.getBlockGlobalInfo(chainId);
        if (result == null || result.isFailed()) {
            return;
        }
        Map<String, Object> map = result.getData();
        ApiContext.localHeight = Long.parseLong(map.get("localHeight").toString());
        ApiContext.networkHeight = Long.parseLong(map.get("networkHeight").toString());

        if(ApiContext.magicNumber == 0) {
            result = WalletRpcHandler.getNetworkInfo(chainId);
            map = result.getData();
            ApiContext.magicNumber = Integer.parseInt(map.get("magicNumber").toString());
        }
    }
}
