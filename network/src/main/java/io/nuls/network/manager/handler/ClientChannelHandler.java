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

package io.nuls.network.manager.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.LocalInfoManager;
import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.handler.base.BaseChannelHandler;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.message.VersionMessage;
import io.nuls.tools.log.Log;

import java.io.IOException;
/**
 * client channel handler
 * @author lan
 * @date 2018/10/15
 *
 */
public class ClientChannelHandler extends BaseChannelHandler {
    private AttributeKey<Node> key = AttributeKey.valueOf("node");

    private NetworkParam networkParam = NetworkParam.getInstance();

    public ClientChannelHandler() {
         super();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        Attribute<Node> nodeAttribute = ctx.channel().attr(key);
        Node node = nodeAttribute.get();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Channel channel = ctx.channel();
        Attribute<Node> nodeAttribute = channel.attr(key);
        Node node = nodeAttribute.get();
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        String remoteIP = socketChannel.remoteAddress().getHostString();
        //already exist peer ip （In or Out）
        boolean isCrossConnect=this.isServerCrossConnect(channel);
        if( ConnectionManager.getInstance().isPeerConnectExist(remoteIP,Node.OUT,isCrossConnect)){
            channel.close();
            return;
        }
        //如果是本机节点访问自己的服务器，则广播本机服务器到全网
        if (LocalInfoManager.getInstance().isSelfConnect(remoteIP)) {
          //广播自己的Ip
             MessageManager.getInstance().broadcastSelfAddrToAllNode(true);
             channel.close();
             return;
        }
        node.setChannel(channel);
        node.setIp(remoteIP);
        node.setRemotePort(socketChannel.remoteAddress().getPort());
        node.setCanConnect(false);
        boolean success = ConnectionManager.getInstance().processConnectedClientNode(node);
        //非本机,发送version
        NodeGroupConnector nodeGroupConnector=node.getFirstNodeGroupConnector();
        nodeGroupConnector.setStatus(Node.CONNECTING);
        VersionMessage versionMessage=MessageFactory.getInstance().buildVersionMessage(node,nodeGroupConnector.getMagicNumber());
        if(null == versionMessage){
            //TODO:exception
        }
        BaseMeesageHandlerInf handler=NetworkMessageHandlerFactory.getInstance().getHandler(versionMessage);
        handler.send(versionMessage, node, false,true);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Node node=ConnectionManager.getInstance().getNodeByCache(this.getNodeIdByChannel( ctx.channel()),Node.OUT);
        if(null != node) {
            node.setCanConnect(true);
            //移除连接
            Log.info("Client Node is Inactive:" + node.getIp() + ":" + node.getRemotePort());
            ConnectionManager.getInstance().removeCacheConnectNodeMap(node.getId(),Node.OUT);
        }
//        Attribute<Node> nodeAttribute = ctx.channel().attr(key);
//        Node node = nodeAttribute.get();
//        if (node != null) {
//            nodeManager.removeNode(node);
//        } else {
//            SocketChannel socketChannel = (SocketChannel) ctx.channel();
//            String remoteIP = socketChannel.remoteAddress().getHostString();
//            int port = socketChannel.remoteAddress().getPort();
//            Log.info("-----------------client channelInactive  node is null -----------------" + remoteIP + ":" + port);
//        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Attribute<Node> nodeAttribute = ctx.channel().attr(key);
            Node node = nodeAttribute.get();

            if (node != null) {
                    ByteBuf buf = (ByteBuf) msg;
                    try {
                        MessageManager.getInstance().receiveMessage(buf,node.getId(),false);
                    } finally {
                        buf.release();
                    }
                    // NetworkThreadPool.doRead(buf, node);
            } else {
                SocketChannel socketChannel = (SocketChannel) ctx.channel();
                String remoteIP = socketChannel.remoteAddress().getHostString();
                int port = socketChannel.remoteAddress().getPort();
                Log.info("-----------------client channelRead  node is null -----------------" + remoteIP + ":" + port);
            }
        } catch (Exception e) {
            throw e;
        }
    }
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        Log.info("-----------------client channelInactive  node is channelUnregistered -----------------");

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!(cause instanceof IOException)) {
            //Log.error(cause);
            Log.error("===========网路消息解析错误===========");
        }
        ctx.channel().close();
    }

}
