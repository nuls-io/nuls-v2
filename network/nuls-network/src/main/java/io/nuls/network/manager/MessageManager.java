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
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.manager.handler.NetworkMessageHandlerFactory;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.message.*;

import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 消息管理器，用于收发消息
 * message manager：  send and rec msg
 * @author lan
 * @date 2018/11/01
 *
 */
public class MessageManager extends BaseManager{

    private static MessageManager instance = new MessageManager();
    public static MessageManager getInstance(){
        return instance;
    }
    public void  sendToNode(BaseMessage message, Node node, boolean aysn) {
        //向节点发送消息
        broadcastToANode(message,node,aysn);

    }

    public  BaseMessage getMessageInstance(String command) {
        Class<? extends BaseMessage> msgClass  = MessageFactory.getMessage(command);
        if (null == msgClass) {
            return null;
        }
        try {
            BaseMessage  message = msgClass.getDeclaredConstructor().newInstance();
            return message;
        } catch (InstantiationException e) {
            Log.error(e);

        } catch (IllegalAccessException e) {
            Log.error(e);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 验证消息
     * @param data
     * @return
     */
    public boolean validate(BaseMessage data){
        if (data.getHeader() == null) {
            Log.error("NET_MESSAGE_ERROR");
            return false;
        }
        Log.info("data.getHeader().getPayloadLength()="+data.getHeader().getPayloadLength());
        Log.info("data.getMsgBody()="+(data.getMsgBody()== null));
        if(data.getHeader().getPayloadLength()== 4 && data.getMsgBody() == null){
            //ok
            return true;
        } else if (data.getHeader().getPayloadLength() != data.getMsgBody().size()) {
            Log.error("NET_MESSAGE_LENGTH_ERROR");
            return false;
        }

         if(!data.isCheckSumValid()){
                Log.error("NET_MESSAGE_CHECKSUM_ERROR");
                return false;
            }
        return true;
    }
    public void receiveMessage(ByteBuf buffer,String nodeKey,boolean isServer) throws NulsException {
        //统一接收消息处理
        try {
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
            while (!byteBuffer.isFinished()) {
                MessageHeader header = byteBuffer.readNulsData(new MessageHeader());
                Log.debug((isServer?"Server":"Client")+":----receive message-- magicNumber:"+ header.getMagicNumber()+"==CMD:"+header.getCommandStr());
                BaseMessage message=MessageManager.getInstance().getMessageInstance(header.getCommandStr());
                byteBuffer.setCursor(byteBuffer.getCursor() - header.size());
                if(null != message) {
                    BaseMeesageHandlerInf handler = NetworkMessageHandlerFactory.getInstance().getHandler(message);
                    if(header.getCommandStr().equals(NetworkConstant.CMD_MESSAGE_ADDR)){
                        Log.info("================go ADDR===============");
                        message = byteBuffer.readNulsData((AddrMessage)message);
                    }else {
                        message = byteBuffer.readNulsData(message);
                    }
                    if (!validate(message)) {
                        return;
                    } else {
                        handler.recieve(message, nodeKey, isServer);
                    }
                }else{
                    //外部消息，转外部接口
                    long magicNum=header.getMagicNumber();
                    int chainId=NodeGroupManager.getInstance().getChainIdByMagicNum(magicNum);
                    int msgSize = (int)(header.size()+header.getPayloadLength());
                    String response = CmdDispatcher.call(header.getCommandStr(), new Object[]{chainId,nodeKey,byteBuffer.readBytes(msgSize)},1.0 );
                    Log.info(response);
                }
               }

        } catch (Exception e) {
            e.printStackTrace();
            throw new NulsException(NetworkErrorCode.DATA_ERROR, e);
        } finally {
            buffer.clear();
        }
    }

    public NetworkEventResult broadcastSelfAddrToAllNode(boolean asyn) {
        if(LocalInfoManager.getInstance().isAddrBroadcast()){
            return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
        }
        List<Node> connectNodes= ConnectionManager.getInstance().getCacheAllNodeList();
        for(Node connectNode:connectNodes){
            List<NodeGroupConnector> nodeGroupConnectors=connectNode.getNodeGroupConnectors();
            for(NodeGroupConnector nodeGroupConnector:nodeGroupConnectors){
                if(Node.HANDSHAKE == nodeGroupConnector.getStatus()){
                    List<IpAddress> addressesList=new ArrayList<>();
                    addressesList.add(LocalInfoManager.getInstance().getExternalAddress());
                    AddrMessage addrMessage= MessageFactory.getInstance().buildAddrMessage(addressesList,nodeGroupConnector.getMagicNumber());
                    Log.info("broadcastSelfAddrToAllNode================"+addrMessage.getMsgBody().size()+"==getIpAddressList()=="+addrMessage.getMsgBody().getIpAddressList().size());
                    this.sendToNode(addrMessage,connectNode,asyn);
                }

            }
        }

        if(connectNodes.size() > 0) {
            //已经广播
            LocalInfoManager.getInstance().setAddrBroadcast(true);
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }

    /**
     * 发送请求地址消息
     * @param magicNumber
     * @param asyn
     */
    public boolean sendGetAddrMessage(long magicNumber,boolean isCross,boolean asyn) {
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(magicNumber);
        if(isCross){
            //get Cross Seed
            List<String> seeds = NetworkParam.getInstance().getMoonSeedIpList();
            for(String seed : seeds)
            {
                Node node = nodeGroup.getConnectCrossNode(seed);
                if(null != node){
                     GetAddrMessage getAddrMessage = MessageFactory.getInstance().buildGetAddrMessage(node,magicNumber);
                     this.sendToNode(getAddrMessage,node,true);
                     return true;
                }
            }
        }else{
            //get self seed
            List<String> seeds = NetworkParam.getInstance().getSeedIpList();
            for(String seed : seeds)
            {
                Node node = nodeGroup.getConnectNode(seed);
                if(null != node){
                    GetAddrMessage getAddrMessage = MessageFactory.getInstance().buildGetAddrMessage(node,magicNumber);
                    this.sendToNode(getAddrMessage,node,true);
                    return true;
                }
            }
        }
        return false;
    }
    public NetworkEventResult broadcastAddrToAllNode(BaseMessage addrMessage, Node excludeNode,boolean asyn) {
         NodeGroup nodeGroup=NodeGroupManager.getInstance().getNodeGroupByMagic(addrMessage.getHeader().getMagicNumber());
        Collection<Node> connectNodes=nodeGroup.getConnectNodes();
        if(null != connectNodes && connectNodes.size()>0){
            for(Node connectNode:connectNodes){
                if(connectNode.getId().equals(excludeNode.getId())){
                    continue;
                }
                this.sendToNode(addrMessage,connectNode,asyn);
            }
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }
    private  boolean isHandShakeMessage(BaseMessage message){
        if(message.getHeader().getCommandStr().equals(NetworkConstant.CMD_MESSAGE_VERSION) || message.getHeader().getCommandStr().equals(NetworkConstant.CMD_MESSAGE_VERACK)){
            return true;
        }
        return false;
    }
    public NetworkEventResult broadcastToANode(BaseMessage message, Node node, boolean asyn) {
//        not handShakeMessage must be  validate peer status
        if(!isHandShakeMessage(message)) {
            NodeGroupConnector nodeGroupConnector = node.getNodeGroupConnector(message.getHeader().getMagicNumber());
            if (Node.HANDSHAKE != nodeGroupConnector.getStatus()) {
                return new NetworkEventResult(false, NetworkErrorCode.NET_NODE_DEAD);
            }
        }
        if (node.getChannel() == null || !node.getChannel().isActive()) {
            return new NetworkEventResult(false, NetworkErrorCode.NET_NODE_MISS_CHANNEL);
        }
        try {
            MessageHeader header = message.getHeader();
            BaseNulsData body = message.getMsgBody();
            header.setPayloadLength(body.size());
            ChannelFuture future = node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
            if (!asyn) {
                future.await();
                boolean success = future.isSuccess();
                if (!success) {
                    return new NetworkEventResult(false, NetworkErrorCode.NET_BROADCAST_FAIL);
                }
            }
        } catch (Exception e) {
            Log.error(e);
            return new NetworkEventResult(false, NetworkErrorCode.NET_MESSAGE_ERROR);
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }
    public NetworkEventResult broadcastToNodes(byte[] message, List<Node> nodes, boolean asyn) {
        for(Node node:nodes) {
            if (node.getChannel() == null || !node.getChannel().isActive()) {
                Log.info(node.getId() + "is inActive");
            }
            try {
                ChannelFuture future = node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(message));
                if (!asyn) {
                    future.await();
                    boolean success = future.isSuccess();
                    if (!success) {
                        Log.info(node.getId() + "is fail");
                    }
                }
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }

    @Override
    public void init() {
        MessageFactory.putMessage(VersionMessage.class);
        MessageFactory.putMessage(VerackMessage.class);
        MessageFactory.putMessage(GetAddrMessage.class);
        MessageFactory.putMessage(AddrMessage.class);
        MessageFactory.putMessage(ByeMessage.class);
    }

    @Override
    public void start() {

    }
}
