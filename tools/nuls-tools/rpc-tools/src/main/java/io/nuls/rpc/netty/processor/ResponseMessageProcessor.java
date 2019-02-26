package io.nuls.rpc.netty.processor;

import io.netty.channel.Channel;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.invoke.KernelInvoke;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.util.Map;


/**
 * 消息处理器
 * Send message processor
 * @author  tag
 * 2019/2/25
 * */
public class ResponseMessageProcessor {
    /**
     * 与已连接的模块握手
     * Shake hands with the core module (Manager)
     *
     * @return boolean
     * @throws Exception 握手失败, handshake failed
     */
    public static boolean handshakeKernel(String url) throws Exception {
        Channel channel = ConnectManager.getConnectByUrl(url);
        if (channel == null) {
            throw new Exception("Kernel not available");
        }

        /*
        发送握手消息
        Send handshake message
         */
        Message message = MessageUtil.basicMessage(MessageType.NegotiateConnection);
        message.setMessageData(MessageUtil.defaultNegotiateConnection());
        ConnectManager.sendMessage(channel,JSONUtils.obj2json(message));

        /*
        是否收到正确的握手确认
        Whether received the correct handshake confirmation?
         */
        return receiveNegotiateConnectionResponse(ConnectManager.CHANNEL_MAP.get(url));
    }

    /**
     * 同步本地模块与核心模块（Manager）
     * 1. 发送本地信息给Manager
     * 2. 获取本地所依赖的角色的连接信息
     * <p>
     * Synchronize Local Module and Core Module (Manager)
     * 1. Send local information to Manager
     * 2. Get connection information for locally dependent roles
     *
     * @throws Exception 核心模块（Manager）不可用，Core Module (Manager) Not Available
     */
    public static void syncKernel(String kernelUrl) throws Exception {
        /*
        打造用于同步的Request
        Create Request for Synchronization
         */
        Request request = MessageUtil.defaultRequest();
        request.getRequestMethods().put("registerAPI", ConnectManager.LOCAL);
        Message message = MessageUtil.basicMessage(MessageType.Request);
        message.setMessageData(request);

        /*
        连接核心模块（Manager）
        Connect to Core Module (Manager)
         */
        Channel channel = ConnectManager.getConnectByUrl(kernelUrl);
        if(channel == null){
            throw new Exception("Kernel not available");
        }

        /*
        发送请求
        Send request
        */
        ConnectManager.sendMessage(channel,JSONUtils.obj2json(message));

        /*
        获取返回的数据，放入本地变量
        Get the returned data and place it in the local variable
         */
        Response response = receiveResponse(ConnectManager.CHANNEL_MAP.get(kernelUrl),message.getMessageId(), Constants.TIMEOUT_TIMEMILLIS);
        BaseInvoke baseInvoke = new KernelInvoke();
        baseInvoke.callBack(response);

        /*
        当有新模块注册到Kernel(Manager)时，需要同步连接信息
         */
        requestAndInvoke(ModuleE.KE.abbr, "registerAPI", JSONUtils.json2map(JSONUtils.obj2json(ConnectManager.LOCAL)), "0", "1", baseInvoke);
        Log.debug("Sync manager success. " + JSONUtils.obj2json(ConnectManager.ROLE_MAP));

        /*
        判断所有依赖的模块是否已经启动（发送握手信息）
        Determine whether all dependent modules have been started (send handshake information)
         */
        if (ConnectManager.LOCAL.getDependencies() == null) {
            ConnectManager.startService = true;
            Log.debug("Start service!");
            return;
        }

        for (String role : ConnectManager.LOCAL.getDependencies().keySet()) {
            String url = ConnectManager.getRemoteUri(role);
            if(StringUtils.isBlank(url)){
                Log.error("Dependent modules cannot be connected: " + role);
                return;
            }
            try {
                ConnectManager.getConnectByUrl(url);
            } catch (Exception e) {
                Log.error("Dependent modules cannot be connected: " + role);
                ConnectManager.startService = false;
                return;
            }
        }

        ConnectManager.startService = true;
        Log.debug("Start service!");
    }

    /**
     * 发送Request，并等待Response
     * Send Request and wait for Response
     *
     * @param role   远程方法所属的角色，The role of remote method
     * @param cmd    远程方法的命令，Command of the remote method
     * @param params 远程方法所需的参数，Parameters of the remote method
     * @return 远程方法的返回结果，Response of the remote method
     * @throws Exception 请求超时（1分钟），timeout (1 minute)
     */
    public static Response requestAndResponse(String role, String cmd, Map params) throws Exception {
        return requestAndResponse(role, cmd, params, Constants.TIMEOUT_TIMEMILLIS);
    }

