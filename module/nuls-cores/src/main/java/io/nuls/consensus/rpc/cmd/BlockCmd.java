package io.nuls.consensus.rpc.cmd;

import io.nuls.core.rpc.model.*;
import io.nuls.consensus.service.BlockService;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * Consensus Block Related Interface
 * @author tag
 * 2018/11/7
 * */
@Component
@NulsCoresCmd(module = ModuleE.CS)
public class BlockCmd extends BaseCmd {
    @Autowired
    private BlockService service;

    /**
     * Cache new block header
     * */
    @CmdAnnotation(cmd = "cs_addBlock", version = 1.0, description = "Receive and cache new blocks/Receiving and caching new blocks")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "blockHeader", parameterType = "String", parameterDes = "Block head")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Whether the interface execution is successful or not")
    }))
    public Response addBlock(Map<String,Object> params){
        Result result = service.addBlock(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Verify block correctness
     * */
    @CmdAnnotation(cmd = "cs_validBlock", version = 1.0, description = "Verify Block/verify block correctness")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    @Parameter(parameterName = "download", requestType = @TypeDescriptor(value = int.class), parameterDes = "Block status")
    @Parameter(parameterName = "block", parameterType = "String", parameterDes = "Block information")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Verification results")
    }))
    public Response validBlock(Map<String,Object> params){
        Result result = service.validBlock(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Receive blocks that require caching
     * */
    @CmdAnnotation(cmd = "cs_receiveHeaderList", version = 1.0, description = "Receive and cache block list/Receive and cache block lists")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class),parameterDes = "chainid")
    @Parameter(parameterName = "headerList", parameterType = "List<String>",parameterDes = "Block header list")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Successfully received and processed")
    }))
    public Response receiveHeaderList(Map<String,Object> params){
        Result result = service.receiveHeaderList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Block rollback
     * */
    @CmdAnnotation(cmd = "cs_chainRollBack", version = 1.0, description = "Block rollback/chain rollback")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class),parameterDes = "chainid")
    @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = int.class),parameterDes = "The height to which the block is rolled back")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Block rollback result")
    }))
    public Response chainRollBack(Map<String,Object> params){
        Result result = service.chainRollBack(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
