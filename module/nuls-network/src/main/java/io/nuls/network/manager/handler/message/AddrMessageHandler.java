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
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddressShare;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.body.AddrMessageBody;
import io.nuls.network.utils.IpUtil;
import io.nuls.network.utils.LoggerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
        int chainId = nodeGroup.getChainId();
//        LoggerUtil.logger(chainId).info("AddrMessageHandler Recieve:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        AddrMessage addrMessage = (AddrMessage) message;
        /*
         *空消息错误返回
         *Empty message error return
         */
        if (null == addrMessage.getMsgBody()) {
            LoggerUtil.logger(chainId).error("rec error addr message.");
            return NetworkEventResult.getResultFail(NetworkErrorCode.NET_MESSAGE_ERROR);
        }
        List<IpAddressShare> ipAddressList = addrMessage.getMsgBody().getIpAddressList();
        if (node.isCrossConnect()) {
            return crossNetRecieveMessage(ipAddressList, nodeGroup);

        } else {
            AddrMessageBody addrMessageBody = (AddrMessageBody) message.getMsgBody();
            if (addrMessageBody.getIsCross() == (byte) 1) {
                int getMessageChainId = addrMessageBody.getChainId();
                return crossNetRecieveMessage(ipAddressList, NodeGroupManager.getInstance().getNodeGroupByChainId(getMessageChainId));
            }
            return commonNetRecieveMessage(ipAddressList, nodeGroup, node);
        }

    }

    private boolean inValidateAddress(IpAddressShare ipAddress) {
        if (!IpUtil.isboolIp(ipAddress.getIpStr())) {
            return true;
        }
        if (IpUtil.isSelf(ipAddress.getIpStr())) {
            return true;
        }
        return false;
    }

    private NetworkEventResult commonNetRecieveMessage(List<IpAddressShare> ipAddressList, NodeGroup nodeGroup, Node node) {
        /*
         * 判断地址是否本地已经拥有，如果拥有不转发
         * Determine whether the address is already owned locally. If it does not forward
         */
        Map<String, Node> allNodes = nodeGroup.getLocalNetNodeContainer().getAllCanShareNodes();
        List<IpAddressShare> reShareAddrList = new ArrayList<>();
        int chainId = nodeGroup.getChainId();
        for (IpAddressShare ipAddress : ipAddressList) {
            if (inValidateAddress(ipAddress)) {
                continue;
            }
            LoggerUtil.logger(chainId).debug("add check node address ={}:{} crossPort={}", ipAddress.getIp().getHostAddress(), ipAddress.getPort(), ipAddress.getCrossPort());
            Node exsitNode = allNodes.get(ipAddress.getIpStr() + NetworkConstant.COLON + ipAddress.getPort());
            if (null != exsitNode) {
                if (ipAddress.getCrossPort() > 0 && 0 == exsitNode.getRemoteCrossPort()) {
                    exsitNode.setRemoteCrossPort(ipAddress.getCrossPort());
                    reShareAddrList.add(ipAddress);
                    nodeGroup.addCrossCheckNodes(ipAddress.getIp().getHostAddress(), ipAddress.getPort(), ipAddress.getCrossPort());
                    LoggerUtil.logger(chainId).debug("update  ip={},crossPort={}", ipAddress.getIp().getHostAddress(), ipAddress.getCrossPort());
                } else {
                    continue;
                }
            }
            nodeGroup.addNeedCheckNode(ipAddress.getIp().getHostAddress(), ipAddress.getPort(), ipAddress.getCrossPort(), node.isCrossConnect());
        }
        //有个特殊逻辑，之前的种子节点并没有跨链端口存在，此时分享的地址里含有了跨链端口信息，则需要补充进行新的广播
        if (reShareAddrList.size() > 0) {
            AddrMessage reSendAddrMessage = MessageFactory.getInstance().buildAddrMessage(reShareAddrList, nodeGroup.getMagicNumber(), nodeGroup.getChainId(), (byte) 0);
            LoggerUtil.logger(chainId).debug("reSendAddrMessage addrSize = {}", reShareAddrList.size());
            MessageManager.getInstance().broadcastNewAddr(reSendAddrMessage, node, false, true);
        }
        return NetworkEventResult.getResultSuccess();
    }

    /**
     * 跨链网络连接接收到地址
     *
     * @param ipAddressList
     * @param nodeGroup
     * @return
     */
    private NetworkEventResult crossNetRecieveMessage(List<IpAddressShare> ipAddressList, NodeGroup nodeGroup) {
        for (IpAddressShare ipAddress : ipAddressList) {
            if (inValidateAddress(ipAddress)) {
                continue;
            }
            LoggerUtil.logger(nodeGroup.getChainId()).debug("add check node address ={}:{} crossPort={}", ipAddress.getIp().getHostAddress(), ipAddress.getPort(), ipAddress.getCrossPort());
            nodeGroup.addNeedCheckNode(ipAddress.getIp().getHostAddress(), ipAddress.getPort(), ipAddress.getCrossPort(), true);
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
        LoggerUtil.logger(node.getNodeGroup().getChainId()).info("AddrMessageHandler Send:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        return super.send(message, node, asyn);
    }
}
