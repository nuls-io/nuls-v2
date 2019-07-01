package io.nuls.crosschain.base.rpc.cmd;

import io.nuls.base.data.CoinData;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.dto.input.CoinDTO;
import io.nuls.crosschain.base.service.CrossChainService;

import java.util.List;
import java.util.Map;

/**
 * 跨链模块服务处理接口类
 * @author tag
 * @date 2019/4/8
 */
@Component
public class CrossChainCmd  extends BaseCmd {
    @Autowired
    private CrossChainService service;

    /**
     * 创建跨链交易
     * */
    @CmdAnnotation(cmd = "createCrossTx", version = 1.0, description = "创建跨链转账交易/Creating Cross-Chain Transfer Transactions")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "listFrom", requestType = @TypeDescriptor(value = CoinDTO.class), parameterDes = "转出信息列表")
    @Parameter(parameterName = "listTo", requestType = @TypeDescriptor(value = CoinDTO.class), parameterDes = "转如信息列表")
    @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "备注", canNull = true)
    @ResponseData(name = "返回值", description = "跨链交易HASH", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "跨链交易HASH")
    }))
    public Response createCrossTx(Map<String,Object> params){
        Result result = service.createCrossTx(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }


    /**
     * 查询跨链交易处理状态
     * */
    @CmdAnnotation(cmd = "getCrossTxState", version = 1.0, description = "查询跨链交易处理状态/get cross transaction process state")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "交易HASH")
    @ResponseData(name = "返回值", description = "跨链交易是否处理完成", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "跨链交易是否处理完成")
    }))
    public Response getCrossTxState(Map<String,Object> params){
        Result result = service.getCrossTxState(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询已注册跨链的链信息列表
     * */
    @CmdAnnotation(cmd = "getRegisteredChainInfoList", version = 1.0, description = "查询在主网上注册跨链的链信息/Query for cross-chain chain information registered on the main network")
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = ChainInfo.class, description = "已注册跨链的链信息")
    }))
    public Response getRegisteredChainInfoList(Map<String,Object> params){
        Result result = service.getRegisteredChainInfoList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
