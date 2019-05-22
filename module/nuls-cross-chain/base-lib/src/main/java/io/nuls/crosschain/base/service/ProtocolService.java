package io.nuls.crosschain.base.service;

import io.nuls.crosschain.base.message.*;

/**
 * 跨链模块协议处理接口类
 * @author tag
 * @date 2019/4/8
 */
public interface ProtocolService {
    /**
     * 链内节点获取完整跨链交易
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void getCtx(int chainId, String nodeId, GetCtxMessage messageBody);

    /**
     * 其他链向本链获取完整跨链交易
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void getOtherCtx(int chainId, String nodeId, GetOtherCtxMessage messageBody);

    /**
     * 主网向发起链验证跨链交易请求
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void verifyCtx(int chainId, String nodeId, VerifyCtxMessage messageBody);

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
    void recvCtxState(int chainId, String nodeId, CtxStateMessage messageBody);

    /**
     * 发起链接收主网发送来获取链资产消息
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void getCirculat(int chainId, String nodeId, GetCirculationMessage messageBody);

    /**
     * 接收链内节点发送的完整跨链交易
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void recvCtx(int chainId, String nodeId, NewCtxMessage messageBody);

    /**
     * 接收其他链发送的完整跨链交易
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void recvOtherCtx(int chainId, String nodeId, NewOtherCtxMessage messageBody);

    /**
     * 接收跨链交易验证结果
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void recvVerifyRs(int chainId,String nodeId,VerifyCtxResultMessage messageBody);

    /**
     * 广播跨链交易Hash给其他链节点
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void recvCtxHash(int chainId,String nodeId,BroadCtxHashMessage messageBody);

    /**
     * 广播跨链交易Hash给链内其他节点
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void recvCtxSign(int chainId,String nodeId,BroadCtxSignMessage messageBody);

    /**
     * 接收链资产统计消息
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void recvCirculat(int chainId,String nodeId,CirculationMessage messageBody);

    /**
     * 接收已注册的跨链交易信息
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param messageBody 消息体
     * */
    void recRegisteredChainInfo(int chainId,String nodeId,RegisteredChainMessage messageBody);
}
