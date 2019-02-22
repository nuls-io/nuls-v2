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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.manager.handler.message.GetAddrMessageHandler;
import io.nuls.network.manager.handler.message.OtherModuleMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.model.message.GetAddrMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.tools.crypto.Sha256Hash;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.nuls.network.utils.LoggerUtil.Log;

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
            e.printStackTrace();
            Log.error(e.getMessage());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 验证消息
     * validate message checkSum
     *
     * @param data data
     * @return boolean
     */
    private boolean validate(byte[] data, long pChecksum) {
        byte[] bodyHash = Sha256Hash.hashTwice(data);
        byte[] get4Byte = ByteUtils.subBytes(bodyHash, 0, 4);
        long checksum = ByteUtils.bytesToBigInteger(get4Byte).longValue();
        return checksum == pChecksum;
    }

    public void receiveMessage(ByteBuf buffer, Node node) throws NulsException {
        //统一接收消息处理
        try {
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
            MessageHeader header = new MessageHeader();
            int headerSize = header.size();
            byte[] payLoad = byteBuffer.getPayload();
            byte[] payLoadBody = ByteUtils.subBytes(payLoad, headerSize, payLoad.length - headerSize);
            byte[] headerByte = ByteUtils.copyOf(payLoad, headerSize);
            header.parse(headerByte, 0);
            if (!validate(payLoadBody, header.getChecksum())) {
                Log.error("validate  false ======================");
                return;
            }
            BaseMessage message = MessageManager.getInstance().getMessageInstance(header.getCommandStr());
            byteBuffer.setCursor(0);
            while (!byteBuffer.isFinished()) {
                Log.debug((node.isServer() ? "Server" : "Client") + ":----receive message-- magicNumber:" + header.getMagicNumber() + "==CMD:" + header.getCommandStr());
                NetworkEventResult result = null;
                if (null != message) {
                    Log.debug("==============================Network module self message");
                    message = byteBuffer.readNulsData(message);
                    BaseMeesageHandlerInf handler = MessageHandlerFactory.getInstance().getHandler(header.getCommandStr());
                    result = handler.recieve(message, node);
                    message = byteBuffer.readNulsData(message);
                } else {
                    //外部消息，转外部接口
                    LoggerUtil.blockLogs(header.getCommandStr(), node, payLoadBody, "received");
                    Log.debug("==============================receive other module message, hash-" + NulsDigestData.calcDigestData(payLoadBody).getDigestHex() + "node-" + node.getId());
                    OtherModuleMessageHandler handler = MessageHandlerFactory.getInstance().getOtherModuleHandler();
                    result = handler.recieve(header, payLoadBody, node);
                    Log.debug("s=={}==={}", byteBuffer.getPayload().length, byteBuffer.getCursor());
                    byteBuffer.setCursor(payLoad.length);
                    Log.debug("e=={}==={}", byteBuffer.getPayload().length, byteBuffer.getCursor());
                }
                if (!result.isSuccess()) {
                    Log.error("receiveMessage deal fail:" + result.getErrorCode().getMsg());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            throw new NulsException(NetworkErrorCode.DATA_ERROR, e);
        } finally {
            buffer.clear();
        }
    }


    public NetworkEventResult broadcastSelfAddrToAllNode(Collection<Node> connectNodes, IpAddress ipAddress, boolean asyn) {
        for (Node connectNode : connectNodes) {
            if (NodeConnectStatusEnum.AVAILABLE == connectNode.getConnectStatus()) {
                List<IpAddress> addressesList = new ArrayList<>();
                addressesList.add(ipAddress);
                AddrMessage addrMessage = MessageFactory.getInstance().buildAddrMessage(addressesList, connectNode.getMagicNumber());
                Log.debug("broadcastSelfAddrToAllNode================" + addrMessage.getMsgBody().size() + "==getIpAddressList()==" + addrMessage.getMsgBody().getIpAddressList().size());
                this.sendToNode(addrMessage, connectNode, asyn);
            }
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }

    /**
     * 发送请求地址消息
     *
     * @param nodeGroup NodeGroup
     * @param isCross   boolean
     * @param asyn      boolean
     */
    public boolean sendGetAddrMessage(NodeGroup nodeGroup, boolean isCross, boolean asyn) {

        if (isCross) {
            //get Cross nodes
            Collection<Node> nodes = nodeGroup.getCrossNodeContainer().getConnectedNodes().values();
            for (Node node : nodes) {
                if (NodeConnectStatusEnum.AVAILABLE == node.getConnectStatus()) {
                    GetAddrMessage getAddrMessage = MessageFactory.getInstance().buildGetAddrMessage(node, nodeGroup.getMagicNumber());
                    GetAddrMessageHandler.getInstance().send(getAddrMessage, node, true);
                    return true;
                }
            }
        } else {
            //get self seed
            Collection<Node> nodes = nodeGroup.getLocalNetNodeContainer().getConnectedNodes().values();
            for (Node node : nodes) {
                if (NodeConnectStatusEnum.AVAILABLE == node.getConnectStatus()) {
                    GetAddrMessage getAddrMessage = MessageFactory.getInstance().buildGetAddrMessage(node, nodeGroup.getMagicNumber());
                    GetAddrMessageHandler.getInstance().send(getAddrMessage, node, true);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 广播消息到所有节点，排除特定节点
     *
     * @param addrMessage
     * @param excludeNode
     * @param asyn
     * @return
     */
    public NetworkEventResult broadcastToAllNode(BaseMessage addrMessage, Node excludeNode, boolean isCross, boolean asyn) {
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(addrMessage.getHeader().getMagicNumber());
        Collection<Node> connectNodes = null;
        if (isCross) {
            connectNodes = nodeGroup.getCrossNodeContainer().getConnectedNodes().values();
        } else {
            connectNodes = nodeGroup.getLocalNetNodeContainer().getConnectedNodes().values();
        }
        if (null != connectNodes && connectNodes.size() > 0) {
            for (Node connectNode : connectNodes) {
                if (null != excludeNode && connectNode.getId().equals(excludeNode.getId())) {
                    continue;
                }
                this.sendToNode(addrMessage, connectNode, asyn);
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
            ChannelFuture future = node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
            if (!asyn) {
                future.await();
                Log.debug("{}==================ChannelFuture1", TimeManager.currentTimeMillis());
                if (!future.isSuccess()) {
                    return new NetworkEventResult(false, NetworkErrorCode.NET_BROADCAST_FAIL);
                }
                Log.debug("{}==================ChannelFuture2", TimeManager.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
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
    public NetworkEventResult broadcastToNodes(byte[] message, List<Node> nodes, boolean asyn) {
        Log.debug("{}==================broadcastToNodes begin", TimeManager.currentTimeMillis());
        for (Node node : nodes) {
            Log.debug("==================node {}", node.getId());
            if (node.getChannel() == null || !node.getChannel().isActive()) {
                Log.info(node.getId() + "is inActive");
                continue;
            }
            try {
                ChannelFuture future = node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(message));
                Log.debug("==================writeAndFlush end");
                if (!asyn) {
                    Log.debug("{}==========B========ChannelFuture", TimeManager.currentTimeMillis());
                    future.await();
                    boolean success = future.isSuccess();
                    Log.debug("==========B========{}success?{}", node.getId(), success);
                    if (!success) {
                        return new NetworkEventResult(false, NetworkErrorCode.NET_BROADCAST_FAIL);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.error(e.getMessage());
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
}
