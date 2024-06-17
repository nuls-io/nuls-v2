package io.nuls.crosschain.base.service;

import io.nuls.crosschain.base.message.*;

/**
 * Cross chain module protocol processing interface class
 * @author tag
 * @date 2019/4/8
 */
public interface ProtocolService {
    /**
     * Other chains obtain complete cross chain transactions from this chain
     * @param chainId     Chain to which the message belongsId
     * @param nodeId      The node that sent this messageId
     * @param messageBody Message Body
     * */
    void getOtherCtx(int chainId, String nodeId, GetOtherCtxMessage messageBody);


    /**
     * Query cross chain transaction processing status from other chains
     * @param chainId     Chain to which the message belongsId
     * @param nodeId      The node that sent this messageId
     * @param messageBody Message Body
     * */
    void getCtxState(int chainId, String nodeId, GetCtxStateMessage messageBody);

    /**
     * Initiate link to receive cross chain transaction processing results sent by the main network
     * @param chainId     Chain to which the message belongsId
     * @param nodeId      The node that sent this messageId
     * @param messageBody Message Body
     * */
    void receiveCtxState(int chainId, String nodeId, CtxStateMessage messageBody);

    /**
     * Initiate a link to the main network to receive chain asset messages
     * @param chainId     Chain to which the message belongsId
     * @param nodeId      The node that sent this messageId
     * @param messageBody Message Body
     * */
    void getCirculation(int chainId, String nodeId, GetCirculationMessage messageBody);


    /**
     * Receive complete cross chain transactions sent by other chains
     * @param chainId     Chain to which the message belongsId
     * @param nodeId      The node that sent this messageId
     * @param messageBody Message Body
     * */
    void receiveOtherCtx(int chainId, String nodeId, NewOtherCtxMessage messageBody);

    /**
     * Broadcast Cross Chain TransactionsHashTo other chain nodes
     * @param chainId     Chain to which the message belongsId
     * @param nodeId      The node that sent this messageId
     * @param messageBody Message Body
     * */
    void receiveCtxHash(int chainId,String nodeId,BroadCtxHashMessage messageBody);

    /**
     * Broadcast Cross Chain TransactionsHashTo other nodes in the chain
     * @param chainId     Chain to which the message belongsId
     * @param nodeId      The node that sent this messageId
     * @param messageBody Message Body
     * */
    void receiveCtxSign(int chainId,String nodeId,BroadCtxSignMessage messageBody);



}
