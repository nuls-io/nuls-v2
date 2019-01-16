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

package io.nuls.network.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.LocalInfoManager;
import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseChannelHandler;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.message.VersionMessage;

import java.io.IOException;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * client channel handler
 * @author lan
 * @date 2018/10/15
 *
 */
public class ClientChannelHandler extends BaseChannelHandler {
    private AttributeKey<Node> key = AttributeKey.valueOf("node");

    public ClientChannelHandler() {
         super();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }
    private   boolean validSelfConnection(ChannelHandlerContext ctx,String remoteIP){
        Channel channel = ctx.channel();
        //如果是本机节点访问自己的服务器，则广播本机服务器到全网
        if (LocalInfoManager.getInstance().isSelfIp(remoteIP)) {
            //广播自己的Ip
            MessageManager.getInstance().broadcastSelfAddrToAllNode(true);
            channel.close();
            return false;
        }
        return true;
    }
    /**
     * 校验channel是否可用
     * Verify that the channel is available
     * @param ctx ctx
     * @param magicNumber magicNumber
     * @return boolean
     */
    private   boolean validConnectNumber(ChannelHandlerContext ctx,long magicNumber, String remoteIP){
        Channel channel = ctx.channel();
        //already exist peer ip （In or Out）
        if( ConnectionManager.getInstance().isPeerConnectExceedMax(remoteIP,magicNumber,1,Node.OUT)){
//            Log.info("dup connect,close channel");
            channel.close();
            return false;
        }
        return true;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Channel channel = ctx.channel();
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        String remoteIP = socketChannel.remoteAddress().getHostString();
        if(!validSelfConnection(ctx,remoteIP)){
            return;
        }
        Attribute<Node> nodeAttribute = channel.attr(key);
        Node node = nodeAttribute.get();
        NodeGroupConnector nodeGroupConnector=node.getFirstNodeGroupConnector();
        if(!validConnectNumber(ctx,nodeGroupConnector.getMagicNumber(),remoteIP)){
            return;
        }
        node.setChannel(channel);
        node.setIp(remoteIP);
        node.setRemotePort(socketChannel.remoteAddress().getPort());
        node.setIdle(false);
        Log.info("Client Node is active:{}",node.getId());
        boolean success = ConnectionManager.getInstance().processConnectNode(node);
        if(!success){
            Log.error("Client Node processConnectNode fail:{}" ,node.getId());
            channel.close();
            return;
        }
        //非本机,发送version
        nodeGroupConnector.setStatus(NodeGroupConnector.CONNECTING);
        VersionMessage versionMessage=MessageFactory.getInstance().buildVersionMessage(node,nodeGroupConnector.getMagicNumber());
        if(null == versionMessage){
            //exception
            Log.error("build versionMessage error");
            channel.close();
            return;
        }
        BaseMeesageHandlerInf handler=MessageHandlerFactory.getInstance().getHandler(versionMessage);
        handler.send(versionMessage, node, false,true);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String nodeId =  this.getNodeIdByChannel( ctx.channel());
        super.channelInactive(ctx);
        Node node=ConnectionManager.getInstance().getNodeByCache(nodeId,Node.OUT);
        if(null != node) {
            //移除连接
            Log.info("Client Node is Inactive:" + node.getId());
            node.setIdle(true);
            ConnectionManager.getInstance().removeCacheConnectNodeMap(node.getId(),Node.OUT);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        String remoteIP = socketChannel.remoteAddress().getHostString();
        int port = socketChannel.remoteAddress().getPort();
        ByteBuf buf = (ByteBuf) msg;
        try {
            Attribute<Node> nodeAttribute = ctx.channel().attr(key);
            Node node = nodeAttribute.get();
            if (node != null) {
                MessageManager.getInstance().receiveMessage(buf,node,false);
            } else {
                Log.info("-----------------client channelRead  node is null -----------------" + remoteIP + ":" + port);
                ctx.channel().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            buf.release();
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
            cause.printStackTrace();
            Log.error(cause.getMessage());
        }
        ctx.channel().close();
    }

}
