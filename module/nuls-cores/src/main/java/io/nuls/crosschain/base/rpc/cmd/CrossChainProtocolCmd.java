package io.nuls.crosschain.base.rpc.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.ResponseData;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.constant.CrossChainErrorCode;
import io.nuls.crosschain.base.message.*;
import io.nuls.crosschain.base.service.ProtocolService;

import java.util.Map;

/**
 * Cross chain module protocol processing interface class
 * @author tag
 * @date 2019/4/8
 */
@Component
public class CrossChainProtocolCmd extends BaseCmd {
    @Autowired
    private ProtocolService service;

    /**
     * Cross chain node acquisition of complete cross chain transactions
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_OTHER_CTX_MESSAGE, version = 1.0, description = "Cross chain nodes obtain complete transactions from this node/Cross-chain nodes obtain complete transactions from their own nodes")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "nodeIP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "Message Body")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response getOtherCtx(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        GetOtherCtxMessage message = new GetOtherCtxMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.getOtherCtx(chainId,nodeId,message);
        return success();
    }

    /**
     * Query the status of cross chain transaction processing
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_CTX_STATE_MESSAGE, version = 1.0, description = "Obtaining Cross Chain Transaction Processing Status/Getting the state of cross-chain transaction processing")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "nodeIP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "Message Body")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response getCtxState(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        GetCtxStateMessage message = new GetCtxStateMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.getCtxState(chainId,nodeId,message);
        return success();
    }

    /**
     * Initiate link to receive cross chain transaction processing results sent by the main network
     * */
    @CmdAnnotation(cmd = CommandConstant.CTX_STATE_MESSAGE, version = 1.0, description = "Cross chain transaction processing status messages/receive cross transaction state")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "nodeIP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "Message Body")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response recvCtxState(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        CtxStateMessage message = new CtxStateMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveCtxState(chainId,nodeId,message);
        return success();
    }

    /**
     * Initiate a link to the main network to receive chain asset messages
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_CIRCULLAT_MESSAGE, version = 1.0, description = "Query the asset information message of this chain/get chain circulation")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "nodeIP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "Message Body")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response getCirculat(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        GetCirculationMessage message = new GetCirculationMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.getCirculation(chainId,nodeId,message);
        return success();
    }


    /**
     * Receive cross chain transactions sent by other chains
     * */
    @CmdAnnotation(cmd = CommandConstant.NEW_OTHER_CTX_MESSAGE, version = 1.0, description = "Receive complete transactions broadcasted across chain nodes/Receiving Complete Transactions for Cross-Chain Node Broadcasting")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "nodeIP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "Message Body")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response recvOtherCtx(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        NewOtherCtxMessage message = new NewOtherCtxMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveOtherCtx(chainId,nodeId,message);
        return success();
    }


    /**
     * Receive Chain Broadcast Cross Chain TransactionsHashTo other nodes in the chain
     * */
    @CmdAnnotation(cmd = CommandConstant.BROAD_CTX_HASH_MESSAGE, version = 1.0, description = "Receive transactions broadcasted across chain nodesHash/Transaction Hash receiving cross-link node broadcasting")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "nodeIP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "Message Body")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response recvCtxHash(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        BroadCtxHashMessage message = new BroadCtxHashMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveCtxHash(chainId,nodeId,message);
        return success();
    }

    /**
     * Receive Chain Broadcast Cross Chain TransactionsHashTo other nodes in the chain
     * */
    @CmdAnnotation(cmd = CommandConstant.BROAD_CTX_SIGN_MESSAGE, version = 1.0, description = "Receive transaction signatures broadcasted by nodes within the chain/Transaction signature for broadcasting in receiving chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "nodeIP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "Message Body")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response recvCtxSign(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        BroadCtxSignMessage message = new BroadCtxSignMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveCtxSign(chainId,nodeId,message);
        return success();
    }
}
