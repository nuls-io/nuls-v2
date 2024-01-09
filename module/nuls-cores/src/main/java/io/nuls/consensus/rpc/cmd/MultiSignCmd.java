package io.nuls.consensus.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.consensus.service.MultiSignService;

import java.util.Map;

/**
 * Multiple account related interfaces
 * Multi-Sign Account Related Interface
 *
 * @author tag
 * 2019/7/25
 * */
@Component
@NulsCoresCmd(module = ModuleE.CS)
public class MultiSignCmd extends BaseCmd {
    @Autowired
    private MultiSignService service;

    /**
     * Multiple account creation nodes
     * */
    @CmdAnnotation(cmd = "cs_createMultiAgent", version = 1.0, description = "Multiple account creation nodes/Multi-Sign Account create agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "agentAddress", parameterType = "String", parameterDes = "Node address(Multiple signed addresses)")
    @Parameter(parameterName = "packingAddress", parameterType = "String", parameterDes = "Node block address")
    @Parameter(parameterName = "rewardAddress", parameterType = "String", parameterDes = "Reward Address,Default node address", canNull = true)
    @Parameter(parameterName = "commissionRate", requestType = @TypeDescriptor(value = int.class), parameterDes = "commission rate")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "Mortgage amount")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Signature account password")
    @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "Signature account address")
    @ResponseData(name = "Return value", description = "Return aMap,Including threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign"),
            @Key(name = "txHash",  description = "transactionhash"),
            @Key(name = "completed", valueType = boolean.class, description = "true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures")
    }))
    public Response createMultiAgent(Map<String,Object> params){
        Result result = service.createMultiAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Multiple account cancellation nodes
     * */
    @CmdAnnotation(cmd = "cs_stopMultiAgent", version = 1.0, description = "Multiple account cancellation nodes/Multi-Sign Account stop agent")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Node address(Multiple signed addresses)")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Signature account password")
    @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "Signature account address")
    @ResponseData(name = "Return value", description = "Return aMap,Including threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign"),
            @Key(name = "txHash",  description = "transactionhash"),
            @Key(name = "completed", valueType = boolean.class, description = "true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures")
    }))
    public Response stopMultiAgent(Map<String,Object> params){
        Result result = service.stopMultiAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Multiple account delegation consensus
     * */
    @CmdAnnotation(cmd = "cs_multiDeposit", version = 1.0, description = "Multiple account delegation consensus/Multi-Sign Account deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Multiple account addresses signed")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "nodeHASH")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "Entrusted amount")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Signature account password")
    @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "Signature account address")
    @ResponseData(name = "Return value", description = "Return aMap,Including threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign"),
            @Key(name = "txHash",  description = "transactionhash"),
            @Key(name = "completed", valueType = boolean.class, description = "true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures")
    }))
    public Response multiDeposit(Map<String,Object> params){
        Result result = service.multiDeposit(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Consensus on account exit with multiple signatures
     * */
    @CmdAnnotation(cmd = "cs_multiWithdraw", version = 1.0, description = "Consensus on account exit with multiple signatures/Multi-Sign Account withdraw deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Multiple account addresses signed")
    @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "Join consensus tradingHASH")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Signature account password")
    @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "Signature account address")
    @ResponseData(name = "Return value", description = "Return aMap,Including threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign"),
            @Key(name = "txHash",  description = "transactionhash"),
            @Key(name = "completed", valueType = boolean.class, description = "true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures")
    }))
    public Response multiWithdraw(Map<String,Object> params){
        Result result = service.multiWithdraw(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
