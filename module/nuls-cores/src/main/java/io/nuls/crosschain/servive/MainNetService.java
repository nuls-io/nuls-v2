package io.nuls.crosschain.servive;

import io.nuls.core.basic.Result;
import io.nuls.crosschain.base.message.CirculationMessage;

import java.util.Map;

/**
 * Unique methods for cross chain modules in the main network
 * @author tag
 * @date 2019/4/23
 */
public interface MainNetService {
    /**
     * Register a new cross chain transaction
     * Register new cross-chain transactions
     *
     * @param params
     * @return        Processing result
     * */
    Result registerCrossChain(Map<String,Object> params);

    /**
     * Register new assets in the chain
     * Register new cross-chain transactions
     *
     * @param params
     * @return        Processing result
     * */
    Result registerAssert(Map<String,Object> params);

    /**
     * Cancelling Cross Chain Transactions
     * Cancellation of cross-chain transactions
     *
     * @param params
     * @return        Processing result
     */
    Result cancelCrossChain(Map<String,Object> params);

    /**
     * Cross chain registration message change
     * Cross-Chain Registration Message Change
     *
     * @param params
     * @return        Processing result
     */
    Result crossChainRegisterChange(Map<String, Object> params);

    /**
     * Receive chain asset statistics messages
     * Receive Chain Asset Statistics Message
     *
     * @param chainId     Chain to which the message belongsId
     * @param nodeId      The node that sent this messageId
     * @param message Message Body
     * */
    void receiveCirculation(int chainId, String nodeId, CirculationMessage message);

    /**
     * The main network chain management module obtains friend chain asset information from the cross chain module
     * Access to Friendship Chain Asset Information
     *
     * @param params
     * @return        Processing result
     * */
    Result getFriendChainCirculation(Map<String,Object> params);

    /**
     * Smart contract assets cross chain
     * Smart contract assets cross chain
     *
     * @param params
     * @return        Processing result
     * */
    Result tokenOutCrossChain(Map<String,Object> params);
}
