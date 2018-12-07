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
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.message.*;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.body.*;
import io.nuls.tools.log.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
/**
 * 消息工厂，用于组装消息
 * message  build factory
 * @author lan
 * @date 2018/11/01
 *
 */
public class MessageFactory {
    private static MessageFactory instance=new MessageFactory();
    private static final Map<String, Class<? extends BaseMessage>> MESSAGE_MAP = new HashMap<>();
    NodeGroupManager nodeGroupManager=NodeGroupManager.getInstance();
    private  MessageFactory(){

    }

    public static MessageFactory getInstance(){
        return instance;
    }

    public static void putMessage(Class<? extends BaseMessage> msgClass) {
        try {
            BaseMessage message = msgClass.getDeclaredConstructor().newInstance();
            MESSAGE_MAP.put(message.getHeader().getCommandStr(), msgClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class<? extends BaseMessage> getMessage(String command) {
        return MESSAGE_MAP.get(command);
    }

    public VersionMessage buildVersionMessage(Node node,long magicNumber){
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(magicNumber);
        VersionMessageBody versionMessageBody=new VersionMessageBody();
        try {

            InetAddress inetAddrYou=InetAddress.getByName(node.getIp());
            IpAddress addrYou=new IpAddress(inetAddrYou,node.getRemotePort());
            versionMessageBody.setAddrYou(addrYou);
            versionMessageBody.setPortYouCross(node.getRemoteCrossPort());
            IpAddress addrMe=LocalInfoManager.getInstance().getExternalAddress();
            versionMessageBody.setAddrMe(addrMe);
            versionMessageBody.setPortMeCross(NetworkParam.getInstance().getCrossPort());
            VersionMessage versionMessage=new VersionMessage(nodeGroup.getMagicNumber(),NetworkConstant.CMD_MESSAGE_VERSION,versionMessageBody);
            return versionMessage;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.error(e);
        }

        return null;

    }

    public VerackMessage buildVerackMessage(Node node,long magicNumber,int ackCode){
        VerackMessageBody verackMessageBody=new VerackMessageBody(ackCode);
        return new VerackMessage(magicNumber,NetworkConstant.CMD_MESSAGE_VERACK,verackMessageBody);
    }


    public ByeMessage buildByeMessage(Node node,long magicNumber,int byeCode){
        ByeMessageBody byeMessageBody=new ByeMessageBody(byeCode);
        return new ByeMessage(magicNumber,NetworkConstant.CMD_MESSAGE_BYE,byeMessageBody);
    }

    public GetAddrMessage buildGetAddrMessage(Node node,long magicNumber){
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(magicNumber);
        MessageBody messageBody=new MessageBody();
        return new GetAddrMessage(nodeGroup.getMagicNumber(),NetworkConstant.CMD_MESSAGE_GET_ADDR,messageBody);
    }

    /**
     * 判断连接是跨链还是自有网络，
     * 如果是跨链 走跨链： 返回所有自有网络 跨链端口节点。
     * @param node
     * @param magicNumber
     * @return AddrMessage
     *
     *
     */
    public AddrMessage buildAddrMessage(Node node,long magicNumber){
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(magicNumber);
        AddrMessageBody addrMessageBody=new AddrMessageBody();
        List<IpAddress> list=new ArrayList<>();
        Collection<Node> nodes=new ArrayList<>();
        nodes.addAll(nodeGroup.getConnectNodes());
        nodes.addAll(nodeGroup.getDisConnectNodes());

        for(Node peer:nodes){
            /**
             * 排除自身连接信息，比如组网A=====B，A向B请求地址，B给的地址列表需排除A地址。
             */
            if(peer.getIp().equals(node.getIp())){
                continue;
            }
            /**
             * 只有主动连接的节点地址才可使用。
             */
            if(Node.OUT == peer.getType()) {
                try {
                    int port=peer.getRemotePort();
                    if(peer.isCrossConnect()){
                        port=peer.getRemoteCrossPort();
                    }
                    InetAddress inetAddress = InetAddress.getByName(peer.getIp());
                    IpAddress ipAddress = new IpAddress(inetAddress,port);
                    list.add(ipAddress);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        addrMessageBody.setIpAddressList(list);
        return new AddrMessage(nodeGroup.getMagicNumber(),NetworkConstant.CMD_MESSAGE_ADDR,addrMessageBody);
    }

    /**
     * 主动构造地址消息：广播地址消息时使用
     * @param ipAddressList
     * @param magicNumber
     * @return
     */
    public AddrMessage buildAddrMessage(List<IpAddress> ipAddressList ,long magicNumber){
        AddrMessageBody addrMessageBody=new AddrMessageBody();
        addrMessageBody.setIpAddressList(ipAddressList);
        return new AddrMessage(magicNumber,NetworkConstant.CMD_MESSAGE_ADDR,addrMessageBody);
    }

    /**
     * 构造时间请求消息
     * @param magicNumber
     * @return
     */
    public GetTimeMessage buildTimeRequestMessage(long magicNumber,long messageId){
        GetTimeMessageBody messageBody=new GetTimeMessageBody();
        return new GetTimeMessage(magicNumber,NetworkConstant.CMD_MESSAGE_GET_TIME,messageBody);
    }
    /**
     * 构造时间应答消息
     * @param magicNumber
     * @return
     */
    public TimeMessage buildTimeResponseMessage(long magicNumber,long messageId){
        TimeMessageBody messageBody=new TimeMessageBody();
        messageBody.setTime(System.currentTimeMillis());
        return new TimeMessage(magicNumber,NetworkConstant.CMD_MESSAGE_RESPONSE_TIME,messageBody);
    }
}
