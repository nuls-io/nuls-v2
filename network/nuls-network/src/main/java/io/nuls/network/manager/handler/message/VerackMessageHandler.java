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

package io.nuls.network.manager.handler.message;

import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.message.VerackMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.body.VerackMessageBody;
import io.nuls.tools.log.Log;

/**
 * verack message handler
 * @author lan
 * @date 2018/10/20
 */
public class VerackMessageHandler extends BaseMessageHandler {

    private static VerackMessageHandler instance = new VerackMessageHandler();
    NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
    private VerackMessageHandler() {

    }

    public static VerackMessageHandler getInstance() {
        return instance;
    }
    /**
     *
     * 接收消息处理
     * Receive message processing
     * @param message
     * @param nodeKey
     * @param isServer
     * @return
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, String nodeKey,boolean isServer) {
        long magicNumber = message.getHeader().getMagicNumber();
//        Node node  = nodeGroupManager.getNodeGroupByMagic(message.getHeader().getMagicNumber()).getDisConnectNodeMap().get(nodeKey);
        Node node =ConnectionManager.getInstance().getNodeByCache(nodeKey,Node.IN);
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(magicNumber);
        if(isServer){
            //server端能收到verack消息,接收消息并将连接状态跃迁为握手完成
            Log.debug("VerackMessageHandler Recieve:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
            nodeGroup.addConnetNode(node,true);
            //握手完成状态
            node.getNodeGroupConnector(message.getHeader().getMagicNumber()).setStatus(NodeGroupConnector.HANDSHAKE);
        }else{
            //client 端收到verack消息，判断ack状态
            VerackMessage verackMessage = (VerackMessage)message;
            if(VerackMessageBody.VER_CONNECT_MAX == verackMessage.getMsgBody().getAckCode()){
                node.removeGroupConnector(magicNumber);
                nodeGroup.addFailConnect(node.getId(),NetworkConstant.CONNECT_FAIL_LOCK_MINUTE);
                if(node.getNodeGroupConnectors().size() == 0){
                    node.getChannel().close();
                    return new NetworkEventResult(true, null);
                }
            }
        }
        return new NetworkEventResult(true, null);
    }

    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean isServer, boolean asyn) {
        Log.debug("VerackMessageHandler send:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        return super.send(message,node,isServer,asyn);
    }
}
