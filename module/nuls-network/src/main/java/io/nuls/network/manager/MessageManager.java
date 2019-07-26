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
package io.nuls.network.manager;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.network.constant.ManagerStatusEnum;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.manager.handler.message.OtherModuleMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddressShare;
import io.nuls.network.model.dto.PeerCacheMessage;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.model.message.GetAddrMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.network.utils.MessageTestUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 消息管理器，用于收发消息
 * message manager：  send and rec msg
 *
 * @author lan
 * @date 2018/11/01
 */
public class MessageManager extends BaseManager {

    private static MessageManager instance = new MessageManager();

    public static MessageManager getInstance() {
        return instance;
    }

    public void sendToNode(BaseMessage message, Node node, boolean aysn) {
        //向节点发送消息
        broadcastToANode(message, node, aysn);

    }

    public void sendHandlerMsg(BaseMessage message, Node node, boolean aysn) {
        BaseMeesageHandlerInf handler = MessageHandlerFactory.getInstance().getHandler(message.getHeader().getCommandStr());
        handler.send(message, node, aysn);
    }

    /**
     * protocol message  checkSum cal
     *
     * @param msgBody msgBody
     * @return long
     */
    public long getCheckSum(byte[] msgBody) {
        byte[] bodyHash = Sha256Hash.hashTwice(msgBody);
        byte[] get4Byte = ByteUtils.subBytes(bodyHash, 0, 4);
        return ByteUtils.bytesToBigInteger(get4Byte).longValue();
    }

    private BaseMessage getMessageInstance(String command) {
        Class<? extends BaseMessage> msgClass = MessageFactory.getMessage(command);
        if (null == msgClass) {
            return null;
        }
        try {
            return msgClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Log.error(e);
        } catch (NoSuchMethodException e) {
            Log.error(e);
        } catch (InvocationTargetException e) {
            Log.error(e);
        }

        return null;
    }


    /**
     * 验证消息
     * validate message checkSum
     *
     * @param data entity
     * @return boolean
     */
    private boolean validate(byte[] data, long pChecksum) {
        byte[] bodyHash = Sha256Hash.hashTwice(data);
        byte[] get4Byte = ByteUtils.subBytes(bodyHash, 0, 4);
        long checksum = ByteUtils.bytesToBigInteger(get4Byte).longValue();
        return checksum == pChecksum;
    }

    public void receiveMessage(NulsByteBuffer byteBuffer, Node node) throws NulsException {
        //统一接收消息处理
        try {
            if (null == byteBuffer) {
                return;
            }
            MessageHeader header = new MessageHeader();
            int headerSize = header.size();
            byte[] payLoad = byteBuffer.getPayload();
            byte[] payLoadBody = ByteUtils.subBytes(payLoad, headerSize, payLoad.length - headerSize);
            byte[] headerByte = ByteUtils.copyOf(payLoad, headerSize);
            int chainId = NodeGroupManager.getInstance().getChainIdByMagicNum(header.getMagicNumber());
            header.parse(headerByte, 0);
            if (!validate(payLoadBody, header.getChecksum())) {
                LoggerUtil.logger(chainId).error("validate  false ======================cmd:{}", header.getCommandStr());
                return;
            }
            BaseMessage message = MessageManager.getInstance().getMessageInstance(header.getCommandStr());
            byteBuffer.setCursor(0);
            while (!byteBuffer.isFinished()) {
                NetworkEventResult result = null;
                if (null != message) {
                    message = byteBuffer.readNulsData(message);
                    BaseMeesageHandlerInf handler = MessageHandlerFactory.getInstance().getHandler(header.getCommandStr());
                    result = handler.recieve(message, node);
                } else {
                    //外部消息，转外部接口
                    OtherModuleMessageHandler handler = MessageHandlerFactory.getInstance().getOtherModuleHandler();
                    result = handler.recieve(header, payLoadBody, node);
                    byteBuffer.setCursor(payLoad.length);
                }
                if (!result.isSuccess()) {
                    LoggerUtil.logger(chainId).error("receiveMessage deal fail:" + result.getErrorCode().getMsg());
                }
            }
        } catch (Exception e) {
            Log.error("node={},{}", node.getId(), e);
        }
    }


