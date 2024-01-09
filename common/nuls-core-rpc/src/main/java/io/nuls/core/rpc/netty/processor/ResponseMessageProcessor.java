package io.nuls.core.rpc.netty.processor;

import io.netty.channel.Channel;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.invoke.BaseInvoke;
import io.nuls.core.rpc.invoke.KernelInvoke;
import io.nuls.core.rpc.model.InvokeBean;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.*;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.container.RequestContainer;
import io.nuls.core.rpc.netty.processor.container.ResponseContainer;
import io.nuls.core.rpc.util.LocalModuleCall;
import io.nuls.core.rpc.util.SerializeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Message processor
 * Send message processor
 *
 * @author tag
 * 2019/2/25
 */
public class ResponseMessageProcessor {

    private static final Long REGISTER_API_TIME_OUT = 180L * 1000L;
    public static final Map<String, InvokeBean> INVOKE_BEAN_MAP = new HashMap<>();
    public static final Map<String, String> ROLE_MAPPING = new HashMap<>();
    public static final Map<Integer, String> TX_TYPE_MODULE_MAP = new HashMap<>();
    static {
        ROLE_MAPPING.put(ModuleE.AC.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.BL.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.CS.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.CC.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.CM.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.LG.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.NW.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.PU.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.TX.abbr, ModuleE.NC.abbr);
        ROLE_MAPPING.put(ModuleE.SC.abbr, ModuleE.NC.abbr);
        BaseConstant.NULS_CORES_DOMAINS.addAll(ROLE_MAPPING.keySet());
    }
    /**
     * Handshake with connected modules
     * Shake hands with the core module (Manager)
     *
     * @return boolean
     * @throws Exception Handshake failed, handshake failed
     */
    public static boolean handshake(String url) throws Exception {
        Channel channel = ConnectManager.getConnectByUrl(url);
        if (channel == null) {
            throw new Exception("Kernel not available");
        }

        /*
        Send handshake message
        Send handshake message
         */
        Message message = MessageUtil.basicMessage(MessageType.NegotiateConnection);
        message.setMessageData(MessageUtil.defaultNegotiateConnection());

        ResponseContainer responseContainer = RequestContainer.putRequest(message.getMessageID());

        ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(message)));

        try {
            return responseContainer.getFuture().get(Constants.TIMEOUT_TIMEMILLIS, TimeUnit.MILLISECONDS) != null;
        } catch (Exception e) {
            //Timeout Error
            return false;
        } finally {
            RequestContainer.removeResponseContainer(message.getMessageID());
        }
    }


    /**
     * Handshake with connected modules
     * Shake hands with the core module (Manager)
     *
     * @return boolean
     * @throws Exception Handshake failed, handshake failed
     */
    public static boolean handshake(Channel channel) throws Exception {
        /*
        Send handshake message
        Send handshake message
         */
        Message message = MessageUtil.basicMessage(MessageType.NegotiateConnection);
        message.setMessageData(MessageUtil.defaultNegotiateConnection());

        ResponseContainer responseContainer = RequestContainer.putRequest(message.getMessageID());

        ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(message)));

        try {
            return responseContainer.getFuture().get(Constants.TIMEOUT_TIMEMILLIS, TimeUnit.MILLISECONDS) != null;
        } catch (Exception e) {
            //Timeout Error
            return false;
        } finally {
            RequestContainer.removeResponseContainer(message.getMessageID());
        }
    }

    public static void syncKernel(String kernelUrl) throws Exception {
        syncKernel(kernelUrl, new KernelInvoke());
    }

    /**
     * Synchronize local modules with core modules（Manager）
     * 1. Send local information toManager
     * 2. Obtain connection information for locally dependent roles
     * <p>
     * Synchronize Local Module and Core Module (Manager)
     * 1. Send local information to Manager
     * 2. Get connection information for locally dependent roles
     *
     * @throws Exception Core modules（Manager）Not available,Core Module (Manager) Not Available
     */
    public static void syncKernel(String kernelUrl, BaseInvoke callbackInvoke) throws Exception {
        /*
        Creating synchronization forRequest
        Create Request for Synchronization
         */
        Request request = MessageUtil.defaultRequest();
        request.setTimeOut(String.valueOf(REGISTER_API_TIME_OUT));
        request.getRequestMethods().put("RegisterAPI", ConnectManager.LOCAL);
        Message message = MessageUtil.basicMessage(MessageType.Request);
        message.setMessageData(request);

        /*
        Connecting core modules（Manager）
        Connect to Core Module (Manager)
         */
        Channel channel = ConnectManager.getConnectByUrl(kernelUrl);
        if (channel == null) {
            throw new Exception("Kernel not available");
        }

        ResponseContainer responseContainer = RequestContainer.putRequest(message.getMessageID());

        /*
        Send request
        Send request
        */
        ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(message)));

        /*
        Retrieve the returned data and place it in a local variable
        Get the returned entity and place it in the local variable
         */
        Response response = receiveResponse(responseContainer, REGISTER_API_TIME_OUT);
        /*
        Registration message sending failed, resend
        */
        int tryCount = 0;
        while (!response.isSuccess() && tryCount < Constants.TRY_COUNT) {
            Log.info("Failed to send registration message to the core{}second",tryCount + 1);
            responseContainer = RequestContainer.putRequest(message.getMessageID());
            ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(message)));
            response = receiveResponse(responseContainer, REGISTER_API_TIME_OUT);
            tryCount++;
        }
        if (!response.isSuccess()) {
            throw new Exception("Failed to register with the core！");
        }
        callbackInvoke.callBack(response);

        /*
        When a new module is registered toKernel(Manager)When, it is necessary to synchronize connection information
         */
        requestAndInvoke(ModuleE.KE.abbr, "RegisterAPI", JSONUtils.json2map(JSONUtils.obj2json(ConnectManager.LOCAL)), "0", "1", callbackInvoke);
        Log.debug("Sync manager success. " + JSONUtils.obj2json(ConnectManager.ROLE_MAP));
    }

    /**
     * sendRequest, and waitResponse
     * Send Request and wait for Response
     *
     * @param role   The role to which the remote method belongs,The role of remote method
     * @param cmd    Command for remote methods,Command of the remote method
     * @param params The parameters required for remote methods,Parameters of the remote method
     * @return The return result of the remote method,Response of the remote method
     * @throws Exception request timeout（1minute）,timeout (1 minute)
     */
    public static Response requestAndResponse(String role, String cmd, Map params) throws Exception {
        return requestAndResponse(role, cmd, params, Constants.TIMEOUT_TIMEMILLIS);
    }

    /**
     * sendRequest, and waitResponse
     * Send Request and wait for Response
     *
     * @param role    The role to which the remote method belongs,The role of remote method
     * @param cmd     Command for remote methods,Command of the remote method
     * @param params  The parameters required for remote methods,Parameters of the remote method
     * @param timeOut Timeout time, timeout millis
     * @return The return result of the remote method,Response of the remote method
     * @throws Exception request timeout（timeOut）,timeout (timeOut)
     */
    public static Response requestAndResponse(String role, String cmd, Map params, long timeOut) throws Exception {
        if (ModuleE.NC.abbr.equalsIgnoreCase(ConnectManager.LOCAL.getAbbreviation())) {
            String key = role + "_" + cmd;
            InvokeBean invokeBean = INVOKE_BEAN_MAP.get(key);
            if (invokeBean != null) {
                return LocalModuleCall.requestAndResponse(invokeBean, role, cmd, params, timeOut);
            } else {
                Log.warn("Empty requestAndResponse key: {}", key);
            }
        }
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
        request.setTimeOut(String.valueOf(timeOut));
        ResponseContainer responseContainer = sendRequest(role, request);
        return receiveResponse(responseContainer, timeOut);
    }

    /**
     * sendRequest, and automatically call local methods based on the returned results
     * Send the Request and automatically call the local method based on the return result
     *
     * @param role                     The role to which the remote method belongs,The role of remote method
     * @param cmd                      Command for remote methods,Command of the remote method
     * @param params                   The parameters required for remote methods,Parameters of the remote method
     * @param subscriptionPeriod       Remote method call frequency（second）,Frequency of remote method (Second)
     * @param subscriptionEventCounter Remote method call frequency（Change frequency）,Frequency of remote method (Change count)
     * @param baseInvoke               The instance of the class that corresponds to the result,Classes that respond to this result
     * @return messageId, used to unsubscribe / messageId, used to unsubscribe
     * @throws Exception request timeout（1minute）,timeout (1 minute)
     */
    public static String requestAndInvoke(String role, String cmd, Map params, String subscriptionPeriod, String subscriptionEventCounter, BaseInvoke baseInvoke) throws Exception {
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_FALSE, subscriptionPeriod, subscriptionEventCounter);
        ResponseContainer responseContainer = sendRequest(role, request);
        ConnectManager.INVOKE_MAP.put(responseContainer.getMessageId(), baseInvoke);

        RequestContainer.removeResponseContainer(responseContainer.getMessageId());

        return responseContainer.getMessageId();
    }

    /**
     * sendRequest, requires aAckAs confirmation, and automatically call local methods based on the returned results
     * Send the Request, an Ack must be received as an acknowledgement, and automatically call the local method based on the return result
     *
     * @param role                     The role to which the remote method belongs,The role of remote method
     * @param cmd                      Command for remote methods,Command of the remote method
     * @param params                   The parameters required for remote methods,Parameters of the remote method
     * @param subscriptionPeriod       Remote method call frequency（second）,Frequency of remote method (Second)
     * @param subscriptionEventCounter Remote method call frequency（Change frequency）,Frequency of remote method (Change count)
     * @param baseInvoke               The instance of the class that corresponds to the result,Classes that respond to this result
     * @return messageId, used to unsubscribe / messageId, used to unsubscribe
     * @throws Exception request timeout（1minute）,timeout (1 minute)
     */
    public static String requestAndInvokeWithAck(String role, String cmd, Map params, String subscriptionPeriod, String subscriptionEventCounter, BaseInvoke baseInvoke) throws Exception {
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_TRUE, subscriptionPeriod, subscriptionEventCounter);
        ResponseContainer responseContainer = sendRequest(role, request);
        ConnectManager.INVOKE_MAP.put(responseContainer.getMessageId(), baseInvoke);
        return receiveResponse(responseContainer, Constants.TIMEOUT_TIMEMILLIS) != null ? responseContainer.getMessageId() : null;
    }

    /**
     * sendRequestEncapsulationRequestobject(Can call multiple at oncecmd)
     * Send Request, need to wrap the Request object manually(for calling multiple methods at a time)
     *
     * @param role       The role to which the remote method belongs,The role of remote method
     * @param request    Contains all access propertiesRequestObject,Request object containing all necessary information
     * @param baseInvoke The instance of the class that corresponds to the result,Classes that respond to this result
     * @return messageId, used to unsubscribe / messageId, used to unsubscribe
     * @throws Exception request timeout（1minute）,timeout (1 minute)
     */
    public static String requestAndInvoke(String role, Request request, BaseInvoke baseInvoke) throws Exception {
        if (!ConnectManager.isPureDigital(request.getSubscriptionPeriod())
                && !ConnectManager.isPureDigital(request.getSubscriptionEventCounter())) {
            throw new Exception("Wrong value: [SubscriptionPeriod][SubscriptionEventCounter]");
        }
        ResponseContainer responseContainer = sendRequest(role, request);
        ConnectManager.INVOKE_MAP.put(responseContainer.getMessageId(), baseInvoke);
        if (Constants.BOOLEAN_FALSE.equals(request.getRequestAck())) {
            return responseContainer.getMessageId();
        } else {
            return receiveResponse(responseContainer, Constants.TIMEOUT_TIMEMILLIS) != null ? responseContainer.getMessageId() : null;
        }
    }

    /**
     * sendRequest, do not accept returns
     * Send Request and wait for Response
     *
     * @param role       The role to which the remote method belongs,The role of remote method
     * @param request    Command for remote methods,Command of the remote method
     * @return The return result of the remote method,Response of the remote method
     * @throws Exception request timeout（1minute）,timeout (1 minute)
     */
    public static String requestOnly(String role, Request request) throws Exception {
        if (ModuleE.NC.abbr.equalsIgnoreCase(ConnectManager.LOCAL.getAbbreviation())) {
            Map<String, Object> requestMethods = request.getRequestMethods();
            Map.Entry<String, Object> next = requestMethods.entrySet().iterator().next();
            String key = role + "_" + next.getKey();
            InvokeBean invokeBean = INVOKE_BEAN_MAP.get(key);
            if (invokeBean != null) {
                return LocalModuleCall.requestOnly(invokeBean, next.getValue(), role, request);
            } else {
                Log.warn("Empty requestOnly key: {}", key);
            }
        }
        String mappingRole = ROLE_MAPPING.getOrDefault(role, role);
        Message message = MessageUtil.basicMessage(MessageType.RequestOnly);
        message.setMessageData(request);
        Channel channel = ConnectManager.getConnectByRole(mappingRole);
        if (!channel.isWritable()) {
            Log.info("Current request backlog is too high,Waiting for request processing");
            return "0";
        }
        ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(message)));
        return message.getMessageID();
    }

    /**
     * sendRequest, return to theRequestofmessageId
     * Send Request, return the messageId of the Request
     *
     * @param role    The role to which the remote method belongs,The role of remote method
     * @param request Contains all access propertiesRequestObject,Request object containing all necessary information
     * @return messageId, used to unsubscribe / messageId, used to unsubscribe
     * @throws Exception JSONFormat conversion error、connection failed / JSON format conversion error, connection failure
     */
    private static ResponseContainer sendRequest(String role, Request request) throws Exception {
        String mappingRole = ROLE_MAPPING.getOrDefault(role, role);
        Message message = MessageUtil.basicMessage(MessageType.Request);
        message.setMessageData(request);
        Channel channel = ConnectManager.getConnectByRole(mappingRole);

        ResponseContainer responseContainer = RequestContainer.putRequest(message.getMessageID());

        ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(message)));
        if (ConnectManager.isPureDigital(request.getSubscriptionPeriod())
                || ConnectManager.isPureDigital(request.getSubscriptionEventCounter())) {
            /*
            If it is a message that needs to be sent repeatedly（Subscription message）RecordmessageIdCorrespondence with the client, used for unsubscribing
            If it is a message (subscription message) that needs to be sent repeatedly, record the relationship between the messageId and the WsClient
             */
            ConnectManager.MSG_ID_KEY_CHANNEL_MAP.put(message.getMessageID(), channel);
        }

        return responseContainer;
    }

    /**
     * Unsubscribe
     * Unsubscribe
     *
     * @param messageId At subscription timemessageId / MessageId when do subscription
     * @throws Exception JSONFormat conversion error、connection failed / JSON format conversion error, connection failure
     */
    public static void sendUnsubscribe(String messageId) throws Exception {
        if (messageId == null) {
            return;
        }

        Message message = MessageUtil.basicMessage(MessageType.Unsubscribe);
        Unsubscribe unsubscribe = new Unsubscribe();
        unsubscribe.setUnsubscribeMethods(new String[]{messageId});
        message.setMessageData(unsubscribe);

        /*
        according tomessageIdobtainWsClientSend unsubscribe command and then remove local information
        Get the WsClient according to messageId, send the unsubscribe command, and then remove the local information
         */
        Channel channel = ConnectManager.MSG_ID_KEY_CHANNEL_MAP.get(messageId);
        if (channel != null) {
            ConnectManager.sendMessage(channel, SerializeUtil.getBuffer(JSONUtils.obj2ByteArray(message)));
            Log.debug("Unsubscribe：" + JSONUtils.obj2json(message));
            ConnectManager.INVOKE_MAP.remove(messageId);
        }
    }

    /**
     * according tomessageIdobtainResponse
     * Get response by messageId
     *
     * @param responseContainer Result container/ Result container
     * @param timeOut           Time out, in milliseconds / Timeout, in milliseconds
     * @return Response
     */
    private static Response receiveResponse(ResponseContainer responseContainer, long timeOut) {
        try {
            return responseContainer.getFuture().get(timeOut, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if(timeOut > 0){
                //Timeout Error
                return MessageUtil.newFailResponse(responseContainer.getMessageId(), CommonCodeConstanst.REQUEST_TIME_OUT);
            }else{
                return MessageUtil.newSuccessResponse(responseContainer.getMessageId());
            }
        } finally {
            RequestContainer.removeResponseContainer(responseContainer.getMessageId());
        }
    }
}
