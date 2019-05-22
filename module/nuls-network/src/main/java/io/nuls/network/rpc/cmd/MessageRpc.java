/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.network.rpc.cmd;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.network.constant.CmdConstant;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.network.model.po.ProtocolHandlerPo;
import io.nuls.network.model.po.RoleProtocolPo;
import io.nuls.network.utils.LoggerUtil;

import java.util.*;

/**
 * @author lan
 * @description 消息远程调用
 * 模块消息处理器注册
 * 发送消息调用
 * @date 2018/11/12
 **/
@Component
public class MessageRpc extends BaseCmd {

    private MessageHandlerFactory messageHandlerFactory = MessageHandlerFactory.getInstance();


    @CmdAnnotation(cmd = CmdConstant.CMD_NW_PROTOCOL_REGISTER, version = 1.0,
            description = "protocol cmd register")
    @Parameter(parameterName = "role", parameterType = "string")
    @Parameter(parameterName = "protocolCmds", parameterType = "arrays")
    public Response protocolRegister(Map params) {
        try {
            String role = String.valueOf(params.get("role"));
            /*
             * 如果外部模块修改了调用注册信息，进行重启，则清理缓存信息，并重新注册
             * clear cache protocolRoleHandler
             */
            messageHandlerFactory.clearCacheProtocolRoleHandlerMap(role);
            List<Map<String, String>> protocolCmds = (List<Map<String, String>>) params.get("protocolCmds");
            List<ProtocolHandlerPo> protocolHandlerPos = new ArrayList<>();
            for (Map map : protocolCmds) {
                ProtocolRoleHandler protocolRoleHandler = new ProtocolRoleHandler(role, map.get("handler").toString());
                messageHandlerFactory.addProtocolRoleHandlerMap(map.get("protocolCmd").toString(), protocolRoleHandler);
                ProtocolHandlerPo protocolHandlerPo = new ProtocolHandlerPo(map.get("protocolCmd").toString(), map.get("handler").toString());
                protocolHandlerPos.add(protocolHandlerPo);
            }
            /*
             * 进行持久化存库
             * save info to storage
             */
            RoleProtocolPo roleProtocolPo = new RoleProtocolPo();
            roleProtocolPo.setRole(role);
            roleProtocolPo.setProtocolHandlerPos(protocolHandlerPos);
            StorageManager.getInstance().getDbService().saveOrUpdateProtocolRegisterInfo(roleProtocolPo);
            Log.info("----------------------------new message register---------------------------");
            Log.info(roleProtocolPo.toString());
        } catch (Exception e) {
            Log.error(e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }

    /**
     * nw_broadcast
     * 外部广播接收
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_BROADCAST, version = 1.0,
            description = "broadcast message")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]")
    @Parameter(parameterName = "excludeNodes", parameterType = "string")
    @Parameter(parameterName = "messageBody", parameterType = "string")
    @Parameter(parameterName = "command", parameterType = "string")
    @Parameter(parameterName = "isCross", parameterType = "boolean")
    public Response broadcast(Map params) {
        Map<String, Object> rtMap = new HashMap<>();
        rtMap.put("value", true);
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String excludeNodes = String.valueOf(params.get("excludeNodes"));
            byte[] messageBody = RPCUtil.decode(String.valueOf(params.get("messageBody")));
            String cmd = String.valueOf(params.get("command"));
            MessageManager messageManager = MessageManager.getInstance();
            NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
            if (null == nodeGroup) {
                LoggerUtil.logger(chainId).error("chain is not exist!");
                return failed(NetworkErrorCode.PARAMETER_ERROR);
            }
            long magicNumber = nodeGroup.getMagicNumber();
            long checksum = messageManager.getCheckSum(messageBody);
            MessageHeader header = new MessageHeader(cmd, magicNumber, checksum, messageBody.length);
            byte[] headerByte = header.serialize();
            byte[] message = new byte[headerByte.length + messageBody.length];
            boolean isCross = false;
            if (null != params.get("isCross")) {
                isCross = Boolean.valueOf(params.get("isCross").toString());
            }
            System.arraycopy(headerByte, 0, message, 0, headerByte.length);
            System.arraycopy(messageBody, 0, message, headerByte.length, messageBody.length);
            NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
            Collection<Node> nodesCollection = nodeGroup.getAvailableNodes(isCross);
            excludeNodes = NetworkConstant.COMMA + excludeNodes + NetworkConstant.COMMA;
            List<Node> nodes = new ArrayList<>();
            for (Node node : nodesCollection) {
                if (!excludeNodes.contains(NetworkConstant.COMMA + node.getId() + NetworkConstant.COMMA)) {
                    nodes.add(node);
                    /*begin test code*/
//                    LoggerUtil.modulesMsgLogs(cmd, node, messageBody, "send");
                    /*end test code*/
                }
            }
            if (0 == nodes.size()) {
                rtMap.put("value", false);
            } else {
                messageManager.broadcastToNodes(message, nodes, true);
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success(rtMap);
    }

