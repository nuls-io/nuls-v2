/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.message.ForwardTxMessage;
import io.nuls.transaction.message.base.BaseMessage;
import io.nuls.transaction.model.bo.Chain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.constant.TxCmd.*;
import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 网络消息发送
 * @author: Charlie
 * @date: 2019/04/16
 */
public class NetworkCall {

    /**
     * 给网络上节点广播消息
     *
     * @param chain
     * @param message
     * @return
     */
    public static boolean broadcast(Chain chain, BaseMessage message) throws NulsException {
        return broadcast(chain, message, null);
    }

    /**
     * 给网络上节点广播消息
     *  1.转发交易hash
     *  2.广播完整交易
     *
     * @param chain
     * @param message
     * @param excludeNodes 排除的节点
     * @return
     */
    public static boolean broadcast(Chain chain, BaseMessage message, String excludeNodes) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("excludeNodes", excludeNodes);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", message.getCommand());
            Request request = MessageUtil.newRequest("nw_broadcast", params, Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
            ResponseMessageProcessor.requestOnly(ModuleE.NW.abbr, request);
            return true;
        } catch (IOException e) {
            LOG.error(e);
            throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
        } catch (Exception e) {
            LOG.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 给指定节点发送消息
     *
     * @param chain
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean sendToNode(Chain chain, BaseMessage message, String nodeId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("nodes", nodeId);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", message.getCommand());
            TransactionCall.requestAndResponse(ModuleE.NW.abbr, "nw_sendPeersMsg", params);
            return true;
        } catch (IOException e) {
            LOG.error(e);
            throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
        } catch (RuntimeException e){
            LOG.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 向网络模块注册网络消息协议
     * register Network Message Protocol with Network Module
     *
     * @return
     */
    public static boolean registerProtocol() throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            List<Map<String, String>> cmds = new ArrayList<>();
            params.put("role", ModuleE.TX.abbr);
            //模块启动时向网络模块注册网络协议处理器
            List<String> list = List.of(NW_NEW_HASH, NW_ASK_TX, NW_RECEIVE_TX);
            for (String s : list) {
                Map<String, String> cmd = new HashMap<>(TxConstant.INIT_CAPACITY_4);
                cmd.put("protocolCmd", s);
                cmd.put("handler", s);
                cmds.add(cmd);
            }
            params.put("protocolCmds", cmds);

            TransactionCall.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", params);
            return true;
        } catch (RuntimeException e){
            LOG.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 转发交易
     * 发送hash到其他节点
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
     * 转发交易
     * 发送hash到其他节点
     * Forward transaction hash to other peer nodes
     *
     * @param chain
     * @param hash
     * @return
     */
    public static boolean forwardTxHash(Chain chain, NulsHash hash, String excludeNodes) throws NulsException {
        ForwardTxMessage message = new ForwardTxMessage();
        message.setCommand(NW_NEW_HASH);
        message.setHash(hash);
        return NetworkCall.broadcast(chain, message, excludeNodes);
    }



    /**
     * 广播完整交易到网络中
     * Send the complete transaction to the specified node
     *
     * @param chain
     * @param tx
     * @return
     */
    public static boolean broadcastTx(Chain chain, Transaction tx) throws NulsException {
        BroadcastTxMessage message = new BroadcastTxMessage();
        message.setCommand(NW_RECEIVE_TX);
        message.setTx(tx);
        return NetworkCall.broadcast(chain, message);
    }


    /**
     * 发送完整交易到指定节点
     * Send the complete transaction to the specified node
     *
     * @param chain
     * @param nodeId
     * @param tx
     * @return
     */
    public static boolean sendTxToNode(Chain chain, String nodeId, Transaction tx) throws NulsException {
        BroadcastTxMessage message = new BroadcastTxMessage();
        message.setCommand(NW_RECEIVE_TX);
        message.setTx(tx);
        return NetworkCall.sendToNode(chain, message, nodeId);
    }



}
