package io.nuls.crosschain.nuls.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.nuls.servive.MainNetService;

import java.util.Map;

/**
 * 主网跨链模块特有方法
 * @author tag
 * @date 2019/4/23
 */
@Component
public class MainNetCmd extends BaseCmd {
    @Autowired
    private MainNetService service;
    /**
     * 友链向主网链管理模块注册跨链信息,链管理模块通知跨链模块
     * */
    @CmdAnnotation(cmd = "registerCrossChain", version = 1.0, description = "register Cross Chain")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response registerCrossChain(Map<String,Object> params){
        Result result = service.registerCrossChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 友链向主网连管理模块注销跨链信息，连管理模块通知跨链模块
     * */
    @CmdAnnotation(cmd = "cancelCrossChain", version = 1.0, description = "cancel Cross Chain")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response cancelCrossChain(Map<String,Object> params){
        Result result = service.cancelCrossChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 友链向主网连管理模块注销跨链信息，连管理模块通知跨链模块
     */
    @CmdAnnotation(cmd = "crossChainRegisterChange", version = 1.0, description = "cancel Cross Chain")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response crossChainRegisterChange(Map<String, Object> params) {
        Result result = service.crossChainRegisterChange(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 友链向主网查询所有跨链注册信息
     * Friend Chain inquires all cross-chain registration information from the main network
     * */
    @CmdAnnotation(cmd = "getChains", version = 1.0, description = "cancel Cross Chain")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
    public Response getChains(Map<String,Object> params){
        Result result = service.getCrossChainList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 主网链管理模块向跨链模块获取友链资产信息
     * Access to Friendship Chain Asset Information
     * */
    @CmdAnnotation(cmd = "getFriendChainCirculat", version = 1.0, description = "cancel Cross Chain")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "assetIds", parameterType = "int")
    public Response getFriendChainCirculat(Map<String,Object> params){
        Result result = service.getFriendChainCirculat(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());

    }

}
