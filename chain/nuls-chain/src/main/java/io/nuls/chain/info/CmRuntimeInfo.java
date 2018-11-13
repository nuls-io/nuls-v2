package io.nuls.chain.info;

import io.nuls.tools.constant.ErrorCode;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public class CmRuntimeInfo {
    public static String dataPath;

    public static String getAssetKey(short chainId, long assetId) {
        return chainId + "-" + assetId;
    }

    public static Map<String, String> addError(Map<String, String> map, String code) {
        ErrorCode errorCode = ErrorCode.init(code);
        map.put(errorCode.getCode(), errorCode.getMsg());
        return map;
    }
}
