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

import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.po.NodePo;
import io.nuls.network.utils.IpUtil;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.network.utils.LoggerUtil.Log;


/**
 * address message handler
 *
 * @author lan
 * @date 2018/11/02
 */
public class AddrMessageHandler extends BaseMessageHandler {

    private static AddrMessageHandler instance = new AddrMessageHandler();

    private AddrMessageHandler() {

    }

    public static AddrMessageHandler getInstance() {
        return instance;
    }

    /**
     * @param message address message
     * @param node    peer node
     * @return NetworkEventResult
     * @description 接收消息处理
     * Receive message processing
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, Node node) {
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(message.getHeader().getMagicNumber());

        Log.debug("AddrMessageHandler Recieve:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        AddrMessage addrMessage = (AddrMessage) message;
        /*
         *空消息错误返回
         *Empty message error return
         */
        if (null == addrMessage.getMsgBody()) {
            Log.error("rec error addr message.");
            return NetworkEventResult.getResultFail(NetworkErrorCode.NET_MESSAGE_ERROR);
        }
        List<IpAddress> ipAddressList = addrMessage.getMsgBody().getIpAddressList();
        /*
         * 判断地址是否本地已经拥有，如果拥有不转发，PEER是跨链网络也不转发
         * Determine whether the address is already owned locally. If it does not forward, PEER is not a cross-chain network.
         */
        List<NodePo> addNodes = new ArrayList<>();
        List<IpAddress> addAddressList = new ArrayList<>();
        for (IpAddress ipAddress : ipAddressList) {
            if (!IpUtil.isboolIp(ipAddress.getIp().getHostAddress())) {
                continue;
            }
            nodeGroup.addNeedCheckNode(ipAddress, node.isCrossConnect());
        }
        return NetworkEventResult.getResultSuccess();
    }

    /**
     * AddrMessageHandler sending a message
     *
     * @param message address message
     * @param node    peer info
     * @param asyn    default true
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean asyn) {
        Log.debug("AddrMessageHandler Send:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        return super.send(message, node, asyn);
    }
}
