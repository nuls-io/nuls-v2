package io.nuls.core.rpc.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdPriority;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Message;
import io.nuls.core.rpc.model.message.MessageType;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.handler.message.TextMessageHandler;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务器端事件触发处理类
 * Server-side event trigger processing class
 *
 * @author tag
 * 2019/2/21
 */
public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    private ThreadPoolExecutor requestExecutorService = new ThreadPoolExecutor(Constants.THREAD_POOL_SIZE, Constants.THREAD_POOL_SIZE, 0L,TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(), new NulsThreadFactory("server-handler-request"));

    private ThreadPoolExecutor responseExecutorService = new ThreadPoolExecutor(Constants.THREAD_POOL_SIZE, Constants.THREAD_POOL_SIZE, 0L,TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(), new NulsThreadFactory("server-handler-request"));

/*
    private ThreadPoolExecutor requestExecutorService = ThreadUtils.createThreadPool(Constants.THREAD_POOL_SIZE, 0, new NulsThreadFactory("server-handler-request"));

    private ThreadPoolExecutor responseExecutorService = ThreadUtils.createThreadPool(Constants.THREAD_POOL_SIZE, 0, new NulsThreadFactory("server-handler-response"));
*/

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame txMsg = (TextWebSocketFrame) msg;
            Message message = JSONUtils.json2pojo(txMsg.text(), Message.class);
            MessageType messageType = MessageType.valueOf(message.getMessageType());
            int priority = CmdPriority.DEFAULT.getPriority();
            TextMessageHandler messageHandler = new TextMessageHandler((SocketChannel) ctx.channel(), message,priority);
            if(requestExecutorService.getQueue().size() >= 500 || responseExecutorService.getQueue().size() > 500){
                String role = ConnectManager.getRoleByChannel(ctx.channel());
                Log.info("链接{}当前请求线程池总线程数量{},运行中线程数量{},等待队列数量{}",role,requestExecutorService.getPoolSize(),requestExecutorService.getActiveCount(),requestExecutorService.getQueue().size());
                Log.info("链接{}当前响应线程池总线程数量{},运行中线程数量{},等待队列数量{}",role,responseExecutorService.getPoolSize(),responseExecutorService.getActiveCount(),responseExecutorService.getQueue().size());
            }
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
                }
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
