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
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.manager.handler.base.BaseChannelHandler;
import io.nuls.network.model.Node;
import io.nuls.network.utils.IpUtil;

import java.io.IOException;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * Server channel handler
 *
 * @author lan
 * @date 2018/10/20
 */

@ChannelHandler.Sharable
public class ServerChannelHandler extends BaseChannelHandler {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        boolean success = ConnectionManager.getInstance().nodeConnectIn(socketChannel.remoteAddress().getHostString(), socketChannel.remoteAddress().getPort(), socketChannel);
        if (!success) {
            ctx.close();
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.info("Server Node is Inactive:" + nodeId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String remoteIP = channel.remoteAddress().getHostString();
        Log.info("Server Node is exceptionCaught:{}:{}", remoteIP, channel.remoteAddress().getPort());
        Log.error(cause.getMessage());
        Log.error("----------------- server exceptionCaught -------------------");
        if (!(cause instanceof IOException)) {
            String nodeId = IpUtil.getNodeId(channel.remoteAddress());
            //通常发生IOException是因为连接的节点断开了
            Log.error("----------------nodeId:" + nodeId);
            Log.error(cause);
        }
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        ByteBuf buf = (ByteBuf) msg;
        String remoteIP = channel.remoteAddress().getHostString();
        int port = channel.remoteAddress().getPort();
        Log.info("{}-----------------server channelRead-----------------{}:{}", TimeManager.currentTimeMillis(), remoteIP, port);
        NulsByteBuffer byteBuffer = null;
        Node node = null;
        try {
            String nodeId = IpUtil.getNodeId(channel.remoteAddress());
            Attribute<Node> nodeAttribute = channel.attr(AttributeKey.valueOf("node-" + nodeId));
            node = nodeAttribute.get();
            if (node != null) {
                Log.info("-----------------server channelRead  node={} -----------------", node.getId());
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                byteBuffer = new NulsByteBuffer(bytes);
            } else {
                Log.info("-----------------Server channelRead  node is null -----------------" + remoteIP + ":" + channel.remoteAddress().getPort());
                ctx.channel().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            buf.clear();
        }
        MessageManager.getInstance().receiveMessage(byteBuffer, node);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Attribute<Node> nodeAttribute = channel.attr(AttributeKey.valueOf("node-" + nodeId));

        Node node = nodeAttribute.get();
        if (node != null && node.getDisconnectListener() != null) {
            node.getDisconnectListener().action();
        }
        Log.info("Server Node is channelUnregistered:{}:{}", channel.remoteAddress().getHostString(), channel.remoteAddress().getPort());
    }

}
