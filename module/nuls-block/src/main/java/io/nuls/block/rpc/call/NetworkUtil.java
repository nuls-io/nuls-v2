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
package io.nuls.block.rpc.call;

import io.nuls.base.data.BaseBusinessMessage;
import io.nuls.base.data.NulsHash;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.model.Node;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.log.logback.NulsLogger;

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
     * @param chainId 链Id/chain id
     * @return
     */
    public static List<Node> getAvailableNodes(int chainId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(6);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("state", 1);
            params.put("isCross", 0);
            params.put("startPage", 0);
            params.put("pageSize", 0);

            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_getNodes", params);
            if (!response.isSuccess()) {
                return List.of();
            }
            Map responseData = (Map) response.getResponseData();
            List list = (List) responseData.get("nw_getNodes");
            List<Node> nodes = new ArrayList<>();
            for (Object o : list) {
                Map map = (Map) o;
                Node node = new Node();
                node.setId((String) map.get("nodeId"));
                node.setHeight(Long.parseLong(map.get("blockHeight").toString()));
                String blockHash = (String) map.get("blockHash");
                if (StringUtils.isBlank(blockHash)) {
                    continue;
                }
                node.setHash(NulsHash.fromHex(blockHash));
                nodes.add(node);
            }
            return nodes;
        } catch (Exception e) {
            commonLog.error("", e);
            return List.of();
        }
    }

    /**
     * 根据链ID重置网络节点
     *
     * @param chainId 链Id/chain id
     */
    public static void resetNetwork(int chainId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_reconnect", params);
        } catch (Exception e) {
            commonLog.error("", e);
        }
    }

    /**
     * 给网络上节点广播消息
     *
     * @param chainId      链Id/chain id
     * @param message
     * @param excludeNodes 排除的节点
     * @return
     */
    public static boolean broadcast(int chainId, BaseBusinessMessage message, String excludeNodes, String command) {
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("excludeNodes", excludeNodes);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", command);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_broadcast", params).isSuccess();
            messageLog.debug("broadcast " + message.getClass().getName() + ", chainId:" + chainId + ", success:" + success);
            return success;
        } catch (Exception e) {
            messageLog.error("", e);
            return false;
        }
    }

    /**
     * 给指定节点发送消息
     *
     * @param chainId 链Id/chain id
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean sendToNode(int chainId, BaseBusinessMessage message, String nodeId, String command) {
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("nodes", nodeId);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", command);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_sendPeersMsg", params).isSuccess();
            messageLog.debug("send " + message.getClass().getName() + " to node-" + nodeId + ", chainId:" + chainId + ", success:" + success);
            return success;
        } catch (Exception e) {
            messageLog.error("", e);
            return false;
        }
    }

    /**
     * 给网络上节点广播消息
     *
     * @param chainId 链Id/chain id
     * @param message
     * @return
     */
    public static boolean broadcast(int chainId, BaseBusinessMessage message, String command) {
        return broadcast(chainId, message, null, command);
    }

    /**
     * 针对某个异步消息返回执行结果
     *
     * @param chainId 链Id/chain id
     * @param hash
     * @param nodeId
     */
    public static void sendFail(int chainId, NulsHash hash, String nodeId) {
        CompleteMessage message = new CompleteMessage();
        message.setRequestHash(hash);
        message.setSuccess(false);
        sendToNode(chainId, message, nodeId, COMPLETE_MESSAGE);
    }

    /**
     * 针对某个异步消息返回执行结果
     *
     * @param chainId 链Id/chain id
     * @param hash
     * @param nodeId
     */
    public static void sendSuccess(int chainId, NulsHash hash, String nodeId) {
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
     * @param chainId 链Id/chain id
     * @param hash
     * @param height
     * @param nodeId
     */
    public static void setHashAndHeight(int chainId, NulsHash hash, long height, String nodeId) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("nodeId", nodeId);
            params.put("blockHeight", height);
            params.put("blockHash", hash.toString());
            ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_updateNodeInfo", params);
        } catch (Exception e) {
            commonLog.error("", e);
        }
    }

    /**
     * 注册消息处理器
     *
     * @return
     */
    public static void register() {
        try {
            Map<String, Object> map = new HashMap<>(2);
            List<Map<String, String>> cmds = new ArrayList<>();
            map.put("role", ModuleE.BL.abbr);
            List<String> list = List.of(COMPLETE_MESSAGE, BLOCK_MESSAGE, GET_BLOCK_MESSAGE, FORWARD_SMALL_BLOCK_MESSAGE, GET_BLOCKS_BY_HEIGHT_MESSAGE, GET_TXGROUP_MESSAGE, SMALL_BLOCK_MESSAGE, GET_SMALL_BLOCK_MESSAGE, TXGROUP_MESSAGE);
            for (String s : list) {
                Map<String, String> cmd = new HashMap<>(2);
                cmd.put("protocolCmd", s);
                cmd.put("handler", s);
                cmds.add(cmd);
            }
            map.put("protocolCmds", cmds);
            ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map);
        } catch (Exception e) {
            commonLog.error("",e);
        }
    }


}
