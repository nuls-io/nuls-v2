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
import io.nuls.rpc.model.CmdParameter;
import io.nuls.rpc.model.message.*;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.DateUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
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
    static void negotiateConnectionResponse(WebSocket webSocket) throws JsonProcessingException {
        NegotiateConnectionResponse negotiateConnectionResponse = new NegotiateConnectionResponse();
        negotiateConnectionResponse.setNegotiationStatus("0");
        negotiateConnectionResponse.setNegotiationComment("Incompatible protocol version");

        Message rspMsg = basicMessage(Constants.nextSequence(), MessageType.NegotiateConnectionResponse);
        rspMsg.setMessageData(negotiateConnectionResponse);
        webSocket.send(JSONUtils.obj2json(rspMsg));
    }

    /**
     * For NegotiateConnectionResponse
     * Send NegotiateConnectionResponse
     */
    static void ack(WebSocket webSocket, String messageId) throws JsonProcessingException {
        Ack ack = new Ack();
        ack.setRequestId(messageId);

        Message rspMsg = basicMessage(Constants.nextSequence(), MessageType.Ack);
        rspMsg.setMessageData(ack);
        webSocket.send(JSONUtils.obj2json(rspMsg));
    }

    /**
     * @return After current processing, do need to keep the Request information and wait for the next processing?
     * True: keep, False: remove
     */
    public static boolean response(WebSocket webSocket, Message message) {
        /*
        Get Request from message
         */
        String messageId = message.getMessageId();
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);

        String key = webSocket.toString() + messageId;
        int nextProcess = nextProcess(key, Integer.parseInt(request.getSubscriptionPeriod()));
        try {
            switch (nextProcess) {
                case Constants.INVOKE_EXECUTE_KEEP:
                    execute(webSocket, request.getRequestMethods(), messageId);
                    ServerRuntime.cmdInvokeTime.put(key, TimeService.currentTimeMillis());
                    return true;
                case Constants.INVOKE_EXECUTE_REMOVE:
                    execute(webSocket, request.getRequestMethods(), messageId);
                    ServerRuntime.cmdInvokeTime.put(key, TimeService.currentTimeMillis());
                    return false;
                case Constants.INVOKE_SKIP_KEEP:
                    return true;
                case Constants.INVOKE_SKIP_REMOVE:
                    return false;
                default:
                    return false;
            }
        } catch (WebsocketNotConnectedException e) {
            Log.error("Socket disconnected, remove");
            return false;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    private static void execute(WebSocket webSocket, Map requestMethods, String messageId) throws Exception {
        for (Object method : requestMethods.keySet()) {
            /*
            Execute at once
             */
            long startTimemillis = TimeService.currentTimeMillis();
            Map params = (Map) requestMethods.get(method);
            CmdDetail cmdDetail = params == null || params.get(Constants.VERSION_KEY_STR) == null
                    ? ServerRuntime.getLocalInvokeCmd((String) method)
                    : ServerRuntime.getLocalInvokeCmd((String) method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));

            Message rspMessage = basicMessage(Constants.nextSequence(), MessageType.Response);
            Response response = ServerRuntime.newResponse(messageId, "", "");
            response.setRequestId(messageId);
            response.setResponseStatus(Constants.booleanString(false));

            if (cmdDetail == null) {
                response.setResponseComment(Constants.CMD_NOT_FOUND + ":" + method + "," + (params != null ? params.get(Constants.VERSION_KEY_STR) : ""));
                response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
                rspMessage.setMessageData(response);
                webSocket.send(JSONUtils.obj2json(rspMessage));
                return;
            }

            String validationString = paramsValidation(cmdDetail, params);
            if (validationString != null) {
                response.setResponseComment(validationString);
                response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
                rspMessage.setMessageData(response);
                webSocket.send(JSONUtils.obj2json(rspMessage));
                return;
            }

            response = invoke(cmdDetail.getInvokeClass(), cmdDetail.getInvokeMethod(), params);
            response.setRequestId(messageId);
            // 在结果外面自动封装方法名
            Map<String, Object> responseData = new HashMap<>(1);
            responseData.put(method.toString(), response.getResponseData());
            response.setResponseData(responseData);
            response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
            rspMessage.setMessageData(response);
            Log.info("webSocket.send: " + JSONUtils.obj2json(rspMessage));

            webSocket.send(JSONUtils.obj2json(rspMessage));
        }
    }

    /**
     * Judging the Logic of Processing
     * 1: Process only once, then discarding
     * 2:
     */
    private static int nextProcess(String key, int subscriptionPeriod) {
        if (subscriptionPeriod <= 0) {
            /*
            Execute
             */
            return Constants.INVOKE_EXECUTE_REMOVE;
        }

        if (!ServerRuntime.cmdInvokeTime.containsKey(key)) {
            /*
            If the key doesn't in map, Set the current time to invoke time
            Then execute at once
             */
            ServerRuntime.cmdInvokeTime.put(key, TimeService.currentTimeMillis());
            return Constants.INVOKE_EXECUTE_KEEP;
        } else if (ServerRuntime.cmdInvokeTime.get(key) == Constants.UNSUBSCRIBE_TIMEMILLIS) {
            /*
            If the value is UNSUBSCRIBE_TIMEMILLIS, remove immediately without execution
             */
            ServerRuntime.cmdInvokeTime.remove(key);
            Log.info("Remove: " + key);
            return Constants.INVOKE_SKIP_REMOVE;
        } else if (TimeService.currentTimeMillis() - ServerRuntime.cmdInvokeTime.get(key) < subscriptionPeriod * Constants.MILLIS_PER_SECOND) {
            /*
            If the execution interval is not yet reached, skip this time without execution
             */
            return Constants.INVOKE_SKIP_KEEP;
        } else {
            /*
            Execute
             */
            return Constants.INVOKE_EXECUTE_KEEP;
        }
    }

    private static String paramsValidation(CmdDetail cmdDetail, Map params) {
        // 判断参数是否正确
        List<CmdParameter> cmdParameterList = cmdDetail.getParameters();
        for (CmdParameter cmdParameter : cmdParameterList) {

            /*
            If the parameter format is specified, but the incoming parameter is empty, an error message is returned.
             */
            if (!StringUtils.isNull(cmdParameter.getParameterValidRange()) || !StringUtils.isNull(cmdParameter.getParameterValidRegExp())) {
                if (params == null || params.get(cmdParameter.getParameterName()) == null) {
                    return Constants.PARAM_NULL + ":" + cmdParameter.getParameterName();
//                    Response response = ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.PARAM_NULL + ":" + cmdParameter.getParameterName());
//                    response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
//                    rspMessage.setMessageData(response);
//                    System.out.println("你参数空了：" + JSONUtils.obj2json(rspMessage));
//                    webSocket.send(JSONUtils.obj2json(rspMessage));
//                    return false;
                }
            }

            if (!paramsRangeValidation(cmdParameter, params)) {
                return Constants.PARAM_WRONG_RANGE + ":" + cmdParameter.getParameterName();
            }

            if (!paramsRegexValidation(cmdParameter, params)) {
                return Constants.PARAM_WRONG_FORMAT + ":" + cmdParameter.getParameterName();
            }

        }

        return null;
    }

    /**
     * Verify that the range is correct
     * Note:
     * If no range is set, skipped directly.
     * If the format in the Annotation is incorrect, skipped directly.
     */
    private static boolean paramsRangeValidation(CmdParameter cmdParameter, Map params) {
        if (StringUtils.isNull(cmdParameter.getParameterValidRange())) {
            return true;
        }
        if (!cmdParameter.getParameterValidRange().matches(Constants.RANGE_REGEX)) {
            return true;
        }
        if (params == null || params.get(cmdParameter.getParameterName()) == null) {
            return false;
        }

        String range = cmdParameter.getParameterValidRange();
        int start = range.startsWith("(")
                ? Integer.parseInt(range.substring(range.indexOf("(") + 1, range.indexOf(","))) + 1
                : Integer.parseInt(range.substring(range.indexOf("[") + 1, range.indexOf(",")));
        int end = range.endsWith(")")
                ? Integer.parseInt(range.substring(range.indexOf(",") + 1, range.indexOf(")"))) + 1
                : Integer.parseInt(range.substring(range.indexOf(",") + 1, range.indexOf("]")));
        int value = Integer.parseInt(params.get(cmdParameter.getParameterName()).toString());

        return start <= value && value <= end;
    }

    private static boolean paramsRegexValidation(CmdParameter cmdParameter, Map params) {
        if (StringUtils.isNull(cmdParameter.getParameterValidRegExp())) {
            return true;
        }
        if (params == null || params.get(cmdParameter.getParameterName()) == null) {
            return false;
        }

//            try {
//                if (params == null || params.get(cmdParameter.getParameterName()) == null) {
//                    Response response = ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.PARAM_NULL + ":" + cmdParameter.getParameterName());
//                    response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
//                    rspMessage.setMessageData(response);
//                    System.out.println("你参数空了：" + JSONUtils.obj2json(rspMessage));
//                    webSocket.send(JSONUtils.obj2json(rspMessage));
//                    return false;
//                }
        String value = params.get(cmdParameter.getParameterName()).toString();
        return value.matches(cmdParameter.getParameterValidRegExp());
//        if (!value.matches(cmdParameter.getParameterValidRegExp())) {
//            Response response = ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.PARAM_WRONG_FORMAT + ":" + cmdParameter.getParameterName());
//            response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
//            rspMessage.setMessageData(response);
//            System.out.println("你Format错了：" + JSONUtils.obj2json(rspMessage));
//            webSocket.send(JSONUtils.obj2json(rspMessage));
//            return false;
//        }
//            } catch (Exception e) {
//                Log.error(e);
//                Response response = ServerRuntime.newResponse(messageId, Constants.booleanString(false), e.getMessage());
//                response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
//                rspMessage.setMessageData(response);
//                webSocket.send(JSONUtils.obj2json(rspMessage));
//                return false;
//            }

    }


    /**
     * For Unsubscribe
     */
    static void unsubscribe(WebSocket webSocket, Message message) {
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
