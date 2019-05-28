package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.CirculationMessage;
import io.nuls.crosschain.base.message.GetCirculationMessage;
import io.nuls.crosschain.base.message.GetRegisteredChainMessage;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.ChainManagerCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.servive.MainNetService;
import io.nuls.crosschain.nuls.utils.LoggerUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.util.Map;

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

    @Override
    public Result registerCrossChain(Map<String, Object> params) {

        return Result.getSuccess(SUCCESS);
    }

    @Override
    public Result cancelCrossChain(Map<String, Object> params) {
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
            RegisteredChainMessage registeredChainMessage = ChainManagerCall.getRegisteredChainInfo();
            NetWorkCall.sendToNode(chainId, registeredChainMessage, nodeId, CommandConstant.REGISTERED_CHAIN_MESSAGE);
        }catch (Exception e){
            LoggerUtil.commonLog.error(e);
        }
    }

    @Override
    public void receiveCirculation(int chainId, String nodeId, CirculationMessage messageBody) {
        Chain chain = chainManager.getChainMap().get(nulsCrossChainConfig.getMainChainId());
        chain.getLogger().info("接收到友链:{}节点:{}发送的资产该链最新资产流通量信\n\n", chainId, nodeId);
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
