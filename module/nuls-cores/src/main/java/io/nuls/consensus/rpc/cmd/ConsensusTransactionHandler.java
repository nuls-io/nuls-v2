package io.nuls.consensus.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.consensus.service.AgentService;
import io.nuls.consensus.service.DepositService;

import java.util.Map;

/**
 * Consensus module transaction validator
 * @author tag
 * @date 2019/6/1
 */
@Component
@NulsCoresCmd(module = ModuleE.CS)
public class ConsensusTransactionHandler extends BaseCmd {

    @Autowired
    private AgentService agentService;

    @Autowired
    private DepositService depositService;

    /**
     * Cancel node transaction verification
     */
    @CmdAnnotation(cmd = "stopAgentValid", version = 1.0, description = "stop agent transaction validate")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "transaction")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Stop node transaction verification results")
    }))
    public Response stopAgentValid(Map<String, Object> params) {
        Result result = agentService.stopAgentValid(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Node validation
     */
    @CmdAnnotation(cmd = "createAgentValid", version = 1.0, description = "create agent transaction validate")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "transaction")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Create node validation results")
    }))
    public Response createAgentValid(Map<String, Object> params) {
        Result result = agentService.createAgentValid(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Exit consensus transaction verification
     */
    @CmdAnnotation(cmd = "withdrawValid", version = 1.0, description = "withdraw deposit agent transaction validate")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "transaction")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Exit consensus transaction verification result")
    }))
    public Response withdrawValid(Map<String, Object> params) {
        Result result = depositService.withdrawValid(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Entrusted consensus transaction verification
     */
    @CmdAnnotation(cmd = "depositValid", version = 1.0, description = "deposit agent transaction validate")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "transaction")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Verification results of entrusted transactions")
    }))
    public Response depositValid(Map<String, Object> params) {
        Result result = depositService.depositValid(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

}
