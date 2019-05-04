package io.nuls.poc.rpc.cmd;

import io.nuls.poc.service.DepositService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * 共识委托相关接口
 * @author tag
 * 2018/11/7
 * */
@Component
public class DepositCmd extends BaseCmd {
    @Autowired
    private DepositService service;

    /**
     * 委托共识
     * */
    @CmdAnnotation(cmd = "cs_depositToAgent", version = 1.0, description = "deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "agentHash", parameterType = "String")
    @Parameter(parameterName = "deposit", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
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
    @CmdAnnotation(cmd = "cs_withdraw", version = 1.0, description = "withdraw deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "txHash", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    public Response withdraw(Map<String,Object> params){
        Result result = service.withdraw(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询委托信息列表
     * */
    @CmdAnnotation(cmd = "cs_getDepositList", version = 1.0, description = "query delegation information list 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "pageNumber", parameterType = "int")
    @Parameter(parameterName = "pageSize", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "agentHash", parameterType = "String")
    public Response getDepositList(Map<String,Object> params){
        Result result = service.getDepositList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
