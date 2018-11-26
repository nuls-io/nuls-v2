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
package io.nuls.network.manager.handler.base;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.nuls.network.constant.NetworkParam;

/**
 * channel handler
 * @author  lan
 * @date 2018/11/01
 */
public abstract class BaseChannelHandler  extends ChannelInboundHandlerAdapter {
    /**
     * 通过channel获取nodeId
     *Get the nodeId through the channel
     * @param channel
     * @return
     */
    protected String getNodeIdByChannel( Channel channel){
        SocketChannel socketChannel = (SocketChannel) channel;
        String remoteIP = socketChannel.remoteAddress().getHostString();
        int port=socketChannel.remoteAddress().getPort();
        return (remoteIP+":"+port);
    }

    /**
     * 判断是否跨连连接
     *Determine whether to connect across connections
     * @param channel
     * @return
     */
    protected boolean isServerCrossConnect(Channel channel){
        SocketChannel socketChannel = (SocketChannel) channel;
        int port=socketChannel.localAddress().getPort();
        if(NetworkParam.getInstance().getCrossPort()==port){
            return true;
        }
        return false;
    }

    /**
     * 校验channel是否可用
     * Verify that the channel is available
     * @param ctx
     * @return
     */
    protected abstract boolean validChannel(ChannelHandlerContext ctx);

}
