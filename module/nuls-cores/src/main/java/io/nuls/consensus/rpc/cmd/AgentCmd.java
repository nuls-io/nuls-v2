package io.nuls.consensus.rpc.cmd;

import io.nuls.core.rpc.model.*;
import io.nuls.consensus.model.dto.output.AgentDTO;
import io.nuls.consensus.service.AgentService;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.List;
import java.util.Map;

/**
 * Consensus node related interfaces
 *
 * @author tag
 * 2018/11/7
 */
@Component
@NulsCoresCmd(module = ModuleE.CS)
public class AgentCmd extends BaseCmd {
    @Autowired
    private AgentService service;

    /**
     * Create nodes
     */
    @CmdAnnotation(cmd = "cs_createAgent", version = 1.0, description = "Create node transactions/create agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "agentAddress", parameterType = "String", parameterDes = "Node address")
    @Parameter(parameterName = "packingAddress", parameterType = "String", parameterDes = "Node block address")
    @Parameter(parameterName = "rewardAddress", parameterType = "String", parameterDes = "Reward Address,Default node address", canNull = true)
    @Parameter(parameterName = "commissionRate", requestType = @TypeDescriptor(value = int.class), parameterDes = "commission rate")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "Mortgage amount")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "password")
    @ResponseData(name = "Return value", description = "Create node transactionsHASH", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Create node transactionsHASH")
    }))
    public Response createAgent(Map<String, Object> params) {
        Result result = service.createAgent(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Unregister node
     */
    @CmdAnnotation(cmd = "cs_stopAgent", version = 1.0, description = "Unregister node/stop agent")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Node address")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "password")
    @ResponseData(name = "Return value", description = "Stop node transactionsHASH", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Stop node transactionsHASH")
    }))
    public Response stopAgent(Map<String, Object> params) {
        Result result = service.stopAgent(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Get deregistered nodescoindata
     */
    @CmdAnnotation(cmd = "cs_getStopAgentCoinData", version = 1.0, description = "Get deregistered nodesCoinData")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "nodehash")
    @Parameter(parameterName = "lockHeight", parameterType = "Long", parameterDes = "Lock height")
    @ResponseData(name = "Return value", description = "Stop node transactionsCoinDataofHex", responseType = @TypeDescriptor(value = String.class))
    public Response cs_getStopAgentCoinData(Map<String, Object> params) {
        Result result = service.getStopAgentCoinData(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query consensus node list
     */
    @CmdAnnotation(cmd = "cs_getAgentList", version = 1.0, description = "Query the list of consensus nodes in the current network/Query the list of consensus nodes in the current network")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "Page number", canNull = true)
    @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "Page size", canNull = true)
    @Parameter(parameterName = "keyWord", parameterType = "String", parameterDes = "keyword", canNull = true)
    @ResponseData(name = "Return value", description = "Return aPageObject, only described herePageCollection in objects",
            responseType = @TypeDescriptor(value = List.class, collectionElement = AgentDTO.class)
    )
    public Response getAgentList(Map<String, Object> params) {
        Result result = service.getAgentList(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query specified node information
     */
    @CmdAnnotation(cmd = "cs_getAgentInfo", version = 1.0, description = "Query detailed information of pointing nodes/Query pointer node details")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "nodeHASH")
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = AgentDTO.class))
    public Response getAgentInfo(Map<String, Object> params) {
        Result result = service.getAgentInfo(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Get the current node's outbound address
     */
    @CmdAnnotation(cmd = "cs_getNodePackingAddress", version = 1.0, description = "Get the current node's outbound address/Get the current node's out-of-block address")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @ResponseData(name = "Return value", description = "Current node block address", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "packAddress", description = "Current node block address")
    }))
    public Response getNodePackingAddress(Map<String, Object> params) {
        Result result = service.getNodePackingAddress(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Get all node block addresses/specifyNBlock output address
     */
    @CmdAnnotation(cmd = "cs_getAgentAddressList", version = 1.0, description = "Obtain the current consensus node block address list or query the most recentNOutbound address of blocks/Get all node out-of-block addresses or specify N block out-of-block designations")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @ResponseData(name = "Return value", description = "Consensus node list", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "packAddress", description = "Consensus node list")
    }))
    public Response getAgentAddressList(Map<String, Object> params) {
        Result result = service.getAgentAddressList(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query the status of specified consensus nodes
     */
    @CmdAnnotation(cmd = "cs_getAgentStatus", version = 1.0, description = "Query the status of specified consensus nodes/query the specified consensus node status 1.0")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "nodeHASH")
    @ResponseData(name = "Return value", description = "Node status", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "status", valueType = Byte.class, description = "Node status")
    }))
    public Response getAgentStatus(Map<String, Object> params) {
        Result result = service.getAgentStatus(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Modify node consensus status
     */
    @CmdAnnotation(cmd = "cs_updateAgentConsensusStatus", version = 1.0, description = "Modify node consensus status/modifying the Node Consensus State")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @ResponseData(description = "No specific return value, no error indicates successful modification of node consensus state")
    public Response updateAgentConsensusStatus(Map<String, Object> params) {
        Result result = service.updateAgentConsensusStatus(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Modify node packaging status
     */
    @CmdAnnotation(cmd = "cs_updateAgentStatus", version = 1.0, description = "Modify node packaging status/modifying the Packing State of Nodes")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "status", requestType = @TypeDescriptor(value = int.class), parameterDes = "Node status")
    @ResponseData(description = "No specific return value, no error indicates successful modification of node packaging status")
    public Response updateAgentStatus(Map<String, Object> params) {
        Result result = service.updateAgentStatus(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Obtain the outbound account information of the current node
     */
    @CmdAnnotation(cmd = "cs_getPackerInfo", version = 1.0, description = "Obtain the outbound account information of the current node/modifying the Packing State of Nodes")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address", description = "Current node block address"),
            @Key(name = "password", description = "Current node password"),
            @Key(name = "packAddressList", valueType = List.class, valueElement = String.class, description = "Current packaging address list"),
    }))
    public Response getPackerInfo(Map<String, Object> params) {
        Result result = service.getPackerInfo(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Obtain the outbound account information of the current node
     */
    @CmdAnnotation(cmd = "cs_getSeedNodeInfo", version = 1.0, description = "Obtain seed node information/get seed node info")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address", description = "Current node block address"),
            @Key(name = "password", description = "Current node password"),
            @Key(name = "packAddressList", valueType = List.class, valueElement = String.class, description = "Current packaging address list"),
    }))
    public Response getSeedNodeInfo(Map<String, Object> params) {
        Result result = service.getSeedNodeInfo(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
