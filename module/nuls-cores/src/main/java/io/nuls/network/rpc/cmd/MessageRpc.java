/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.base.RPCUtil;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.network.constant.CmdConstant;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.network.utils.LoggerUtil;

import java.util.*;

/**
 * @author lan
 * @description Message remote call
 * Module Message Processor Registration
 * Send Message Call
 * @date 2018/11/12
 **/
@Component
@NulsCoresCmd(module = ModuleE.NW)
public class MessageRpc extends BaseCmd {

    private MessageHandlerFactory messageHandlerFactory = MessageHandlerFactory.getInstance();

    @CmdAnnotation(cmd = CmdConstant.CMD_NW_PROTOCOL_REGISTER, version = 1.0,
            description = "Module Protocol Instruction Registration")
    @Parameters(value = {
            @Parameter(parameterName = "role", requestType = @TypeDescriptor(value = String.class), parameterDes = "Module Role Name"),
            @Parameter(parameterName = "protocolCmds", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "Register Instruction List")
    })
    @ResponseData(description = "No specific return value, successful without errors")
    public Response protocolRegister(Map params) {
        String role = String.valueOf(params.get("role"));
        try {
            /*
             * If the external module modifies the call registration information and restarts, clear the cache information and register again
             * clear cache protocolRoleHandler
             */
            messageHandlerFactory.clearCacheProtocolRoleHandlerMap(role);
            List<String> protocolCmds = (List<String>) params.get("protocolCmds");
            for (String cmd : protocolCmds) {
                messageHandlerFactory.addProtocolRoleHandlerMap(cmd, CmdPriority.DEFAULT, role);
            }
            Log.info("----------------------------new message register---------------------------");
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(role, e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }

    @CmdAnnotation(cmd = CmdConstant.CMD_NW_PROTOCOL_PRIORITY_REGISTER, version = 1.0,
            description = "Module protocol instruction registration with priority parameters")
    @Parameters(value = {
            @Parameter(parameterName = "role", requestType = @TypeDescriptor(value = String.class), parameterDes = "Module Role Name"),
            @Parameter(parameterName = "protocolCmds", requestType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "cmd", valueType = String.class, description = "Protocol instruction name,12byte"),
                    @Key(name = "priority", valueType = String.class, description = "priority,3Level,HIGH,DEFAULT,LOWER")
            }), parameterDes = "Register Instruction List")
    })
    @ResponseData(description = "No specific return value, successful without errors")
    public Response protocolRegisterWithPriority(Map params) {
        String role = String.valueOf(params.get("role"));
        try {
            /*
             * If the external module modifies the call registration information and restarts, clear the cache information and register again
             * clear cache protocolRoleHandler
             */
            messageHandlerFactory.clearCacheProtocolRoleHandlerMap(role);
            List<Map<String, Object>> protocolCmds = (List<Map<String, Object>>) params.get("protocolCmds");
            for (Map<String, Object> cmdMap : protocolCmds) {
                String cmd = (String) cmdMap.get("cmd");
                String priority = cmdMap.get("priority") == null ? "DEFAULT" : cmdMap.get("priority").toString();
                messageHandlerFactory.addProtocolRoleHandlerMap(cmd, CmdPriority.valueOf(priority), role);
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(role, e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }

    /**
     * nw_broadcast
     * External broadcast reception
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_BROADCAST, version = 1.0,
            description = "Broadcast messages")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Connected ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "excludeNodes", requestType = @TypeDescriptor(value = String.class), parameterDes = "eliminatepeernodeId, separated by commas"),
            @Parameter(parameterName = "messageBody", requestType = @TypeDescriptor(value = String.class), parameterDes = "Message BodyHex"),
            @Parameter(parameterName = "command", requestType = @TypeDescriptor(value = String.class), parameterDes = "Message Protocol Instructions"),
            @Parameter(parameterName = "isCross", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "Is it cross chain"),
            @Parameter(parameterName = "percent", requestType = @TypeDescriptor(value = int.class), parameterDes = "Broadcast transmission ratio,Not filled in,default100"),

    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class, description = "Returned when no node has been sent outfalse")
    }))
    public Response broadcast(Map params) {
        Map<String, Object> rtMap = new HashMap<>();
        rtMap.put("value", true);
        int percent = NetworkConstant.FULL_BROADCAST_PERCENT;
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String excludeNodes = String.valueOf(params.get("excludeNodes"));
            String messageBodyStr = String.valueOf(params.get("messageBody"));
            byte[] messageBody = RPCUtil.decode(messageBodyStr);
            String cmd = String.valueOf(params.get("command"));
            Object percentParam = params.get("percent");
            if (null != percentParam) {
                percent = Integer.valueOf(String.valueOf(percentParam));
            }
            MessageManager messageManager = MessageManager.getInstance();
            NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
            if (null == nodeGroup) {
//                LoggerUtil.logger(chainId).error("chain is not exist!");
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
            Collection<Node> nodesCollection = nodeGroup.getAvailableNodes(isCross);
            excludeNodes = NetworkConstant.COMMA + excludeNodes + NetworkConstant.COMMA;
            List<Node> nodes = new ArrayList<>();
            for (Node node : nodesCollection) {
                if (!excludeNodes.contains(NetworkConstant.COMMA + node.getId() + NetworkConstant.COMMA)) {
                    nodes.add(node);
                }
            }
            if (0 == nodes.size()) {
                rtMap.put("value", false);
            } else {
                messageManager.broadcastToNodes(message, cmd, nodes, true, percent);
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success(rtMap);
    }

    /**
     * nw_sendPeersMsg
     */

    @CmdAnnotation(cmd = CmdConstant.CMD_NW_SEND_PEERS_MSG, version = 1.0,
            description = "Send messages to specified nodes")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Connected ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "nodes", requestType = @TypeDescriptor(value = String.class), parameterDes = "Specify sendingpeernodeIdString concatenated with commas"),
            @Parameter(parameterName = "messageBody", requestType = @TypeDescriptor(value = String.class), parameterDes = "Message BodyHex"),
            @Parameter(parameterName = "command", requestType = @TypeDescriptor(value = String.class), parameterDes = "Message Protocol Instructions")
    })
    @ResponseData(description = "No specific return value, successful without errors")
    public Response sendPeersMsg(Map params) {
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String nodes = String.valueOf(params.get("nodes"));
            String messageBodyStr = String.valueOf(params.get("messageBody"));
            byte[] messageBody = RPCUtil.decode(messageBodyStr);
            String cmd = String.valueOf(params.get("command"));
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
                    nodesList.add(availableNode);
                } else {
                    LoggerUtil.logger(chainId).error("cmd={},node = {} is not available!", cmd, nodeId);
                }
            }
            if (nodesList.size() > 0) {
                messageManager.broadcastToNodes(message, cmd, nodesList, true, NetworkConstant.FULL_BROADCAST_PERCENT);
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }
}
