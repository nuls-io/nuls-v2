package io.nuls.rpc.netty.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.Channel;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdDetail;
import io.nuls.rpc.model.CmdParameter;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.data.DateUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.rpc.info.Constants.CMD_NOT_FOUND;

/**
 * 消息处理器
 * Receive message processor
 * @author  tag
 * 2019/2/25
 * */
public class RequestMessageProcessor {
    public static final Map<String, Object> handlerMap = new HashMap<>();
    public static final Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

    /**
     * 确认握手成功
     * Confirm successful handshake
     *
     * @param channel 用于发送消息 / Used to send message
     * @throws JsonProcessingException JSON解析错误 / JSON parsing error
     */
    public static void negotiateConnectionResponse(Channel channel, String messageId) throws JsonProcessingException {
        NegotiateConnectionResponse negotiateConnectionResponse = new NegotiateConnectionResponse();
        negotiateConnectionResponse.setRequestId(messageId);
        negotiateConnectionResponse.setNegotiationStatus("1");
        negotiateConnectionResponse.setNegotiationComment("Connection true!");

        Message rspMsg = MessageUtil.basicMessage(MessageType.NegotiateConnectionResponse);
        rspMsg.setMessageData(negotiateConnectionResponse);
        ConnectManager.sendMessage(channel,JSONUtils.obj2json(rspMsg));
    }


    /**
     * 确认收到Request
     * Confirm receipt of Request
     *
     * @param channel 用于发送消息 / Used to send message
     * @param messageId 原始消息ID / The origin message ID
     * @throws JsonProcessingException JSON解析错误 / JSON parsing error
     */
    public static void ack(Channel channel, String messageId) throws JsonProcessingException {
        Ack ack = new Ack();
        ack.setRequestId(messageId);
        Message rspMsg = MessageUtil.basicMessage(MessageType.Ack);
        rspMsg.setMessageData(ack);
        ConnectManager.sendMessage(channel,JSONUtils.obj2json(rspMsg));
    }

    /**
     * 服务还未启动完成
     * The service has not been started yet.
     *
     * @param channel 链接通道
     * @param messageId 请求ID
     * */
    public static void serviceNotStarted(Channel channel, String messageId) throws JsonProcessingException {
        Response response = MessageUtil.newResponse(messageId, Constants.BOOLEAN_FALSE, "Service not started!");
        Message rspMsg = MessageUtil.basicMessage(MessageType.Response);
        rspMsg.setMessageData(response);
        ConnectManager.sendMessage(channel,JSONUtils.obj2json(rspMsg));
    }

    /**
     * 取消订阅
     * For Unsubscribe
     *
     * @serialData   取消订阅的客户端连接信息/Unsubscribed client connection information
     * @param message 取消订阅的消息体 / Unsubscribe message
     */
    public static synchronized void unsubscribe(ConnectData channelData, Message message) {
        Unsubscribe unsubscribe = JSONUtils.map2pojo((Map) message.getMessageData(), Unsubscribe.class);
        for (String requestId : unsubscribe.getUnsubscribeMethods()) {
            channelData.unsubscribe(requestId);
        }
    }