    public NetworkEventResult broadcastSelfAddrToAllNode(Collection<Node> connectNodes, IpAddressShare ipAddress, boolean isCrossAddress, boolean asyn) {
        for (Node connectNode : connectNodes) {
            List<IpAddressShare> addressesList = new ArrayList<>();
            addressesList.add(ipAddress);
            AddrMessage addrMessage = MessageFactory.getInstance().buildAddrMessage(addressesList, connectNode.getMagicNumber(), connectNode.getNodeGroup().getChainId(), isCrossAddress ? (byte) 1 : (byte) 0);
            LoggerUtil.logger(connectNode.getNodeGroup().getChainId()).info("broadcastSelfAddrToAllNode===node={}", connectNode.getId());
            this.sendToNode(addrMessage, connectNode, asyn);
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }

    /**
     * 发送请求地址消息
     *
     * @param nodeGroup      NodeGroup
     * @param isConnectCross boolean
     * @param isCrossAddress boolean
     * @param asyn           boolean
     */
    public void sendGetAddressMessage(NodeGroup nodeGroup, boolean isConnectCross, boolean isCrossAddress, boolean asyn) {
        LoggerUtil.logger(nodeGroup.getChainId()).info("sendGetAddrMessage chainId={},isCross={}", nodeGroup.getChainId(), isConnectCross);
        List<Node> nodes = new ArrayList<>();
        if (isConnectCross) {
            nodes.addAll(nodeGroup.getCrossNodeContainer().getConnectedNodes().values());
        } else {
            nodes.addAll(nodeGroup.getLocalNetNodeContainer().getConnectedNodes().values());
        }
        for (Node node : nodes) {
            if (NodeConnectStatusEnum.AVAILABLE == node.getConnectStatus()) {
                GetAddrMessage getAddrMessage = MessageFactory.getInstance()
                        .buildGetAddrMessage(nodeGroup, isCrossAddress);
                sendHandlerMsg(getAddrMessage, node, asyn);
            }
        }
    }

    /**
     * 通过本地网络传递获取跨链网络地址
     *
     * @param connectNodeGroup
     * @param messageNodeGroup
     * @param isConnectCross
     * @param isCrossAddress
     * @param asyn
     */
    public void sendGetCrossAddressMessage(NodeGroup connectNodeGroup, NodeGroup messageNodeGroup, boolean isConnectCross, boolean isCrossAddress, boolean asyn) {
        LoggerUtil.logger(connectNodeGroup.getChainId()).info("sendGetAddrMessage chainId={},isCross={},getCrossAddress={}", connectNodeGroup.getChainId(), isConnectCross, isCrossAddress);
        List<Node> nodes = new ArrayList<>();
        if (isConnectCross) {
            nodes.addAll(connectNodeGroup.getCrossNodeContainer().getConnectedNodes().values());
        } else {
            nodes.addAll(connectNodeGroup.getLocalNetNodeContainer().getConnectedNodes().values());
        }
        for (Node node : nodes) {
            if (NodeConnectStatusEnum.AVAILABLE == node.getConnectStatus()) {
                GetAddrMessage getAddrMessage = MessageFactory.getInstance()
                        .buildGetAddrMessage(messageNodeGroup, isCrossAddress);
                sendHandlerMsg(getAddrMessage, node, asyn);
            }
        }
    }

    /**
     * 广播消息到所有节点，排除特定节点
     *
     * @param message
     * @param excludeNode
     * @param asyn
     * @return
     */
    public NetworkEventResult broadcastToAllNode(BaseMessage message, Node excludeNode, boolean isCross, boolean asyn) {
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(message.getHeader().getMagicNumber());
        Collection<Node> connectNodes = null;
        if (isCross) {
            //跨链节点，不做广播分享
            return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
//            connectNodes = nodeGroup.getCrossNodeContainer().getConnectedNodes().values();
        } else {
            connectNodes = nodeGroup.getLocalNetNodeContainer().getConnectedNodes().values();
        }
        if (null != connectNodes && connectNodes.size() > 0) {
            for (Node connectNode : connectNodes) {
                if (null != excludeNode && connectNode.getId().equals(excludeNode.getId())) {
                    continue;
                }
                this.sendToNode(message, connectNode, asyn);
            }
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }

    public NetworkEventResult broadcastNewAddr(BaseMessage message, Node excludeNode, boolean isCross, boolean asyn) {
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(message.getHeader().getMagicNumber());
        List<Node> connectNodes = null;
        if (isCross) {
            connectNodes = nodeGroup.getCrossNodeContainer().getAvailableNodes();
        } else {
            connectNodes = nodeGroup.getLocalNetNodeContainer().getAvailableNodes();
        }
        if (null != connectNodes && connectNodes.size() > 0) {
            for (Node connectNode : connectNodes) {
                if (null != excludeNode && connectNode.getId().equals(excludeNode.getId())) {
                    continue;
                }
                this.sendHandlerMsg(message, connectNode, asyn);
            }
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }

    /**
     * 判断是否是握手消息
     * isHandShakeMessage?
     *
     * @param message
     * @return
     */
    private boolean isHandShakeMessage(BaseMessage message) {
        return (message.getHeader().getCommandStr().equals(NetworkConstant.CMD_MESSAGE_VERSION) || message.getHeader().getCommandStr().equals(NetworkConstant.CMD_MESSAGE_VERACK));

    }

    private NetworkEventResult broadcastToANode(BaseMessage message, Node node, boolean asyn) {
        /*
         *not handShakeMessage must be  validate peer status
         */
        if (!isHandShakeMessage(message)) {
            if (NodeConnectStatusEnum.AVAILABLE != node.getConnectStatus()) {
                Log.error("============={} status is not handshake(AVAILABLE)", node.getId());
                return new NetworkEventResult(false, NetworkErrorCode.NET_NODE_DEAD);
            }
        }
        if (node.getChannel() == null || !node.getChannel().isActive()) {
            Log.error("============={} getChannel is not Active", node.getId());
            return new NetworkEventResult(false, NetworkErrorCode.NET_NODE_MISS_CHANNEL);
        }
        try {
            MessageHeader header = message.getHeader();
            BaseNulsData body = message.getMsgBody();
            header.setPayloadLength(body.size());
            if (asyn) {
                node.getChannel().eventLoop().execute(() -> {
                    Channel channel = node.getChannel();
                    if (channel != null) {
                        try {
                            if (!channel.isWritable()) {
                                LoggerUtil.COMMON_LOG.error("#### isWritable=false,send fail.node={},cmd={}", node.getId(), header.getCommandStr());

                            }
                            channel.writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
                        } catch (IOException e) {
                            LoggerUtil.COMMON_LOG.error(e);
                        }
                    }

                });
            } else {
                ChannelFuture future = node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
                future.await();
                boolean success = future.isSuccess();
                if (!success) {
                    return new NetworkEventResult(false, NetworkErrorCode.NET_BROADCAST_FAIL);
                }
            }

        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return new NetworkEventResult(false, NetworkErrorCode.NET_MESSAGE_ERROR);
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }


    /**
     * broadcast message to nodes
     *
     * @param message
     * @param nodes
     * @param asyn
     * @return
     */
    public NetworkEventResult broadcastToNodes(byte[] message, String cmd, List<Node> nodes, boolean asyn, int percent) {
        if (nodes.size() > NetworkConstant.MIN_PEER_NUMBER && percent < NetworkConstant.FULL_BROADCAST_PERCENT) {
            Collections.shuffle(nodes);
            double d = BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(NetworkConstant.FULL_BROADCAST_PERCENT), 2, RoundingMode.HALF_DOWN).doubleValue();
            int toIndex = (int) (nodes.size() * d);
            nodes = nodes.subList(0, toIndex);
        }
        for (Node node : nodes) {
            if (node.getChannel() == null || !node.getChannel().isActive()) {
                Log.info("broadcastToNodes node={} is not Active", node.getId());
                continue;
            }
            try {
                if (asyn) {
                    node.getChannel().eventLoop().execute(() -> {
                        Channel channel = node.getChannel();
                        if (channel != null) {
                            if (!channel.isWritable()) {
                                if (!MessageTestUtil.isLowerLeverCmd(cmd)) {
                                    LoggerUtil.COMMON_LOG.debug("#### isWritable=false,node={},cmd={} add to cache", node.getId(), cmd);
                                    node.getCacheSendMsgQueue().addLast(new PeerCacheMessage(message));
                                } else {
                                    LoggerUtil.COMMON_LOG.debug("#### isWritable=false,node={},cmd={} send to peer is drop", node.getId(), cmd);
                                }
                            } else {
                                channel.writeAndFlush(Unpooled.wrappedBuffer(message));
                            }
                        }
                    });
                } else {
                    ChannelFuture future = node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(message));
                    future.await();
                    boolean success = future.isSuccess();
                    if (!success) {
                        return new NetworkEventResult(false, NetworkErrorCode.NET_BROADCAST_FAIL);
                    }
                }
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }

    @Override
    public void init() throws Exception {
        MessageFactory.getInstance().init();
        MessageHandlerFactory.getInstance().init();

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void change(ManagerStatusEnum toStatus) throws Exception {

    }
}
