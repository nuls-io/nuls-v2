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

package io.nuls.rpc.server.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdDetail;
import io.nuls.rpc.model.CmdParameter;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.server.runtime.ServerRuntime;
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
 */
public class CmdHandler {


    /**
     * 确认握手成功
     * Confirm successful handshake
     *
     * @param webSocket 用于发送消息 / Used to send message
     * @throws JsonProcessingException JSON解析错误 / JSON parsing error
     */
    public static void negotiateConnectionResponse(WebSocket webSocket) throws JsonProcessingException {
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
     *
     * @param webSocket 用于发送消息 / Used to send message
     * @param messageId 原始消息ID / The origin message ID
     * @throws JsonProcessingException JSON解析错误 / JSON parsing error
     */
    public static void ack(WebSocket webSocket, String messageId) throws JsonProcessingException {
        Ack ack = new Ack();
        ack.setRequestId(messageId);

        Message rspMsg = MessageUtil.basicMessage(MessageType.Ack);
        rspMsg.setMessageData(ack);
        webSocket.send(JSONUtils.obj2json(rspMsg));
    }

    public static void serviceNotStarted(WebSocket webSocket, String messageId) throws JsonProcessingException {
        Response response = MessageUtil.newResponse(messageId, Constants.BOOLEAN_FALSE, "Service not started!");
        Message rspMsg = MessageUtil.basicMessage(MessageType.Response);
        rspMsg.setMessageData(response);
        webSocket.send(JSONUtils.obj2json(rspMsg));
    }


    /**
     * 取消订阅
     * For Unsubscribe
     *
     * @param message 取消订阅的消息体 / Unsubscribe message
     */
    public static synchronized void unsubscribe(WebSocket webSocket, Message message) {
        Unsubscribe unsubscribe = JSONUtils.map2pojo((Map) message.getMessageData(), Unsubscribe.class);
        for (String requestId : unsubscribe.getUnsubscribeMethods()) {
            ServerRuntime.UNSUBSCRIBE_LIST.add(ServerRuntime.genUnsubscribeKey(webSocket, requestId));
        }
    }

    /**
     * 处理Request，返回bool类型表示处理完之后是保留还是丢弃
     * After current processing, do need to keep the Request information and wait for the next processing?
     * True: keep, False: remove
     *
     * @param webSocket 用于发送消息 / Used to send message
     * @param messageId 原始消息ID / The origin message ID
     * @param request   请求 / The request
     * @return boolean
     */
    public static boolean responseWithPeriod(WebSocket webSocket, String messageId, Request request) {

        String key = ServerRuntime.genUnsubscribeKey(webSocket, messageId);
        if (ServerRuntime.UNSUBSCRIBE_LIST.contains(key)) {
            Log.info("取消订阅responseWithPeriod：" + key);
            return false;
        }

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
                    ServerRuntime.CMD_INVOKE_TIME.put(key, TimeService.currentTimeMillis());
                    return true;
                case Constants.EXECUTE_AND_REMOVE:
                    callCommandsWithPeriod(webSocket, request.getRequestMethods(), messageId);
                    ServerRuntime.CMD_INVOKE_TIME.put(key, TimeService.currentTimeMillis());
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
            Log.error(e.getMessage());
            return false;
        }
    }


    /**
     * 处理Request，自动调用正确的方法，返回结果
     * Processing Request, automatically calling the correct method, returning the result
     *
     * @param webSocket      用于发送消息 / Used to send message
     * @param requestMethods 请求的方法集合 / The collections of request method
     * @param messageId      原始消息ID / The origin message ID
     * @throws Exception 连接失败 / Connected failed
     */
    @SuppressWarnings("unchecked")
    public static void callCommandsWithPeriod(WebSocket webSocket, Map requestMethods, String messageId) throws Exception {
        for (Object object : requestMethods.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) object;
            String method = entry.getKey();
            Map params = entry.getValue();

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
                    ? ServerRuntime.getLocalInvokeCmd(method)
                    : ServerRuntime.getLocalInvokeCmd(method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));

            /*
            找不到本地方法，则返回"CMD_NOT_FOUND"错误
            If the local method cannot be found, the "CMD_NOT_FOUND" error is returned
             */
            if (cmdDetail == null) {
                response.setResponseComment(Constants.CMD_NOT_FOUND + ":" + method + "," + (params != null ? params.get(Constants.VERSION_KEY_STR) : ""));
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
                Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
                rspMessage.setMessageData(response);
                webSocket.send(JSONUtils.obj2json(rspMessage));
                return;
            }

