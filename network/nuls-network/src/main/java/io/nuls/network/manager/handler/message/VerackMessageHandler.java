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
import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * version ack message handler
 * @author lan
 * @date 2018/10/20
 */
public class VerackMessageHandler extends BaseMessageHandler {
    private static VerackMessageHandler instance = new VerackMessageHandler();
    private VerackMessageHandler() {

    }

    public static VerackMessageHandler getInstance() {
        return instance;
    }
    /**
     *
     * 接收消息处理
     * Receive message processing
     * @param message   address message
     * @param nodeKey      peer node key
     * @param isServer client=false or server=true
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, String nodeKey,boolean isServer) {
        long magicNumber = message.getHeader().getMagicNumber();
        Node node =ConnectionManager.getInstance().getNodeByCache(nodeKey,Node.IN);
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(magicNumber);
        VerackMessage verackMessage = (VerackMessage)message;
        if(isServer){
            if(VerackMessageBody.VER_CONNECT_MAX == verackMessage.getMsgBody().getAckCode()) {
                 node.removeGroupConnector(magicNumber);
            }else{
                /*
                 *server端能收到verack消息,接收消息并将连接状态跃迁为握手完成
                 *The server can receive the verack message, receive the message and transition the connection state to the handshake.
                 */
                Log.debug("VerackMessageHandler Recieve:{}-{}",node.getIp()+":"+node.getRemotePort(),message.getHeader().getCommandStr());
                nodeGroup.addConnetNode(node,true);
                /*
                 *更新连接为握手完成状态
                 *Update connection to handshake completion status
                 */
                node.getNodeGroupConnector(message.getHeader().getMagicNumber()).setStatus(NodeGroupConnector.HANDSHAKE);
            }
        }else{
            /*
             *client 端收到verack消息，判断ack状态,如果是对方连接数过大，则移除连接，并且CONNECT_FAIL_LOCK_MINUTE 分钟内不需要再次连接
             *The client receives the verack message and determines the ack status. If the number of connections is too large,
              *  the connection is removed and the connection does not need to be reconnected within CONNECT_FAIL_LOCK_MINUTE minutes.
             */

            if(VerackMessageBody.VER_CONNECT_MAX == verackMessage.getMsgBody().getAckCode()){
                node.removeGroupConnector(magicNumber);
                nodeGroup.addFailConnect(node.getId(),NetworkConstant.CONNECT_FAIL_LOCK_MINUTE);
                /*
                 * 如果连接只承载一条链的业务，则将链断开
                 *If the connection only carries a chain of services, then disconnect the chain
                 */
                if(node.getNodeGroupConnectors().size() == 0){
                    node.getChannel().close();
                    return   NetworkEventResult.getResultSuccess();
                }
            }
        }
        return NetworkEventResult.getResultSuccess();
    }
    /**
     *
     * VerackMessageHandler sending a message
     * @param message   address message
     * @param node      peer info
     * @param isServer client=false or server=true
     * @param asyn  default true
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean isServer, boolean asyn) {
        Log.debug("VerackMessageHandler send:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        return super.send(message,node,isServer,asyn);
    }
}
