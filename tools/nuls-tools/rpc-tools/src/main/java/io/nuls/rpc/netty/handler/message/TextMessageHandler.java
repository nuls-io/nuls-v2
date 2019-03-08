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
package io.nuls.rpc.netty.handler.message;

import io.netty.channel.socket.SocketChannel;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.RequestMessageProcessor;
import io.nuls.rpc.netty.processor.container.RequestContainer;
import io.nuls.rpc.netty.processor.container.ResponseContainer;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Map;

/**
 * 文本类型的消息处理器
 * Text type message handler
 * @author ln
 * 2019/2/27
 * */
public class TextMessageHandler implements Runnable {

    private SocketChannel channel;
    private String msg;

    public TextMessageHandler(SocketChannel channel, String msg) {
        this.channel = channel;
        this.msg = msg;
    }

    @Override
    public void run() {
        handler();
    }

    private void handler() {
        try {
            ConnectData connectData = ConnectManager.CHANNEL_DATA_MAP.get(channel);


            Message message = JSONUtils.json2pojo(msg, Message.class);

            /*
             * 获取该链接对应的ConnectData对象
             * Get the ConnectData object corresponding to the link
             * */
            switch (MessageType.valueOf(message.getMessageType())) {
                case NegotiateConnection:
                    /*
                    握手，直接响应
                     */
                    RequestMessageProcessor.negotiateConnectionResponse(channel, message);
                    break;
                case Unsubscribe:
                    /*
                    取消订阅，直接响应
                     */
                    Log.debug("UnsubscribeFrom<" + channel.remoteAddress().getHostString() + ":" + channel.remoteAddress().getPort() + ">: " + msg);
                    RequestMessageProcessor.unsubscribe(connectData, message);
                    break;
                case Request:
                    String messageId = message.getMessageId();
                    /*
                    如果不能提供服务，则直接返回
                    If no service is available, return directly
                     */
//                    if (!ConnectManager.isReady()) {
//                        RequestMessageProcessor.serviceNotStarted(channel, messageId);
//                        break;
//                    }

                    /*
                    Request，根据是否需要定时推送放入不同队列，等待处理
                    Request, put in different queues according to the response mode. Wait for processing
                     */
                    Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);

                    if (!ConnectManager.isPureDigital(request.getSubscriptionEventCounter())
                            && !ConnectManager.isPureDigital(request.getSubscriptionPeriod())) {
                        RequestMessageProcessor.callCommandsWithPeriod(channel, request.getRequestMethods(), messageId);
                    } else {
                        if (ConnectManager.isPureDigital(request.getSubscriptionPeriod())) {
                            connectData.getRequestPeriodLoopQueue().offer(new Object[]{message, request});
                            connectData.getIdToPeriodMessageMap().put(messageId, message);
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
                        RequestMessageProcessor.ack(channel, messageId);
                    }
                    break;
                case NegotiateConnectionResponse:
                case Ack:
//                    NegotiateConnectionResponse nres = JSONUtils.map2pojo((Map) message.getMessageData(), NegotiateConnectionResponse.class);
//                    ResponseContainer resContainer = RequestContainer.getResponseContainer(nres.getRequestId());

                    ResponseContainer resContainer = RequestContainer.getResponseContainer(((Map<String, String>) message.getMessageData()).get("requestId"));

                    if (resContainer != null && resContainer.getFuture() != null) {
                        resContainer.getFuture().complete(new Response());
                    }
                    break;
                case Response:

                    Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);

                    /*
                    如果收到已请求超时的返回直接丢弃
                    Discard directly if you receive a return that has been requested for a timeout
                     */
                    if (connectData.getTimeOutMessageList().contains(response.getRequestId())) {
                        break;
                    }

                    /*
                    Response：还要判断是否需要自动处理
                    Response: Determines whether automatic processing is required
                     */
                    if (ConnectManager.INVOKE_MAP.containsKey(response.getRequestId())) {
                        connectData.getResponseAutoQueue().offer(response);
                    } else {
                        ResponseContainer responseContainer = RequestContainer.getResponseContainer(response.getRequestId());
                        if(responseContainer != null && responseContainer.getFuture() != null) {
                            responseContainer.getFuture().complete(response);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
