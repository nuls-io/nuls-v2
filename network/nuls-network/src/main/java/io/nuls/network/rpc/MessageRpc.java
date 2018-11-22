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
package io.nuls.network.rpc;

import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @program: nuls2.0
 * @description:
 * @author: lan
 * @create: 2018/11/12
 **/
public class MessageRpc extends BaseCmd{
    /**
     * nw_broadcast
     * 外部广播接收
     */
//    @CmdAnnotation(cmd = "nw_broadcast", version = 1.0, preCompatible = true)
    public Response broadcast(List params) {
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get(0)));
            String excludeNodes = String.valueOf(params.get(1));
            byte [] messageBody =HexUtil.hexStringToBytes(String.valueOf(params.get(2)));
            String cmd =String.valueOf(params.get(3));
            MessageManager messageManager=MessageManager.getInstance();
            long magicNumber = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId).getMagicNumber();
            long checksum = messageManager.getCheckSum(messageBody);
            MessageHeader header = new MessageHeader(cmd,magicNumber,checksum,messageBody.length);
            byte [] headerByte = header.serialize();
            byte [] message = new byte[headerByte.length+messageBody.length];
            System.arraycopy(headerByte, 0, message, 0, headerByte.length);
            System.arraycopy(messageBody, 0, message, headerByte.length, messageBody.length);
            NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
            NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
            Collection<Node> nodesCollection=nodeGroup.getConnectNodes();
                excludeNodes=","+excludeNodes+",";
                List<Node> nodes = new ArrayList<>();
                for(Node node:nodesCollection){
                    if(!excludeNodes.contains(node.getId())){
                    nodes.add(node);
                    }
                }
               messageManager.broadcastToNodes(message,nodes,true);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }
    /**
     * nw_sendPeersMsg
     *
     */
//    @CmdAnnotation(cmd = "nw_sendPeersMsg", version = 1.0, preCompatible = true)
    public Response sendPeersMsg(List params) {
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get(0)));
            String nodes = String.valueOf(params.get(1));
            byte [] messageBody =HexUtil.hexStringToBytes(String.valueOf(params.get(2)));
            String cmd =String.valueOf(params.get(3));
            MessageManager messageManager=MessageManager.getInstance();
            NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
            NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
            long magicNumber = nodeGroup.getMagicNumber();
            long checksum = messageManager.getCheckSum(messageBody);
            MessageHeader header = new MessageHeader(cmd,magicNumber,checksum,messageBody.length);
            byte [] headerByte = header.serialize();
            byte [] message = new byte[headerByte.length+messageBody.length];
            System.arraycopy(headerByte, 0, message, 0, headerByte.length);
            System.arraycopy(messageBody, 0, message, headerByte.length, messageBody.length);
            String []nodeIds=nodes.split(",");
            List<Node> nodesList = new ArrayList<>();
            for(String nodeId:nodeIds){
                if(null != nodeGroup.getConnectNode(nodeId)){
                    nodesList.add(nodeGroup.getConnectNode(nodeId));
                }
            }
            messageManager.broadcastToNodes(message,nodesList,true);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        return success();
    }
}
