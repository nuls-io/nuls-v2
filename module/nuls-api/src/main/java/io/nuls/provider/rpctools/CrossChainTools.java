package io.nuls.provider.rpctools;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.api.model.AssetInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 查询跨链模块数据
 *
 * @author: PierreLuo
 * @date: 2022/3/10
 */
@Component
public class CrossChainTools implements CallRpc {


    public List<AssetInfo> getRegisteredChainInfoList(int chainId) {
        try {
            return callRpc(ModuleE.CC.abbr, "getRegisteredChainInfoList", new HashMap(), (Function<Map<String, Object>, List<AssetInfo>>) map -> {
                if (map == null) {
                    return null;
                }
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) map.get("list");
                List<AssetInfo> assetList = new ArrayList<>();
                for (Map<String, Object> resultMap : resultList) {
                    int id = (Integer) resultMap.get("chainId");
                    if (id != chainId) {
                        List<Map<String, Object>> list = (List<Map<String, Object>>) resultMap.get("assetInfoList");
                        if (list != null) {
                            for (Map<String, Object> assetMap : list) {
                                String symbol = assetMap.get("symbol").toString();
                                int decimals = Integer.parseInt(assetMap.get("decimalPlaces").toString());
                                int assetId = Integer.parseInt(assetMap.get("assetId").toString());
                                assetList.add(new AssetInfo(id, assetId, symbol, decimals));
                            }
                        }
                    }
                }
                return assetList;
            });
        } catch (NulsRuntimeException e) {
            return null;
        }
    }
}
