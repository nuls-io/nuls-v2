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

import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.constant.NodeStatusEnum;
import io.nuls.network.manager.*;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.manager.threads.TimeService;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.BestBlockInfo;
import io.nuls.network.model.message.VerackMessage;
import io.nuls.network.model.message.VersionMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.body.VerackMessageBody;
import io.nuls.network.model.message.body.VersionMessageBody;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.rpc.external.BlockRpcService;
import io.nuls.network.rpc.external.impl.BlockRpcServiceImpl;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;

import java.util.Map;

/**
 * version message handler
 * client 先发起version消息，server接收到后，
 * 1 是否自连接 2.校验业务连接重叠，连接过载
 *
 * @author lan
 * @date 2018/10/20
 */
public class VersionMessageHandler extends BaseMessageHandler {

    private static VersionMessageHandler instance = new VersionMessageHandler();
    private NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();

    private VersionMessageHandler() {

    }

    public static VersionMessageHandler getInstance() {
        return instance;
    }

    /**
     * 服务器被动连接规则
     * 1. 不超过最大被动连接数
     * 2. 如果自己已经主动连接了对方，不接受对方的被动连接
     * 3. 相同的IP的被动连接不超过n次
     *
     * @param ip
     * @param port
     * @return boolean
     */
    private boolean canConnectIn(NodesContainer nodesContainer, int maxInCount, int sameIpMaxCount, String ip, int port) {

        int size = nodesContainer.getConnectedCount(Node.IN);
        if (size >= maxInCount) {
            return false;
        }

        Map<String, Node> connectedNodes = nodesContainer.getConnectedNodes();

        int sameIpCount = 0;

        for (Node node : connectedNodes.values()) {
            //不会存在两次被动连接都是同一个端口的，即使是同一台服务器
            //if(ip.equals(node.getIp()) && (node.getPort().intValue() == port || node.getType() == Node.OUT)) {
            if (ip.equals(node.getIp()) && node.getType() == Node.OUT) {
                return false;
            }
            if (ip.equals(node.getIp())) {
                sameIpCount++;
            }
            if (sameIpCount >= sameIpMaxCount) {
                return false;
            }
        }

        return true;
    }

    /**
     * server recieve handler
     *
     * @param message message
     * @param node    Node
     */
    private void serverRecieveHandler(BaseMessage message, Node node) {
        VersionMessageBody versionBody = (VersionMessageBody) message.getMsgBody();
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(message.getHeader().getMagicNumber());
        String myIp = versionBody.getAddrYou().getIp().getHostAddress();
        int myPort = versionBody.getAddrYou().getPort();
        //设置magicNumber
        node.setMagicNumber(nodeGroup.getMagicNumber());
        String ip = node.getIp();
        node.setExternalIp(myIp);
        int maxIn;
        NodesContainer nodesContainer = null;
        int sameIpMaxCount = nodeGroup.getSameIpMaxCount(node.isCrossConnect());
        if (node.isCrossConnect()) {
            maxIn = nodeGroup.getMaxCrossIn();
            nodesContainer = nodeGroup.getCrossNodeContainer();
        } else {
            maxIn = nodeGroup.getMaxIn();
            nodesContainer = nodeGroup.getLocalNetNodeContainer();
        }

        if (!canConnectIn(nodesContainer, maxIn, sameIpMaxCount, node.getIp(), node.getRemotePort())) {
            node.getChannel().close();
            return;
        }
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
        nodesContainer.getConnectedNodes().put(node.getId(), node);
        nodesContainer.markCanuseNodeByIp(ip, NodeStatusEnum.AVAILABLE);

        //监听被动连接的断开
        node.setDisconnectListener(() -> {
            Log.info("------------in node disconnect:" + node.getId());
            if (node.isCrossConnect()) {
                nodeGroup.getCrossNodeContainer().getConnectedNodes().remove(node.getId());
                nodeGroup.getCrossNodeContainer().markCanuseNodeByIp(ip, NodeStatusEnum.CONNECTABLE);
            } else {
                nodeGroup.getLocalNetNodeContainer().getConnectedNodes().remove(node.getId());
                nodeGroup.getLocalNetNodeContainer().markCanuseNodeByIp(ip, NodeStatusEnum.CONNECTABLE);
            }

        });
        //存储需要的信息,协议版本信息，远程跨链端口信息
        node.setVersionProtocolInfos(versionBody.getProtocolVersion(), versionBody.getBlockHeight(), versionBody.getBlockHash());
        node.setRemoteCrossPort(versionBody.getPortMeCross());
        //回复version
        VersionMessage versionMessage = MessageFactory.getInstance().buildVersionMessage(node, message.getHeader().getMagicNumber());
        send(versionMessage, node, true);
    }

    /**
     * client recieve handler
     *
     * @param message message
     * @param node    Node
     */
    private void clientRecieveHandler(BaseMessage message, Node node) {
        VersionMessageBody versionBody = (VersionMessageBody) message.getMsgBody();
        String myIp = versionBody.getAddrYou().getIp().getHostAddress();
        int myPort = versionBody.getAddrYou().getPort();
        //设置magicNumber
        node.setExternalIp(myIp);
        //client发出version后获得，得到server回复，建立握手
//       Log.debug("VersionMessageHandler Recieve:Client"+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        //存储需要的信息
        node.setVersionProtocolInfos(versionBody.getProtocolVersion(), versionBody.getBlockHeight(), versionBody.getBlockHash());
        node.setRemoteCrossPort(versionBody.getPortMeCross());
        node.setConnectStatus(NodeConnectStatusEnum.AVAILABLE);
        node.setConnectTime(TimeService.currentTimeMillis());
        if (node.isCrossConnect()) {
            node.getNodeGroup().getCrossNodeContainer().setLatestHandshakeSuccTime(TimeService.currentTimeMillis());
        } else {
            node.getNodeGroup().getLocalNetNodeContainer().setLatestHandshakeSuccTime(TimeService.currentTimeMillis());
        }
        //client:接收到server端消息，进行verack答复
        VerackMessage verackMessage = MessageFactory.getInstance().buildVerackMessage(node, message.getHeader().getMagicNumber(), VerackMessageBody.VER_SUCCESS);
        MessageManager.getInstance().sendToNode(verackMessage, node, true);

    }

    /**
     * 接收消息处理
     * Receive message processing
     *
     * @param message message
     * @param node    Node
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, Node node) {
        if (Node.IN == node.getType()) {
            serverRecieveHandler(message, node);
        } else {
            clientRecieveHandler(message, node);
        }
        return NetworkEventResult.getResultSuccess();
    }

    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean asyn) {
//        Log.debug("VersionMessageHandler send:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        BlockRpcService blockRpcService = SpringLiteContext.getBean(BlockRpcServiceImpl.class);
        int chainId = NodeGroupManager.getInstance().getChainIdByMagicNum(message.getHeader().getMagicNumber());
        BestBlockInfo bestBlockInfo = blockRpcService.getBestBlockHeader(chainId);
        VersionMessage versionMessage = (VersionMessage) message;
        versionMessage.getMsgBody().setBlockHash(bestBlockInfo.getHash());
        versionMessage.getMsgBody().setBlockHeight(bestBlockInfo.getBlockHeight());
        return super.send(message, node, asyn);
    }
}
