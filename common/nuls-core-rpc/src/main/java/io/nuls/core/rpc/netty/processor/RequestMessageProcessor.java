package io.nuls.core.rpc.netty.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.Channel;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdDetail;
import io.nuls.core.rpc.model.CmdParameter;
import io.nuls.core.rpc.model.message.*;
import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.rpc.util.SerializeUtil;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.core.rpc.info.Constants.CMD_NOT_FOUND;

/**
 * Message processor
 * Receive message processor
 *
 * @author tag
 * 2019/2/25
 */
public class RequestMessageProcessor {
    public static final Map<String, Object> handlerMap = new HashMap<>();
    public static final Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

    /**
     * Confirm successful handshake
     * Confirm successful handshake
     *
     * @param channel Used for sending messages / Used to send message
     * @throws JsonProcessingException JSONParsing error / JSON parsing error
     */
    public static void negotiateConnectionResponse(Channel channel, Message message) throws JsonProcessingException {
        NegotiateConnectionResponse negotiateConnectionResponse = new NegotiateConnectionResponse();
        negotiateConnectionResponse.setRequestID(message.getMessageID());
        negotiateConnectionResponse.setNegotiationStatus("1");
        negotiateConnectionResponse.setNegotiationComment("Connection true!");

        Message rspMsg = MessageUtil.basicMessage(MessageType.NegotiateConnectionResponse);
        rspMsg.setMessageData(negotiateConnectionResponse);
        ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(rspMsg)));

        //Save after successful handshakechannelCorresponding information with roles
        NegotiateConnection negotiateConnection = JSONUtils.map2pojo((Map) message.getMessageData(), NegotiateConnection.class);
        ConnectManager.cacheConnect(negotiateConnection.getAbbreviation(), channel, false);
    }


    /**
     * Confirm receiptRequest
     * Confirm receipt of Request
     *
     * @param channel   Used for sending messages / Used to send message
     * @param messageId Original messageID / The origin message ID
     * @throws JsonProcessingException JSONParsing error / JSON parsing error
     */
    public static void ack(Channel channel, String messageId) throws JsonProcessingException {
        Ack ack = new Ack();
        ack.setRequestId(messageId);
        Message rspMsg = MessageUtil.basicMessage(MessageType.Ack);
        rspMsg.setMessageData(ack);
        ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(rspMsg)));
    }

    /**
     * The service has not yet been started and completed
     * The service has not been started yet.
     *
     * @param channel   Link Channel
     * @param messageId requestID
     */
    public static void serviceNotStarted(Channel channel, String messageId) throws JsonProcessingException {
        Response response = MessageUtil.newFailResponse(messageId, "Service not started!");
        Message rspMsg = MessageUtil.basicMessage(MessageType.Response);
        rspMsg.setMessageData(response);
        ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(rspMsg)));
    }

    /**
     * Unsubscribe
     * For Unsubscribe
     *
     * @param message Unsubscribed message body / Unsubscribe message
     * @serialData Unsubscribed client connection information/Unsubscribed client connection information
     */
    public static synchronized void unsubscribe(ConnectData channelData, Message message) {
        Unsubscribe unsubscribe = JSONUtils.map2pojo((Map) message.getMessageData(), Unsubscribe.class);
        for (String requestId : unsubscribe.getUnsubscribeMethods()) {
            channelData.unsubscribe(requestId);
        }
    }

    /**
     * handleRequest, return toboolType represents whether to keep or discard after processing
     * After current processing, do need to keep the Request information and wait for the next processing?
     * True: keep, False: remove
     *
     * @param channelData Used for sending messages / Used to send message
     * @param message     Original message / The origin message
     * @param request     request / The request
     * @return boolean
     */
    public static boolean responseWithPeriod(ConnectData channelData, Message message, Request request) {
        /*
        Calculate how to handle thisRequest
        Calculate how to handle the Request
         */
        int nextProcess = nextProcess(channelData, message, Integer.parseInt(request.getSubscriptionPeriod()));
        try {
            /*
            nextProcessFor specific meanings, please refer to"Constants.INVOKE_EXECUTE_KEEP"Annotations for
            The specific meaning of nextProcess refers to the annotation of "Constants.INVOKE_EXECUTE_KEEP"
             */
            switch (nextProcess) {
                case Constants.EXECUTE_AND_KEEP:
                    callCommandsWithPeriod(channelData.getChannel(), request.getRequestMethods(), message.getMessageID(), false);
                    channelData.getCmdInvokeTime().put(message, NulsDateUtils.getCurrentTimeMillis());
                    return true;
                case Constants.EXECUTE_AND_REMOVE:
                    callCommandsWithPeriod(channelData.getChannel(), request.getRequestMethods(), message.getMessageID(), false);
                    channelData.getCmdInvokeTime().put(message, NulsDateUtils.getCurrentTimeMillis());
                    return false;
                case Constants.SKIP_AND_KEEP:
                    return true;
                case Constants.SKIP_AND_REMOVE:
                    return false;
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }


    /**
     * handleRequestAutomatically call the correct method and return the result
     * Processing Request, automatically calling the correct method, returning the result
     *
     * @param channel        Used for sending messages / Used to send message
     * @param requestMethods Collection of requested methods / The collections of request method
     * @param messageId      Original messageID / The origin message ID
     * @param isSubscribe    is subscribe message
     * @throws JsonProcessingException Server side processing exception
     */
    @SuppressWarnings("unchecked")
    public static void callCommandsWithPeriod(Channel channel, Map requestMethods, String messageId, boolean isSubscribe) throws JsonProcessingException {
        for (Object object : requestMethods.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) object;
            String method = entry.getKey();
            Map params = entry.getValue();

            /*
            Construct the returned message object
            Construct the returned message object
             */
            Response response = MessageUtil.newResponse(messageId, Response.FAIL, "");
            try {
                 /*
                Registered locallycmdObtain the corresponding method in
                Get the corresponding method from the locally registered CMD
                */
                CmdDetail cmdDetail = params == null || params.get(Constants.VERSION_KEY_STR) == null
                        ? ConnectManager.getLocalInvokeCmd(method)
                        : ConnectManager.getLocalInvokeCmd(method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));

                /*
                If the local method cannot be found, return"CMD_NOT_FOUND"error
                If the local method cannot be found, the "CMD_NOT_FOUND" error is returned
                */
                if (cmdDetail == null) {
                    response.setResponseComment(Constants.CMD_NOT_FOUND + ":" + method + "," + (params != null ? params.get(Constants.VERSION_KEY_STR) : ""));
                    response.setResponseErrorCode(CommonCodeConstanst.CMD_NOTFOUND.getCode());
                    Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
                    rspMessage.setMessageData(response);
                    ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(rspMessage)));
                    return;
                }

                /*
                Basic validation of parameters based on registration information
                Basic verification of parameters based on registration information
                */
                String validationString = paramsValidation(cmdDetail, params);
                if (validationString != null) {
                    response.setResponseComment(validationString);
                    response.setResponseErrorCode(CommonCodeConstanst.PARAMETER_ERROR.getCode());
                    Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
                    rspMessage.setMessageData(response);
                    ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(rspMessage)));
                    return;
                }

                Message rspMessage = execute(cmdDetail, params, messageId);
                ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(rspMessage)));

                /*
                After successful execution, check whether the interface has been subscribed. If subscribed, change the number of times the interface is triggered
                After successful execution, determine if the interface has been subscribed, and if subscribed, change the number of triggers for the interface
                */
                if (ConnectManager.SUBSCRIBE_COUNT.containsKey(method) && isSubscribe) {
                    ConnectManager.eventTrigger(method, (Response) rspMessage.getMessageData());
                }
            } catch (Exception e) {
                Log.error(e);
                response.setResponseComment("Server-side processing failed!");
                response.setResponseErrorCode(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION.getCode());
                Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
                rspMessage.setMessageData(response);
                ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(rspMessage)));
            }
        }
    }

    /**
     * handleRequest, do not return results
     * Processing Request, automatically calling the correct method, returning the result
     *
     * @param requestMethods Collection of requested methods / The collections of request method
     * @throws JsonProcessingException Server side processing exception
     */
    @SuppressWarnings("unchecked")
    public static void callCommands(Map requestMethods) throws JsonProcessingException {
        for (Object object : requestMethods.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) object;
            String method = entry.getKey();
            Map params = entry.getValue();
            try {
                 /*
                Registered locallycmdObtain the corresponding method in
                Get the corresponding method from the locally registered CMD
                */
                CmdDetail cmdDetail = params == null || params.get(Constants.VERSION_KEY_STR) == null
                        ? ConnectManager.getLocalInvokeCmd(method)
                        : ConnectManager.getLocalInvokeCmd(method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));

                /*
                If the local method cannot be found, return"CMD_NOT_FOUND"error
                If the local method cannot be found, the "CMD_NOT_FOUND" error is returned
                */
                if (cmdDetail == null) {
                    Log.info("Call method does not exist!");
                    return;
                }

                /*
                Basic validation of parameters based on registration information
                Basic verification of parameters based on registration information
                */
                String validationString = paramsValidation(cmdDetail, params);
                if (validationString != null) {
                    Log.info("Parameter validation error!");
                    return;
                }
                invoke(cmdDetail.getInvokeClass(), cmdDetail.getInvokeMethod(), params);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }

    /**
     * Call local methods and encapsulate the results asMessageObject, throughWebsocketreturn
     * Call the local method, encapsulate the result as a Message object, and return it through Websocket
     *
     * @param cmdDetail CmdDetail
     * @param params    Map, {key, value}
     * @param messageId Original messageID / The origin message ID
     * @return Message
     * @throws Exception Any exceptions returned by the called method / Any exception returned by the invoked method
     */
    private static Message execute(CmdDetail cmdDetail, Map params, String messageId) throws Exception {
        long startTimemillis = NulsDateUtils.getCurrentTimeMillis();
        Response response = invoke(cmdDetail.getInvokeClass(), cmdDetail.getInvokeMethod(), params);
        response.setRequestID(messageId);
        Map<String, Object> responseData = new HashMap<>(1);
        responseData.put(cmdDetail.getMethodName(), response.getResponseData());
        response.setResponseData(responseData);
        response.setResponseProcessingTime((NulsDateUtils.getCurrentTimeMillis() - startTimemillis) + "");
        Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
        rspMessage.setMessageData(response);
        return rspMessage;
    }


    /**
     * handleRequestIf it reachesEventCountIf the sending conditions are met, then send
     * Processing Request, if EventCount's sending condition is met, then send
     *
     * @param channel      Used for sending messages / Used to send message
     * @param realResponse Subscription event triggered, returning data
     */
    public static void responseWithEventCount(Channel channel, Response realResponse) {
        Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
        rspMessage.setMessageData(realResponse);
        try {
            Log.debug("responseWithEventCount: " + JSONUtils.obj2json(rspMessage));
            ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(rspMessage)));
        } catch (JsonProcessingException e) {
            Log.error(e);
        }
    }


    /**
     * Calculate how to handle thisRequest
     * Calculate how to handle the Request
     *
     * @param channelData        Server side link information / Server-side Link Information
     * @param message            Original message / The origin message
     * @param subscriptionPeriod Unit: second
     * @return int
     */
    private static int nextProcess(ConnectData channelData, Message message, int subscriptionPeriod) {
        if (subscriptionPeriod == 0) {
            /*
            No need to execute repeatedly, returnEXECUTE_AND_REMOVE（Execute and then discard）
            No duplication of execution is required, return EXECUTE_AND_REMOVE (execute, then discard)
             */
            return Constants.EXECUTE_AND_REMOVE;
        }

        if (!channelData.getCmdInvokeTime().containsKey(message)) {
            /*
            First execution, set the current time as the execution time, returnEXECUTE_AND_KEEP（Execute and then retain）
            First execution, set the current time as execution time, return EXECUTE_AND_KEEP (execution, then keep)
             */
            channelData.getCmdInvokeTime().put(message, 0L);
//            return Constants.EXECUTE_AND_KEEP;
        }

        if (NulsDateUtils.getCurrentTimeMillis() - channelData.getCmdInvokeTime().get(message) < subscriptionPeriod * Constants.MILLIS_PER_SECOND) {
            /*
            Execution conditions not met, returnSKIP_AND_KEEP（Do not execute, then keep）
            If the execution condition is not met, return SKIP_AND_KEEP (not executed, then keep)
             */
            return Constants.SKIP_AND_KEEP;
        }

        /*
        None of the above, returnEXECUTE_AND_KEEP（Execute and then retain）
        None of the above, return EXECUTE_AND_KEEP (execute, then keep)
         */
        return Constants.EXECUTE_AND_KEEP;

    }


    /**
     * Verify the validity of parameters
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
            If a parameter format is defined but the parameter is empty, an error is returned
            If the parameter format is specified, but the incoming parameter is empty, an error message is returned.
             */
            if (!StringUtils.isNull(cmdParameter.getParameterValidRange()) || !StringUtils.isNull(cmdParameter.getParameterValidRegExp())) {
                if (params == null || params.get(cmdParameter.getParameterName()) == null) {
                    return Constants.PARAM_NULL + ":" + cmdParameter.getParameterName();
                }
            }

            /*
            Verify if the parameters are within the defined range
            Verify that the parameters are within the defined range
             */
            if (!paramsRangeValidation(cmdParameter, params)) {
                return Constants.PARAM_WRONG_RANGE + ":" + cmdParameter.getParameterName();
            }

            /*
            Verify whether the parameters match the defined regularity
            Verify that parameters match defined regular expressions
             */
            if (!paramsRegexValidation(cmdParameter, params)) {
                return Constants.PARAM_WRONG_FORMAT + ":" + cmdParameter.getParameterName();
            }
        }

        return null;
    }


    /**
     * Verify if the parameters are within the defined range
     * Verify that the range is correct
     *
     * @param cmdParameter Parameter format
     * @param params       Parameters of remote method
     * @return boolean
     */
    private static boolean paramsRangeValidation(CmdParameter cmdParameter, Map params) {
        /*
        No range set, verify as true
        If no range is set, Validation is true.
         */
        if (StringUtils.isNull(cmdParameter.getParameterValidRange())) {
            return true;
        }

        /*
        Format error in setting range, verify as true
        If the format in the Annotation is incorrect, Validation is true.
         */
        if (!cmdParameter.getParameterValidRange().matches(Constants.RANGE_REGEX)) {
            return true;
        }

        /*
        Parameter is empty, validation is false
        The parameter is empty, Validation is false
         */
        if (params == null || params.get(cmdParameter.getParameterName()) == null) {
            return false;
        }

        /*
        Get the set range
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
        Determine if it is within the range
        Judge whether it is within the range
         */
        return value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
    }

    /**
     * Verify whether the parameters match the defined regularity
     * Verify that parameters match defined regular expressions
     *
     * @param cmdParameter Parameter format
     * @param params       Parameters of remote method
     * @return boolean
     */
    private static boolean paramsRegexValidation(CmdParameter cmdParameter, Map params) {
        /*
        No regularization set, verify as true
        If no regex is set, Validation is true.
         */
        if (StringUtils.isNull(cmdParameter.getParameterValidRegExp())) {
            return true;
        }

        /*
        Parameter is empty, validation is false
        The parameter is empty, Validation is false
         */
        if (params == null || params.get(cmdParameter.getParameterName()) == null) {
            return false;
        }

        /*
        Determine whether to match regular expressions
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
            return MessageUtil.newFailResponse("", CMD_NOT_FOUND);
        }
        long start = System.currentTimeMillis();
//        Log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
//        Log.info("-=-=-=-::{},{}",method.toString(),cmd.getClass().getName());
//        Log.info("-=-=-=-::{}",JSONUtils.obj2json(params));
        Object invoke = method.invoke(cmd, params);
//        Log.info("cmd: {}, invoke obj: {}", cmd.getClass().getName(), invoke.getClass().getName());
        Response response = (Response) invoke;
//        Response response = (Response) method.invoke(cmd, params);
        long use = System.currentTimeMillis() - start;
        if (use > 1000) {
            Log.warn(invokeMethod + " , use:{}ms", use);
        }
        return response;
    }
}
