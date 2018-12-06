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

import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.message.TimeMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.tools.log.Log;

/**
 * time message handler
 * @author lan
 * @date 2018/11/02
 *
 */
public class TimeMessageHandler extends BaseMessageHandler {

    private static TimeMessageHandler instance = new TimeMessageHandler();

    private TimeMessageHandler() {

    }

    public static TimeMessageHandler getInstance() {
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
        NodeGroup nodeGroup=NodeGroupManager.getInstance().getNodeGroupByMagic(message.getHeader().getMagicNumber());
        Log.info("============================nodeKey="+nodeKey);
        Node node =nodeGroup .getConnectNodeMap().get(nodeKey);
        Log.debug("TimeMessageHandler Recieve:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        /*
         * 处理应答消息
         */
        TimeMessage timeMessage=(TimeMessage)message;

        return new NetworkEventResult(true, null);
    }

    /**
     *
     * TimeMessageHandler sending a message
     * @param message
     * @param node
     * @param isServer
     * @param asyn
     * @return
     */
    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean isServer, boolean asyn) {
        Log.debug("TimeMessageHandler Send:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        return super.send(message,node,isServer,asyn);
    }
}
