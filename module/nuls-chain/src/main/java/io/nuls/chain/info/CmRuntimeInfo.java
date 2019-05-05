package io.nuls.chain.info;

import io.nuls.core.constant.ErrorCode;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public class CmRuntimeInfo {
    /**
     * 主网资产链id
     */
    public static String nulsChainId;
    /**
     * 主网资产id
     */
    public static String nulsAssetId;

    public static String getMainAssetKey() {
        return nulsChainId + "-" + nulsAssetId;
    }
    public static String getAssetKey(int assetChainId,int assetId) {
        return assetChainId + "-" + assetId;
    }


    public static String getMainChainId() {
        return nulsChainId;
    }

    public static int getMainIntChainId() {
        return Integer.valueOf(nulsChainId);
    }
    public static int getMainIntAssetId() {
        return Integer.valueOf(nulsAssetId);
    }
    public static String getMainChainAssetKey() {
        String assetKey = CmRuntimeInfo.getAssetKey(Integer.valueOf(nulsChainId), Integer.valueOf(nulsAssetId));
        return CmRuntimeInfo.getChainAssetKey(Integer.valueOf(nulsChainId), assetKey);
    }

    public static String getChainAssetKey(int addressChainId, String assetKey) {
        return addressChainId + "-" + assetKey;
    }

    public static Map<String, String> addError(Map<String, String> map, String code) {
        ErrorCode errorCode = ErrorCode.init(code);
        map.put(errorCode.getCode(), errorCode.getMsg());
        return map;
    }
}
