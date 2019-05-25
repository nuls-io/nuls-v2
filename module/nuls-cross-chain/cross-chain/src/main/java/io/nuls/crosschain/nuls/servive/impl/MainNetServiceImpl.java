package io.nuls.crosschain.nuls.servive.impl;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.GetCirculationMessage;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.rpc.call.ChainManagerCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.servive.MainNetService;
import io.nuls.core.basic.Result;
import io.nuls.crosschain.nuls.utils.LoggerUtil;
import java.util.Map;
import static io.nuls.core.constant.CommonCodeConstanst.DATA_ERROR;
import static io.nuls.core.constant.CommonCodeConstanst.PARAMETER_ERROR;
import static io.nuls.core.constant.CommonCodeConstanst.SUCCESS;


/**
 * 主网跨链模块特有方法
 * @author tag
 * @date 2019/4/23
 */
@Component
public class MainNetServiceImpl implements MainNetService {
    @Override
    public Result registerCrossChain(Map<String, Object> params) {
        return null;
    }

    @Override
    public Result cancelCrossChain(Map<String, Object> params) {
        return null;
    }

    @Override
    public Result getCrossChainList(Map<String, Object> params) {
        int chainId = Integer.parseInt(params.get(ParamConstant.CHAIN_ID).toString());
        String nodeId = params.get(ParamConstant.NODE_ID).toString();
        try {
            RegisteredChainMessage registeredChainMessage = ChainManagerCall.getRegisteredChainInfo();
            NetWorkCall.sendToNode(chainId, registeredChainMessage, nodeId, CommandConstant.REGISTERED_CHAIN_MESSAGE);
            return Result.getSuccess(SUCCESS);
        }catch (Exception e){
            LoggerUtil.commonLog.error(e);
            return Result.getFailed(DATA_ERROR);
        }
    }

    @Override
    public Result getFriendChainCirculat(Map<String, Object> params) {
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
