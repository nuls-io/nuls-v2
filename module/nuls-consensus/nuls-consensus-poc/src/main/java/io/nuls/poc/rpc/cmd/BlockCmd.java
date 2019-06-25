package io.nuls.poc.rpc.cmd;

import io.nuls.core.rpc.model.*;
import io.nuls.poc.service.BlockService;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * 共识区块相关接口
 * @author tag
 * 2018/11/7
 * */
@Component
public class BlockCmd extends BaseCmd {
    @Autowired
    private BlockService service;

    /**
     * 缓存新区块头
     * */
    @CmdAnnotation(cmd = "cs_addBlock", version = 1.0, description = "add block 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "区块头")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "接口执行成功与否")
    }))
    public Response addBlock(Map<String,Object> params){
        Result result = service.addBlock(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 验证区块正确性
     * */
    @CmdAnnotation(cmd = "cs_validBlock", version = 1.0, description = "verify block correctness 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    @Parameter(parameterName = "download", parameterType = "int", parameterDes = "区块状态")
    @Parameter(parameterName = "block", parameterType = "String", parameterDes = "区块信息")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "验证结果")
    }))
    public Response validBlock(Map<String,Object> params){
        Result result = service.validBlock(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 接收需缓存的区块
     * */
    @CmdAnnotation(cmd = "cs_receiveHeaderList", version = 1.0, description = "verify block correctness 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int",parameterDes = "链id")
    @Parameter(parameterName = "headerList", parameterType = "List<String>",parameterDes = "区块头列表")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "是否成功接收处理")
    }))
    public Response receiveHeaderList(Map<String,Object> params){
        Result result = service.receiveHeaderList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 区块回滚
     * */
    @CmdAnnotation(cmd = "cs_chainRollBack", version = 1.0, description = "chain roll back 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int",parameterDes = "链id")
    @Parameter(parameterName = "height", parameterType = "int",parameterDes = "区块回滚到的高度")
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "区块回滚结果")
    }))
    public Response chainRollBack(Map<String,Object> params){
        Result result = service.chainRollBack(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
