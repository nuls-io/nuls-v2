/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.transaction.rpc.call;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BaseBusinessMessage;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.message.ForwardTxMessage;
import io.nuls.transaction.model.bo.Chain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.constant.TxCmd.*;
import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * Network message sending
 *
 * @author: Charlie
 * @date: 2019/04/16
 */
public class NetworkCall {

    /**
     * Broadcast messages to nodes on the network
     * 1.Forwarding transactionshash
     * 2.Broadcast complete transactions
     *
     * @param chain
     * @param message
     * @param excludeNodes Excluded nodes
     * @return
     */
    public static boolean broadcast(Chain chain, BaseBusinessMessage message, String excludeNodes, String cmd, int percent) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("excludeNodes", excludeNodes);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", cmd);
            params.put("percent", percent);
            Request request = MessageUtil.newRequest("nw_broadcast", params, Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
            String messageId = ResponseMessageProcessor.requestOnly(ModuleE.NW.abbr, request);
            return messageId.equals("0") ? false : true;
        } catch (IOException e) {
            LOG.error("message:" + cmd + " failed", e);
            throw new NulsException(TxErrorCode.TX_BROADCAST_FAIL);
        } catch (Exception e) {
            LOG.error("message:" + cmd + " failed", e);
            throw new NulsException(TxErrorCode.TX_BROADCAST_FAIL);
        }
    }

    /**
     * Send messages to specified nodes
     *
     * @param chain
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean sendToNode(Chain chain, BaseBusinessMessage message, String nodeId, String cmd) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("nodes", nodeId);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", cmd);
            Request request = MessageUtil.newRequest("nw_sendPeersMsg", params, Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
            String messageId = ResponseMessageProcessor.requestOnly(ModuleE.NW.abbr, request);
            return messageId.equals("0") ? false : true;
        } catch (IOException e) {
            LOG.error("message:" + cmd + " failed", e);
            throw new NulsException(TxErrorCode.SEND_MSG_FAIL);
        } catch (Exception e) {
            LOG.error("message:" + cmd + " failed", e);
            throw new NulsException(TxErrorCode.SEND_MSG_FAIL);
        }
    }

    /**
     * Forwarding transactions
     * sendhashTo other nodes
     * Forward transaction hash to other peer nodes
     *
     * @param chain
     * @param hash
     * @return
     */
    public static boolean forwardTxHash(Chain chain, NulsHash hash) throws NulsException {
        return forwardTxHash(chain, hash, null);
    }


    /**
     * Forwarding transactions
     * sendhashTo other nodes
     * Forward transaction hash to other peer nodes
     *
     * @param chain
     * @param hash
     * @return
     */
    public static boolean forwardTxHash(Chain chain, NulsHash hash, String excludeNodes) throws NulsException {
        ForwardTxMessage message = new ForwardTxMessage();
        message.setTxHash(hash);
        return NetworkCall.broadcast(chain, message, excludeNodes, NW_NEW_HASH, 50);
    }

    /**
     * Broadcast complete new transactions to the network
     * Only the node that created the transaction will directly broadcast the complete transaction to the network, as other nodes will definitely not have the transaction
     * Send the complete transaction to the specified node
     *
     * @param chain
     * @param tx
     * @return
     */
    public static boolean broadcastTx(Chain chain, Transaction tx, String excludeNodes) throws NulsException {
        BroadcastTxMessage message = new BroadcastTxMessage();
        message.setTx(tx);
        return NetworkCall.broadcast(chain, message, excludeNodes, NW_RECEIVE_TX, 100);
    }

    /**
     * Broadcast complete new transactions to the network
     * Only the node that created the transaction will directly broadcast the complete transaction to the network, as other nodes will definitely not have the transaction
     * Send the complete transaction to the specified node
     *
     * @param chain
     * @param tx
     * @return
     */
    public static boolean broadcastTx(Chain chain, Transaction tx) throws NulsException {
        return broadcastTx(chain, tx, null);
    }


    /**
     * Send complete transaction to specified node
     * Send the complete transaction to the specified node
     *
     * @param chain
     * @param nodeId
     * @param tx
     * @return
     */
    public static boolean sendTxToNode(Chain chain, String nodeId, Transaction tx) throws NulsException {
        BroadcastTxMessage message = new BroadcastTxMessage();
        message.setTx(tx);
        return NetworkCall.sendToNode(chain, message, nodeId, NW_RECEIVE_TX);
    }


}
