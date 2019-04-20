package io.nuls.poc.rpc.cmd;

import io.nuls.poc.service.AgentService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.protocol.ResisterTx;
import io.nuls.tools.protocol.TxMethodType;
import io.nuls.tools.protocol.TxProperty;

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
    @CmdAnnotation(cmd = "cs_createAgent", version = 1.0, description = "create agent 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "agentAddress", parameterType = "String")
    @Parameter(parameterName = "packingAddress", parameterType = "String")
    @Parameter(parameterName = "rewardAddress", parameterType = "String")
    @Parameter(parameterName = "commissionRate", parameterType = "int")
    @Parameter(parameterName = "deposit", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    public Response createAgent(Map<String,Object> params){
        Result result = service.createAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 节点验证
     * */
    @CmdAnnotation(cmd = "cs_createAgentValid", version = 1.0, description = "create agent transaction validate 1.0")
    @ResisterTx(txType = TxProperty.CREATE_AGENT,methodType = TxMethodType.VALID,methodName = "cs_createAgentValid")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response createAgentValid(Map<String,Object> params){
        Result result = service.createAgentValid(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 注销节点
     * */
    @CmdAnnotation(cmd = "cs_stopAgent", version = 1.0, description = "stop agent 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response stopAgent(Map<String,Object> params){
        Result result = service.stopAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 注销节点交易验证
     * */
    @CmdAnnotation(cmd = "cs_stopAgentValid", version = 1.0, description = "stop agent transaction validate 1.0")
    @ResisterTx(txType = TxProperty.STOP_AGENT,methodType = TxMethodType.VALID,methodName = "cs_stopAgentValid")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response stopAgentValid(Map<String,Object> params){
        Result result = service.stopAgentValid(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }



    /**
     * 查询共识节点列表
     * */
    @CmdAnnotation(cmd = "cs_getAgentList", version = 1.0, description = "query consensus node list 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "pageNumber", parameterType = "int")
    @Parameter(parameterName = "pageSize", parameterType = "int")
    @Parameter(parameterName = "keyWord", parameterType = "String")
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
    @CmdAnnotation(cmd = "cs_getAgentInfo", version = 1.0, description = "query specified node information 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "agentHash", parameterType = "String")
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
    @CmdAnnotation(cmd = "cs_getNodePackingAddress", version = 1.0, description = "Get the current node's out-of-block address 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
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
    @CmdAnnotation(cmd = "cs_getAgentAddressList", version = 1.0, description = "Get all node out-of-block addresses/specify N block out-of-block designations")
    @Parameter(parameterName = "chainId", parameterType = "int")
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
    @CmdAnnotation(cmd = "cs_getAgentStatus", version = 1.0, description = "query the specified consensus node status 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "agentHash", parameterType = "String")
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
    @CmdAnnotation(cmd = "cs_updateAgentConsensusStatus", version = 1.0, description = "modifying the Node Consensus State 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
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
    @CmdAnnotation(cmd = "cs_updateAgentStatus", version = 1.0, description = "modifying the Packing State of Nodes 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "status", parameterType = "int")
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
    @CmdAnnotation(cmd = "cs_getPackerInfo", version = 1.0, description = "modifying the Packing State of Nodes 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getPackerInfo(Map<String,Object> params){
        Result result = service.getPackerInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
