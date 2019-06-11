package io.nuls.core.rpc.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.Message;
import io.nuls.core.rpc.model.message.MessageType;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.handler.message.TextMessageHandler;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务器端事件触发处理类
 * Server-side event trigger processing class
 *
 * @author tag
 * 2019/2/21
 */
public class ServerHandler extends SimpleChannelInboundHandler<Object> {
    private ExecutorService requestExecutorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);

    private ExecutorService responseExecutorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
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
    }
}
