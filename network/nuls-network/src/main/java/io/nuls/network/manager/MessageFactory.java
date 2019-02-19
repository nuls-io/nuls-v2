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

import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.manager.handler.message.*;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.message.*;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.body.*;
import io.nuls.network.netty.container.NodesContainer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * 消息工厂，用于组装消息
 * message  build factory
 *
 * @author lan
 * @date 2018/11/01
 */
public class MessageFactory {
    private static MessageFactory instance = new MessageFactory();
    private static final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = new HashMap<>();
    private NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();

    private MessageFactory() {

    }

    public static MessageFactory getInstance() {
        return instance;
    }

    void init() {
        MessageFactory.putMessage(VersionMessage.class, VersionMessageHandler.getInstance());
        MessageFactory.putMessage(VerackMessage.class, VerackMessageHandler.getInstance());
        MessageFactory.putMessage(GetAddrMessage.class, GetAddrMessageHandler.getInstance());
        MessageFactory.putMessage(AddrMessage.class, AddrMessageHandler.getInstance());
        MessageFactory.putMessage(GetTimeMessage.class, GetTimeMessageHandler.getInstance());
        MessageFactory.putMessage(TimeMessage.class, TimeMessageHandler.getInstance());
    }

    /**
     * putMessage
     *
     * @param msgClass BaseMessage
     */
    static void putMessage(Class<? extends BaseMessage> msgClass, BaseMeesageHandlerInf handlerInf) {
        try {
            BaseMessage message = msgClass.getDeclaredConstructor().newInstance();
            MESSAGE_MAP.put(message.getHeader().getCommandStr(), msgClass);
            MessageHandlerFactory.addHandler(message.getHeader().getCommandStr(), handlerInf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param command String
     * @return BaseMessage
     */
    static Class<? extends BaseMessage> getMessage(String command) {
        return MESSAGE_MAP.get(command);
    }

    /**
     * @param node        peer connection
     * @param magicNumber net id
     * @return VersionMessage
     */
    public VersionMessage buildVersionMessage(Node node, long magicNumber) {
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(magicNumber);
        VersionMessageBody versionMessageBody = new VersionMessageBody();
        try {

            InetAddress inetAddrYou = InetAddress.getByName(node.getIp());
            IpAddress addrYou = new IpAddress(inetAddrYou, node.getRemotePort());
            versionMessageBody.setAddrYou(addrYou);
            versionMessageBody.setPortYouCross(node.getRemoteCrossPort());
            int localPort = 0;
            if (node.isCrossConnect()) {
                localPort = NetworkParam.getInstance().getCrossPort();
            } else {
                localPort = NetworkParam.getInstance().getPort();
            }
            IpAddress addrMe = new IpAddress(NetworkParam.getInstance().getExternalIp(), localPort);
            versionMessageBody.setAddrMe(addrMe);
            versionMessageBody.setPortMeCross(NetworkParam.getInstance().getCrossPort());
            return new VersionMessage(nodeGroup.getMagicNumber(), NetworkConstant.CMD_MESSAGE_VERSION, versionMessageBody);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }

        return null;

    }

    /**
     * @param node        peer connection
     * @param magicNumber net id
     * @param ackCode     ack code
     * @return VerackMessage
     */
    public VerackMessage buildVerackMessage(Node node, long magicNumber, int ackCode) {
        VerackMessageBody verackMessageBody = new VerackMessageBody(ackCode);
        return new VerackMessage(magicNumber, NetworkConstant.CMD_MESSAGE_VERACK, verackMessageBody);
    }


    /**
     * @param node        peer connection
     * @param magicNumber net id
     * @return GetAddrMessage
     */
    GetAddrMessage buildGetAddrMessage(Node node, long magicNumber) {
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(magicNumber);
        MessageBody messageBody = new MessageBody();
        return new GetAddrMessage(nodeGroup.getMagicNumber(), NetworkConstant.CMD_MESSAGE_GET_ADDR, messageBody);
    }


    /**
     * 主动构造地址消息：广播地址消息时使用
     * Actively constructing address messages: used when broadcasting address messages
     *
     * @param ipAddressList ip set
     * @param magicNumber   net id
     * @return AddrMessage
     */
    public AddrMessage buildAddrMessage(List<IpAddress> ipAddressList, long magicNumber) {
        AddrMessageBody addrMessageBody = new AddrMessageBody();
        addrMessageBody.setIpAddressList(ipAddressList);
        return new AddrMessage(magicNumber, NetworkConstant.CMD_MESSAGE_ADDR, addrMessageBody);
    }

    /**
     * 构造时间请求消息
     *
     * @param magicNumber net id
     * @param messageId   messageId
     * @return GetTimeMessage
     */
    public GetTimeMessage buildTimeRequestMessage(long magicNumber, long messageId) {
        GetTimeMessageBody messageBody = new GetTimeMessageBody();
        messageBody.setMessageId(messageId);
        return new GetTimeMessage(magicNumber, NetworkConstant.CMD_MESSAGE_GET_TIME, messageBody);
    }

    /**
     * 构造时间应答消息
     *
     * @param magicNumber net id
     * @param messageId   messageId
     * @return TimeMessage
     */
    public TimeMessage buildTimeResponseMessage(long magicNumber, long messageId) {
        TimeMessageBody messageBody = new TimeMessageBody();
        messageBody.setMessageId(messageId);
        messageBody.setTime(System.currentTimeMillis());
        return new TimeMessage(magicNumber, NetworkConstant.CMD_MESSAGE_RESPONSE_TIME, messageBody);
    }
}
