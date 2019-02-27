package io.nuls.rpc.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.log.Log;

/**
 * 服务器端事件触发处理类
 * Server-side event trigger processing class
 * @author tag
 * 2019/2/21
 * */
public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        /*
        * 缓存链接通道
        * cache link channel
        * */
        ConnectManager.createConnectData(socketChannel,ConnectManager.getRemoteUri(socketChannel));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof TextWebSocketFrame){
            WebSocketFrame frame = (WebSocketFrame) msg;
            MessageHandler.handWebSocketFrame(ctx,frame);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Log.info("链接断开:"+ConnectManager.getRemoteUri((SocketChannel) ctx.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ConnectManager.disConnect((SocketChannel) ctx.channel());
    }
}
