package io.nuls.crosschain.nuls.utils;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.Coin;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;

import java.util.*;

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

        if (chainId == config.getChainId()
                && assetId == config.getAssetId()) {
            return true;
        }
        return false;
    }

    public static List<P2PHKSignature> getMisMatchSigns(Chain chain, TransactionSignature transactionSignature, List<String> addressList){
        List<P2PHKSignature>misMatchSignList = new ArrayList<>();
        Iterator<P2PHKSignature> iterator = transactionSignature.getP2PHKSignatures().iterator();
        while (iterator.hasNext()){
            P2PHKSignature signature = iterator.next();
            boolean isMatchSign = false;
            for (String address:addressList) {
                if(Arrays.equals(AddressTool.getAddress(signature.getPublicKey(), chain.getChainId()), AddressTool.getAddress(address))){
                    isMatchSign = true;
                    break;
                }
            }
            if(!isMatchSign){
                misMatchSignList.add(signature);
                iterator.remove();
            }
        }
        return misMatchSignList;
    }

    /**
     * 获取当前签名拜占庭数量
     * */
    @SuppressWarnings("unchecked")
    public static int getByzantineCount(List<String> packAddressList, Chain chain){
        int agentCount = packAddressList.size();
        int minPassCount = agentCount*chain.getConfig().getByzantineRatio()/ NulsCrossChainConstant.MAGIC_NUM_100;
        if(minPassCount == 0){
            minPassCount = 1;
        }
        chain.getLogger().info("当前共识节点数量为：{},最少签名数量为:{}",agentCount,minPassCount );
        return minPassCount;
    }

    /**
     * 获取当前共识地址账户
     * */
    @SuppressWarnings("unchecked")
    public static List<String> getCurrentPackAddresList(Chain chain){
        Map packerInfo = ConsensusCall.getPackerInfo(chain);
        return (List<String>) packerInfo.get("packAddressList");
    }
}
