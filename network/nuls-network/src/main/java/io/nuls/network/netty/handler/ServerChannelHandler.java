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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.LocalInfoManager;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.handler.base.BaseChannelHandler;
import io.nuls.network.model.Node;
import io.nuls.tools.log.Log;

import java.io.IOException;

/**
 * Server channel handler
 * @author lan
 * @date 2018/10/20
 */

@ChannelHandler.Sharable
public class ServerChannelHandler extends BaseChannelHandler {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String remoteIP = channel.remoteAddress().getHostString();
        Log.info("============"+remoteIP+"====="+channel.remoteAddress().getPort());
        //查看是否是本机尝试连接本机地址 ，如果是直接关闭连接
        if (LocalInfoManager.getInstance().isSelfIp(remoteIP)) {
            Log.info("Server----------------------Local connect close: ------------------------- " + remoteIP+":"+channel.remoteAddress().getPort());
            ctx.channel().close();
            return;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        boolean isCrossConnect=isServerCrossConnect(ctx.channel());
        Node node = new Node(socketChannel.remoteAddress().getHostString(),socketChannel.remoteAddress().getPort(), Node.IN,isCrossConnect);
        node.setIdle(false);
        node.setChannel(ctx.channel());
        Log.debug("Server Node is active:" +node.getId());
        boolean success = ConnectionManager.getInstance().processConnectNode(node);
        if (!success) {
            Log.debug("Server Node processConnectNode fail:" +node.getId());
            ctx.channel().close();
            return;
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String remoteIP = channel.remoteAddress().getHostString();
        String nodeId = this.getNodeIdByChannel( ctx.channel());
        super.channelInactive(ctx);
        Log.info("Server Node is Inactive:" +remoteIP + ":" + channel.remoteAddress().getPort());
        Node node=ConnectionManager.getInstance().getNodeByCache(nodeId,Node.IN);
        if(null != node) {
            node.setIdle(true);
            ConnectionManager.getInstance().removeCacheConnectNodeMap(node.getId(),Node.IN);
            Log.info("Server Node is Inactive:" + node.getIp() + ":" + node.getRemotePort());
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.error("----------------- server exceptionCaught -------------------");
        SocketChannel channel = (SocketChannel) ctx.channel();
        String remoteIP = channel.remoteAddress().getHostString();
        Log.info("Server Node is exceptionCaught:" +remoteIP + ":" + channel.remoteAddress().getPort());
        if (!(cause instanceof IOException)) {
            Log.error(cause);
        }
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        ByteBuf buf = (ByteBuf) msg;
        String remoteIP = channel.remoteAddress().getHostString();
        int port=channel.remoteAddress().getPort();
        String nodeKey=remoteIP+":"+port;
        try {
            MessageManager.getInstance().receiveMessage(buf,nodeKey,true);
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            buf.release();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String remoteIP = channel.remoteAddress().getHostString();
        Log.info("Server Node is channelUnregistered:" +remoteIP + ":" + channel.remoteAddress().getPort());
        Log.info("-----------------server channelInactive  node is channelUnregistered -----------------");
    }

}
