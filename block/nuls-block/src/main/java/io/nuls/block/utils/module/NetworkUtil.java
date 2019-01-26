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
package io.nuls.block.utils.module;

import io.nuls.base.data.NulsDigestData;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.message.base.BaseMessage;
import io.nuls.block.model.Node;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.*;
import static io.nuls.block.utils.LoggerUtil.commonLog;


/**
 * 调用网络模块接口的工具
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午3:48
 */
public class NetworkUtil {

    /**
     * 根据链ID获取可用节点
     *
     * @param chainId
     * @return
     */
    public static List<Node> getAvailableNodes(int chainId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(6);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("state", 1);
            params.put("isCross", 0);
            params.put("startPage", 0);
            params.put("pageSize", 0);

            Response response = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_getNodes", params);
            if (!response.isSuccess()) {
                return List.of();
            }
            Map responseData = (Map) response.getResponseData();
            List list = (List) responseData.get("nw_getNodes");
            List nodes = new ArrayList();
            for (Object o : list) {
                Map map = (Map) o;
                Node node = new Node();
                node.setId((String) map.get("nodeId"));
                node.setHeight(Long.parseLong(map.get("blockHeight").toString()));
                node.setHash(NulsDigestData.fromDigestHex((String) map.get("blockHash")));
                nodes.add(node);
            }
            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return List.of();
        }
    }

    /**
     * 根据链ID重置网络节点
     *
     * @param chainId
     */
    public static void resetNetwork(int chainId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);

            CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_reconnect", params);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

    /**
     * 给网络上节点广播消息
     *
     * @param chainId
     * @param message
     * @param excludeNodes 排除的节点
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message, String excludeNodes, String command) {
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("excludeNodes", excludeNodes);
            params.put("messageBody", HexUtil.encode(message.serialize()));
            params.put("command", command);
            boolean success = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_broadcast", params).isSuccess();

            messageLog.debug("broadcast " + message.getClass().getName() +", chainId:" + chainId + ", success:" + success);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            messageLog.error(e);
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
    public static boolean sendToNode(int chainId, BaseMessage message, String nodeId, String command) {
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("nodes", nodeId);
            params.put("messageBody", HexUtil.encode(message.serialize()));
            params.put("command", command);
            boolean success = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_sendPeersMsg", params).isSuccess();
            messageLog.debug("send " + message.getClass().getName() + " to node-" + nodeId + ", chainId:" + chainId + ", success:" + success);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            messageLog.error(e);
            return false;
        }
    }

    /**
     * 给网络上节点广播消息
     *
     * @param chainId
     * @param message
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message, String command) {
        return broadcast(chainId, message, null, command);
    }

    /**
     * 针对某个异步消息返回执行结果
     *
     * @param chainId
     * @param hash
     * @param nodeId
     */
    public static void sendFail(int chainId, NulsDigestData hash, String nodeId) {
        CompleteMessage message = new CompleteMessage();
        message.setRequestHash(hash);
        message.setSuccess(false);
        sendToNode(chainId, message, nodeId, COMPLETE_MESSAGE);
    }

    /**
     * 针对某个异步消息返回执行结果
     *
     * @param chainId
     * @param hash
     * @param nodeId
     */
    public static void sendSuccess(int chainId, NulsDigestData hash, String nodeId) {
        CompleteMessage message = new CompleteMessage();
        message.setRequestHash(hash);
        message.setSuccess(true);
        sendToNode(chainId, message, nodeId, COMPLETE_MESSAGE);
    }

    /**
     * 更新网络节点最新高度与hash
     * 1.收到smallblock时更新
     * 2.收到转发请求并且本地确定有这个hash的区块时更新
     *
     * @param chainId
     * @param hash
     * @param height
     * @param nodeId
     */
    public static void setHashAndHeight(int chainId, NulsDigestData hash, long height, String nodeId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("nodeId", nodeId);
            params.put("blockHeight", height);
            params.put("blockHash", hash.toString());

            CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_updateNodeInfo", params);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public static long currentTime() {
        try {
//            Map<String, Object> params = new HashMap<>(1);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response response = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_currentTimeMillis", null);
            Map responseData = (Map) response.getResponseData();
            Map result = (Map) responseData.get("nw_currentTimeMillis");
            return (Long) result.get("currentTimeMillis");
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("get nw_currentTimeMillis fail");
        }
        return System.currentTimeMillis();
    }

    /**
     * 注册消息处理器
     *
     * @return
     */
    public static boolean register() {
        try {
            Map<String, Object> map = new HashMap<>(2);
            List<Map<String, String>> cmds = new ArrayList<>();
            map.put("role", ModuleE.BL.abbr);
            List<String> list = List.of(COMPLETE_MESSAGE, BLOCK_MESSAGE, GET_BLOCK_MESSAGE, FORWARD_SMALL_BLOCK_MESSAGE, GET_BLOCKS_BY_HEIGHT_MESSAGE, GET_TXGROUP_MESSAGE, SMALL_BLOCK_MESSAGE, GET_SMALL_BLOCK_MESSAGE, TXGROUP_MESSAGE, "test");
            for (String s : list) {
                Map<String, String> cmd = new HashMap<>(2);
                cmd.put("protocolCmd", s);
                cmd.put("handler", s);
                cmds.add(cmd);
            }
            map.put("protocolCmds", cmds);
            boolean success = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map).isSuccess();
            commonLog.debug("get nw_protocolRegister " + success);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("get nw_protocolRegister fail");
        }
        return false;
    }

}
