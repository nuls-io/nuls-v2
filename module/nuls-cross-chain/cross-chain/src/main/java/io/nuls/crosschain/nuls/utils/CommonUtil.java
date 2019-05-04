package io.nuls.crosschain.nuls.utils;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.Coin;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;

/**
 * 跨链模块基础工具类
 *
 * @author: tag
 * @date: 2019/4/12
 */
public class CommonUtil {
    private static NulsCrossChainConfig config = SpringLiteContext.getBean(NulsCrossChainConfig.class);
    /**
     * RPCUtil 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     * @throws NulsException
     */
    public static <T> T getInstanceRpcStr(String data, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (StringUtils.isBlank(data)) {
            throw new NulsException(NulsCrossChainErrorCode.DATA_NOT_FOUND);
        }
        return getInstance(RPCUtil.decode(data), clazz);
    }

    public static <T> T getInstance(byte[] bytes, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (null == bytes || bytes.length == 0) {
            throw new NulsException(NulsCrossChainErrorCode.DATA_NOT_FOUND);
        }
        try {
            BaseNulsData baseNulsData = clazz.getDeclaredConstructor().newInstance();
            baseNulsData.parse(new NulsByteBuffer(bytes));
            return (T) baseNulsData;
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsException(NulsCrossChainErrorCode.DESERIALIZE_ERROR);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(NulsCrossChainErrorCode.DESERIALIZE_ERROR);
        }
    }

    public static boolean isNulsAsset(Coin coin) {
        return isNulsAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isNulsAsset(int chainId, int assetId) {
        if (chainId == config.getMainChainId()
                && assetId == config.getMainAssetId()) {
            return true;
        }
        return false;
    }

    public static boolean isLocalAsset(Coin coin) {
        return isLocalAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isLocalAsset(int chainId, int assetId) {

        if (chainId == config.getConfigBean().getChainId()
                && assetId == config.getConfigBean().getAssetsId()) {
            return true;
        }
        return false;
    }
}
