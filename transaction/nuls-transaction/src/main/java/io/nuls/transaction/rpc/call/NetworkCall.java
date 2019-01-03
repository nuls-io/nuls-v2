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
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;

import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.message.TransactionMessage;
import io.nuls.transaction.message.base.BaseMessage;
import io.nuls.transaction.model.bo.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.constant.TxCmd.*;

/**
 * 调用网络模块接口的工具
 *
 * @author qinyifeng
 * @date 2018/12/25
 */
public class NetworkCall {

    /**
     * 根据链ID获取可用节点
     *
     * @param chainId
     * @param isCross 是否跨链
     * @return
     */
    public static List<Node> getAvailableNodes(int chainId, int isCross, String excludeNodes) {
        try {
            Map<String, Object> params = new HashMap<>(6);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("state", 1);
            params.put("isCross", isCross);
            params.put("startPage", 0);
            params.put("pageSize", 0);

            Response response = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_getNodes", params);
            Map responseData = (Map) response.getResponseData();
            List list = (List) responseData.get("nw_getNodes");
            List nodes = new ArrayList();
            for (Object o : list) {
                Map map = (Map) o;
                Node node = new Node();
                node.setId((String) map.get("nodeId"));
                node.setHeight(Long.parseLong(map.get("blockHeight").toString()));
                node.setHash(NulsDigestData.fromDigestHex((String) map.get("blockHash")));
                //排除指定节点
                if (StringUtils.isBlank(excludeNodes) || !node.getId().equals(excludeNodes)) {
                    nodes.add(node);
                }
            }
            return nodes;
        } catch (Exception e) {
            Log.error(e);
            return List.of();
        }
    }

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
     * 向网络模块注册网络消息协议
     * register Network Message Protocol with Network Module
     *
     * @return
     */
    public static boolean registerProtocol() {
        try {
            Map<String, Object> map = new HashMap<>();
            List<Map<String, String>> cmds = new ArrayList<>();
            map.put("role", ModuleE.TX.abbr);
            //模块启动时向网络模块注册网络协议处理器
            List<String> list = List.of(NW_NEW_HASH, NW_ASK_TX, NW_RECEIVE_TX, NW_NEW_CROSS_HASH, NW_ASK_CROSS_TX, NW_NEW_MN_TX, NW_VERIFY_FC, NW_VERIFY_MN, NW_VERIFYR_ESULT, NW_CROSS_NODE_RS);
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
        message.setCommand(NW_NEW_HASH);
        message.setRequestHash(hash);
        return NetworkCall.broadcast(chainId, message);
    }

    /**
     * 发送完整交易到指定节点
     * Send the complete transaction to the specified node
     *
     * @param chainId
     * @param nodeId
     * @param tx
     * @return
     */
    public static boolean sendTxToNode(int chainId, String nodeId, Transaction tx) {
        TransactionMessage message = new TransactionMessage();
        message.setCommand(NW_RECEIVE_TX);
        message.setTx(tx);
        return NetworkCall.sendToNode(chainId, message, nodeId);
    }
}
