package io.nuls.rpc.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.nuls.rpc.model.message.Ack;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.parse.JSONUtils;

import java.util.Map;

/**
 * 客户端事件触发处理类
 * Client Event Triggering Processing Class
 * @author tag
 * 2019/2/21
 * */
public class ClientHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketClientHandshaker handShaker;
    private ChannelPromise handshakeFuture;
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
                System.out.println("WebSocket Client connected! response headers[sec-webSocket-extensions]:{}" + response.headers());
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
            if (frame instanceof TextWebSocketFrame) {
                handWebSocketFrame(ctx,frame);
            } else if (frame instanceof CloseWebSocketFrame) {
                ch.close();
            }
        }
    }

    /**
     * 处理客户端与服务端之前的websocket业务
     *
     * @param ctx
     * @param frame
     */
    private void handWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)throws Exception{
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        ConnectData connectData = ConnectManager.getConnectDataByChannel(socketChannel);
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame msg = (TextWebSocketFrame) frame;
            Message message = JSONUtils.json2pojo(msg.text(), Message.class);
            switch (MessageType.valueOf(message.getMessageType())) {
                case NegotiateConnectionResponse:
                    connectData.getNegotiateResponseQueue().offer(message);
                    break;
                case Ack:
                    Ack ack = JSONUtils.map2pojo((Map) message.getMessageData(), Ack.class);
                    connectData.getAckQueue().offer(ack);
                    break;
                case Response:
                    Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                    /*
                    如果收到已请求超时的返回直接丢弃
                    Discard directly if you receive a return that has been requested for a timeout
                     */
                    if(connectData.getTimeOutMessageList().contains(response.getRequestId())){
                        break;
                    }

                    /*
                    Response：还要判断是否需要自动处理
                    Response: Determines whether automatic processing is required
                     */
                    if (ConnectManager.INVOKE_MAP.containsKey(response.getRequestId())) {
                        connectData.getResponseAutoQueue().offer(response);
                    } else {
                        connectData.getResponseManualQueue().offer(response);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
