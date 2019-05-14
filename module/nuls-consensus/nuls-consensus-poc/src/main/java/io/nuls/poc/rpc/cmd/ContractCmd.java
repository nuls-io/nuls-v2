package io.nuls.poc.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.poc.service.ContractService;

import java.util.Map;

/**
 * 智能合约与共识交互接口
 * @author tag
 * 2019/5/5
 * */
@Component
public class ContractCmd extends BaseCmd {
    @Autowired
    private ContractService service;

    /**
     * 创建节点
     * */
    @CmdAnnotation(cmd = "cs_createContractAgent", version = 1.0, description = "create agent 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "packingAddress", parameterType = "String")
    @Parameter(parameterName = "deposit", parameterType = "String")
    @Parameter(parameterName = "commissionRate", parameterType = "String")
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
    @CmdAnnotation(cmd = "cs_stopContractAgent", version = 1.0, description = "stop agent 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response stopAgent(Map<String,Object> params){
        Result result = service.stopAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 委托共识
     * */
    @CmdAnnotation(cmd = "cs_contractDeposit", version = 1.0, description = "deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "agentHash", parameterType = "String")
    @Parameter(parameterName = "deposit", parameterType = "String")
    public Response depositToAgent(Map<String,Object> params){
        Result result = service.depositToAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 退出共识
     * */
    @CmdAnnotation(cmd = "cs_contractWithdraw", version = 1.0, description = "withdraw deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "joinAgentHash", parameterType = "String")
    public Response withdraw(Map<String,Object> params){
        Result result = service.withdraw(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询节点信息
     * */
    @CmdAnnotation(cmd = "cs_getContractAgentInfo", version = 1.0, description = "withdraw deposit agent transaction 1.0")
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
     * 查询委托信息
     * */
    @CmdAnnotation(cmd = "cs_getContractDepositInfo", version = 1.0, description = "withdraw deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "joinAgentHash", parameterType = "String")
    public Response getDepositInfo(Map<String,Object> params){
        Result result = service.getDepositInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 交易模块触发CoinBase智能合约
     * */
    @CmdAnnotation(cmd = "cs_triggerCoinBaseContract", version = 1.0, description = "withdraw deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    @Parameter(parameterName = "stateRoot", parameterType = "String")
    public Response triggerCoinBaseContract(Map<String,Object> params){
        Result result = service.triggerCoinBaseContract(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
