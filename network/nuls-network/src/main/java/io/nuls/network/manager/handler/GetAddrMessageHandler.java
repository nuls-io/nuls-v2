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

import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.tools.log.Log;

/**
 * get address message handler
 * @author lan
 * @date 2018/10/15
 *
 */
public class GetAddrMessageHandler extends BaseMessageHandler {

    private static GetAddrMessageHandler instance = new GetAddrMessageHandler();

    private GetAddrMessageHandler() {

    }

    public static GetAddrMessageHandler getInstance() {
        return instance;
    }

    @Override
    public NetworkEventResult recieve(BaseMessage message, String nodeKey,boolean isServer) {
        Node node = NodeGroupManager.getInstance().getNodeGroupByMagic(message.getHeader().getMagicNumber()).getConnectNodeMap().get(nodeKey);
        Log.debug("GetAddrMessageHandler Recieve:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        //发送addr消息
        MessageManager.getInstance().sendToNode(MessageFactory.getInstance().buildAddrMessage(node,message.getHeader().getMagicNumber()),node,true);
        return new NetworkEventResult(true, null);
    }

    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean isServer, boolean asyn) {
        Log.debug("GetAddrMessageHandler Send:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        return super.send(message,node,isServer,asyn);
    }
}
