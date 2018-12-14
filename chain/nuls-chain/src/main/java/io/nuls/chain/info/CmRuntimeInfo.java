package io.nuls.chain.info;

import io.nuls.tools.constant.ErrorCode;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public class CmRuntimeInfo {


    public static String getAssetKey(int chainId, int assetId) {
        return chainId + "-" + assetId;
    }

    public static String getMainAsset() {
        String chainId = CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID);
        String assetId = CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_ID);
        return CmRuntimeInfo.getAssetKey(Integer.valueOf(chainId), Integer.valueOf(assetId));
    }

    public static String getChainAssetKey(int chainId, String assetKey) {
        return chainId + "-" + assetKey;
    }

    public static Map<String, String> addError(Map<String, String> map, String code) {
        ErrorCode errorCode = ErrorCode.init(code);
        map.put(errorCode.getCode(), errorCode.getMsg());
        return map;
    }
}
