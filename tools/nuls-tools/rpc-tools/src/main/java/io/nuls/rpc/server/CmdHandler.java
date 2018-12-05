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

package io.nuls.rpc.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdDetail;
import io.nuls.rpc.model.CmdParameter;
import io.nuls.rpc.model.message.*;
import io.nuls.tools.core.ioc.SpringLiteContext;
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
 * 解析从客户端收到的消息，调用正确的方法
 * Resolve the message received from the client and invoke the correct method
 *
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class CmdHandler {

    /**
     * 确认握手成功
     * Confirm successful handshake
     */
    static void negotiateConnectionResponse(WebSocket webSocket) throws JsonProcessingException {
        NegotiateConnectionResponse negotiateConnectionResponse = new NegotiateConnectionResponse();
        negotiateConnectionResponse.setNegotiationStatus("1");
        negotiateConnectionResponse.setNegotiationComment("Connection true!");

        Message rspMsg = MessageUtil.basicMessage(MessageType.NegotiateConnectionResponse);
        rspMsg.setMessageData(negotiateConnectionResponse);
        webSocket.send(JSONUtils.obj2json(rspMsg));
    }

    /**
     * 确认收到Request
     * Confirm receipt of Request
     */
    static void ack(WebSocket webSocket, String messageId) throws JsonProcessingException {
        Ack ack = new Ack();
        ack.setRequestId(messageId);

        Message rspMsg = MessageUtil.basicMessage(MessageType.Ack);
        rspMsg.setMessageData(ack);
        webSocket.send(JSONUtils.obj2json(rspMsg));
    }

    /**
     * 取消订阅
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
     * 处理Request，返回bool类型表示处理完之后是保留还是丢弃
     * After current processing, do need to keep the Request information and wait for the next processing?
     * True: keep, False: remove
     */
    public static boolean responseWithPeriod(WebSocket webSocket, String messageId, Request request) {

        String key = webSocket.toString() + messageId;

        /*
        计算如何处理该Request
        Calculate how to handle the Request
         */
        int nextProcess = nextProcess(key, Integer.parseInt(request.getSubscriptionPeriod()));
        try {
            /*
            nextProcess的具体含义参考"Constants.INVOKE_EXECUTE_KEEP"的注释
            The specific meaning of nextProcess refers to the annotation of "Constants.INVOKE_EXECUTE_KEEP"
             */
            switch (nextProcess) {
                case Constants.EXECUTE_AND_KEEP:
                    callCommandsWithPeriod(webSocket, request.getRequestMethods(), messageId);
                    ServerRuntime.cmdInvokeTime.put(key, TimeService.currentTimeMillis());
                    return true;
                case Constants.EXECUTE_AND_REMOVE:
                    callCommandsWithPeriod(webSocket, request.getRequestMethods(), messageId);
                    ServerRuntime.cmdInvokeTime.put(key, TimeService.currentTimeMillis());
                    return false;
                case Constants.SKIP_AND_KEEP:
                    return true;
                case Constants.SKIP_AND_REMOVE:
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

    /**
     * 处理Request，自动调用正确的方法，返回结果
     * Processing Request, automatically calling the correct method, returning the result
     */
    public static void callCommandsWithPeriod(WebSocket webSocket, Map requestMethods, String messageId) throws Exception {
        for (Object method : requestMethods.keySet()) {

            long startTimemillis = TimeService.currentTimeMillis();
            Map params = (Map) requestMethods.get(method);

            /*
            构造返回的消息对象
            Construct the returned message object
             */
            Response response = MessageUtil.newResponse(messageId, "", "");
            response.setRequestId(messageId);
            response.setResponseStatus(Constants.BOOLEAN_FALSE);

            /*
            从本地注册的cmd中得到对应的方法
            Get the corresponding method from the locally registered CMD
             */
            CmdDetail cmdDetail = params == null || params.get(Constants.VERSION_KEY_STR) == null
                    ? ServerRuntime.getLocalInvokeCmd((String) method)
                    : ServerRuntime.getLocalInvokeCmd((String) method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));

            /*
            找不到本地方法，则返回"CMD_NOT_FOUND"错误
            If the local method cannot be found, the "CMD_NOT_FOUND" error is returned
             */
            if (cmdDetail == null) {
                response.setResponseComment(Constants.CMD_NOT_FOUND + ":" + method + "," + (params != null ? params.get(Constants.VERSION_KEY_STR) : ""));
                response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
                Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
                rspMessage.setMessageData(response);
                webSocket.send(JSONUtils.obj2json(rspMessage));
                return;
            }

            /*
            根据注册信息进行参数的基础验证
            Basic verification of parameters based on registration information
             */
            String validationString = paramsValidation(cmdDetail, params);
            if (validationString != null) {
                response.setResponseComment(validationString);
                response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
                Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
                rspMessage.setMessageData(response);
                webSocket.send(JSONUtils.obj2json(rspMessage));
                return;
            }

            Message rspMessage = execute(cmdDetail, params, messageId, startTimemillis);
            Log.info("webSocket.send: " + JSONUtils.obj2json(rspMessage));
            webSocket.send(JSONUtils.obj2json(rspMessage));
        }
    }

    public static Message execute(CmdDetail cmdDetail, Map params, String messageId, long startTimemillis) throws Exception {
        /*
            调用本地方法，把结果封装为Message对象，通过Websocket返回
            Call the local method, encapsulate the result as a Message object, and return it through Websocket
             */
        Response response = invoke(cmdDetail.getInvokeClass(), cmdDetail.getInvokeMethod(), params);
        response.setRequestId(messageId);
        Map<String, Object> responseData = new HashMap<>(1);
        responseData.put(cmdDetail.getMethodName(), response.getResponseData());
        response.setResponseData(responseData);
        response.setResponseProcessingTime((TimeService.currentTimeMillis() - startTimemillis) + "");
        Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
        rspMessage.setMessageData(response);
        return rspMessage;
    }

    /**
     * 计算如何处理该Request
     * Calculate how to handle the Request
     */
    private static int nextProcess(String key, int subscriptionPeriod) {
        if (subscriptionPeriod == 0) {
            /*
            不需要重复执行，返回EXECUTE_AND_REMOVE（执行，然后丢弃）
            No duplication of execution is required, return EXECUTE_AND_REMOVE (execute, then discard)
             */
            return Constants.EXECUTE_AND_REMOVE;
        }

        if (!ServerRuntime.cmdInvokeTime.containsKey(key)) {
            /*
            第一次执行，设置当前时间为执行时间，返回EXECUTE_AND_KEEP（执行，然后保留）
            First execution, set the current time as execution time, return EXECUTE_AND_KEEP (execution, then keep)
             */
            ServerRuntime.cmdInvokeTime.put(key, TimeService.currentTimeMillis());
            return Constants.EXECUTE_AND_KEEP;
        }

        if (ServerRuntime.cmdInvokeTime.get(key) == Constants.UNSUBSCRIBE_TIMEMILLIS) {
            /*
            得到取消订阅命令，返回SKIP_AND_REMOVE（不执行，然后丢弃）
            Get the unsubscribe command, return SKIP_AND_REMOVE (not executed, then discarded)
             */
            ServerRuntime.cmdInvokeTime.remove(key);
            Log.info("Remove: " + key);
            return Constants.SKIP_AND_REMOVE;
        }

        if (TimeService.currentTimeMillis() - ServerRuntime.cmdInvokeTime.get(key) < subscriptionPeriod * Constants.MILLIS_PER_SECOND) {
            /*
            没有达到执行条件，返回SKIP_AND_KEEP（不执行，然后保留）
            If the execution condition is not met, return SKIP_AND_KEEP (not executed, then keep)
             */
            return Constants.SKIP_AND_KEEP;
        }

        /*
        以上都不是，返回EXECUTE_AND_KEEP（执行，然后保留）
        None of the above, return EXECUTE_AND_KEEP (execute, then keep)
         */
        return Constants.EXECUTE_AND_KEEP;

    }

    /**
     * 验证参数的有效性
     * Verify the validity of the parameters
     */
    private static String paramsValidation(CmdDetail cmdDetail, Map params) {

        List<CmdParameter> cmdParameterList = cmdDetail.getParameters();
        for (CmdParameter cmdParameter : cmdParameterList) {
            /*
            如果定义了参数格式，但是参数为空，返回错误
            If the parameter format is specified, but the incoming parameter is empty, an error message is returned.
             */
            if (!StringUtils.isNull(cmdParameter.getParameterValidRange()) || !StringUtils.isNull(cmdParameter.getParameterValidRegExp())) {
                if (params == null || params.get(cmdParameter.getParameterName()) == null) {
                    return Constants.PARAM_NULL + ":" + cmdParameter.getParameterName();
                }
            }

            /*
            验证参数是否在定义的范围内
            Verify that the parameters are within the defined range
             */
            if (!paramsRangeValidation(cmdParameter, params)) {
                return Constants.PARAM_WRONG_RANGE + ":" + cmdParameter.getParameterName();
            }

            /*
            验证参数是否匹配定义的正则
            Verify that parameters match defined regular expressions
             */
            if (!paramsRegexValidation(cmdParameter, params)) {
                return Constants.PARAM_WRONG_FORMAT + ":" + cmdParameter.getParameterName();
            }
        }

        return null;
    }

    /**
     * 验证参数是否在定义的范围内
     * Verify that the range is correct
     */
    private static boolean paramsRangeValidation(CmdParameter cmdParameter, Map params) {
        /*
        没有设定范围，验证为真
        If no range is set, Validation is true.
         */
        if (StringUtils.isNull(cmdParameter.getParameterValidRange())) {
            return true;
        }

        /*
        设定范围格式错误，验证为真
        If the format in the Annotation is incorrect, Validation is true.
         */
        if (!cmdParameter.getParameterValidRange().matches(Constants.RANGE_REGEX)) {
            return true;
        }

        /*
        参数为空，验证为假
        The parameter is empty, Validation is false
         */
        if (params == null || params.get(cmdParameter.getParameterName()) == null) {
            return false;
        }

        /*
        获取设定的范围
        Get the set range
         */
        String range = cmdParameter.getParameterValidRange();
        int start = range.startsWith("(")
                ? Integer.parseInt(range.substring(range.indexOf("(") + 1, range.indexOf(","))) + 1
                : Integer.parseInt(range.substring(range.indexOf("[") + 1, range.indexOf(",")));
        int end = range.endsWith(")")
                ? Integer.parseInt(range.substring(range.indexOf(",") + 1, range.indexOf(")"))) + 1
                : Integer.parseInt(range.substring(range.indexOf(",") + 1, range.indexOf("]")));
        int value = Integer.parseInt(params.get(cmdParameter.getParameterName()).toString());

        /*
        判断是否在范围内
        Judge whether it is within the range
         */
        return start <= value && value <= end;
    }

    /**
     * 验证参数是否匹配定义的正则
     * Verify that parameters match defined regular expressions
     */
    private static boolean paramsRegexValidation(CmdParameter cmdParameter, Map params) {
        /*
        没有设定正则，验证为真
        If no regex is set, Validation is true.
         */
        if (StringUtils.isNull(cmdParameter.getParameterValidRegExp())) {
            return true;
        }

        /*
        参数为空，验证为假
        The parameter is empty, Validation is false
         */
        if (params == null || params.get(cmdParameter.getParameterName()) == null) {
            return false;
        }

        /*
        判断是否匹配正则表达式
        Verify that parameters match defined regular expressions
         */
        String value = params.get(cmdParameter.getParameterName()).toString();
        return value.matches(cmdParameter.getParameterValidRegExp());
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
