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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.nuls.network.constant.NetworkParam;
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


    private NetworkParam networkParam = NetworkParam.getInstance();


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String remoteIP = channel.remoteAddress().getHostString();
        Log.info("============"+remoteIP+"====="+channel.remoteAddress().getPort());
        //查看是否是本机尝试连接本机地址 ，如果是直接关闭连接
        if (LocalInfoManager.getInstance().isSelfConnect(remoteIP)) {
            Log.info("Server----------------------本机尝试连接本机地址关闭 ------------------------- " + remoteIP);
            ctx.channel().close();
            return;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        boolean isCrossConnect=isServerCrossConnect(ctx.channel());
        //already exist peer ip （In or Out）
        if( ConnectionManager.getInstance().isPeerConnectExist(socketChannel.remoteAddress().getHostString(),Node.IN,isCrossConnect)){
            ctx.channel().close();
            return;
        }

        Node node = new Node(socketChannel.remoteAddress().getHostString(),socketChannel.remoteAddress().getPort(), Node.IN,isCrossConnect);
        node.setCanConnect(false);
        node.setChannel(ctx.channel());
        boolean success = ConnectionManager.getInstance().processConnectedServerNode(node);
        if (!success) {
            ctx.channel().close();
            return;
        }
        //此时无法知道client的魔法参数，不能发送version消息,此时业务法对MaxIn做判断
    }



    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Node node=ConnectionManager.getInstance().getNodeByCache(this.getNodeIdByChannel( ctx.channel()),Node.OUT);
        if(null != node) {
            node.setCanConnect(true);
            ConnectionManager.getInstance().removeCacheConnectNodeMap(node.getId(),Node.IN);
            Log.info("Server Node is Inactive:" + node.getIp() + ":" + node.getRemotePort());
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.error("----------------- server exceptionCaught -------------------");
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
        } finally {
            buf.release();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        Log.info("-----------------server channelInactive  node is channelUnregistered -----------------");
    }

}
