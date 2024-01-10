package io.nuls.consensus.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.consensus.service.ContractService;

import java.util.List;
import java.util.Map;

/**
 * Smart Contract and Consensus Interaction Interface
 * @author tag
 * 2019/5/5
 * */
@Component
@NulsCoresCmd(module = ModuleE.CS)
public class CsmContractCmd extends BaseCmd {
    @Autowired
    private ContractService service;

    /**
     * Create nodes
     * */
    @CmdAnnotation(cmd = "cs_createContractAgent", version = 1.0, description = "Smart contract creation node/contract create agent")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "packingAddress", parameterType = "String",parameterDes = "Block address")
    @Parameter(parameterName = "deposit", parameterDes = "Mortgage amount")
    @Parameter(parameterName = "commissionRate", parameterDes = "commission rate")
    @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    @Parameter(parameterName = "contractSender", parameterDes = "Contract caller address")
    @Parameter(parameterName = "contractBalance", parameterDes = "Current balance of contract address")
    @Parameter(parameterName = "contractNonce", parameterDes = "The current contract addressnoncevalue")
    @Parameter(parameterName = "blockTime", requestType = @TypeDescriptor(value = long.class), parameterDes = "The current packaged block time")
    @ResponseData(name = "Return value", description = "Return transactionHASHAnd transactions",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
    public Response createAgent(Map<String,Object> params){
        Result result = service.createAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Unregister node
     * */
    @CmdAnnotation(cmd = "cs_stopContractAgent", version = 1.0, description = "Smart contract cancellation node/contract stop agent")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    @Parameter(parameterName = "contractSender", parameterDes = "Contract caller address")
    @Parameter(parameterName = "contractBalance", parameterDes = "Current balance of contract address")
    @Parameter(parameterName = "contractNonce", parameterDes = "The current contract addressnoncevalue")
    @Parameter(parameterName = "blockTime", requestType = @TypeDescriptor(value = long.class), parameterDes = "The current packaged block time")
    @ResponseData(name = "Return value", description = "Return transactionHASHAnd transactions",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
    public Response stopAgent(Map<String,Object> params){
        Result result = service.stopAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Commission consensus
     * */
    @CmdAnnotation(cmd = "cs_contractDeposit", version = 1.0, description = "Smart Contract Entrustment Consensus/contract deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "agentHash", parameterDes = "Delegated nodesHASH")
    @Parameter(parameterName = "deposit", parameterDes = "Entrusted amount")
    @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    @Parameter(parameterName = "contractSender", parameterDes = "Contract caller address")
    @Parameter(parameterName = "contractBalance", parameterDes = "Current balance of contract address")
    @Parameter(parameterName = "contractNonce", parameterDes = "The current contract addressnoncevalue")
    @Parameter(parameterName = "blockTime", requestType = @TypeDescriptor(value = long.class), parameterDes = "The current packaged block time")
    @ResponseData(name = "Return value", description = "Return transactionHASHAnd transactions",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
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
    @CmdAnnotation(cmd = "cs_contractWithdraw", version = 1.0, description = "Consensus on smart contract exit/contract withdraw deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "joinAgentHash", parameterDes = "nodeHASH")
    @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    @Parameter(parameterName = "contractSender", parameterDes = "Contract caller address")
    @Parameter(parameterName = "contractBalance", parameterDes = "Current balance of contract address")
    @Parameter(parameterName = "contractNonce", parameterDes = "The current contract addressnoncevalue")
    @Parameter(parameterName = "blockTime", requestType = @TypeDescriptor(value = long.class), parameterDes = "The current packaged block time")
    @ResponseData(name = "Return value", description = "Return transactionHASHAnd transactions",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
    public Response withdraw(Map<String,Object> params){
        Result result = service.withdraw(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query node information
     * */
    @CmdAnnotation(cmd = "cs_getContractAgentInfo", version = 1.0, description = "Smart contract nodes/contract get agent info")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "agentHash", parameterDes = "nodeHASH")
    @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    @Parameter(parameterName = "contractSender", parameterDes = "Contract caller address")
    @ResponseData(name = "Return value", description = "Node information",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
    public Response getAgentInfo(Map<String,Object> params){
        Result result = service.getAgentInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query commission information
     * */
    @CmdAnnotation(cmd = "cs_getContractDepositInfo", version = 1.0, description = "Smart contract query for specified account delegation information/Intelligent Contract Query for Assigned Account Delegation Information")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "joinAgentHash", parameterDes = "nodeHASH")
    @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    @Parameter(parameterName = "contractSender", parameterDes = "Contract caller address")
    @ResponseData(name = "Return value", description = "Entrustment information",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
    public Response getDepositInfo(Map<String,Object> params){
        Result result = service.getDepositInfo(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Transaction module triggeredCoinBaseSmart contracts
     * */
    @CmdAnnotation(cmd = "cs_triggerCoinBaseContract", version = 1.0, description = "Transaction module triggeredCoinBaseSmart contracts/trigger coin base contract")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "tx", parameterDes = "Transaction information")
    @Parameter(parameterName = "blockHeader", parameterDes = "Block head")
    @Parameter(parameterName = "stateRoot", parameterDes = "stateRoot")
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "stateRoot")
    }))
    public Response triggerCoinBaseContract(Map<String,Object> params){
        Result result = service.triggerCoinBaseContract(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
