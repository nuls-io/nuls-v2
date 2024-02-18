package io.nuls.core.rpc.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdPriority;
import io.nuls.core.rpc.model.RequestOnly;
import io.nuls.core.rpc.model.message.Message;
import io.nuls.core.rpc.model.message.MessageType;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.handler.message.TextMessageHandler;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Client Event Trigger Processing Class
 * Client Event Triggering Processing Class
 *
 * @author tag
 * 2019/2/21
 */
public class ClientHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketClientHandshaker handShaker;
    private ChannelPromise handshakeFuture;

    private ThreadPoolExecutor requestExecutorService = new ThreadPoolExecutor(Constants.THREAD_POOL_SIZE, Constants.THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(), new NulsThreadFactory("server-handler-request"));

    private ThreadPoolExecutor responseExecutorService = new ThreadPoolExecutor(Constants.THREAD_POOL_SIZE, Constants.THREAD_POOL_SIZE, 0L,TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(), new NulsThreadFactory("server-handler-request"));


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
                //Handshake protocol return, set to end handshake
                this.handShaker.finishHandshake(ch, response);
                //Set successfully
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
                TextWebSocketFrame txMsg = (TextWebSocketFrame) msg;
                ByteBuf content = txMsg.content();
                byte[] bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
                Message message = JSONUtils.byteArray2pojo(bytes, Message.class);
                MessageType messageType = MessageType.valueOf(message.getMessageType());
                int priority = CmdPriority.DEFAULT.getPriority();
                TextMessageHandler messageHandler = new TextMessageHandler((SocketChannel) ctx.channel(), message, priority);
                if(messageType.equals(MessageType.Response)
                        || messageType.equals(MessageType.NegotiateConnectionResponse)
                        || messageType.equals(MessageType.Ack) ){
                    responseExecutorService.execute(messageHandler);
                }else{
                    if(messageType.equals(MessageType.Request)){
                        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
                        if(request.getRequestMethods().size() == 1){
                            for (String cmd:request.getRequestMethods().keySet()) {
                                if(ConnectManager.CMD_PRIORITY_MAP.containsKey(cmd)){
                                    messageHandler.setPriority(ConnectManager.CMD_PRIORITY_MAP.get(cmd));
                                }
                            }
                        }
                        messageHandler.setRequest(request);
                        requestExecutorService.execute(messageHandler);
                    }else if(messageType.equals(MessageType.RequestOnly)){
                        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
                        ConnectData connectData = ConnectManager.CHANNEL_DATA_MAP.get(ctx.channel());
                        int messageSize = bytes.length;
                        if(!connectData.requestOnlyQueueReachLimit()){
                            connectData.getRequestOnlyQueue().offer(new RequestOnly(request, messageSize));
                            connectData.addRequestOnlyQueueMemSize(messageSize);
                        }else{
                            Log.debug("RequestOnlyThe queue cache is full, discarding newly received messages,messageId:{},Memory occupied by queue：{}", message.getMessageID(),connectData.getRequestOnlyQueueMemSize());
                        }
                    }else{
                        requestExecutorService.execute(messageHandler);
                    }
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
