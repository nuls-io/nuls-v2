package io.nuls.poc.rpc.cmd;

import io.nuls.poc.service.BlockService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "download", parameterType = "int")
    @Parameter(parameterName = "block", parameterType = "String")
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "headerList", parameterType = "List<String>")
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "height", parameterType = "int")
    public Response chainRollBack(Map<String,Object> params){
        Result result = service.chainRollBack(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