            Message rspMessage = execute(cmdDetail, params, messageId);
            Log.debug("responseWithPeriod: " + JSONUtils.obj2json(rspMessage));
            webSocket.send(JSONUtils.obj2json(rspMessage));
        }
    }

    /**
     * 调用本地方法，把结果封装为Message对象，通过Websocket返回
     * Call the local method, encapsulate the result as a Message object, and return it through Websocket
     *
     * @param cmdDetail CmdDetail
     * @param params    Map, {key, value}
     * @param messageId 原始消息ID / The origin message ID
     * @return Message
     * @throws Exception 调用的方法返回的任何异常 / Any exception returned by the invoked method
     */
    private static Message execute(CmdDetail cmdDetail, Map params, String messageId) throws Exception {
        long startTimemillis = TimeService.currentTimeMillis();
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
     * 处理Request，如果达到EventCount的发送条件，则发送
     * Processing Request, if EventCount's sending condition is met, then send
     *
     * @param webSocket 用于发送消息 / Used to send message
     * @param messageId 原始消息ID / The origin message ID
     * @param request   请求 / The request
     * @param cmd       触发EventCount的命令 / Command to trigger EventCount
     * @return boolean
     */
    public static boolean responseWithEventCount(WebSocket webSocket, String messageId, Request request, String cmd) {
        String unsubscribeKey = ServerRuntime.genUnsubscribeKey(webSocket, messageId);
        if (ServerRuntime.UNSUBSCRIBE_LIST.contains(unsubscribeKey)) {
            Log.info("取消订阅responseWithEventCount：" + unsubscribeKey);
            return false;
        }

        for (Object method : request.getRequestMethods().keySet()) {
            long changeCount = ServerRuntime.getCmdChangeCount((String) method);
            long eventCount = Long.parseLong(request.getSubscriptionEventCounter());
            if (changeCount == 0) {
                continue;
            }
            if (!cmd.equals(method)) {
                continue;
            }

            if (changeCount % eventCount == 0) {
                Response response = ServerRuntime.getCmdLastValue((String) method);
                String eventCountKey = ServerRuntime.genEventCountKey(webSocket, messageId, (String) method);
                boolean hasSent = ServerRuntime.hasSent(eventCountKey);
                if (hasSent) {
                    continue;
                }


                /*
                这段代码非常不优雅，可以改进下（我没时间了，怕改出BUG），代码的业务逻辑如下：
                用户的cmd会返回一个对象，RPC会自动把这个对象替换为Map，Key是调用的方法名，Value是内容（Berzeck强烈要求）
                因此在这里创建一个新对象
                注意：如果不创建新对象，直接使用response对象的话，在大家都订阅了一个方法的时候会出现多重嵌套的问题（因为对象的引用被改变了）
                 */
                Response realResponse = new Response();
                realResponse.setRequestId(messageId);
                realResponse.setResponseStatus(response.getResponseStatus());
                realResponse.setResponseComment(response.getResponseComment());
                realResponse.setResponseMaxSize(response.getResponseMaxSize());

                Map<String, Object> responseData = new HashMap<>(1);
                responseData.put((String) method, response.getResponseData());
                realResponse.setResponseData(responseData);

                Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
                rspMessage.setMessageData(response);
                try {
                    Log.info("responseWithEventCount: " + JSONUtils.obj2json(rspMessage));
                    webSocket.send(JSONUtils.obj2json(rspMessage));
                } catch (WebsocketNotConnectedException e) {
                    Log.error("Socket disconnected, remove");
                } catch (JsonProcessingException e) {
                    Log.error(e);
                }

                ServerRuntime.CMD_LAST_RESPONSE_BE_USED.put(eventCountKey, true);
            }
        }
        return true;
    }


    /**
     * 计算如何处理该Request
     * Calculate how to handle the Request
     *
     * @param key                Key
     * @param subscriptionPeriod Unit: second
     * @return int
     */
    private static int nextProcess(String key, int subscriptionPeriod) {
        if (subscriptionPeriod == 0) {
            /*
            不需要重复执行，返回EXECUTE_AND_REMOVE（执行，然后丢弃）
            No duplication of execution is required, return EXECUTE_AND_REMOVE (execute, then discard)
             */
            return Constants.EXECUTE_AND_REMOVE;
        }

        if (!ServerRuntime.CMD_INVOKE_TIME.containsKey(key)) {
            /*
            第一次执行，设置当前时间为执行时间，返回EXECUTE_AND_KEEP（执行，然后保留）
            First execution, set the current time as execution time, return EXECUTE_AND_KEEP (execution, then keep)
             */
            ServerRuntime.CMD_INVOKE_TIME.put(key, TimeService.currentTimeMillis());
            return Constants.EXECUTE_AND_KEEP;
        }

        if (TimeService.currentTimeMillis() - ServerRuntime.CMD_INVOKE_TIME.get(key) < subscriptionPeriod * Constants.MILLIS_PER_SECOND) {
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
     *
     * @param cmdDetail CmdDetail
     * @param params    Parameters of remote method
     * @return String: null means no error
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
     *
     * @param cmdParameter Parameter format
     * @param params       Parameters of remote method
     * @return boolean
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
     *
     * @param cmdParameter Parameter format
     * @param params       Parameters of remote method
     * @return boolean
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
     *
     * @param invokeClass  Class
     * @param invokeMethod Method
     * @param params       Parameters of remote method
     * @return Response
     * @throws Exception Any exceptions
     */
    @SuppressWarnings("unchecked")
    private static Response invoke(String invokeClass, String invokeMethod, Map params) throws Exception {
        Class clz = Class.forName(invokeClass);
        Method method = clz.getDeclaredMethod(invokeMethod, Map.class);

        BaseCmd cmd;
        if (SpringLiteContext.getBeanByClass(invokeClass) == null) {
            Constructor constructor = clz.getConstructor();
            cmd = (BaseCmd) constructor.newInstance();
        } else {
            cmd = (BaseCmd) SpringLiteContext.getBeanByClass(invokeClass);
        }

        return (Response) method.invoke(cmd, params);
    }
}
