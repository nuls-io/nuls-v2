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

import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.netty.container.NodesContainer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static io.nuls.network.utils.LoggerUtil.Log;

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
        Log.debug("GetAddrMessageHandler Recieve:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        //发送addr消息
        List<IpAddress> ipAddresses = getAvailableNodes(node);
        AddrMessage addressMessage = MessageFactory.getInstance().buildAddrMessage(ipAddresses, message.getHeader().getMagicNumber());
        if (0 == addressMessage.getMsgBody().getIpAddressList().size()) {
            Log.info("No Address");
        } else {
            MessageManager.getInstance().sendToNode(addressMessage, node, true);
        }
        return NetworkEventResult.getResultSuccess();
    }

    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean asyn) {
        Log.debug("GetAddrMessageHandler Send:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        return super.send(message, node, asyn);
    }

    private List<IpAddress> getAvailableNodes(Node node) {
        NodeGroup nodeGroup = null;
        List<IpAddress> addressList = new ArrayList<>();
        if (NetworkParam.getInstance().isMoonNode()) {
            //是主网节点，回复
            //从跨链连接过来的请求
            nodeGroup = NodeGroupManager.getInstance().getMoonMainNet();
        } else {
            nodeGroup = node.getNodeGroup();
        }
        if (null == nodeGroup) {
            return addressList;
        } else {
            Collection<Node> nodes = nodeGroup.getLocalNetNodeContainer().getConnectedNodes().values();
            nodes.addAll(nodeGroup.getLocalNetNodeContainer().getCanConnectNodes().values());
            addAddress(nodes, addressList, node.getIp(), node.isCrossConnect());
        }
        return addressList;

    }

    private void addAddress(Collection<Node> nodes, List<IpAddress> list, String fromIp, boolean isCross) {
        for (Node peer : nodes) {
            /*
             * 排除自身连接信息，比如组网A=====B，A向B请求地址，B给的地址列表需排除A地址。
             * Exclude self-connection information, such as networking A=====B,
             * A requests an address from B, and the address list given by B excludes the A address.
             */
            if (peer.getIp().equals(fromIp)) {
                continue;
            }
            /*
             * 只有主动连接的节点地址才可使用。
             * Only active node addresses are available for use.
             */
            if (Node.OUT == peer.getType()) {
                try {
                    int port = peer.getRemotePort();
                    if (isCross) {
                        port = peer.getRemoteCrossPort();
                    }
                    InetAddress inetAddress = InetAddress.getByName(peer.getIp());
                    IpAddress ipAddress = new IpAddress(inetAddress, port);
                    list.add(ipAddress);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
