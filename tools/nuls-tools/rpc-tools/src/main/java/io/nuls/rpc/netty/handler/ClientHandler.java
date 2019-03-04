package io.nuls.rpc.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.handler.message.TextMessageHandler;
import io.nuls.tools.log.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 客户端事件触发处理类
 * Client Event Triggering Processing Class
 * @author tag
 * 2019/2/21
 * */
public class ClientHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketClientHandshaker handShaker;
    private ChannelPromise handshakeFuture;

    private ThreadLocal<ExecutorService> threadExecutorService = ThreadLocal.withInitial(() -> Executors.newFixedThreadPool(Thread.activeCount()));

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }
    public WebSocketClientHandshaker getHandshaker() {
        return handShaker;
    }

    public void setHandshaker(WebSocketClientHandshaker handShaker) {
        this.handShaker = handShaker;
    }

    public ChannelPromise getHandshakeFuture() {
        return handshakeFuture;
    }

    public void setHandshakeFuture(ChannelPromise handshakeFuture) {
        this.handshakeFuture = handshakeFuture;
    }

    public ChannelFuture handshakeFuture() {
        return this.handshakeFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)throws Exception{
        Channel ch = ctx.channel();
        FullHttpResponse response;
        if (!this.handShaker.isHandshakeComplete()) {
            try {
                response = (FullHttpResponse) msg;
                //握手协议返回，设置结束握手
                this.handShaker.finishHandshake(ch, response);
                //设置成功
                this.handshakeFuture.setSuccess();
                Log.info("WebSocket Client connected! response headers[sec-webSocket-extensions]:{}" + response.headers());
            } catch (WebSocketHandshakeException var7) {
                FullHttpResponse res = (FullHttpResponse) msg;
                String errorMsg = String.format("WebSocket Client failed to connect,status:%s,reason:%s", res.status(), res.content().toString(CharsetUtil.UTF_8));
                this.handshakeFuture.setFailure(new Exception(errorMsg));
            }
        } else if (msg instanceof FullHttpResponse) {
            response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        } else {
            WebSocketFrame frame = (WebSocketFrame) msg;

            if (frame instanceof CloseWebSocketFrame) {
                ch.close();
            } else if(msg instanceof TextWebSocketFrame){
                TextWebSocketFrame txMsg = (TextWebSocketFrame) msg;
                TextMessageHandler messageHandler = new TextMessageHandler((SocketChannel) ctx.channel(), txMsg.text());
                threadExecutorService.get().execute(messageHandler);
            } else {
                Log.warn("Unsupported message format");
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        ConnectData connectData = ConnectManager.getConnectDataByChannel(socketChannel);
        if (connectData != null) {
            connectData.setConnected(false);
        }
        Log.info("链接断开:"+ConnectManager.getRemoteUri((SocketChannel) ctx.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ConnectManager.disConnect((SocketChannel) ctx.channel());
    }
}
