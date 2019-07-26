package io.nuls.poc.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.poc.service.MultiSignService;

import java.util.Map;

/**
 * 多签账户相关接口
 * Multi-Sign Account Related Interface
 *
 * @author tag
 * 2019/7/25
 * */
@Component
public class MultiSignCmd extends BaseCmd {
    @Autowired
    private MultiSignService service;

    /**
     * 多签账户创建节点
     * */
    @CmdAnnotation(cmd = "cs_createMultiAgent", version = 1.0, description = "多签账户创建节点/Multi-Sign Account create agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "agentAddress", parameterType = "String", parameterDes = "节点地址(多签地址)")
    @Parameter(parameterName = "packingAddress", parameterType = "String", parameterDes = "节点出块地址")
    @Parameter(parameterName = "rewardAddress", parameterType = "String", parameterDes = "奖励地址,默认节点地址", canNull = true)
    @Parameter(parameterName = "commissionRate", requestType = @TypeDescriptor(value = int.class), parameterDes = "佣金比例")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "抵押金额")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "签名账户密码")
    @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "签名账户地址")
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txHash",  description = "交易hash,交易已完成(已广播)"),
            @Key(name = "completed", valueType = boolean.class, description = "true:交易已完成(已广播),false:交易没完成,没有达到最小签名数")
    }))
    public Response createMultiAgent(Map<String,Object> params){
        Result result = service.createMultiAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 多签账户注销节点
     * */
    @CmdAnnotation(cmd = "cs_stopMultiAgent", version = 1.0, description = "多签账户注销节点/Multi-Sign Account stop agent")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "节点地址(多签地址)")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "签名账户密码")
    @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "签名账户地址")
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txHash",  description = "交易hash,交易已完成(已广播)"),
            @Key(name = "completed", valueType = boolean.class, description = "true:交易已完成(已广播),false:交易没完成,没有达到最小签名数")
    }))
    public Response stopMultiAgent(Map<String,Object> params){
        Result result = service.stopMultiAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 多签账户委托共识
     * */
    @CmdAnnotation(cmd = "cs_multiDeposit", version = 1.0, description = "多签账户委托共识/Multi-Sign Account deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "多签账户地址")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "节点HASH")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "委托金额")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "签名账户密码")
    @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "签名账户地址")
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txHash",  description = "交易hash,交易已完成(已广播)"),
            @Key(name = "completed", valueType = boolean.class, description = "true:交易已完成(已广播),false:交易没完成,没有达到最小签名数")
    }))
    public Response multiDeposit(Map<String,Object> params){
        Result result = service.multiDeposit(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 多签账户退出共识
     * */
    @CmdAnnotation(cmd = "cs_multiWithdraw", version = 1.0, description = "多签账户退出共识/Multi-Sign Account withdraw deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "多签账户地址")
    @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "加入共识交易HASH")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "签名账户密码")
    @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "签名账户地址")
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txHash",  description = "交易hash,交易已完成(已广播)"),
            @Key(name = "completed", valueType = boolean.class, description = "true:交易已完成(已广播),false:交易没完成,没有达到最小签名数")
    }))
    public Response multiWithdraw(Map<String,Object> params){
        Result result = service.multiWithdraw(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