    /**
     * 发送Request，并等待Response
     * Send Request and wait for Response
     *
     * @param role    远程方法所属的角色，The role of remote method
     * @param cmd     远程方法的命令，Command of the remote method
     * @param params  远程方法所需的参数，Parameters of the remote method
     * @param timeOut 超时时间, timeout millis
     * @return 远程方法的返回结果，Response of the remote method
     * @throws Exception 请求超时（timeOut），timeout (timeOut)
     */
    public static Response requestAndResponse(String role, String cmd, Map params, long timeOut) throws Exception {
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
        String messageId = sendRequest(role, request);
        return receiveResponse(ConnectManager.getConnectDataByRole(role),messageId, timeOut);
    }

    /**
     * 发送Request，并根据返回结果自动调用本地方法
     * Send the Request and automatically call the local method based on the return result
     *
     * @param role                     远程方法所属的角色，The role of remote method
     * @param cmd                      远程方法的命令，Command of the remote method
     * @param params                   远程方法所需的参数，Parameters of the remote method
     * @param subscriptionPeriod       远程方法调用频率（秒），Frequency of remote method (Second)
     * @param subscriptionEventCounter 远程方法调用频率（改变次数），Frequency of remote method (Change count)
     * @param baseInvoke               响应该结果的类的实例，Classes that respond to this result
     * @return messageId，用以取消订阅 / messageId, used to unsubscribe
     * @throws Exception 请求超时（1分钟），timeout (1 minute)
     */
    public static String requestAndInvoke(String role, String cmd, Map params, String subscriptionPeriod, String subscriptionEventCounter, BaseInvoke baseInvoke) throws Exception {
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_FALSE, subscriptionPeriod, subscriptionEventCounter);
        String messageId = sendRequest(role, request);
        ConnectManager.INVOKE_MAP.put(messageId, baseInvoke);
        return messageId;
    }

    /**
     * 发送Request，需要一个Ack作为确认，并根据返回结果自动调用本地方法
     * Send the Request, an Ack must be received as an acknowledgement, and automatically call the local method based on the return result
     *
     * @param role                     远程方法所属的角色，The role of remote method
     * @param cmd                      远程方法的命令，Command of the remote method
     * @param params                   远程方法所需的参数，Parameters of the remote method
     * @param subscriptionPeriod       远程方法调用频率（秒），Frequency of remote method (Second)
     * @param subscriptionEventCounter 远程方法调用频率（改变次数），Frequency of remote method (Change count)
     * @param baseInvoke               响应该结果的类的实例，Classes that respond to this result
     * @return messageId，用以取消订阅 / messageId, used to unsubscribe
     * @throws Exception 请求超时（1分钟），timeout (1 minute)
     */
    public static String requestAndInvokeWithAck(String role, String cmd, Map params, String subscriptionPeriod, String subscriptionEventCounter, BaseInvoke baseInvoke) throws Exception {
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_TRUE, subscriptionPeriod, subscriptionEventCounter);
        String messageId = sendRequest(role, request);
        ConnectManager.INVOKE_MAP.put(messageId, baseInvoke);
        return receiveAck(role,messageId) ? messageId : null;
    }

    /**
     * 发送Request，封装Request对象(可以一次调用多个cmd)
     * Send Request, need to wrap the Request object manually(for calling multiple methods at a time)
     *
     * @param role       远程方法所属的角色，The role of remote method
     * @param request    包含所有访问属性的Request对象，Request object containing all necessary information
     * @param baseInvoke 响应该结果的类的实例，Classes that respond to this result
     * @return messageId，用以取消订阅 / messageId, used to unsubscribe
     * @throws Exception 请求超时（1分钟），timeout (1 minute)
     */
    public static String requestAndInvoke(String role, Request request, BaseInvoke baseInvoke) throws Exception {
        if (!ConnectManager.isPureDigital(request.getSubscriptionPeriod())
                && !ConnectManager.isPureDigital(request.getSubscriptionEventCounter())) {
            throw new Exception("Wrong value: [SubscriptionPeriod][SubscriptionEventCounter]");
        }

        String messageId = sendRequest(role, request);
        ConnectManager.INVOKE_MAP.put(messageId, baseInvoke);
        if (Constants.BOOLEAN_FALSE.equals(request.getRequestAck())) {
            return messageId;
        } else {
            return receiveAck(role,messageId) ? messageId : null;
        }
    }


    /**
     * 发送Request，返回该Request的messageId
     * Send Request, return the messageId of the Request
     *
     * @param role    远程方法所属的角色，The role of remote method
     * @param request 包含所有访问属性的Request对象，Request object containing all necessary information
     * @return messageId，用以取消订阅 / messageId, used to unsubscribe
     * @throws Exception JSON格式转换错误、连接失败 / JSON format conversion error, connection failure
     */
    private static String sendRequest(String role, Request request) throws Exception {

        Message message = MessageUtil.basicMessage(MessageType.Request);
        message.setMessageData(request);

        ConnectData connectData = ConnectManager.getConnectDataByRole(role);

        ConnectManager.sendMessage(connectData.getChannel(),JSONUtils.obj2json(message));
        if (ConnectManager.isPureDigital(request.getSubscriptionPeriod())
                || ConnectManager.isPureDigital(request.getSubscriptionEventCounter())) {
            /*
            如果是需要重复发送的消息（订阅消息），记录messageId与客户端的对应关系，用于取消订阅
            If it is a message (subscription message) that needs to be sent repeatedly, record the relationship between the messageId and the WsClient
             */
            ConnectManager.MSG_ID_KEY_CHANNEL_MAP.put(message.getMessageId(), connectData);
        }

        return message.getMessageId();
    }


    /**
     * 取消订阅
     * Unsubscribe
     *
     * @param messageId 订阅时的messageId / MessageId when do subscription
     * @throws Exception JSON格式转换错误、连接失败 / JSON format conversion error, connection failure
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
        根据messageId获取WsClient，发送取消订阅命令，然后移除本地信息
        Get the WsClient according to messageId, send the unsubscribe command, and then remove the local information
         */
        ConnectData connectData = ConnectManager.MSG_ID_KEY_CHANNEL_MAP.get(messageId);
        if (connectData != null) {
            ConnectManager.sendMessage(connectData.getChannel(),JSONUtils.obj2json(message));
            Log.debug("取消订阅：" + JSONUtils.obj2json(message));
            ConnectManager.INVOKE_MAP.remove(messageId);
        }
    }

    /**
     * 是否握手成功
     * Whether shake hands successfully?
     *
     * @param connectData                   被调用模块角色链接/Called module roles
     * @return boolean
     * @throws InterruptedException    连接失败 / connection failure
     */
    public static boolean receiveNegotiateConnectionResponse(ConnectData connectData) throws Exception {
        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            /*
            获取队列中的第一个对象，如果非空，则说明握手成功
            Get the first item of the queue, If not empty, the handshake is successful.
             */
            Message message = connectData.firstMessageInNegotiateResponseQueue();
            if (message != null) {
                return true;
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }
        /*
        Timeout Error
         */
        return false;
    }


    /**
     * 根据messageId获取Response
     * Get response by messageId
     *
     * @param connectData    链接通道/Called module roles
     * @param messageId      订阅时的messageId / MessageId when do subscription
     * @return Response
     * @throws Exception JSON格式转换错误、连接失败 / JSON format conversion error, connection failure
     */
    private static Response receiveResponse(ConnectData connectData, String messageId, long timeOut) throws Exception {
        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis <= timeOut) {
            /*
            获取队列中的第一个对象
            Get the first item of the queue
             */
            Response response = connectData.firstMessageInResponseManualQueue();
            if (response == null) {
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
                continue;
            }

            if (response.getRequestId().equals(messageId)) {
                /*
                messageId匹配，说明就是需要的结果，返回
                If messageId is the same, then the response is needed
                 */
                return response;
            }

            /*
            messageId不匹配，放回队列
            Add back to the queue
             */
            connectData.getResponseManualQueue().offer(response);

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        connectData.getTimeOutMessageList().add(messageId);
        /*
        Timeout Error
         */
        return MessageUtil.newResponse(messageId, Constants.BOOLEAN_FALSE, Constants.RESPONSE_TIMEOUT);
    }


    /**
     * 获取收到Request的确认
     * Get confirmation of receipt(Ack) of Request
     *
     * @param role      被调用模块角色/Called module roles
     * @param messageId 订阅时的messageId / MessageId when do subscription
     * @return boolean
     * @throws Exception JSON格式转换错误、连接失败 / JSON format conversion error, connection failure
     */
    private static boolean receiveAck(String role,String messageId) throws Exception {

        long timeMillis = TimeService.currentTimeMillis();
        ConnectData connectData = ConnectManager.getConnectDataByRole(role);
        while (TimeService.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            /*
            获取队列中的第一个对象，如果是空，舍弃
            Get the first item of the queue, If it is an empty object, discard
             */
            Ack ack = connectData.firstMessageInAckQueue();
            if (ack == null) {
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
                continue;
            }

            if (ack.getRequestId().equals(messageId)) {
                /*
                messageId匹配，说明就是需要的结果，返回
                If messageId is the same, then the ack is needed
                 */
                return true;
            }

            /*
            messageId不匹配，放回队列
            Add back to the queue
             */
            connectData.getAckQueue().offer(ack);

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        /*
        Timeout Error
         */
        return false;
    }
}