    /**
     * 处理Request，返回bool类型表示处理完之后是保留还是丢弃
     * After current processing, do need to keep the Request information and wait for the next processing?
     * True: keep, False: remove
     *
     * @param channelData     用于发送消息 / Used to send message
     * @param message    原始消息 / The origin message
     * @param request    请求 / The request
     * @return boolean
     */
    public static boolean responseWithPeriod(ConnectData channelData,Message message, Request request) {
        /*
        计算如何处理该Request
        Calculate how to handle the Request
         */
        int nextProcess = nextProcess(channelData, message, Integer.parseInt(request.getSubscriptionPeriod()));
        try {
            /*
            nextProcess的具体含义参考"Constants.INVOKE_EXECUTE_KEEP"的注释
            The specific meaning of nextProcess refers to the annotation of "Constants.INVOKE_EXECUTE_KEEP"
             */
            switch (nextProcess) {
                case Constants.EXECUTE_AND_KEEP:
                    callCommandsWithPeriod(channelData.getChannel(), request.getRequestMethods(), message.getMessageId());
                    channelData.getCmdInvokeTime().put(message, TimeService.currentTimeMillis());
                    return true;
                case Constants.EXECUTE_AND_REMOVE:
                    callCommandsWithPeriod(channelData.getChannel(), request.getRequestMethods(), message.getMessageId());
                    channelData.getCmdInvokeTime().put(message, TimeService.currentTimeMillis());
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
     *
     * @param channel        用于发送消息 / Used to send message
     * @param requestMethods 请求的方法集合 / The collections of request method
     * @param messageId      原始消息ID / The origin message ID
     * @throws Exception 连接失败 / Connected failed
     */
    @SuppressWarnings("unchecked")
    public static void callCommandsWithPeriod(Channel channel, Map requestMethods, String messageId) throws Exception {
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
                    ? ConnectManager.getLocalInvokeCmd(method)
                    : ConnectManager.getLocalInvokeCmd(method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));

            /*
            找不到本地方法，则返回"CMD_NOT_FOUND"错误
            If the local method cannot be found, the "CMD_NOT_FOUND" error is returned
             */
            if (cmdDetail == null) {
                response.setResponseComment(Constants.CMD_NOT_FOUND + ":" + method + "," + (params != null ? params.get(Constants.VERSION_KEY_STR) : ""));
                Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
                rspMessage.setMessageData(response);
                ConnectManager.sendMessage(channel,JSONUtils.obj2json(rspMessage));
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
                ConnectManager.sendMessage(channel,JSONUtils.obj2json(rspMessage));
                return;
            }

            Message rspMessage = execute(cmdDetail, params, messageId);
            ConnectManager.sendMessage(channel,JSONUtils.obj2json(rspMessage));

            /*
            执行成功之后判断该接口是否被订阅过，如果被订阅则改变该接口触发次数
            After successful execution, determine if the interface has been subscribed, and if subscribed, change the number of triggers for the interface
             */
            if(ConnectManager.SUBSCRIBE_COUNT.containsKey(method)){
                ConnectManager.eventTrigger(method,(Response) rspMessage.getMessageData());
            }
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
     * @param channel     用于发送消息 / Used to send message
     * @param realResponse  订阅事件触发，返回数据
     */
    public static void responseWithEventCount(Channel channel, Response realResponse) {
        Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
        rspMessage.setMessageData(realResponse);
        try {
            Log.debug("responseWithEventCount: " + JSONUtils.obj2json(rspMessage));
            ConnectManager.sendMessage(channel,JSONUtils.obj2json(rspMessage));
        } catch (WebsocketNotConnectedException e) {
            Log.error("Socket disconnected, remove");
        } catch (JsonProcessingException e) {
            Log.error(e);
        }
    }


    /**
     * 计算如何处理该Request
     * Calculate how to handle the Request
     *
     * @param channelData             服务器端链接信息 / Server-side Link Information
     * @param message                 原始消息 / The origin message
     * @param subscriptionPeriod      Unit: second
     * @return int
     */
    private static int nextProcess(ConnectData channelData, Message message, int subscriptionPeriod) {
        if (subscriptionPeriod == 0) {
            /*
            不需要重复执行，返回EXECUTE_AND_REMOVE（执行，然后丢弃）
            No duplication of execution is required, return EXECUTE_AND_REMOVE (execute, then discard)
             */
            return Constants.EXECUTE_AND_REMOVE;
        }

        if (!channelData.getCmdInvokeTime().containsKey(message)) {
            /*
            第一次执行，设置当前时间为执行时间，返回EXECUTE_AND_KEEP（执行，然后保留）
            First execution, set the current time as execution time, return EXECUTE_AND_KEEP (execution, then keep)
             */
            channelData.getCmdInvokeTime().put(message, 0L);
//            return Constants.EXECUTE_AND_KEEP;
        }

        if (TimeService.currentTimeMillis() - channelData.getCmdInvokeTime().get(message) < subscriptionPeriod * Constants.MILLIS_PER_SECOND) {
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

        BigDecimal start = range.startsWith("(")
                ? new BigDecimal(range.substring(range.indexOf("(") + 1, range.indexOf(",")))
                : new BigDecimal(range.substring(range.indexOf("[") + 1, range.indexOf(",")));


        BigDecimal end = range.endsWith(")")
                ? new BigDecimal(range.substring(range.indexOf(",") + 1, range.indexOf(")")))
                : new BigDecimal(range.substring(range.indexOf(",") + 1, range.indexOf("]")));

        BigDecimal value = new BigDecimal(params.get(cmdParameter.getParameterName()).toString());
        /*
        判断是否在范围内
        Judge whether it is within the range
         */
        return value.compareTo(start)>=0 && value.compareTo(end)<=0;
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
        Class<?> clz = classMap.get(invokeClass);
        if (clz == null) {
            clz = Class.forName(invokeClass);
            classMap.put(invokeClass, clz);
        }
        Method method = clz.getDeclaredMethod(invokeMethod, Map.class);
        BaseCmd cmd = (BaseCmd) handlerMap.get(invokeClass);
        if (cmd == null) {
            return MessageUtil.newResponse("", Constants.BOOLEAN_FALSE, CMD_NOT_FOUND);
        }
        return (Response) method.invoke(cmd, params);
    }
}
