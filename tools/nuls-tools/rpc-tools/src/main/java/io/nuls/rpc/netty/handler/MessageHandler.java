package io.nuls.rpc.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.RequestMessageProcessor;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Map;

/**
 * 服务器端事件触发处理类
 * Server-side event trigger processing class
 * @author tag
 * 2019/2/21
 * */
public class MessageHandler {
    /**
     * 处理客户端与服务端之前的webSocket业务
     *
     * @param ctx
     * @param frame
     */
    public static void handWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)throws Exception{
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        ConnectData connectData = ConnectManager.getConnectDataByChannel(socketChannel);
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame msg = (TextWebSocketFrame) frame;
            Message message = JSONUtils.json2pojo(msg.text(), Message.class);
            /*
             * 获取该链接对应的ConnectData对象
             * Get the ConnectData object corresponding to the link
             * */
            switch (MessageType.valueOf(message.getMessageType())) {
                case NegotiateConnection:
                    /*
                    握手，直接响应
                     */
                    RequestMessageProcessor.negotiateConnectionResponse(socketChannel);
                    break;
                case Unsubscribe:
                    /*
                    取消订阅，直接响应
                     */
                    Log.debug("UnsubscribeFrom<" + socketChannel.remoteAddress().getHostString() + ":" + socketChannel.remoteAddress().getPort() + ">: " + msg);
                    RequestMessageProcessor.unsubscribe(connectData, message);
                    break;
                case Request:
                    String messageId = message.getMessageId();
                    /*
                    如果不能提供服务，则直接返回
                    If no service is available, return directly
                     */
                    if (!ConnectManager.isReady()) {
                        RequestMessageProcessor.serviceNotStarted(socketChannel, messageId);
                        break;
                    }

                    /*
                    Request，根据是否需要定时推送放入不同队列，等待处理
                    Request, put in different queues according to the response mode. Wait for processing
                     */
                    Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);

                    if (!ConnectManager.isPureDigital(request.getSubscriptionEventCounter())
                            && !ConnectManager.isPureDigital(request.getSubscriptionPeriod())) {
                        connectData.getRequestSingleQueue().offer(new Object[]{messageId, request});
                    } else {
                        if (ConnectManager.isPureDigital(request.getSubscriptionPeriod())) {
                            connectData.getRequestPeriodLoopQueue().offer(new Object[]{message, request});
                            connectData.getIdToPeriodMessageMap().put(messageId,message);
                        }
                        if (ConnectManager.isPureDigital(request.getSubscriptionEventCounter())) {
                            connectData.subscribeByEvent(message);
                        }
                    }

                    /*
                    如果需要一个Ack，则发送
                    Send Ack if needed
                     */
                    if (Constants.BOOLEAN_TRUE.equals(request.getRequestAck())) {
                        RequestMessageProcessor.ack(socketChannel, messageId);
                    }
                    break;

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
