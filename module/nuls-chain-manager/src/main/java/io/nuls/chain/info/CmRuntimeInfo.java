package io.nuls.chain.info;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public class CmRuntimeInfo {


    public static final String COMMA = ",";
    public static final String COLON = ":";
    public static final String MID_LINE = "-";

    /**
     * 主网资产链id
     */
    public static String nulsChainId;
    /**
     * 主网资产id
     */
    public static String nulsAssetId;

    public static String getMainAssetKey() {
        return nulsChainId + MID_LINE + nulsAssetId;
    }

    public static String getAssetKey(int assetChainId, int assetId) {
        return assetChainId + MID_LINE + assetId;
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
        return addressChainId + MID_LINE + assetKey;
    }

    public static boolean isAddressChainEqAssetChain(int addressChainId, String assetKey) {
        return addressChainId == Integer.valueOf(assetKey.split(MID_LINE)[0]);
    }

    public static String getAssetIdByAssetKey(String assetKey) {
        return assetKey.split(MID_LINE)[1];
    }


}
