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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.manager.handler.base.BaseChannelHandler;
import io.nuls.network.model.Node;

import java.io.IOException;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * client channel handler
 *
 * @author lan
 * @date 2018/10/15
 */
public class ClientChannelHandler extends BaseChannelHandler {
    private AttributeKey<Node> key = AttributeKey.valueOf("node");

    public ClientChannelHandler() {
        super();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        Attribute<Node> nodeAttribute = ctx.channel().attr(key);
        Node node = nodeAttribute.get();
        if (node != null && node.getRegisterListener() != null) {
            node.getRegisterListener().action();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        Attribute<Node> nodeAttribute = ctx.channel().attr(key);

        Node node = nodeAttribute.get();
        if (node != null) {
            node.setChannel(ctx.channel());
        }
        if (node != null && node.getConnectedListener() != null) {
            node.getConnectedListener().action();
        }
        Log.info("Client Node is active:{}", node.getId());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        String remoteIP = socketChannel.remoteAddress().getHostString();
        int port = socketChannel.remoteAddress().getPort();
        Log.info("{}-----------------client channelRead-----------------{}:{}", TimeManager.currentTimeMillis(), remoteIP, port);
        ByteBuf buf = (ByteBuf) msg;
        NulsByteBuffer byteBuffer = null;
        Node node = null;
        try {
            Attribute<Node> nodeAttribute = ctx.channel().attr(key);
            node = nodeAttribute.get();
            if (node != null) {
                Log.info("-----------------client channelRead  node={} -----------------", node.getId());
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                byteBuffer = new NulsByteBuffer(bytes);
            } else {
                Log.info("-----------------client channelRead  node is null -----------------" + remoteIP + ":" + port);
                ctx.channel().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
//            throw e;
        } finally {
            buf.clear();
        }
        MessageManager.getInstance().receiveMessage(byteBuffer, node);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        Attribute<Node> nodeAttribute = ctx.channel().attr(key);
        Node node = nodeAttribute.get();
        if (node != null && node.getDisconnectListener() != null) {
            Log.debug("-----------------client channelInactive  node is channelUnregistered node={}-----------------",node.getId());
            node.getDisconnectListener().action();
        }
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
