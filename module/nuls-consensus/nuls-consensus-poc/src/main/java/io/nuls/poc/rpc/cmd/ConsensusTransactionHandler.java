package io.nuls.poc.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.poc.service.AgentService;
import io.nuls.poc.service.DepositService;

import java.util.Map;

@Service
public class ConsensusTransactionHandler extends BaseCmd {

    @Autowired
    private AgentService agentService;

    @Autowired
    private DepositService depositService;

    /**
     * 注销节点交易验证
     */
    @CmdAnnotation(cmd = "stopAgentValid", version = 1.0, description = "stop agent transaction validate 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response stopAgentValid(Map<String, Object> params) {
        Result result = agentService.stopAgentValid(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 节点验证
     */
    @CmdAnnotation(cmd = "createAgentValid", version = 1.0, description = "create agent transaction validate 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response createAgentValid(Map<String, Object> params) {
        Result result = agentService.createAgentValid(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 退出共识交易验证
     */
    @CmdAnnotation(cmd = "withdrawValid", version = 1.0, description = "withdraw deposit agent transaction validate 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response withdrawValid(Map<String, Object> params) {
        Result result = depositService.withdrawValid(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 委托共识交易验证
     */
    @CmdAnnotation(cmd = "depositValid", version = 1.0, description = "deposit agent transaction validate 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response depositValid(Map<String, Object> params) {
        Result result = depositService.depositValid(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

}
