package io.nuls.core.rpc.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.Message;
import io.nuls.core.rpc.model.message.MessageType;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.handler.message.TextMessageHandler;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 客户端事件触发处理类
 * Client Event Triggering Processing Class
 *
 * @author tag
 * 2019/2/21
 */
public class ClientHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketClientHandshaker handShaker;
    private ChannelPromise handshakeFuture;

    private ThreadPoolExecutor requestExecutorService = ThreadUtils.createThreadPool(Constants.THREAD_POOL_SIZE, 0, new NulsThreadFactory("client-handler-request"));

    private ThreadPoolExecutor responseExecutorService = ThreadUtils.createThreadPool(Constants.THREAD_POOL_SIZE, 0, new NulsThreadFactory("client-handler-response"));

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

    public ClientHandler(WebSocketClientHandshaker handShaker) {
        this.handShaker = handShaker;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handShaker.handshake(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        FullHttpResponse response;
        if (!this.handShaker.isHandshakeComplete()) {
            try {
                response = (FullHttpResponse) msg;
                //握手协议返回，设置结束握手
                this.handShaker.finishHandshake(ch, response);
                //设置成功
                this.handshakeFuture.setSuccess();
                Log.debug("WebSocket Client connected! response headers[sec-webSocket-extensions]:{}" + response.headers());
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
            } else if (msg instanceof TextWebSocketFrame) {
                if(requestExecutorService.getQueue().size() >= 10000 || responseExecutorService.getQueue().size() > 10000){
                    Log.info("当前请求线程池总线程数量{},运行中线程数量{},等待队列数量{}",requestExecutorService.getPoolSize(),requestExecutorService.getActiveCount(),requestExecutorService.getQueue().size());
                    Log.info("当前相应线程池总线程数量{},运行中线程数量{},等待队列数量{}",responseExecutorService.getPoolSize(),responseExecutorService.getActiveCount(),responseExecutorService.getQueue().size());
                }
                TextWebSocketFrame txMsg = (TextWebSocketFrame) msg;
                Message message = JSONUtils.json2pojo(txMsg.text(), Message.class);
                MessageType messageType = MessageType.valueOf(message.getMessageType());
                TextMessageHandler messageHandler = new TextMessageHandler((SocketChannel) ctx.channel(), message);
                if(messageType.equals(MessageType.Response)
                        || messageType.equals(MessageType.NegotiateConnectionResponse)
                        || messageType.equals(MessageType.Ack) ){
                    responseExecutorService.execute(messageHandler);
                }else{
                    requestExecutorService.execute(messageHandler);
                }
            } else {
                Log.warn("Unsupported message format");
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ConnectManager.disConnect((SocketChannel) ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
