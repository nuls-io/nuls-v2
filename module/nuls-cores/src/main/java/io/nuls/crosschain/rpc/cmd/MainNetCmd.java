package io.nuls.crosschain.rpc.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.constant.CrossChainErrorCode;
import io.nuls.crosschain.base.message.CirculationMessage;
import io.nuls.crosschain.base.service.ResetLocalVerifierService;
import io.nuls.crosschain.constant.ParamConstant;
import io.nuls.crosschain.servive.MainNetService;
import io.nuls.crosschain.srorage.RegisteredCrossChainService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unique methods for cross chain modules in the main network
 * @author tag
 * @date 2019/4/23
 */
@Component
@NulsCoresCmd(module = ModuleE.CC)
public class MainNetCmd extends BaseCmd {
    @Autowired
    private MainNetService service;

    @Autowired
    RegisteredCrossChainService registeredCrossChainService;

    @Autowired
    ResetLocalVerifierService resetLocalVerifierService;

    /**
     * Friendly Chain registers cross chain information with the main network chain management module,Chain management module notifies cross chain modules
     * */
    @CmdAnnotation(cmd = "registerCrossChain", version = 1.0, description = "Chain registration cross chain/register Cross Chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "chainName", parameterType = "String", parameterDes = "Chain Name")
    @Parameter(parameterName = "minAvailableNodeNum", requestType = @TypeDescriptor(value = int.class), parameterDes = "Minimum number of links")
    @Parameter(parameterName = "assetInfoList", parameterType = "List<AssetInfo>", parameterDes = "Asset List")
    @Parameter(parameterName = "registerTime", requestType = @TypeDescriptor(value = long.class), parameterDes = "Chain registration time")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class ,description = "Processing results")
    }))
    public Response registerCrossChain(Map<String,Object> params){
        Result result = service.registerCrossChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Register new assets in the chain
     * */
    @CmdAnnotation(cmd = "registerAsset", version = 1.0, description = "Chain registration of new assets/register Cross Chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "assetID")
    @Parameter(parameterName = "symbol", parameterType = "String", parameterDes = "Asset symbols")
    @Parameter(parameterName = "assetName", parameterType = "String", parameterDes = "Asset Name")
    @Parameter(parameterName = "usable", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "Is it available")
    @Parameter(parameterName = "decimalPlaces", requestType = @TypeDescriptor(value = int.class), parameterDes = "accuracy")
    @Parameter(parameterName = "time", requestType = @TypeDescriptor(value = long.class), parameterDes = "Chain registration time")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class ,description = "Processing results")
    }))
    public Response registerAsset(Map<String,Object> params){
        Result result = service.registerAssert(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Friendly Chain Cancellation of Cross Chain Assets
     * */
    @CmdAnnotation(cmd = "cancelCrossChain", version = 1.0, description = "Designated chain assets exit cross chain/Specified Chain Assets Exit Cross Chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "assetID")
    @Parameter(parameterName = "time", requestType = @TypeDescriptor(value = long.class), parameterDes = "Chain registration time")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class ,description = "Processing results")
    }))
    public Response cancelCrossChain(Map<String,Object> params){
        Result result = service.cancelCrossChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Changes in cross chain assets of Friendly Chain
     */
    @CmdAnnotation(cmd = "crossChainRegisterChange", version = 1.0, description = "Changes in cross chain assets of Friendly Chain/Registered Cross Chain change")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response crossChainRegisterChange(Map<String, Object> params) {
        Result result = service.crossChainRegisterChange(params);
        if (result.isFailed()) {
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Receive asset information sent by other chain nodes
     * */
    @CmdAnnotation(cmd = CommandConstant.CIRCULATION_MESSAGE, version = 1.0, description = "Receive asset information sent by other chain nodes/Receiving asset information sent by other link nodes")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "nodeIP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "Message Body")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response recvCirculat(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        CirculationMessage message = new CirculationMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveCirculation(chainId,nodeId,message);
        return success();
    }

    /**
     * The main network chain management module obtains friend chain asset information from the cross chain module
     * Access to Friendship Chain Asset Information
     * */
    @CmdAnnotation(cmd = "getFriendChainCirculate", version = 1.0, description = "Obtaining Friendly Chain Asset Information/Access to Friendship Chain Asset Information")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "assetIds", parameterType = "String",parameterDes = "assetIDMultiple assetsIDSeparate with commas")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response getFriendChainCirculate(Map<String,Object> params){
        Result result = service.getFriendChainCirculation(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * Smart contract assets cross chain
     * Smart contract assets cross chain
     * */
    @CmdAnnotation(cmd = "cc_tokenOutCrossChain", version = 1.0, description = "Smart contract assets cross chain/Smart contract assets cross chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "assetID")
    @Parameter(parameterName = "from", parameterDes = "Outgoing address")
    @Parameter(parameterName = "to", parameterDes = "Address transfer")
    @Parameter(parameterName = "value", parameterDes = "money")
    @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    @Parameter(parameterName = "contractSender", parameterDes = "Contract caller address")
    @Parameter(parameterName = "contractBalance", parameterDes = "Current balance of contract address")
    @Parameter(parameterName = "contractNonce", parameterDes = "The current contract addressnoncevalue")
    @Parameter(parameterName = "blockTime", requestType = @TypeDescriptor(value = long.class), parameterDes = "The current packaged block time")
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash",valueType = Boolean.class, description = "transactionhash"),
            @Key(name = "tx",valueType = Boolean.class, description = "Transaction String")
    }))
    public Response tokenOutCrossChain(Map<String,Object> params){
        Result result = service.tokenOutCrossChain(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        Map data = (Map) result.getData();
        String txHash = (String) data.get(ParamConstant.TX_HASH);
        String txHex = (String) data.get(ParamConstant.TX);
        Map resultMap = new HashMap();
        resultMap.put(ParamConstant.VALUE, List.of(txHash, txHex));
        return success(resultMap);
    }

    @CmdAnnotation(cmd = "cc_getRegisterChainInfo", version = 1.0, description = "Obtain all registered chain information")
    public Response getRegisterChainInfo(Map<String,Object> params){
        return success(registeredCrossChainService.get());
    }

    @CmdAnnotation(cmd = "createResetLocalVerifierTx", version = 1.0, description = "Create a reset chain validator transaction transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "Call address")
    @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "Call address password")
    @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "Remarks", canNull = true)
    @ResponseData(name = "Return value", description = "transactionHASH", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "transactionHASH")
    }))
    public Response createResetLocalVerifierTx(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String address = (String) params.get("address");
        String password = (String) params.get("password");
        Result<Map> res = resetLocalVerifierService.createResetLocalVerifierTx(chainId,address,password);
        if(res.isSuccess()){
            return success(res);
        }else{
            return failed(res.getErrorCode());
        }
    }

}
