package io.nuls.crosschain.nuls.rpc.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.constant.CrossChainErrorCode;
import io.nuls.crosschain.base.message.CirculationMessage;
import io.nuls.crosschain.base.message.GetRegisteredChainMessage;
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
    @CmdAnnotation(cmd = "registerCrossChain", version = 1.0, description = "链注册跨链/register Cross Chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "chainName", parameterType = "String", parameterDes = "链名称")
    @Parameter(parameterName = "minAvailableNodeNum", requestType = @TypeDescriptor(value = int.class), parameterDes = "最小链接数")
    @Parameter(parameterName = "assetInfoList", parameterType = "List<AssetInfo>", parameterDes = "资产列表")
    @Parameter(parameterName = "registerTime", requestType = @TypeDescriptor(value = long.class), parameterDes = "链注册时间")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class ,description = "处理结果")
    }))
    public Response registerCrossChain(Map<String,Object> params){
        Result result = service.registerCrossChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 注册链新资产
     * */
    @CmdAnnotation(cmd = "registerAsset", version = 1.0, description = "链注册跨链/register Cross Chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产ID")
    @Parameter(parameterName = "symbol", parameterType = "String", parameterDes = "资产符号")
    @Parameter(parameterName = "assetName", parameterType = "String", parameterDes = "资产名称")
    @Parameter(parameterName = "usable", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "是否可用")
    @Parameter(parameterName = "decimalPlaces", requestType = @TypeDescriptor(value = int.class), parameterDes = "精度")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class ,description = "处理结果")
    }))
    public Response registerAsset(Map<String,Object> params){
        Result result = service.registerAssert(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 友链向主网连管理模块注销跨链信息，连管理模块通知跨链模块
     * */
    @CmdAnnotation(cmd = "cancelCrossChain", version = 1.0, description = "指定链资产退出跨链/Specified Chain Assets Exit Cross Chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产ID")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class ,description = "处理结果")
    }))
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
    @CmdAnnotation(cmd = "crossChainRegisterChange", version = 1.0, description = "跨链注册信息变更/Registered Cross Chain change")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @ResponseData(description = "无特定返回值，没有错误即成功")
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
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
    public Response getChains(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        GetRegisteredChainMessage message = new GetRegisteredChainMessage();
        message.parse(new NulsByteBuffer(decode));
        service.getCrossChainList(chainId,nodeId,message);
        return success();
    }

    /**
     * 接收其他链节点发送的资产信息
     * */
    @CmdAnnotation(cmd = CommandConstant.CIRCULATION_MESSAGE, version = 1.0, description = "接收其他链节点发送的资产信息/Receiving asset information sent by other link nodes")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
    public Response recvCirculat(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        CirculationMessage message = new CirculationMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveCirculation(chainId,nodeId,message);
        return success();
    }

    /**
     * 主网链管理模块向跨链模块获取友链资产信息
     * Access to Friendship Chain Asset Information
     * */
    @CmdAnnotation(cmd = "getFriendChainCirculate", version = 1.0, description = "获取友链资产信息/Access to Friendship Chain Asset Information")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "assetIds", parameterType = "String",parameterDes = "资产ID，多个资产ID用逗号分隔")
    @ResponseData(description = "无特定返回值，没有错误即成功")
    public Response getFriendChainCirculate(Map<String,Object> params){
        Result result = service.getFriendChainCirculation(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

}
