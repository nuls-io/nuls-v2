/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.network.model.dto.IpAddressShare;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.body.GetAddrMessageBody;
import io.nuls.network.utils.LoggerUtil;

import java.util.List;

/**
 * 发送与接收 连接地址 协议消息处理类
 * get address message handler
 * Send and receive connection address protocol message processing class
 *
 * @author lan
 * @date 2018/10/15
 */
public class GetAddrMessageHandler extends BaseMessageHandler {

    private static GetAddrMessageHandler instance = new GetAddrMessageHandler();

    private GetAddrMessageHandler() {

    }

    public static GetAddrMessageHandler getInstance() {
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
        int chainId = node.getNodeGroup().getChainId();
        LoggerUtil.logger(chainId).info("GetAddrMessageHandler Recieve:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        GetAddrMessageBody getAddrMessageBody = (GetAddrMessageBody) message.getMsgBody();
        //发送addr消息
        List<IpAddressShare> ipAddresses = NodeGroupManager.getInstance().getAvailableShareNodes(node, getAddrMessageBody.getChainId(), (getAddrMessageBody.getIsCrossAddress() == (byte) 1 ? true : false));
        AddrMessage addressMessage = MessageFactory.getInstance().buildAddrMessage(ipAddresses, message.getHeader().getMagicNumber(), getAddrMessageBody.getChainId(), getAddrMessageBody.getIsCrossAddress());
        if (0 == addressMessage.getMsgBody().getIpAddressList().size()) {
            LoggerUtil.logger(chainId).info("No Address");
        } else {
            LoggerUtil.logger(chainId).info("send addressMessage node = {}", node.getId());
            MessageManager.getInstance().sendHandlerMsg(addressMessage, node, true);
        }
        return NetworkEventResult.getResultSuccess();
    }

    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean asyn) {
        //TODO pierre test
        LoggerUtil.logger(node.getNodeGroup().getChainId()).debug("GetAddrMessageHandler Send:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        return super.send(message, node, asyn);
    }

}
