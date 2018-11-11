package io.nuls.chain.info;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public class CmRuntimeInfo {
    public static String dataPath;

    public static String getAssetKey(short chainId, short assetId) {
        return chainId + "-" + assetId;
    }
}
