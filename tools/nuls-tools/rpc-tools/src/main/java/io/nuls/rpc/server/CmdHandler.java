/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.rpc.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdDetail;
import io.nuls.rpc.model.message.*;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.DateUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;
import org.java_websocket.WebSocket;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Call the correct method based on request information
 *
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class CmdHandler {


    /**
     * Build basic message object
     */
    public static Message basicMessage(String messageId, MessageType messageType) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setMessageType(messageType.name());
        message.setTimestamp(TimeService.currentTimeMillis() + "");
        message.setTimezone(DateUtils.getTimeZone() + "");
        return message;
    }

    /**
     * For NegotiateConnection
     * Default NegotiateConnection object
     */
    public static NegotiateConnection defaultNegotiateConnection() {
        NegotiateConnection negotiateConnection = new NegotiateConnection();
        negotiateConnection.setProtocolVersion("1.0");
        negotiateConnection.setCompressionAlgorithm("zlib");
        negotiateConnection.setCompressionRate("0");
        return negotiateConnection;
    }

    /**
     * For NegotiateConnectionResponse
     * Send NegotiateConnectionResponse
     */
    public static void negotiateConnectionResponse(WebSocket webSocket) throws JsonProcessingException {
        NegotiateConnectionResponse negotiateConnectionResponse = new NegotiateConnectionResponse();
        negotiateConnectionResponse.setNegotiationStatus("0");
        negotiateConnectionResponse.setNegotiationComment("Incompatible protocol version");

        Message rspMsg = basicMessage(Constants.nextSequence() + "", MessageType.NegotiateConnectionResponse);
        rspMsg.setMessageData(negotiateConnectionResponse);
        webSocket.send(JSONUtils.obj2json(rspMsg));
    }

    /**
     * For Response
     */
    public static boolean response(WebSocket webSocket, Message message) throws Exception {
        String messageId = message.getMessageId();
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        Map requestMethods = request.getRequestMethods();

        boolean addBack = false;
        int subscriptionPeriod = Integer.parseInt(request.getSubscriptionPeriod());

        /*
        subscriptionPeriod > 0, means send response every time.
        subscriptionPeriod <= 0, means send response only once.
         */
        String key = webSocket.toString() + messageId;
        if (subscriptionPeriod > 0) {
            addBack = true;

            if (!ServerRuntime.cmdInvokeTime.containsKey(key)) {
                ServerRuntime.cmdInvokeTime.put(key, TimeService.currentTimeMillis());
            } else {
                /*
                If the value is unsubscribed magic parameter, returns immediately without execution
                Return false
                 */
                if (ServerRuntime.cmdInvokeTime.get(key) == Constants.UNSUBSCRIBE_TIMEMILLIS) {
                    ServerRuntime.cmdInvokeTime.remove(key);
                    Log.info("Remove: " + key);
                    return false;
                }

                /*
                If the execution interval is not yet reached, returns immediately without execution
                 */
                if (TimeService.currentTimeMillis() - ServerRuntime.cmdInvokeTime.get(key) < subscriptionPeriod * 1000) {
                    return true;
                }
            }
        }

        for (Object method : requestMethods.keySet()) {
            /*
            Execute at once
             */
            long startTimemillis = TimeService.currentTimeMillis();
            Map params = (Map) requestMethods.get(method);
            CmdDetail cmdDetail = params == null || params.get(Constants.VERSION_KEY_STR) == null
                    ? ServerRuntime.getLocalInvokeCmd((String) method)
                    : ServerRuntime.getLocalInvokeCmd((String) method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));

            Response response = cmdDetail == null
                    ? ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.CMD_NOT_FOUND + ":" + method + "," + (params != null ? params.get(Constants.VERSION_KEY_STR) : ""))
                    : invoke(cmdDetail.getInvokeClass(), cmdDetail.getInvokeMethod(), params);
            // 在结果外面自动封装方法名
            Map<String, Object> responseData = new HashMap<>(1);
            responseData.put(method.toString(), response.getResponseData());
            response.setResponseData(responseData);
            response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
            response.setRequestId(messageId);

            Message rspMessage = basicMessage(Constants.nextSequence(), MessageType.Response);
            rspMessage.setMessageData(response);
            Log.info("webSocket.send: " + JSONUtils.obj2json(rspMessage));
            try {
                webSocket.send(JSONUtils.obj2json(rspMessage));
                ServerRuntime.cmdInvokeTime.put(key, TimeService.currentTimeMillis());
            } catch (Exception e) {
                Log.error("Socket disconnect, remove!");
                addBack = false;
            }
        }

        return addBack;
    }

    /**
     * For Unsubscribe
     */
    public static void unsubscribe(WebSocket webSocket, Message message) throws Exception {
        Unsubscribe unsubscribe = JSONUtils.map2pojo((Map) message.getMessageData(), Unsubscribe.class);
        for (String str : unsubscribe.getUnsubscribeMethods()) {
            String key = webSocket.toString() + str;
            ServerRuntime.cmdInvokeTime.put(key, Constants.UNSUBSCRIBE_TIMEMILLIS);
        }
    }


    /**
     * Call local cmd.
     * 1. If the interface is injected via @Autowired, the injected object is used
     * 2. If the interface has no special annotations, construct a new object by reflection
     */
    private static Response invoke(String invokeClass, String invokeMethod, Map params) throws Exception {

        Class clz = Class.forName(invokeClass);
        @SuppressWarnings("unchecked") Method method = clz.getDeclaredMethod(invokeMethod, Map.class);

        BaseCmd cmd;
        if (SpringLiteContext.getBeanByClass(invokeClass) == null) {
            @SuppressWarnings("unchecked") Constructor constructor = clz.getConstructor();
            cmd = (BaseCmd) constructor.newInstance();
        } else {
            cmd = (BaseCmd) SpringLiteContext.getBeanByClass(invokeClass);
        }

        return (Response) method.invoke(cmd, params);
    }
}
