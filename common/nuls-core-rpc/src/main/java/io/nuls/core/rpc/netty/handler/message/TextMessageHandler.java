/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.core.rpc.netty.handler.message;

import io.netty.channel.socket.SocketChannel;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.rpc.model.RequestOnly;
import io.nuls.core.rpc.netty.processor.RequestMessageProcessor;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.Message;
import io.nuls.core.rpc.model.message.MessageType;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.container.RequestContainer;
import io.nuls.core.rpc.netty.processor.container.ResponseContainer;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.NulsDateUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Text type message processor
 * Text type message handler
 *
 * @author ln
 * 2019/2/27
 */
public class TextMessageHandler implements Runnable, Comparable<TextMessageHandler> {

    private SocketChannel channel;
    private Message message;
    private int priority;
    private Request request;
    private int messageSize;

    public TextMessageHandler(SocketChannel channel, Message message, int priority) {
        this.channel = channel;
        this.message = message;
        this.priority = priority;
    }

    @Override
    public int compareTo(TextMessageHandler o) {
        return  Integer.compare(o.priority,this.priority);
    }

    @Override
    public void run() {
        handler();
    }

    @SuppressWarnings("unchecked")
    private void handler() {
        try {
            ConnectData connectData = ConnectManager.CHANNEL_DATA_MAP.get(channel);

            /*
             * Get the corresponding linkConnectDataobject
             * Get the ConnectData object corresponding to the link
             * */
            switch (MessageType.valueOf(message.getMessageType())) {
                case NegotiateConnection:
                    /*
                    Handshake, direct response
                     */
                    RequestMessageProcessor.negotiateConnectionResponse(channel, message);
                    break;
                case Unsubscribe:
                    /*
                    Unsubscribe and respond directly
                     */
                    RequestMessageProcessor.unsubscribe(connectData, message);
                    break;
                case Request:
                    String messageId = message.getMessageID();
                    /*
                    RequestAccording to whether it is necessary to schedule push to different queues and wait for processing
                    Request, put in different queues according to the response mode. Wait for processing
                     */
                    if (!ConnectManager.isPureDigital(request.getSubscriptionEventCounter())
                            && !ConnectManager.isPureDigital(request.getSubscriptionPeriod())) {
                        if(null==request.getTimeOut() || request.getTimeOut().isEmpty()){
                            RequestMessageProcessor.callCommandsWithPeriod(channel, request.getRequestMethods(), messageId, false);
                        }else{
                            long requestTime = Long.parseLong(message.getTimestamp());
                            long timeOut = Long.parseLong(request.getTimeOut());
                            long currentTime = System.currentTimeMillis() ;
                            if(timeOut == 0 ||  currentTime< requestTime + timeOut){
                                RequestMessageProcessor.callCommandsWithPeriod(channel, request.getRequestMethods(), messageId, false);
                            }else{
                                Log.debug("Request timeout discarding request, current time：{}Request time:{},Timeout time:{},Request Method：{}", currentTime,requestTime,timeOut,request.getRequestMethods());
                            }
                        }
                    } else {
                        int tryCount = 0;
                        while (connectData == null && tryCount < Constants.TRY_COUNT) {
                            TimeUnit.SECONDS.sleep(2L);
                            connectData = ConnectManager.CHANNEL_DATA_MAP.get(channel);
                            tryCount++;
                        }
                        if (connectData == null) {
                            RequestMessageProcessor.serviceNotStarted(channel, messageId);
                            break;
                        }
                        if (ConnectManager.isPureDigital(request.getSubscriptionPeriod())) {
                            connectData.getRequestPeriodLoopQueue().offer(new Object[]{message, request});
                            connectData.getIdToPeriodMessageMap().put(messageId, message);
                        }
                        if (ConnectManager.isPureDigital(request.getSubscriptionEventCounter())) {
                            connectData.subscribeByEvent(message, request);
                            RequestMessageProcessor.callCommandsWithPeriod(channel, request.getRequestMethods(), messageId, true);

                        }
                    }

                    /*
                    If you need aAckThen send
                    Send Ack if needed
                     */
                    if (Constants.BOOLEAN_TRUE.equals(request.getRequestAck())) {
                        RequestMessageProcessor.ack(channel, messageId);
                    }
                    break;
                case NegotiateConnectionResponse:
                case Ack:
                    ResponseContainer resContainer = RequestContainer.getResponseContainer(((Map<String, String>) message.getMessageData()).get("RequestID"));
                    if (resContainer != null && resContainer.getFuture() != null) {
                        resContainer.getFuture().complete(new Response());
                    }
                    break;
                case Response:
                    Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                    /*
                    Response：Also need to determine if automatic processing is required
                    Response: Determines whether automatic processing is required
                     */
                    if (ConnectManager.INVOKE_MAP.containsKey(response.getRequestID())) {
                        connectData.getResponseAutoQueue().offer(response);
                    } else {
                        ResponseContainer responseContainer = RequestContainer.getResponseContainer(response.getRequestID());
                        if (responseContainer != null && responseContainer.getFuture() != null) {
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }
}
