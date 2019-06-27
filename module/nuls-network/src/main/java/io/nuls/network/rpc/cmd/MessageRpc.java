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

import io.nuls.base.RPCUtil;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.DateUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
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
import io.nuls.network.model.message.base.MessageHeader;
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
            description = "模块协议指令注册")
    @Parameters(value = {
            @Parameter(parameterName = "role", parameterType = "String", parameterDes = "模块角色名称"),
            @Parameter(parameterName = "protocolCmds", parameterType = "List", parameterDes = "注册指令列表")
    })
    @ResponseData(description = "无特定返回值，没有错误即成功")
    public Response protocolRegister(Map params) {
        String role = String.valueOf(params.get("role"));
        try {
            /*
             * 如果外部模块修改了调用注册信息，进行重启，则清理缓存信息，并重新注册
             * clear cache protocolRoleHandler
             */
            messageHandlerFactory.clearCacheProtocolRoleHandlerMap(role);
            List<String> protocolCmds = (List<String>) params.get("protocolCmds");
            for (String cmd : protocolCmds) {
                messageHandlerFactory.addProtocolRoleHandlerMap(cmd, role);
            }
            /*
             * 进行持久化存库
             * save info to storage
             */
            RoleProtocolPo roleProtocolPo = new RoleProtocolPo();
            roleProtocolPo.setRole(role);
            roleProtocolPo.setProtocolCmds(protocolCmds);
            StorageManager.getInstance().getDbService().saveOrUpdateProtocolRegisterInfo(roleProtocolPo);
            Log.info("----------------------------new message register---------------------------");
            Log.info(roleProtocolPo.toString());
        } catch (Exception e) {
            Log.error(role, e);
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }

    /**
     * nw_broadcast
     * 外部广播接收
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_BROADCAST, version = 1.0,
            description = "广播消息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "连接的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "excludeNodes", parameterType = "String", parameterDes = "排除peer节点Id，用逗号分割"),
            @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体"),
            @Parameter(parameterName = "command", parameterType = "String", parameterDes = "消息协议指令"),
            @Parameter(parameterName = "isCross", parameterType = "Boolean", parameterDes = "是否是跨链")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class, description = "一个节点都没发送出去时返回false")
    }))
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
     * nw_sendPeersMsg
     */

    @CmdAnnotation(cmd = CmdConstant.CMD_NW_SEND_PEERS_MSG, version = 1.0,
            description = "向指定节点发送消息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "连接的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "nodes", parameterType = "String", parameterDes = "指定发送peer节点Id，用逗号拼接的字符串"),
            @Parameter(parameterName = "messageBody", parameterType = "String", parameterDes = "消息体"),
            @Parameter(parameterName = "command", parameterType = "String", parameterDes = "消息协议指令")
    })
    @ResponseData(description = "无特定返回值，没有错误即成功")
    public Response sendPeersMsg(Map params) {
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String nodes = String.valueOf(params.get("nodes"));
            byte[] messageBody = RPCUtil.decode(String.valueOf(params.get("messageBody")));
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

    public static void main(String[] args) {
        System.out.println(new Date(1561566356309L));
        System.out.println(new Date(1561566332071L));

    }
}
