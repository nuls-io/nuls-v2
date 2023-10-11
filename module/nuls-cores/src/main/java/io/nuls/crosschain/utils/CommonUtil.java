package io.nuls.crosschain.utils;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.Coin;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.rpc.call.ConsensusCall;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 跨链模块基础工具类
 *
 * @author: tag
 * @date: 2019/4/12
 */
@Component
public class CommonUtil {
    @Autowired
    private static NulsCoresConfig config;

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
        transactionSignature.setP2PHKSignatures(transactionSignature.getP2PHKSignatures().parallelStream().distinct().collect(Collectors.toList()));
        Iterator<P2PHKSignature> iterator = transactionSignature.getP2PHKSignatures().iterator();
        Set<String> signedList = new HashSet<>();
        String validAddress;
        P2PHKSignature signature;
        while (iterator.hasNext()){
            signature = iterator.next();
            boolean isMatchSign = false;
            validAddress = AddressTool.getAddressString(signature.getPublicKey(), chain.getChainId());
            if(signedList.contains(validAddress)){
                break;
            }
            for (String address:addressList) {
                if(address.equals(validAddress)){
                    signedList.add(address);
                    isMatchSign = true;
                    break;
                }
            }
            if(!isMatchSign){
                misMatchSignList.add(signature);
                iterator.remove();
            }
        }
        chain.getLogger().info("Verification successful account list,signedList:{}",signedList);
        return misMatchSignList;
    }

    /**
     * 获取当前签名拜占庭数量
     * */
    @SuppressWarnings("unchecked")
    public static int getByzantineCount(Chain chain, int agentCount){
        int byzantineRatio = chain.getConfig().getByzantineRatio();
        int minPassCount = agentCount*byzantineRatio/ NulsCrossChainConstant.MAGIC_NUM_100;
        if(minPassCount == 0){
            minPassCount = 1;
        }
        chain.getLogger().debug("当前共识节点数量为：{},最少签名数量为:{}",agentCount,minPassCount );
        return minPassCount;
    }

    /**
     * 获取当前签名拜占庭数量
     * */
    @SuppressWarnings("unchecked")
    public static int getByzantineCount(Transaction ctx, List<String> packAddressList, Chain chain)throws NulsException{
        int agentCount = packAddressList.size();
        int chainId = chain.getChainId();
        int byzantineRatio = chain.getConfig().getByzantineRatio();
        if(ctx.getType() == TxType.VERIFIER_CHANGE){
            VerifierChangeData verifierChangeData = new VerifierChangeData();
            verifierChangeData.parse(ctx.getTxData(),0);
            if(verifierChangeData.getCancelAgentList() != null){
                agentCount += verifierChangeData.getCancelAgentList().size();
            }
            if(verifierChangeData.getRegisterAgentList() != null){
                agentCount -= verifierChangeData.getRegisterAgentList().size();
            }
        }else if(ctx.getType() == config.getCrossCtxType()){
            int fromChainId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getFrom().get(0).getAddress());
            int toChainId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getTo().get(0).getAddress());
            if(chainId == fromChainId || (chainId != toChainId && config.isMainNet())){
                byzantineRatio += NulsCrossChainConstant.FAULT_TOLERANT_RATIO;
                if(byzantineRatio > NulsCrossChainConstant.MAGIC_NUM_100){
                    byzantineRatio = NulsCrossChainConstant.MAGIC_NUM_100;
                }
            }
        }
        int minPassCount = agentCount*byzantineRatio/ NulsCrossChainConstant.MAGIC_NUM_100;
        if(minPassCount == 0){
            minPassCount = 1;
        }
        chain.getLogger().debug("当前共识节点数量为：{},最少签名数量为:{}",agentCount,minPassCount );
        return minPassCount;
    }

    /**
     * 获取当前签名拜占庭数量
     * */
    @SuppressWarnings("unchecked")
    public static int getByzantineCount(List<String> packAddressList, Chain chain, boolean isFromChain){
        int agentCount = packAddressList.size();
        int byzantineRatio = chain.getConfig().getByzantineRatio();
        if(isFromChain){
            byzantineRatio += NulsCrossChainConstant.FAULT_TOLERANT_RATIO;
            if(byzantineRatio > NulsCrossChainConstant.MAGIC_NUM_100){
                byzantineRatio = NulsCrossChainConstant.MAGIC_NUM_100;
            }
        }
        int minPassCount = agentCount * byzantineRatio / NulsCrossChainConstant.MAGIC_NUM_100;
        if(minPassCount == 0){
            minPassCount = 1;
        }
        chain.getLogger().debug("当前共识节点数量为：{},最少签名数量为:{}",agentCount,minPassCount );
        return minPassCount;
    }

    /**
     * 获取当前共识地址账户
     * */
    @SuppressWarnings("unchecked")
    public static List<String> getCurrentPackAddressList(Chain chain){
        Map packerInfo = ConsensusCall.getPackerInfo(chain);
        return (List<String>) packerInfo.get("packAddressList");
    }
}
