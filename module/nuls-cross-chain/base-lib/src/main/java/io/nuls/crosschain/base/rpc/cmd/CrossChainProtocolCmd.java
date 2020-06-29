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
 * 跨链模块协议处理接口类
 * @author tag
 * @date 2019/4/8
 */
@Component
public class CrossChainProtocolCmd extends BaseCmd {
    @Autowired
    private ProtocolService service;

    /**
     * 跨链节点获取完整跨链交易
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_OTHER_CTX_MESSAGE, version = 1.0, description = "跨链节点向本节点获取完整交易/Cross-chain nodes obtain complete transactions from their own nodes")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
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
     * 查询跨链交易处理状态
     * */
    @CmdAnnotation(cmd = CommandConstant.GET_CTX_STATE_MESSAGE, version = 1.0, description = "获取跨链交易处理状态/Getting the state of cross-chain transaction processing")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
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
    @CmdAnnotation(cmd = CommandConstant.CTX_STATE_MESSAGE, version = 1.0, description = "跨链交易处理状态消息/receive cross transaction state")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
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
    @CmdAnnotation(cmd = CommandConstant.GET_CIRCULLAT_MESSAGE, version = 1.0, description = "查询本链资产信息消息/get chain circulation")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
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
     * 接收其他链发送的跨链交易
     * */
    @CmdAnnotation(cmd = CommandConstant.NEW_OTHER_CTX_MESSAGE, version = 1.0, description = "接收跨链节点广播的完整交易/Receiving Complete Transactions for Cross-Chain Node Broadcasting")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
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
     * 接收链广播跨链交易Hash给链内其他节点
     * */
    @CmdAnnotation(cmd = CommandConstant.BROAD_CTX_HASH_MESSAGE, version = 1.0, description = "接收跨链节点广播的交易Hash/Transaction Hash receiving cross-link node broadcasting")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
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
    @CmdAnnotation(cmd = CommandConstant.BROAD_CTX_SIGN_MESSAGE, version = 1.0, description = "接收链内节点广播的交易签名/Transaction signature for broadcasting in receiving chain")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "nodeId", parameterType = "String", parameterDes = "节点IP")
    @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体")
    @ResponseData(description = "无特定返回值，没有错误即成功")
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
