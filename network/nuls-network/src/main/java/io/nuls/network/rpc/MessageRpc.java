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
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;

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
    public CmdResponse broadcast(List params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        String excludeNodes = String.valueOf(params.get(1));
        byte [] message =HexUtil.hexStringToBytes(String.valueOf(params.get(2)));
        //补充魔法参数
        byte [] magicNumber = new byte[4];
        System.arraycopy(message, 0, magicNumber, 0, magicNumber.length);
        if(0 == ByteUtils.byteToLong(magicNumber)){
            long magicNumberLong = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId).getMagicNumber();
            System.arraycopy(ByteUtils.longToBytes(magicNumberLong), 0, message, 0, 4);
        }
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        Collection<Node> nodesCollection=nodeGroup.getConnectNodes();
        try {
            excludeNodes=","+excludeNodes+",";
            List<Node> nodes = new ArrayList<>();
            for(Node node:nodesCollection){
                if(!excludeNodes.contains(node.getId())){
                nodes.add(node);
                }
            }
            MessageManager messageManager=MessageManager.getInstance();
            messageManager.broadcastToNodes(message,nodes,true);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(NetworkErrorCode.PARAMETER_ERROR,  e.getMessage());
        }
        return success();
    }
    /**
     * nw_sendPeersMsg
     *
     */
//    @CmdAnnotation(cmd = "nw_sendPeersMsg", version = 1.0, preCompatible = true)
    public CmdResponse sendPeersMsg(List params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        String nodes = String.valueOf(params.get(1));
        byte [] message =HexUtil.hexStringToBytes(String.valueOf(params.get(2)));
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        //补充魔法参数
        byte [] magicNumber = new byte[4];
        System.arraycopy(message, 0, magicNumber, 0, magicNumber.length);
        if(0 == ByteUtils.byteToLong(magicNumber)){
            long magicNumberLong = nodeGroup.getMagicNumber();
            System.arraycopy(ByteUtils.longToBytes(magicNumberLong), 0, message, 0, magicNumber.length);
        }
        try {
            String []nodeIds=nodes.split(",");
            List<Node> nodesList = new ArrayList<>();
            for(String nodeId:nodeIds){
                if(null != nodeGroup.getConnectNode(nodeId)){
                    nodesList.add(nodeGroup.getConnectNode(nodeId));
                }
            }
            MessageManager messageManager=MessageManager.getInstance();
            messageManager.broadcastToNodes(message,nodesList,true);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(NetworkErrorCode.PARAMETER_ERROR,  e.getMessage());
        }
        return success();
    }
}
