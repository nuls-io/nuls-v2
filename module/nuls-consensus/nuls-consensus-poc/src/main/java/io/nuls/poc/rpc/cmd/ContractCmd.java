package io.nuls.poc.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.poc.service.ContractService;

import java.util.List;
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
    @CmdAnnotation(cmd = "cs_createContractAgent", version = 1.0, description = "contract create agent 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "packingAddress", parameterType = "String",parameterDes = "出块地址")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "抵押金额")
    @Parameter(parameterName = "commissionRate", parameterType = "String", parameterDes = "佣金比例")
    @Parameter(parameterName = "contractAddress", parameterType = "int", parameterDes = "合约地址")
    @Parameter(parameterName = "contractSender", parameterType = "int", parameterDes = "合约调用者地址")
    @Parameter(parameterName = "contractBalance", parameterType = "int", parameterDes = "合约地址的当前余额")
    @Parameter(parameterName = "contractNonce", parameterType = "int", parameterDes = "合约地址的当前nonce值")
    @Parameter(parameterName = "blockTime", parameterType = "int", parameterDes = "当前打包的区块时间")
    @ResponseData(name = "返回值", description = "返回交易HASH和交易",
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
     * 注销节点
     * */
    @CmdAnnotation(cmd = "cs_stopContractAgent", version = 1.0, description = "contract stop agent 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "contractAddress", parameterType = "int", parameterDes = "合约地址")
    @Parameter(parameterName = "contractSender", parameterType = "int", parameterDes = "合约调用者地址")
    @Parameter(parameterName = "contractBalance", parameterType = "int", parameterDes = "合约地址的当前余额")
    @Parameter(parameterName = "contractNonce", parameterType = "int", parameterDes = "合约地址的当前nonce值")
    @Parameter(parameterName = "blockTime", parameterType = "int", parameterDes = "当前打包的区块时间")
    @ResponseData(name = "返回值", description = "返回交易HASH和交易",
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
     * 委托共识
     * */
    @CmdAnnotation(cmd = "cs_contractDeposit", version = 1.0, description = "contract deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "委托的节点HASH")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "委托金额")
    @Parameter(parameterName = "contractAddress", parameterType = "int", parameterDes = "合约地址")
    @Parameter(parameterName = "contractSender", parameterType = "int", parameterDes = "合约调用者地址")
    @Parameter(parameterName = "contractBalance", parameterType = "int", parameterDes = "合约地址的当前余额")
    @Parameter(parameterName = "contractNonce", parameterType = "int", parameterDes = "合约地址的当前nonce值")
    @Parameter(parameterName = "blockTime", parameterType = "int", parameterDes = "当前打包的区块时间")
    @ResponseData(name = "返回值", description = "返回交易HASH和交易",
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
     * 退出共识
     * */
    @CmdAnnotation(cmd = "cs_contractWithdraw", version = 1.0, description = "contract withdraw deposit agent transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "joinAgentHash", parameterType = "String", parameterDes = "节点HASH")
    @Parameter(parameterName = "contractAddress", parameterType = "int", parameterDes = "合约地址")
    @Parameter(parameterName = "contractSender", parameterType = "int", parameterDes = "合约调用者地址")
    @Parameter(parameterName = "contractBalance", parameterType = "int", parameterDes = "合约地址的当前余额")
    @Parameter(parameterName = "contractNonce", parameterType = "int", parameterDes = "合约地址的当前nonce值")
    @Parameter(parameterName = "blockTime", parameterType = "int", parameterDes = "当前打包的区块时间")
    @ResponseData(name = "返回值", description = "返回交易HASH和交易",
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
     * 查询节点信息
     * */
    @CmdAnnotation(cmd = "cs_getContractAgentInfo", version = 1.0, description = "contract get agent info")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "节点HASH")
    @Parameter(parameterName = "contractAddress", parameterType = "int", parameterDes = "合约地址")
    @Parameter(parameterName = "contractSender", parameterType = "int", parameterDes = "合约调用者地址")
    @ResponseData(name = "返回值", description = "节点信息",
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
     * 查询委托信息
     * */
    @CmdAnnotation(cmd = "cs_getContractDepositInfo", version = 1.0, description = "contract get deposit info")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "joinAgentHash", parameterType = "String", parameterDes = "节点HASH")
    @Parameter(parameterName = "contractAddress", parameterType = "int", parameterDes = "合约地址")
    @Parameter(parameterName = "contractSender", parameterType = "int", parameterDes = "合约调用者地址")
    @ResponseData(name = "返回值", description = "委托信息",
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
     * 交易模块触发CoinBase智能合约
     * */
    @CmdAnnotation(cmd = "cs_triggerCoinBaseContract", version = 1.0, description = "trigger coin base contract")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "交易信息")
    @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "区块头")
    @Parameter(parameterName = "stateRoot", parameterType = "String", parameterDes = "stateRoot")
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
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
