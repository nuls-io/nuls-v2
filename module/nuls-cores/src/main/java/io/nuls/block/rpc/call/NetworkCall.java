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

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BaseBusinessMessage;
import io.nuls.base.data.NulsHash;
import io.nuls.block.constant.NodeEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.model.Node;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.*;
import static io.nuls.block.utils.LoggerUtil.COMMON_LOG;


/**
 * Tools for calling network module interfaces
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 afternoon3:48
 */
public class NetworkCall {

    /**
     * According to the chainIDGet available nodes
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static List<Node> getAvailableNodes(int chainId) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(6);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("state", 1);
            params.put("isCross", false);
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
                node.setNodeEnum(NodeEnum.IDLE);
                nodes.add(node);
            }
            return nodes;
        } catch (Exception e) {
            logger.error("", e);
            return List.of();
        }
    }

    /**
     * According to the chainIDReset network nodes
     *
     * @param chainId chainId/chain id
     */
    public static void resetNetwork(int chainId) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(2);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_reconnect", params);
            logger.info("resetNetwork......");
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /**
     * Broadcast messages to nodes on the network
     *
     * @param chainId      chainId/chain id
     * @param message
     * @param excludeNodes Excluded nodes
     * @return
     */
    public static boolean broadcast(int chainId, BaseBusinessMessage message, String excludeNodes, String command) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("excludeNodes", excludeNodes);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", command);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_broadcast", params).isSuccess();
//            logger.debug("broadcast " + message.getClass().getName() + ", success:" + success);
            return success;
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

    /**
     * Send messages to specified nodes
     *
     * @param chainId chainId/chain id
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean sendToNode(int chainId, BaseBusinessMessage message, String nodeId, String command) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("nodes", nodeId);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", command);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_sendPeersMsg", params).isSuccess();
//            logger.debug("send " + message.toString() + " to node-" + nodeId + ", success:" + success);
            return success;
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

    /**
     * Broadcast messages to nodes on the network
     *
     * @param chainId chainId/chain id
     * @param message
     * @return
     */
    public static boolean broadcast(int chainId, BaseBusinessMessage message, String command) {
        return broadcast(chainId, message, null, command);
    }

    /**
     * Return execution result for a certain asynchronous message
     *
     * @param chainId chainId/chain id
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
     * Return execution result for a certain asynchronous message
     *
     * @param chainId chainId/chain id
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
     * Update the latest height of network nodes to matchhash
     * 1.receivesmallblockUpdate on time
     * 2.Received forwarding request and confirmed locally that this is presenthashWhen updating blocks
     *
     * @param chainId chainId/chain id
     * @param hash
     * @param height
     * @param nodeId
     */
    public static void setHashAndHeight(int chainId, NulsHash hash, long height, String nodeId) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("nodeId", nodeId);
            params.put("blockHeight", height);
            params.put("blockHash", hash.toString());
            ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_updateNodeInfo", params);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
