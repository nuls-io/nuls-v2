package io.nuls.crosschain.servive;

import io.nuls.core.basic.Result;
import io.nuls.crosschain.base.message.CirculationMessage;

import java.util.Map;

/**
 * 主网跨链模块特有方法
 * @author tag
 * @date 2019/4/23
 */
public interface MainNetService {
    /**
     * 注册新的跨链交易
     * Register new cross-chain transactions
     *
     * @param params
     * @return        Processing result
     * */
    Result registerCrossChain(Map<String,Object> params);

    /**
     * 注册链新资产
     * Register new cross-chain transactions
     *
     * @param params
     * @return        Processing result
     * */
    Result registerAssert(Map<String,Object> params);

    /**
     * 注销跨链交易
     * Cancellation of cross-chain transactions
     *
     * @param params
     * @return        Processing result
     */
    Result cancelCrossChain(Map<String,Object> params);

    /**
     * 跨链注册消息变更
     * Cross-Chain Registration Message Change
     *
     * @param params
     * @return        Processing result
     */
    Result crossChainRegisterChange(Map<String, Object> params);

    /**
     * 接收链资产统计消息
     * Receive Chain Asset Statistics Message
     *
     * @param chainId     消息所属链Id
     * @param nodeId      发送此消息的节点Id
     * @param message 消息体
     * */
    void receiveCirculation(int chainId, String nodeId, CirculationMessage message);

    /**
     * 主网链管理模块向跨链模块获取友链资产信息
     * Access to Friendship Chain Asset Information
     *
     * @param params
     * @return        Processing result
     * */
    Result getFriendChainCirculation(Map<String,Object> params);

    /**
     * 智能合约资产跨链
     * Smart contract assets cross chain
     *
     * @param params
     * @return        Processing result
     * */
    Result tokenOutCrossChain(Map<String,Object> params);
}
