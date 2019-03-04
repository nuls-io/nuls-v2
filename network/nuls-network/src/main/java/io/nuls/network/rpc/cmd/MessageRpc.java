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

import io.nuls.base.data.NulsDigestData;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.network.model.po.ProtocolHandlerPo;
import io.nuls.network.model.po.RoleProtocolPo;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.nuls.network.utils.LoggerUtil.Log;

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


    @CmdAnnotation(cmd = "nw_protocolRegister", version = 1.0,
            description = "protocol cmd register")
    @Parameter(parameterName = "role", parameterType = "string")
    @Parameter(parameterName = "protocolCmds", parameterType = "arrays")
    public Response protocolRegister(Map params) {
        try {
            String role = String.valueOf(params.get("role"));
            Log.info(role);
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
             * save info to db
             */
            RoleProtocolPo roleProtocolPo = new RoleProtocolPo();
            roleProtocolPo.setRole(role);
            roleProtocolPo.setProtocolHandlerPos(protocolHandlerPos);
            StorageManager.getInstance().getDbService().saveOrUpdateProtocolRegisterInfo(roleProtocolPo);

        } catch (Exception e) {
            e.printStackTrace();
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }

    /**
     * nw_broadcast
     * 外部广播接收
     */
    @CmdAnnotation(cmd = "nw_broadcast", version = 1.0,
            description = "broadcast message")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]")
    @Parameter(parameterName = "excludeNodes", parameterType = "string")
    @Parameter(parameterName = "messageBody", parameterType = "string")
    @Parameter(parameterName = "command", parameterType = "string")
    @Parameter(parameterName = "isCross", parameterType = "boolean")
    public Response broadcast(Map params) {
        try {
            Log.debug("==================broadcast begin");
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String excludeNodes = String.valueOf(params.get("excludeNodes"));
            byte[] messageBody = HexUtil.hexStringToBytes(String.valueOf(params.get("messageBody")));
            String cmd = String.valueOf(params.get("command"));
            MessageManager messageManager = MessageManager.getInstance();
            NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
            if (null == nodeGroup) {
                Log.error("chain is not exist!");
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
                    LoggerUtil.modulesMsgLogs(cmd, node, messageBody, "send");
                    /*end test code*/
                }
            }
            Log.debug("==================broadcast nodes==size={}", nodes.size());
            messageManager.broadcastToNodes(message, nodes, true);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        Log.debug("==================broadcast end");
        return success();
    }


    /**
     * nw_sendPeersMsg
     */
    @CmdAnnotation(cmd = "nw_sendPeersMsg", version = 1.0,
            description = "send peer message")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]")
    @Parameter(parameterName = "nodes", parameterType = "string")
    @Parameter(parameterName = "messageBody", parameterType = "string")
    @Parameter(parameterName = "command", parameterType = "string")
    public Response sendPeersMsg(Map params) {
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String nodes = String.valueOf(params.get("nodes"));
            byte[] messageBody = HexUtil.hexStringToBytes(String.valueOf(params.get("messageBody")));
            String cmd = String.valueOf(params.get("command"));
            Log.debug("{}==================sendPeersMsg begin, cmd-{}", TimeManager.currentTimeMillis(), cmd);
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
                    LoggerUtil.modulesMsgLogs(cmd, availableNode, messageBody, "send");
                    /*end test code*/
                    nodesList.add(availableNode);
                }else{
                    Log.info("node = {} is not available!");
                }
            }
            Log.debug("==================sendPeersMsg nodesList size={}, cmd-{}, hash-{}", nodesList.size(), cmd, NulsDigestData.calcDigestData(messageBody).getDigestHex());
            NetworkEventResult networkEventResult = messageManager.broadcastToNodes(message, nodesList, true);
            Log.debug("=======================networkEventResult {}, cmd-{}", networkEventResult.isSuccess(), cmd);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        Log.debug("==================sendPeersMsg end");
        return success();
    }
}
