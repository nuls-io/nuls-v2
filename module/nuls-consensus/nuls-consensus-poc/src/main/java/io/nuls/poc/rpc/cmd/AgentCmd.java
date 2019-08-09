package io.nuls.poc.rpc.cmd;

import io.nuls.core.rpc.model.*;
import io.nuls.poc.model.dto.output.AgentDTO;
import io.nuls.poc.service.AgentService;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.List;
import java.util.Map;

/**
 * 共识节点相关接口
 * @author tag
 * 2018/11/7
 * */
@Component
public class AgentCmd extends BaseCmd {
    @Autowired
    private AgentService service;

    /**
     * 创建节点
     * */
    @CmdAnnotation(cmd = "cs_createAgent", version = 1.0, description = "创建节点交易/create agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "agentAddress", parameterType = "String", parameterDes = "节点地址")
    @Parameter(parameterName = "packingAddress", parameterType = "String", parameterDes = "节点出块地址")
    @Parameter(parameterName = "rewardAddress", parameterType = "String", parameterDes = "奖励地址,默认节点地址", canNull = true)
    @Parameter(parameterName = "commissionRate", requestType = @TypeDescriptor(value = int.class), parameterDes = "佣金比例")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "抵押金额")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "密码")
    @ResponseData(name = "返回值", description = "创建节点交易HASH", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "创建节点交易HASH")
    }))
    public Response createAgent(Map<String,Object> params){
        Result result = service.createAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 注销节点
     * */
    @CmdAnnotation(cmd = "cs_stopAgent", version = 1.0, description = "注销节点/stop agent")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "节点地址")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "密码")
    @ResponseData(name = "返回值", description = "停止节点交易HASH", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "停止节点交易HASH")
    }))
    public Response stopAgent(Map<String,Object> params){
        Result result = service.stopAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询共识节点列表
     * */
    @CmdAnnotation(cmd = "cs_getAgentList", version = 1.0, description = "查询当前网络中的共识节点列表/Query the list of consensus nodes in the current network")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "页码", canNull = true)
    @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "每页大小", canNull = true)
    @Parameter(parameterName = "keyWord", parameterType = "String", parameterDes = "关键字", canNull = true)
    @ResponseData(name = "返回值", description = "返回一个Page对象，这里只描述Page对象中的集合",
            responseType = @TypeDescriptor(value = List.class, collectionElement = AgentDTO.class)
    )
    public Response getAgentList(Map<String,Object> params){
        Result result = service.getAgentList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询指定节点信息
     * */
    @CmdAnnotation(cmd = "cs_getAgentInfo", version = 1.0, description = "查询指点节点节点详细信息/Query pointer node details")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "节点HASH")
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = AgentDTO.class))
    public Response getAgentInfo(Map<String,Object> params){
        Result result = service.getAgentInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 获取当前节点出块地址
     * */
    @CmdAnnotation(cmd = "cs_getNodePackingAddress", version = 1.0, description = "获取当前节点出块地址/Get the current node's out-of-block address")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @ResponseData(name = "返回值", description = "当前节点出块地址", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "packAddress", description = "当前节点出块地址")
    }))
    public Response getNodePackingAddress(Map<String,Object> params){
        Result result = service.getNodePackingAddress(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 获取所有节点出块地址/指定N个区块出块地址
     * */
    @CmdAnnotation(cmd = "cs_getAgentAddressList", version = 1.0, description = "获取当前网络共识节点出块地址列表或则查询最近N个区块的出块地址/Get all node out-of-block addresses or specify N block out-of-block designations")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @ResponseData(name = "返回值", description = "共识节点列表", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "packAddress", description = "共识节点列表")
    }))
    public Response getAgentAddressList(Map<String,Object> params){
        Result result = service.getAgentAddressList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询指定共识节点状态
     * */
    @CmdAnnotation(cmd = "cs_getAgentStatus", version = 1.0, description = "查询指定共识节点状态/query the specified consensus node status 1.0")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "节点HASH")
    @ResponseData(name = "返回值", description = "节点状态", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "status",valueType = Byte.class, description = "节点状态")
    }))
    public Response getAgentStatus(Map<String,Object> params){
        Result result = service.getAgentStatus(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 修改节点共识状态
     * */
    @CmdAnnotation(cmd = "cs_updateAgentConsensusStatus", version = 1.0, description = "修改节点共识状态/modifying the Node Consensus State")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @ResponseData(description = "无特定返回值，无错误则表示节点共识状态修改成功")
    public Response updateAgentConsensusStatus(Map<String,Object> params){
        Result result = service.updateAgentConsensusStatus(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 修改节点打包状态
     * */
    @CmdAnnotation(cmd = "cs_updateAgentStatus", version = 1.0, description = "修改节点打包状态/modifying the Packing State of Nodes")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "status", requestType = @TypeDescriptor(value = int.class), parameterDes = "节点状态")
    @ResponseData(description = "无特定返回值，无错误则表示节点打包状态修改成功")
    public Response updateAgentStatus(Map<String,Object> params){
        Result result = service.updateAgentStatus(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 获取当前节点的出块账户信息
     * */
    @CmdAnnotation(cmd = "cs_getPackerInfo", version = 1.0, description = "获取当前节点的出块账户信息/modifying the Packing State of Nodes")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address", description = "当前节点出块地址"),
            @Key(name = "password", description = "当前节点密码"),
            @Key(name = "packAddressList", valueType = List.class, valueElement = String.class, description = "当前打包地址列表"),
    }))
    public Response getPackerInfo(Map<String,Object> params){
        Result result = service.getPackerInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 获取当前节点的出块账户信息
     * */
    @CmdAnnotation(cmd = "cs_getSeedNodeInfo", version = 1.0, description = "获取种子节点信息/get seed node info")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address", description = "当前节点出块地址"),
            @Key(name = "password", description = "当前节点密码"),
            @Key(name = "packAddressList", valueType = List.class, valueElement = String.class, description = "当前打包地址列表"),
    }))
    public Response getSeedNodeInfo(Map<String,Object> params){
        Result result = service.getSeedNodeInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
