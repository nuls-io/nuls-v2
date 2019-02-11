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

import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.message.GetTimeMessage;
import io.nuls.network.model.message.TimeMessage;
import io.nuls.network.model.message.base.BaseMessage;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * get time message handler
 *
 * @author lan
 * @date 2018/10/15
 */
public class GetTimeMessageHandler extends BaseMessageHandler {

    private static GetTimeMessageHandler instance = new GetTimeMessageHandler();

    private GetTimeMessageHandler() {

    }

    public static GetTimeMessageHandler getInstance() {
        return instance;
    }

    /**
     * 接收消息处理
     * Receive message processing
     *
     * @param message address message
     * @param node    peer node
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, Node node) {
        Log.debug("GetTimeMessageHandler Recieve:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        GetTimeMessage getTimeMessage = (GetTimeMessage) message;
        /*
         *  回复时间消息
         */
        TimeMessage timeMessage = MessageFactory.getInstance().buildTimeResponseMessage(message.getHeader().getMagicNumber(), getTimeMessage.getMsgBody().getMessageId());
        MessageManager.getInstance().sendToNode(timeMessage, node, true);
        return NetworkEventResult.getResultSuccess();
    }

    /**
     * GetTimeMessageHandler
     *
     * @param message address message
     * @param node    peer info
     * @param asyn    default true
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean asyn) {
        Log.debug("GetTimeMessageHandler Send:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        return super.send(message, node, asyn);
    }
}
