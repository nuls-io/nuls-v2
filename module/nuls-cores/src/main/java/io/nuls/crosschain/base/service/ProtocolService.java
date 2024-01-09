package io.nuls.crosschain.base.service;

import io.nuls.crosschain.base.message.*;

/**
 * 跨链模块协议处理接口类
 * @author tag
 * @date 2019/4/8
 */
public interface ProtocolService {
    /**
     * 其他链向本链获取完整跨链交易
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void getOtherCtx(int chainId, String nodeId, GetOtherCtxMessage messageBody);


    /**
     * 向其他链查询跨链交易处理状态
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void getCtxState(int chainId, String nodeId, GetCtxStateMessage messageBody);

    /**
     * 发起链接收主网发送来的跨链交易处理结果
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void receiveCtxState(int chainId, String nodeId, CtxStateMessage messageBody);

    /**
     * 发起链接收主网发送来获取链资产消息
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void getCirculation(int chainId, String nodeId, GetCirculationMessage messageBody);


    /**
     * 接收其他链发送的完整跨链交易
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void receiveOtherCtx(int chainId, String nodeId, NewOtherCtxMessage messageBody);

    /**
     * 广播跨链交易Hash给其他链节点
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void receiveCtxHash(int chainId,String nodeId,BroadCtxHashMessage messageBody);

    /**
     * 广播跨链交易Hash给链内其他节点
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void receiveCtxSign(int chainId,String nodeId,BroadCtxSignMessage messageBody);
}
