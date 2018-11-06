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

package io.nuls.network.manager.handler;

import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.*;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.VerackMessage;
import io.nuls.network.model.message.VersionMessage;
import io.nuls.network.model.message.body.VersionMessageBody;
import io.nuls.tools.log.Log;

/**
 * version message handler
 * @author lan
 * @date 2018/10/20
 */
public class VersionMessageHandler extends BaseMessageHandler {

    private static VersionMessageHandler instance = new VersionMessageHandler();
    NodeGroupManager nodeGroupManager=NodeGroupManager.getInstance();
    private VersionMessageHandler() {

    }

    public static VersionMessageHandler getInstance() {
        return instance;
    }
    LocalInfoManager   localInfoManager =LocalInfoManager.getInstance();
    @Override
    public NetworkEventResult recieve(BaseMessage message, String nodeKey,boolean isServer) {
        Node node =null;
        VersionMessageBody versionBody=(VersionMessageBody)message.getMsgBody();
        if(isServer){

            node =ConnectionManager.getInstance().getNodeByCache(nodeKey,Node.IN);
            localInfoManager.updateExternalAddress(versionBody.getAddrYou().getIp().getHostAddress(),versionBody.getAddrYou().getPort());
            //node加入到Group的未连接中
            NodeGroup nodeGroup=nodeGroupManager.getNodeGroupByMagic(message.getHeader().getMagicNumber());
            Log.debug("VersionMessageHandler Recieve:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
            //服务端首次知道channel的网络属性，进行channel归属
            node.addGroupConnector(message.getHeader().getMagicNumber());
            NodeGroupConnector nodeGroupConnector=node.getNodeGroupConnector(message.getHeader().getMagicNumber());
            //node加入到Group的未连接中
            nodeGroupConnector.setStatus(Node.CONNECTING);
            nodeGroup.addDisConnetNode(node,true);
            //TODO:存储需要的信息
            node.setVersionProtocolInfos(message.getHeader().getMagicNumber(),versionBody.getProtocolVersion(),versionBody.getBlockHeight(),versionBody.getBlockHash());
            node.setRemoteCrossPort(versionBody.getPortMeCross());
            //回复version
            VersionMessage   versionMessage = MessageFactory.getInstance().buildVersionMessage(node,message.getHeader().getMagicNumber());
            send(versionMessage, node, true,true);
        }else{
            localInfoManager.updateExternalAddress(versionBody.getAddrYou().getIp().getHostAddress(),NetworkParam.getInstance().getPort());
            node = nodeGroupManager.getNodeGroupByMagic(message.getHeader().getMagicNumber()).getDisConnectNodeMap().get(nodeKey);
            Log.debug("VersionMessageHandler Recieve:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
            NodeGroupConnector nodeGroupConnector=node.getNodeGroupConnector(message.getHeader().getMagicNumber());
            nodeGroupConnector.setStatus(Node.HANDSHAKE);
            //node加入到Group的连接中
            NodeGroup nodeGroup=nodeGroupManager.getNodeGroupByMagic(message.getHeader().getMagicNumber());
            nodeGroup.addConnetNode(node,true);
            //TODO:存储需要的信息
            node.setVersionProtocolInfos(message.getHeader().getMagicNumber(),versionBody.getProtocolVersion(),versionBody.getBlockHeight(),versionBody.getBlockHash());
            node.setRemoteCrossPort(versionBody.getPortMeCross());
            //client:接收到server端消息，进行verack答复
            VerackMessage verackMessage=MessageFactory.getInstance().buildVerackMessage(node,message.getHeader().getMagicNumber());
            node = nodeGroupManager.getNodeGroupByMagic(message.getHeader().getMagicNumber()).getConnectNodeMap().get(nodeKey);
            MessageManager.getInstance().sendToNode(verackMessage,node,true);
            //自我连接
            ConnectionManager.getInstance().selfConnection();
        }

        return null;
    }

    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean isServer, boolean asyn) {
        Log.debug("VersionMessageHandler send:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        //VERSION 协议的发送中，节点所属的网络业务状态是连接中
        NodeGroupConnector nodeGroupConnector=node.getNodeGroupConnector(message.getHeader().getMagicNumber());
        nodeGroupConnector.setStatus(Node.CONNECTING);
        return super.send(message,node,isServer,asyn);
    }
}
