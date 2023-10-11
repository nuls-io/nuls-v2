package io.nuls.crosschain.rpc.call;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.crosschain.base.message.CirculationMessage;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.base.model.bo.AssetInfo;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.Circulation;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 与跨链管理模块交易接口
 *
 * @author: tag
 * @date: 2019/4/15
 */
public class ChainManagerCall {
    /**
     * 验证跨链交易资产
     * @param tx
     * @return boolean
     * @throws NulsException
     */
    @SuppressWarnings("unchecked")
    public static boolean verifyCtxAsset(int chainId, Transaction tx) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, NulsCrossChainConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
            HashMap result = (HashMap) CommonCall.request(ModuleE.CM.abbr,"cm_assetCirculateValidator",  params);
            return (boolean) result.get("value");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 跨链交易链资产管理提交
     * @param txList
     * @param blockHeader
     * @return
     * @throws NulsException
     */
    @SuppressWarnings("unchecked")
    public static boolean ctxAssetCirculateCommit(int chainId,List<String> txList, String blockHeader) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, NulsCrossChainConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("txList", txList);
            params.put("blockHeader", blockHeader);
            HashMap result = (HashMap) CommonCall.request(ModuleE.CM.abbr,"cm_assetCirculateCommit", params);
            return (boolean) result.get("value");
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 跨链交易链资产管理回滚
     * @param txList
     * @param blockHeader
     * @return
     * @throws NulsException
     */
    @SuppressWarnings("unchecked")
    public static boolean ctxAssetCirculateRollback(int chainId,List<String> txList, String blockHeader) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, NulsCrossChainConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("txList", txList);
            params.put("blockHeader", blockHeader);
            HashMap result = (HashMap) CommonCall.request(ModuleE.CM.abbr,"cm_assetCirculateRollBack", params);
            return (boolean) result.get("value");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 将链资产流通量发送给链管理模块
     * @return
     * @throws NulsException
     */
    @SuppressWarnings("unchecked")
    public static void sendCirculation(int chainId, CirculationMessage message) throws NulsException {
        try {
            List<Map<String,Object>> assertList = new ArrayList<>();
            if(message.getCirculationList() != null && message.getCirculationList().size() > 0){
                for (Circulation circulation:message.getCirculationList()) {
                    Map<String,Object> assertInfoMap = new HashMap<>(4);
                    assertInfoMap.put("assetId", circulation.getAssetId());
                    assertInfoMap.put("availableAmount", circulation.getAvailableAmount());
                    assertInfoMap.put("freeze", circulation.getFreeze());
                    assertList.add(assertInfoMap);
                }
            }
            Map<String, Object> params = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, NulsCrossChainConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("assets", assertList);
            CommonCall.request(ModuleE.CM.abbr,"updateChainAsset", params);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 向链管理模块查询已注册链信息
     * @return
     * @throws NulsException
     * @param chainManager
     */
    @SuppressWarnings("unchecked")
    public static RegisteredChainMessage getRegisteredChainInfo(ChainManager chainManager) throws NulsException {
        try {
            HashMap result = (HashMap) CommonCall.request(ModuleE.CM.abbr,"getCrossChainInfos", new HashMap(2));
            List<ChainInfo> chainInfoList = new ArrayList<>();
            List<Map<String,Object>> chainInfos = (List<Map<String,Object>> )result.get("chainInfos");
            if(chainInfos != null && chainInfos.size() > 0){
                for (Map<String,Object> chainInfoMap:chainInfos) {
                    ChainInfo chainInfo = JSONUtils.map2pojo(chainInfoMap, ChainInfo.class);
                    for (AssetInfo assetInfo : chainInfo.getAssetInfoList()){
                        if(assetInfo.isUsable()){
                            chainInfoList.add(chainInfo);
                            break;
                        }
                    }
                }
            }
            //如果在跨链模块已经存储了验证人列表，应该已跨链模块的验证人列表为准，链管理只存储了初始验证人列表。
            chainInfoList.forEach(chainInfo -> {
                ChainInfo oldChainInfo = chainManager.getChainInfo(chainInfo.getChainId());
                if(oldChainInfo != null && oldChainInfo.getVerifierList() != null && !oldChainInfo.getVerifierList().isEmpty()){
                    chainInfo.setVerifierList(oldChainInfo.getVerifierList());
                }
            });
            RegisteredChainMessage registeredChainMessage = new RegisteredChainMessage();
            registeredChainMessage.setChainInfoList(chainInfoList);
            return registeredChainMessage;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
