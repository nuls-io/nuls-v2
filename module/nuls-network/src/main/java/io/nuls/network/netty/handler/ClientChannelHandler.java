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
import io.nuls.core.log.Log;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.handler.base.BaseChannelHandler;
import io.nuls.network.model.Node;
import io.nuls.network.utils.LoggerUtil;

import java.io.IOException;

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
        LoggerUtil.COMMON_LOG.info("Client Node is active:{}", node != null ? node.getId() : null);
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
        ByteBuf buf = (ByteBuf) msg;
        NulsByteBuffer byteBuffer = null;
        Node node = null;
        try {
            Attribute<Node> nodeAttribute = ctx.channel().attr(key);
            node = nodeAttribute.get();
            if (node != null) {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                byteBuffer = new NulsByteBuffer(bytes);
            } else {
                Log.error("-----------------client channelRead  node is null -----------------" + remoteIP + ":" + port);
                ctx.channel().close();
            }
        } catch (Exception e) {
            Log.error( e);
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
            LoggerUtil.COMMON_LOG.info("-----------------client channelInactive  node is channelUnregistered node={}-----------------", node.getId());
            node.getDisconnectListener().action();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!(cause instanceof IOException)) {
            LoggerUtil.COMMON_LOG.error(cause.getMessage(), cause);
        }
        ctx.channel().close();
    }

}