    /**
     * 跨链的随机广播
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_CROSS_RANDOM_BROADCAST, version = 1.0,
            description = "nw_crossRandomBroadcast")
    @Parameter(parameterName = "messageBody", parameterType = "string")
    @Parameter(parameterName = "command", parameterType = "string")
    @Parameter(parameterName = "maxPeerCount", parameterType = "int")
    public Response crossRandomBroadcast(Map params) {
        List<String> sendNodes = new ArrayList<>();
        try {
            byte[] messageBody = RPCUtil.decode(String.valueOf(params.get("messageBody")));
            String cmd = String.valueOf(params.get("command"));
            int maxPeerCount = Integer.valueOf(params.get("maxPeerCount").toString());
            MessageManager messageManager = MessageManager.getInstance();
            //随机发出请求
            List<NodeGroup> list = NodeGroupManager.getInstance().getNodeGroups();
            if (list.size() == 0) {
                return success(sendNodes);
            }
            Collections.shuffle(list);
            int count = 0;
            boolean nodesEnough = false;
            for (NodeGroup nodeGroup : list) {
                List<Node> nodes = nodeGroup.getCrossNodeContainer().getAvailableNodes();
                long magicNumber = nodeGroup.getMagicNumber();
                long checksum = messageManager.getCheckSum(messageBody);
                MessageHeader header = new MessageHeader(cmd, magicNumber, checksum, messageBody.length);
                byte[] headerByte = header.serialize();
                byte[] message = new byte[headerByte.length + messageBody.length];
                System.arraycopy(headerByte, 0, message, 0, headerByte.length);
                System.arraycopy(messageBody, 0, message, headerByte.length, messageBody.length);
                List<Node> broadCastNodes = new ArrayList<>();
                for (Node node : nodes) {
                    broadCastNodes.add(node);
                    sendNodes.add(node.getId());
                    count++;
                    if (count >= maxPeerCount) {
                        nodesEnough = true;
                        break;
                    }
                }
                if (broadCastNodes.size() > 0) {
                    messageManager.broadcastToNodes(message, broadCastNodes, true);
                }
                if (nodesEnough) {
                    break;
                }
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success(sendNodes);
    }

    /**
     * nw_sendPeersMsg
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_SEND_PEERS_MSG, version = 1.0,
            description = "send peer message")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]")
    @Parameter(parameterName = "nodes", parameterType = "string")
    @Parameter(parameterName = "messageBody", parameterType = "string")
    @Parameter(parameterName = "command", parameterType = "string")
    public Response sendPeersMsg(Map params) {
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String nodes = String.valueOf(params.get("nodes"));
            byte[] messageBody = RPCUtil.decode(String.valueOf(params.get("messageBody")));
            String cmd = String.valueOf(params.get("command"));
            if (cmd.equalsIgnoreCase("getBlocks")) {
                LoggerUtil.logger(chainId).debug("sendPeersMsg, nodes-{}", nodes);
            }
            MessageManager messageManager = MessageManager.getInstance();
            NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
            NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
            long magicNumber = nodeGroup.getMagicNumber();
            long checksum = messageManager.getCheckSum(messageBody);
            MessageHeader header = new MessageHeader(cmd, magicNumber, checksum, messageBody.length);
            byte[] headerByte = header.serialize();
            byte[] message = new byte[headerByte.length + messageBody.length];
            System.arraycopy(headerByte, 0, message, 0, headerByte.length);
            System.arraycopy(messageBody, 0, message, headerByte.length, messageBody.length);
            String[] nodeIds = nodes.split(",");
            List<Node> nodesList = new ArrayList<>();
            for (String nodeId : nodeIds) {
                Node availableNode = nodeGroup.getAvailableNode(nodeId);
                if (null != availableNode) {
                    /*begin test code*/
//                    LoggerUtil.modulesMsgLogs(cmd, availableNode, messageBody, "send");
                    /*end test code*/
                    nodesList.add(availableNode);
                } else {
                    LoggerUtil.logger(chainId).error("node = {} is not available!", nodeId);
                }
            }
            NetworkEventResult networkEventResult = messageManager.broadcastToNodes(message, nodesList, true);
        } catch (Exception e) {
            Log.error(e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }
}
