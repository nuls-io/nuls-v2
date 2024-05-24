package io.nuls.common;

import io.nuls.core.model.StringUtils;

public class NCUtils {

    public static String getTokenId(int chainId, int assetId) {
        return chainId + "-" + assetId;
    }

    public static int[] splitTokenId(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        String[] arr = key.split("-");
        return new int[]{Integer.parseInt(arr[0]), Integer.parseInt(arr[1])};
    }
}
