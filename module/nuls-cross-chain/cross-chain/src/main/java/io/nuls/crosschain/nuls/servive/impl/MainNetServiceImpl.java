package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.CirculationMessage;
import io.nuls.crosschain.base.message.GetCirculationMessage;
import io.nuls.crosschain.base.message.GetRegisteredChainMessage;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.base.model.bo.AssetInfo;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.ChainManagerCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.servive.MainNetService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.LoggerUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.util.*;

import static io.nuls.core.constant.CommonCodeConstanst.*;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.CHAIN_NOT_EXIST;
import static io.nuls.crosschain.nuls.constant.ParamConstant.CHAIN_ID;


/**
 * 主网跨链模块特有方法
 * @author tag
 * @date 2019/4/23
 */
@Component
public class MainNetServiceImpl implements MainNetService {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private NulsCrossChainConfig nulsCrossChainConfig;

    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;

    @Override
    public Result registerCrossChain(Map<String, Object> params) {
        if (params == null) {
            LoggerUtil.commonLog.error("参数错误");
            return Result.getFailed(PARAMETER_ERROR);
        }
        ChainInfo chainInfo = JSONUtils.map2pojo(params, ChainInfo.class);
        RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
        registeredChainMessage.getChainInfoList().add(chainInfo);
        registeredCrossChainService.save(registeredChainMessage);
        chainManager.setRegisteredCrossChainList(registeredChainMessage.getChainInfoList());
        return Result.getSuccess(SUCCESS);
    }

    @Override
    public Result cancelCrossChain(Map<String, Object> params) {
        if (params == null || params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.ASSET_ID) == null) {
            LoggerUtil.commonLog.error("参数错误");
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int)params.get(ParamConstant.CHAIN_ID);
        int assetId = (int)params.get(ParamConstant.ASSET_ID);
        RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
        for (ChainInfo chainInfo:registeredChainMessage.getChainInfoList()) {
            if(chainInfo.getChainId() == chainId){
                for (AssetInfo assetInfo:chainInfo.getAssetInfoList()) {
                    if (assetInfo.getAssetId() == assetId){
                        assetInfo.setUsable(false);
                    }
                }
            }
        }
        registeredCrossChainService.save(registeredChainMessage);
        chainManager.setRegisteredCrossChainList(registeredChainMessage.getChainInfoList());
        return Result.getSuccess(SUCCESS);
    }

    @Override
    public Result crossChainRegisterChange(Map<String, Object> params) {
        if (params == null || params.get(ParamConstant.CHAIN_ID) == null) {
            LoggerUtil.commonLog.error("参数错误");
            return Result.getFailed(PARAMETER_ERROR);
        }
        if (!nulsCrossChainConfig.isMainNet()) {
            LoggerUtil.commonLog.error("本链不是主网");
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int) params.get(CHAIN_ID);

        if (chainId != nulsCrossChainConfig.getMainChainId()) {
            LoggerUtil.commonLog.error("本链不是主网");
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            LoggerUtil.commonLog.error("链不存在");
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        try {
            chainManager.setRegisteredCrossChainList(ChainManagerCall.getRegisteredChainInfo().getChainInfoList());
        } catch (Exception e) {
            chain.getLogger().error("跨链注册信息更新失败");
            chain.getLogger().error(e);
        }
        return Result.getSuccess(SUCCESS);
    }

    @Override
    public void getCrossChainList(int chainId, String nodeId, GetRegisteredChainMessage message) {
        try {
            int handleChainId = chainId;
            if (nulsCrossChainConfig.isMainNet()) {
                handleChainId = nulsCrossChainConfig.getMainChainId();
            }
            Chain chain = chainManager.getChainMap().get(handleChainId);
            chain.getLogger().info("收到友链节点{}查询已注册链列表消息！",nodeId);
            RegisteredChainMessage registeredChainMessage = ChainManagerCall.getRegisteredChainInfo();
            chain.getLogger().info("当前已注册跨链的链数量为:{}\n\n",registeredChainMessage.getChainInfoList().size());
            NetWorkCall.sendToNode(chainId, registeredChainMessage, nodeId, CommandConstant.REGISTERED_CHAIN_MESSAGE);
        }catch (Exception e){
            LoggerUtil.commonLog.error(e);
        }
    }

    @Override
    public void receiveCirculation(int chainId, String nodeId, CirculationMessage messageBody) {
        Chain chain = chainManager.getChainMap().get(nulsCrossChainConfig.getMainChainId());
        chain.getLogger().info("接收到友链:{}节点:{}发送的资产该链最新资产流通量信息\n\n", chainId, nodeId);
        try {
            ChainManagerCall.sendCirculation(chainId, messageBody);
        } catch (NulsException e) {
            chain.getLogger().error(e);
        }
    }


    @Override
    public Result getFriendChainCirculation(Map<String, Object> params) {
        if(params == null || params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.ASSET_IDS) == null){
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (Integer)params.get(ParamConstant.CHAIN_ID);
        GetCirculationMessage getCirculationMessage = new GetCirculationMessage();
        getCirculationMessage.setAssetIds((String)params.get(ParamConstant.ASSET_IDS));
        NetWorkCall.broadcast(chainId, getCirculationMessage, CommandConstant.GET_CIRCULLAT_MESSAGE,true);
        return Result.getSuccess(SUCCESS);
    }
}

