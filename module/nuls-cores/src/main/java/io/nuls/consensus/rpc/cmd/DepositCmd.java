package io.nuls.consensus.rpc.cmd;

import io.nuls.core.rpc.model.*;
import io.nuls.consensus.model.dto.output.DepositDTO;
import io.nuls.consensus.service.DepositService;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.List;
import java.util.Map;

/**
 * Consensus delegation related interfaces
 * @author tag
 * 2018/11/7
 * */
@Component
@NulsCoresCmd(module = ModuleE.CS)
public class DepositCmd extends BaseCmd {
    @Autowired
    private DepositService service;

    /**
     * Commission consensus
     * */
    @CmdAnnotation(cmd = "cs_depositToAgent", version = 1.0, description = "Create entrusted transactions/deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "nodeHASH")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "Entrusted amount")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    @ResponseData(name = "Return value", description = "Join consensus tradingHash", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Join consensus tradingHash")
    }))
    public Response depositToAgent(Map<String,Object> params){
        Result result = service.depositToAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Exit consensus
     * */
    @CmdAnnotation(cmd = "cs_withdraw", version = 1.0, description = "Exit entrusted transaction/withdraw deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address")
    @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "Join consensus tradingHASH")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    @ResponseData(name = "Return value", description = "Exit consensus tradingHash", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Exit consensus tradingHash")
    }))
    public Response withdraw(Map<String,Object> params){
        Result result = service.withdraw(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query delegation information list
     * */
    @CmdAnnotation(cmd = "cs_getDepositList", version = 1.0, description = "Query delegation information for a specified account or node/Query delegation information for a specified account or node")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "Page number")
    @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "Quantity per page")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "nodeHASH")
    @ResponseData(name = "Return value", description = "Return aPageObject, only described herePageCollection in objects",
            responseType = @TypeDescriptor(value = List.class, collectionElement = DepositDTO.class)
    )
    public Response getDepositList(Map<String,Object> params){
        Result result = service.getDepositList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
