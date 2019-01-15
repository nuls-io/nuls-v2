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
import io.nuls.network.locker.Lockers;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.manager.handler.message.GetAddrMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.network.model.message.*;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.network.model.po.ProtocolHandlerPo;
import io.nuls.network.model.po.RoleProtocolPo;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.crypto.Sha256Hash;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * 消息管理器，用于收发消息
 * message manager：  send and rec msg
 * @author lan
 * @date 2018/11/01
 *
 */
public class MessageManager extends BaseManager{
    /**
     *key : protocol cmd, value : Map<role,ProtocolRoleHandler>
     */
    private static Map<String,Map<String,ProtocolRoleHandler>> protocolRoleHandlerMap = new ConcurrentHashMap<>();
    private static MessageManager instance = new MessageManager();
    public static MessageManager getInstance(){
        return instance;
    }
    private  StorageManager storageManager=StorageManager.getInstance();
    public void  sendToNode(BaseMessage message, Node node, boolean aysn) {
        //向节点发送消息
        broadcastToANode(message,node,aysn);

    }

    public  Map<String,Map<String,ProtocolRoleHandler>> getProtocolRoleHandlerMap() {
        return protocolRoleHandlerMap;
    }

    /**
     * protocol message  checkSum cal
     * @param msgBody msgBody
     * @return long
     */
    public long getCheckSum(byte []msgBody){
        byte [] bodyHash=Sha256Hash.hashTwice(msgBody);
        byte []get4Byte=ByteUtils.subBytes(bodyHash,0,4);
        return ByteUtils.bytesToBigInteger(get4Byte).longValue();
    }
    private  BaseMessage getMessageInstance(String command) {
        Class<? extends BaseMessage> msgClass  = MessageFactory.getMessage(command);
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
     * add handler Map data
     * @param protocolCmd protocolCmd
     * @param handler handler
     */
    public void addProtocolRoleHandlerMap(String protocolCmd,ProtocolRoleHandler handler){
        Lockers.PROTOCOL_HANDLERS_REGISTER_LOCK.lock();
        try {
            Map<String,ProtocolRoleHandler> roleMap = protocolRoleHandlerMap.get(protocolCmd);
            if (null == roleMap) {
                roleMap = new HashMap<>();
                roleMap.put(handler.getRole(),handler);
                protocolRoleHandlerMap.put(protocolCmd,roleMap);
            } else {
                //replace
                roleMap.put(handler.getRole(),handler);

            }
        }finally {
            Lockers.PROTOCOL_HANDLERS_REGISTER_LOCK.unlock();
        }
    }
    public void clearCacheProtocolRoleHandlerMap(String role){
        Lockers.PROTOCOL_HANDLERS_REGISTER_LOCK.lock();
        try {
            Collection<Map<String,ProtocolRoleHandler>> values = protocolRoleHandlerMap.values();
            for (Map<String,ProtocolRoleHandler> value : values) {
                value.remove(role);
            }
        }finally {
            Lockers.PROTOCOL_HANDLERS_REGISTER_LOCK.unlock();
        }

    }
    /**
     * get handler data
     * @param protocolCmd protocolCmd
     * @return Collection
     */
    public Collection<ProtocolRoleHandler> getProtocolRoleHandlerMap(String protocolCmd){
        if(null != protocolRoleHandlerMap.get(protocolCmd)){
            return protocolRoleHandlerMap.get(protocolCmd).values();
        }
        return null;
    }
    /**
     * 验证消息
     * validate message checkSum
     * @param data data
     * @return boolean
     */
    private boolean validate(byte []data,long pChecksum){
        byte [] bodyHash=Sha256Hash.hashTwice(data);
        byte []get4Byte=ByteUtils.subBytes(bodyHash,0,4);
        long checksum=ByteUtils.bytesToBigInteger(get4Byte).longValue();
//        Log.debug("==================local checksum:"+checksum);
//        Log.debug("==================peer checksum:"+pChecksum);
        return checksum == pChecksum;
    }
    public void receiveMessage(ByteBuf buffer,String nodeKey,boolean isServer) throws NulsException {
        //统一接收消息处理
        try {
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
            MessageHeader header = new MessageHeader();
            int headerSize = header.size();
            byte []payLoad = byteBuffer.getPayload();
            byte []payLoadBody = ByteUtils.subBytes(payLoad,headerSize,payLoad.length-headerSize);
            byte []headerByte = ByteUtils.copyOf(payLoad,headerSize);
//            Log.info("=================payLoad length"+payLoadBody.length);

            header.parse(headerByte,0);
//            Log.info("================CMD="+header.getCommandStr());
            if (!validate(payLoadBody,header.getChecksum())) {
//                Log.error("validate  false ======================");
                return;
            }
            byteBuffer.setCursor(0);
            while (!byteBuffer.isFinished()) {
//                Log.debug((isServer?"Server":"Client")+":----receive message-- magicNumber:"+ header.getMagicNumber()+"==CMD:"+header.getCommandStr());
                BaseMessage message=MessageManager.getInstance().getMessageInstance(header.getCommandStr());
                if(null != message) {
                    BaseMeesageHandlerInf handler = MessageHandlerFactory.getInstance().getHandler(message);
                    message = byteBuffer.readNulsData(message);
                    NetworkEventResult result = handler.recieve(message, nodeKey, isServer);
                    if(!result.isSuccess()){
//                        Log.error("receiveMessage fail:"+result.getErrorCode().getMsg());
                    }
                }else{
                    //外部消息，转外部接口
                    long magicNum=header.getMagicNumber();
                    int chainId=NodeGroupManager.getInstance().getChainIdByMagicNum(magicNum);
                    Map<String,Object> paramMap = new HashMap<>();
                    paramMap.put("chainId",chainId);
                    paramMap.put("nodeId",nodeKey);
                    paramMap.put("messageBody",HexUtil.byteToHex(payLoadBody));
                    Collection<ProtocolRoleHandler> protocolRoleHandlers =  getProtocolRoleHandlerMap(header.getCommandStr());
                    if(null == protocolRoleHandlers){
//                        Log.error("unknown mssages. cmd={},may be handle had not be registered to network.",header.getCommandStr());
                    }else{
                        for(ProtocolRoleHandler protocolRoleHandler:protocolRoleHandlers) {
//                            Log.debug("request：{}=={}",protocolRoleHandler.getRole(),protocolRoleHandler.getHandler());
                            CmdDispatcher.requestAndResponse(protocolRoleHandler.getRole(), protocolRoleHandler.getHandler(), paramMap);
//                            Log.debug("response：" + response);
                        }
                    }
                    byteBuffer.setCursor(payLoad.length);
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
                if(NodeGroupConnector.HANDSHAKE == nodeGroupConnector.getStatus()){
                    List<IpAddress> addressesList=new ArrayList<>();
                    addressesList.add(LocalInfoManager.getInstance().getExternalAddress());
                    AddrMessage addrMessage= MessageFactory.getInstance().buildAddrMessage(addressesList,nodeGroupConnector.getMagicNumber());
//                    Log.info("broadcastSelfAddrToAllNode================"+addrMessage.getMsgBody().size()+"==getIpAddressList()=="+addrMessage.getMsgBody().getIpAddressList().size());
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
     * @param magicNumber long
     * @param isCross boolean
     * @param asyn boolean
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
                     GetAddrMessageHandler.getInstance().send(getAddrMessage,node,false,true);
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
                    GetAddrMessageHandler.getInstance().send(getAddrMessage,node,false,true);
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
        return (message.getHeader().getCommandStr().equals(NetworkConstant.CMD_MESSAGE_VERSION) || message.getHeader().getCommandStr().equals(NetworkConstant.CMD_MESSAGE_VERACK));

    }
    private NetworkEventResult broadcastToANode(BaseMessage message, Node node, boolean asyn) {
        /*
        *not handShakeMessage must be  validate peer status
        */
        if(!isHandShakeMessage(message)) {
            NodeGroupConnector nodeGroupConnector = node.getNodeGroupConnector(message.getHeader().getMagicNumber());
            if (NodeGroupConnector.HANDSHAKE != nodeGroupConnector.getStatus()) {
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
            e.printStackTrace();
            Log.error(e.getMessage());
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
                e.printStackTrace();
                Log.error(e.getMessage());
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
        MessageFactory.putMessage(GetTimeMessage.class);
        MessageFactory.putMessage(TimeMessage.class);

        /*
         * 加载协议注册信息
         * load protocolRegister info
         */
        List<RoleProtocolPo> list = storageManager.getProtocolRegisterInfos();
        for(RoleProtocolPo roleProtocolPo : list){
            roleProtocolPo.getRole();
            List<ProtocolHandlerPo>   protocolHandlerPos = roleProtocolPo.getProtocolHandlerPos();
            for(ProtocolHandlerPo protocolHandlerPo : protocolHandlerPos){
                ProtocolRoleHandler protocolRoleHandler = new ProtocolRoleHandler(roleProtocolPo.getRole(),protocolHandlerPo.getHandler());
                addProtocolRoleHandlerMap(protocolHandlerPo.getProtocolCmd(),protocolRoleHandler);
            }
        }
    }

    @Override
    public void start() {

    }
}
