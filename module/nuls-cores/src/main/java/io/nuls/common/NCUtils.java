package io.nuls.common;

public class NCUtils {

    public static String getTokenId(int chainId, int assetId) {
        return chainId + "-" + assetId;
    }
}
