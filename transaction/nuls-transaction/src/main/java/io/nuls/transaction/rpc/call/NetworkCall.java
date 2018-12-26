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

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.message.TransactionMessage;
import io.nuls.transaction.message.base.BaseMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用网络模块接口的工具
 *
 * @author qinyifeng
 * @date 2018/12/25
 */
public class NetworkCall {


    /**
     * 给网络上节点广播消息
     *
     * @param chainId
     * @param message
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message) {
        return broadcast(chainId, message, null);
    }

    /**
     * 给网络上节点广播消息
     *
     * @param chainId
     * @param message
     * @param excludeNodes 排除的节点
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message, String excludeNodes) {
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("excludeNodes", excludeNodes);
            params.put("messageBody", HexUtil.byteToHex(message.serialize()));
            params.put("command", message.getCommand());

            return CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_broadcast", params).isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 给指定节点发送消息
     *
     * @param chainId
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean sendToNode(int chainId, BaseMessage message, String nodeId) {
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("nodes", nodeId);
            params.put("messageBody", HexUtil.byteToHex(message.serialize()));
            params.put("command", message.getCommand());

            return CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_sendPeersMsg", params).isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 注册消息处理器
     *
     * @return
     */
    public static boolean register() {
        try {
            Map<String, Object> map = new HashMap<>();
            List<Map<String, String>> cmds = new ArrayList<>();
            map.put("role", ModuleE.TX.abbr);
            //TODO 模块启动时向网络模块注册网络协议处理器
            List<String> list = List.of(TxCmd.NW_NEW_HASH, TxCmd.NW_RECEIVE_TX);
            for (String s : list) {
                Map<String, String> cmd = new HashMap<>();
                cmd.put("protocolCmd", s);
                cmd.put("handler", s);
                cmds.add(cmd);
            }
            map.put("protocolCmds", cmds);
            return CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map).isSuccess();
        } catch (Exception e) {
            Log.error("get nw_protocolRegister fail");
        }
        return false;
    }

    /**
     * 广播交易hash到其他节点
     * Broadcast transaction hash to other peer nodes
     *
     * @param chainId
     * @param hash
     * @return
     */
    public static boolean broadcastTxHash(int chainId, NulsDigestData hash) {
        BroadcastTxMessage message = new BroadcastTxMessage();
        message.setCommand(TxCmd.NW_NEW_HASH);
        message.setRequestHash(hash);
        return NetworkCall.broadcast(chainId, message);
    }

    /**
     * 发送完整交易到指定节点
     * Send the complete transaction to the specified node
     * @param chainId
     * @param nodeId
     * @param tx
     * @return
     */
    public static boolean sendTxToNode(int chainId, String nodeId, Transaction tx) {
        TransactionMessage message = new TransactionMessage();
        message.setCommand(TxCmd.NW_RECEIVE_TX);
        message.setTx(tx);
        return NetworkCall.sendToNode(chainId, message, nodeId);
    }
}
