package io.nuls.crosschain.base.rpc.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.constant.CrossChainErrorCode;
import io.nuls.crosschain.base.message.*;
import io.nuls.crosschain.base.service.ProtocolService;

import java.util.Map;

/**
 * 跨链模块协议处理接口类
 * @author tag
 * @date 2019/4/8
 */
@Component
public class CrossChainProtocolCmd extends BaseCmd {
    @Autowired
    private ProtocolService service;

    /**
     * 链内节点获取完整跨链交易
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_CTX_MESSAGE, version = 1.0, description = "get cross transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
    public Response getCtx(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        GetCtxMessage message = new GetCtxMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.getCtx(chainId,nodeId,message);
        return success();
    }

    /**
     * 跨链节点获取完整跨链交易
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_OTHER_CTX_MESSAGE, version = 1.0, description = "get cross transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
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
     * 主网向发起链验证跨链交易请求
     * */
    @CmdAnnotation(cmd = CommandConstant.VERIFY_CTX_MESSAGE, version = 1.0, description = "verify cross transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
    public Response verifyCtx(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        VerifyCtxMessage message = new VerifyCtxMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.verifyCtx(chainId,nodeId,message);
        return success();
    }

    /**
     * 查询跨链交易处理状态
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_CTX_STATE_MESSAGE, version = 1.0, description = "verify cross transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
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
     * 发起链接收主网发送来的跨链交易处理结果
     * */
    @CmdAnnotation(cmd = CommandConstant.CTX_STATE_MESSAGE, version = 1.0, description = "receive cross transaction state 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
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
     * 发起链接收主网发送来获取链资产消息
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_CIRCULLAT_MESSAGE, version = 1.0, description = "get chain circulation 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
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
     * 接收链内节点发送的跨链交易
     * */
    @CmdAnnotation(cmd = CommandConstant.NEW_CTX_MESSAGE, version = 1.0, description = "receive cross transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
    public Response recvCtx(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        NewCtxMessage message = new NewCtxMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveCtx(chainId,nodeId,message);
        return success();
    }

    /**
     * 接收其他链发送的跨链交易
     * */
    @CmdAnnotation(cmd = CommandConstant.NEW_OTHER_CTX_MESSAGE, version = 1.0, description = "receive cross transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
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
     * 接收链接收到主网发送过来的跨链交易验证结果
     * */
    @CmdAnnotation(cmd = CommandConstant.CTX_VERIFY_RESULT_MESSAGE, version = 1.0, description = "receive cross transaction verify result 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
    public Response recvVerifyRs(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        VerifyCtxResultMessage message = new VerifyCtxResultMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveVerifyRs(chainId,nodeId,message);
        return success();
    }

    /**
     * 接收链广播跨链交易Hash给链内其他节点
     * */
    @CmdAnnotation(cmd = CommandConstant.BROAD_CTX_HASH_MESSAGE, version = 1.0, description = "receive new cross transaction hash 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
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
     * 接收链广播跨链交易Hash给链内其他节点
     * */
    @CmdAnnotation(cmd = CommandConstant.BROAD_CTX_SIGN_MESSAGE, version = 1.0, description = "receive new cross transaction hash 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
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

    /**
     * 接收到主网返回的已注册跨链交易信息
     * Receive the information returned from the main network to register cross-chain transactions
     * */
    @CmdAnnotation(cmd = CommandConstant.REGISTERED_CHAIN_MESSAGE, version = 1.0, description = "receive circulation 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
    public Response recvRegChain(Map<String,Object> params){
        int chainId = Integer.parseInt(params.get("chainId").toString());
        String nodeId = params.get("nodeId").toString();
        byte[] decode = RPCUtil.decode(params.get("messageBody").toString());
        RegisteredChainMessage message = new RegisteredChainMessage();
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            return failed(CrossChainErrorCode.PARAMETER_ERROR);
        }
        service.receiveRegisteredChainInfo(chainId,nodeId,message);
        return success();
    }
}
