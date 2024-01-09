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
 * Cross chain module service processing interface class
 * @author tag
 * @date 2019/4/8
 */
@Component
public class CrossChainCmd  extends BaseCmd {
    @Autowired
    private CrossChainService service;

    /**
     * Create cross chain transactions
     * */
    @CmdAnnotation(cmd = "createCrossTx", version = 1.0, description = "Create cross chain transfer transactions/Creating Cross-Chain Transfer Transactions")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "listFrom", requestType = @TypeDescriptor(value = List.class ,collectionElement = CoinDTO.class), parameterDes = "Transfer out information list")
    @Parameter(parameterName = "listTo", requestType = @TypeDescriptor(value = List.class ,collectionElement = CoinDTO.class), parameterDes = "Convert to information list")
    @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "Remarks", canNull = true)
    @ResponseData(name = "Return value", description = "Cross chain transactionsHASH", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Cross chain transactionsHASH")
    }))
    public Response createCrossTx(Map<String,Object> params){
        Result result = service.createCrossTx(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * receiveAPI_MODULECross chain transactions for assembly
     * Receiving cross-chain transactions assembled by API_MODULE
     * */
    @CmdAnnotation(cmd = "newApiModuleCrossTx", version = 1.0, description = "receiveAPI_MODULECross chain transactions for assembly/Receiving cross-chain transactions assembled by API_MODULE")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "transaction")
    @ResponseData(name = "Return value", description = "transactionHash", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "transactionHash")
    }))
    public Response newApiModuleCrossTx(Map<String,Object> params){
        Result result = service.newApiModuleCrossTx(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }


    /**
     * Query the status of cross chain transaction processing
     * */
    @CmdAnnotation(cmd = "getCrossTxState", version = 1.0, description = "Query the status of cross chain transaction processing/get cross transaction process state")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "transactionHASH")
    @ResponseData(name = "Return value", description = "Has the cross chain transaction been processed successfully", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value",valueType = Boolean.class, description = "Has the cross chain transaction been processed successfully")
    }))
    public Response getCrossTxState(Map<String,Object> params){
        Result result = service.getCrossTxState(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query the list of registered cross chain chain information
     * */
    @CmdAnnotation(cmd = "getRegisteredChainInfoList", version = 1.0, description = "Search for cross chain information registered on the main website/Query for cross-chain chain information registered on the main network")
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = ChainInfo.class, description = "Registered cross chain chain information")
    }))
    public Response getRegisteredChainInfoList(Map<String,Object> params){
        Result result = service.getRegisteredChainInfoList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Query the minimum number of Byzantine passes for the current signature（Current number of validators*Byzantine proportion of this chain）
     * */
    @CmdAnnotation(cmd = "getByzantineCount", version = 1.0, description = "Query the minimum number of Byzantine passes for the current signature/Query the minimum number of Byzantine passes for the current signature")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = int.class, description = "The current minimum number of signatures in Byzantium")
    }))
    public Response getByzantineCount(Map<String,Object> params){
        Result result = service.getByzantineCount(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
