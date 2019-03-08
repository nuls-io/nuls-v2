package io.nuls.rpc.netty.channel.manager;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.*;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.bootstrap.NettyClient;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.processor.RequestMessageProcessor;
import io.nuls.rpc.netty.thread.RequestByCountProcessor;
import io.nuls.rpc.netty.thread.RequestByPeriodProcessor;
import io.nuls.rpc.netty.thread.ResponseAutoProcessor;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 链接管理类
 * Link Management Class
 *
 * @author tag
 * 2019/2/21
 */
public class ConnectManager {
    private static final Lock cache_lock = new ReentrantLock();
    /**
     * 本模块是否可以启动服务（所依赖模块是否可以连接）
     * Can this module start the service? (Can the dependent modules be connected?)
     */
    public static boolean startService = false;

    /**
     * 本模块所有对外提供的接口的详细信息
     * local module(io.nuls.rpc.RegisterApi) information
     */
    public static final RegisterApi LOCAL = new RegisterApi();

    /**
     * 本模块配置信息
     * Configuration information of this module
     * Key: The key
     * Value: Config detail
     */
    public static final Map<String, ConfigItem> CONFIG_ITEM_MAP = new ConcurrentHashMap<>();

    /**
     * Key: 角色，Value：角色的连接信息
     * Key: role, Value: Connection information of the role
     */
    public static final Map<String, Map> ROLE_MAP = new ConcurrentHashMap<>();

    /**
     * 调用远程方法时，可以设置自动回调的本地方法。
     * Key：调用远程方法的messageId，Value：自动回调的本地方法
     * <p>
     * When calling a remote method, you can set the local method for automatic callback
     * Key: MessageId that calls remote methods, Value: Local method of automatic callback
     */
    public static final Map<String, BaseInvoke> INVOKE_MAP = new ConcurrentHashMap<>();

    /**
     * 链接与链接数据的集合
     * <p>
     * Key: Channel, Value: ConnectData
     */
    public static final Map<Channel, ConnectData> CHANNEL_DATA_MAP = new ConcurrentHashMap<>();

    /**
     * 角色与链接通道集合
     * KEY:ROLE
     * VALUE:Channel
     */
    public static final Map<String, Channel> ROLE_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * messageId对应链接通道对象，用于取消订阅的Request
     * Key：messageId, Value：链接通道
     * <p>
     * key: messageId, value: channel
     */
    public static final ConcurrentMap<String, Channel> MSG_ID_KEY_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 接口被那些Message订阅
     * Interfaces have been subscribed to by those Messages
     * Key:cmd
     * Value:订阅该接口的message队列/Subscribe to the message of the interface
     */
    public static final Map<String, CopyOnWriteArrayList<Message>> CMD_SUBSCRIBE_MESSAGE_MAP = new ConcurrentHashMap<>();

    /**
     * 订阅接口的Message对应的连接
     * Connection corresponding to Message of Subscription Interface
     * Key：订阅消息/Subscribe message
     * Value:该订阅消息所属连接
     */
    public static final Map<Message, ConnectData> MESSAGE_TO_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 接口被订阅次数(事件方式)
     * Number of changes in the return value of the subscribed interface
     * Key: Cmd
     * Value: subscribe count
     */
    public static final Map<String, Integer> SUBSCRIBE_COUNT = new ConcurrentHashMap<>();

    /**
     * 被订阅接口返回值改变次数（事件方式）
     * Number of changes in the return value of the subscribed interface
     * Key: Cmd
     * Value: Change count
     */
    private static final Map<String, Integer> CMD_CHANGE_COUNT = new ConcurrentHashMap<>();

    /**
     * 核心模块（Manager）的连接地址
     * URL of Core Module (Manager)
     */
    /*public static String kernelUrl = "";

    public static void setKernelUrl(String url) {
        kernelUrl = url;
    }*/

    /**
     * 根据cmd命令和版本号获取本地方法
     * Getting local methods from CMD commands and version
     *
     * @param cmd        Command of remote method
     * @param minVersion Version of remote method
     * @return CmdDetail
     */
    public static CmdDetail getLocalInvokeCmd(String cmd, double minVersion) {

        CmdDetail find = null;
        for (CmdDetail cmdDetail : LOCAL.getApiMethods()) {
            /*
            cmd不一致，跳过
            CMD inconsistency, skip
             */
            if (!cmdDetail.getMethodName().equals(cmd)) {
                continue;
            }

            /*
            大版本不一样，跳过
            Big version is different, skip
             */
            if ((int) minVersion != (int) cmdDetail.getVersion()) {
                continue;
            }

            /*
            没有备选方法，则设置当前方法为备选方法
            If there is no alternative method, set the current method as the alternative method
             */
            if (find == null) {
                find = cmdDetail;
                continue;
            }

            /*
            如果当前方法版本更高，则设置当前方法为备选方法
            If the current method version is higher, set the current method as an alternative method
             */
            if (cmdDetail.getVersion() > find.getVersion()) {
                find = cmdDetail;
            }
        }
        return find;
    }


    /**
     * 根据cmd命令获取最高版本的方法，逻辑同上
     * Getting the highest version of local methods from CMD commands
     *
     * @param cmd Command of remote method
     * @return CmdDetail
     */
    public static CmdDetail getLocalInvokeCmd(String cmd) {
        CmdDetail find = null;
        for (CmdDetail cmdDetail : LOCAL.getApiMethods()) {
            if (!cmdDetail.getMethodName().equals(cmd)) {
                continue;
            }

            if (find == null) {
                find = cmdDetail;
                continue;
            }

            if (cmdDetail.getVersion() > find.getVersion()) {
                find = cmdDetail;
            }
        }
        return find;
    }


    /**
     * 扫描指定路径，得到所有接口的详细信息
     * Scan the specified path for details of all interfaces
     *
     * @param packageName Package full path
     * @throws Exception Duplicate commands found
     */
    public static void scanPackage(String packageName) throws Exception {
        /*
        路径为空，跳过
        The path is empty, skip
         */
        if (StringUtils.isNull(packageName)) {
            return;
        }

        List<Class> classList = ScanUtil.scan(packageName);
        for (Class clz : classList) {
            Method[] methods = clz.getDeclaredMethods();
            for (Method method : methods) {
                CmdDetail cmdDetail = annotation2CmdDetail(method);
                if (cmdDetail == null) {
                    continue;
                }

                /*
                重复接口只注册一次
                Repeated interfaces are registered only once
                 */
                if (!isRegister(cmdDetail)) {
                    LOCAL.getApiMethods().add(cmdDetail);
                    RequestMessageProcessor.handlerMap.put(cmdDetail.getInvokeClass(), SpringLiteContext.getBeanByClass(cmdDetail.getInvokeClass()));
                } else {
                    throw new Exception(Constants.CMD_DUPLICATE + ":" + cmdDetail.getMethodName() + "-" + cmdDetail.getVersion());
                }
            }
        }
        LOCAL.getApiMethods().sort(Comparator.comparingDouble(CmdDetail::getVersion));
    }

    public static void addCmdDetail(Class<?> claszs) {
        Method[] methods = claszs.getDeclaredMethods();
        for (Method method : methods) {
            CmdDetail cmdDetail = annotation2CmdDetail(method);
            if (cmdDetail == null) {
                continue;
            }
                /*
                重复接口只注册一次
                Repeated interfaces are registered only once
                 */
            if (!isRegister(cmdDetail)) {
                LOCAL.getApiMethods().add(cmdDetail);
                RequestMessageProcessor.handlerMap.put(cmdDetail.getInvokeClass(), SpringLiteContext.getBeanByClass(cmdDetail.getInvokeClass()));
            }
            ;
//            else {
//                Log.warn(Constants.CMD_DUPLICATE + ":" + cmdDetail.getMethodName() + "-" + cmdDetail.getVersion());
//            }
        }
    }

    /**
     * 保存所有拥有CmdAnnotation注解的方法
     * Save all methods that have CmdAnnotation annotations
     *
     * @param method Method
     * @return CmdDetail
     */
    private static CmdDetail annotation2CmdDetail(Method method) {
        CmdDetail cmdDetail = null;
        List<CmdParameter> cmdParameters = new ArrayList<>();
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            /*
            CmdAnnotation中包含了接口的必要信息
            The CmdAnnotation contains the necessary information for the interface
             */
            if (annotation instanceof CmdAnnotation) {
                CmdAnnotation cmdAnnotation = (CmdAnnotation) annotation;
                cmdDetail = new CmdDetail();
                cmdDetail.setMethodName(cmdAnnotation.cmd());
                cmdDetail.setMethodDescription(cmdAnnotation.description());
                cmdDetail.setMethodMinEvent(cmdAnnotation.minEvent() + "");
                cmdDetail.setMethodMinPeriod(cmdAnnotation.minPeriod() + "");
                cmdDetail.setMethodScope(cmdAnnotation.scope());
                cmdDetail.setVersion(cmdAnnotation.version());
                cmdDetail.setInvokeClass(method.getDeclaringClass().getName());
                cmdDetail.setInvokeMethod(method.getName());
                continue;
            }

            /*
            参数详细说明
            Detailed description of parameters
             */
            if (annotation instanceof Parameter) {
                Parameter parameter = (Parameter) annotation;
                CmdParameter cmdParameter = new CmdParameter(parameter.parameterName(), parameter.parameterType(), parameter.parameterValidRange(), parameter.parameterValidRegExp());
                cmdParameters.add(cmdParameter);
                continue;
            }

            if (annotation instanceof Parameters) {
                Parameters parameters = (Parameters) annotation;
                for (Parameter parameter : parameters.value()) {
                    CmdParameter cmdParameter = new CmdParameter(parameter.parameterName(), parameter.parameterType(), parameter.parameterValidRange(), parameter.parameterValidRegExp());
                    cmdParameters.add(cmdParameter);
                }
            }
        }
        if (cmdDetail == null) {
            return null;
        }
        cmdDetail.setParameters(cmdParameters);
        return cmdDetail;
    }


    /**
     * 判断是否已经注册过，判断方法为：cmd+version唯一
     * Determine if the cmd has been registered
     * 1. The same cmd
     * 2. The same version
     *
     * @param sourceCmdDetail CmdDetail
     * @return boolean
     */
    private static boolean isRegister(CmdDetail sourceCmdDetail) {
        boolean exist = false;
        for (CmdDetail cmdDetail : LOCAL.getApiMethods()) {
            if (cmdDetail.getMethodName().equals(sourceCmdDetail.getMethodName()) && cmdDetail.getVersion() == sourceCmdDetail.getVersion()) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    /**
     * 返回值改变次数增加1
     * Increase the changes number of return value by 1
     *
     * @param cmd Command of remote method
     */
    public static int addCmdChangeCount(String cmd) {
        int count = 1;
        if (!CMD_CHANGE_COUNT.containsKey(cmd)) {
            CMD_CHANGE_COUNT.put(cmd, count);
        } else {
            count = CMD_CHANGE_COUNT.get(cmd) + 1;
            CMD_CHANGE_COUNT.put(cmd, count);
        }
        return count;
    }

    /**
     * 得到返回值的改变数量
     * Get current changes number of return value
     *
     * @param cmd Command of remote method
     * @return long
     */
    public static int getCmdChangeCount(String cmd) {
        try {
            return CMD_CHANGE_COUNT.get(cmd);
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Cmd订阅次数减1
     * Subscription times minus 1
     *
     * @param cmd
     */
    public static void subscribeCountMinus(String cmd) {
        if (SUBSCRIBE_COUNT.containsKey(cmd)) {
            int count = SUBSCRIBE_COUNT.get(cmd) - 1;
            if (count <= 0) {
                SUBSCRIBE_COUNT.remove(cmd);
                CMD_CHANGE_COUNT.remove(cmd);
                return;
            }
            SUBSCRIBE_COUNT.put(cmd, count);
        }
    }

    /**
     * 取消订阅
     * Subscription times minus 1
     *
     * @param message
     */
    public static void subscribeCountMinus(Message message) {
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (Object method : request.getRequestMethods().keySet()) {
            String cmd = (String) method;
            subscribeCountMinus(cmd);
        }
    }

    /**
     * Cmd订阅次数加1
     * Subscription times add 1
     *
     * @param cmd
     */
    public static void subscribeCountAdd(String cmd) {
        if (!SUBSCRIBE_COUNT.containsKey(cmd)) {
            SUBSCRIBE_COUNT.put(cmd, 1);
            return;
        }
        int count = SUBSCRIBE_COUNT.get(cmd) + 1;
        SUBSCRIBE_COUNT.put(cmd, count);
    }

    /**
     * 订阅
     * Subscription times add 1
     *
     * @param message
     */
    public static void subscribeCountAdd(Message message) {
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (Object method : request.getRequestMethods().keySet()) {
            String cmd = (String) method;
            subscribeCountAdd(cmd);
        }
    }

    /**
     * 订阅接口（按接口改变次数）
     * Subscription interface (number of changes per interface)
     *
     * @param connectData 链接信息
     * @param message     订阅消息
     */
    public static void subscribeByEvent(ConnectData connectData, Message message) {
        MESSAGE_TO_CHANNEL_MAP.put(message, connectData);
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (String method : request.getRequestMethods().keySet()) {
            if (CMD_SUBSCRIBE_MESSAGE_MAP.containsKey(method)) {
                CMD_SUBSCRIBE_MESSAGE_MAP.get(method).add(message);
            } else {
                CopyOnWriteArrayList<Message> messageList = new CopyOnWriteArrayList<>();
                messageList.add(message);
                CMD_SUBSCRIBE_MESSAGE_MAP.put(method, messageList);
            }
            subscribeCountAdd(method);
        }
    }

    /**
     * 取消订阅接口（按接口改变次数）
     * Unsubscribe interface (number of changes per interface)
     *
     * @param message 取消的订阅消息
     */
    public static void unsubscribeByEvent(Message message) {
        MESSAGE_TO_CHANNEL_MAP.remove(message);
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (String method : request.getRequestMethods().keySet()) {
            if (CMD_SUBSCRIBE_MESSAGE_MAP.containsKey(method)) {
                CMD_SUBSCRIBE_MESSAGE_MAP.get(method).remove(message);
            }
            subscribeCountMinus(method);
        }
    }

    /**
     * 订阅接口被调用，判断订阅该接口的事件是否触发
     *
     * @param cmd      Command of remote method
     * @param response Response
     */
    public static void eventTrigger(String cmd, Response response) {
        /*
        找到订阅该接口的Message和WsData,然后判断订阅该接口的Message事件是否触发
         */
        CopyOnWriteArrayList<Message> messageList = CMD_SUBSCRIBE_MESSAGE_MAP.get(cmd);
        int changeCount = addCmdChangeCount(cmd);
        for (Message message : messageList) {
            ConnectData connectData = MESSAGE_TO_CHANNEL_MAP.get(message);
            String key = getSubscribeKey(message.getMessageId(), cmd);
            if (connectData.getSubscribeInitCount().containsKey(key)) {
                int initCount = connectData.getSubscribeInitCount().get(key);
                Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
                long eventCount = Long.parseLong(request.getSubscriptionEventCounter());
                if ((changeCount - initCount) % eventCount == 0) {
                    try {
                        connectData.getRequestEventResponseQueue().put(getRealResponse(cmd, message.getMessageId(), response));
                    } catch (InterruptedException e) {
                        Log.error(e);
                    }
                }
            }
        }
    }

    /**
     * 封装真正的返回结果
     * Encapsulate the true return result
     */
    public static Response getRealResponse(String cmd, String messageId, Response response) {
        Response realResponse = new Response();
        realResponse.setRequestId(messageId);
        realResponse.setResponseStatus(response.getResponseStatus());
        realResponse.setResponseComment(response.getResponseComment());
        realResponse.setResponseMaxSize(response.getResponseMaxSize());
        realResponse.setResponseData(response.getResponseData());
        return realResponse;
    }

    public static String getSubscribeKey(String messageId, String cmd) {
        return cmd + "_" + messageId;
    }

    /**
     * 更新模块是否可启动状态
     * Update module bootAble status
     */
    public static void updateStatus() {
        if (!startService) {
            Map dependencies = LOCAL.getDependencies();
            if (dependencies != null && dependencies.size() > 0) {
                for (String role : LOCAL.getDependencies().keySet()) {
                    if (!ROLE_MAP.containsKey(role)) {
                        return;
                    }
                }
            }
            startService = true;
        }
    }

    /**
     * 本模块是否可以启动服务（所依赖模块是否可以连接）
     * Can this module start the service? (Can the dependent modules be connected?)
     */
    public static boolean isReady() {
        return startService;
    }

    /**
     * 根据角色返回角色的连接信息
     * Return the role's connection information based on the role
     */
    public static String getRemoteUri(String role) {
        Map map = ROLE_MAP.get(role);
        return map == null
                ? null
                : "ws://" + map.get(Constants.KEY_IP) + ":" + map.get(Constants.KEY_PORT) + "/ws";
    }

    /**
     * 根据channel的连接信息
     * Return the role's connection information based on the role
     */
    public static String getRemoteUri(SocketChannel channel) {
        return "ws://" + channel.remoteAddress().getHostString() + ":" + channel.remoteAddress().getPort() + "/ws";
    }

    /**
     * 获取队列中的第一个元素，然后从队列中移除
     * Get the first item and remove
     *
     * @return 队列的第一个元素. The first item in queue.
     */
    public static synchronized Message firstMessageInQueue(Queue<Message> messageQueue) {
        Message message = messageQueue.peek();
        messageQueue.poll();
        return message;
    }

    public static ConnectData getConnectDataByRole(String role) throws Exception {
        Channel channel = ROLE_CHANNEL_MAP.get(role);
        if (ROLE_CHANNEL_MAP.isEmpty() || channel == null) {
            String url = getRemoteUri(role);
            if (StringUtils.isBlank(url)) {
                throw new Exception("Connection module not started");
            }
            channel = getConnectByUrl(role);
        }
        return CHANNEL_DATA_MAP.get(channel);
    }


    public static Channel getConnectByRole(String role) throws Exception {
        if (ROLE_CHANNEL_MAP.containsKey(role)) {
            return ROLE_CHANNEL_MAP.get(role);
        }
        String url = getRemoteUri(role);
        if (StringUtils.isBlank(url)) {
            throw new Exception("Connection module not started");
        }
        Channel channel = createConnect(url);
        channel = cacheConnect(role, channel, true);
        return channel;
    }

    public static Channel getConnectByUrl(String url) throws Exception {
        /*
        如果连接已存在，直接返回
        If the connection already exists, return directly
         */
        String role = "";
        for (String key : ROLE_MAP.keySet()) {
            if (url.equals(getRemoteUri(key))) {
                role = key;
                break;
            }
        }
        if (role.isEmpty() && ROLE_CHANNEL_MAP.isEmpty() && !ROLE_MAP.isEmpty()) {
            throw new Exception("Connection module not started");
        }
        if (role.isEmpty()) {
            role = ModuleE.KE.abbr;
        }
        if (ROLE_CHANNEL_MAP.containsKey(role)) {
            return ROLE_CHANNEL_MAP.get(role);
        }
        Channel channel = createConnect(url);
        channel = cacheConnect(role, channel, true);
        return channel;
    }

    public static Channel createConnect(String url) throws Exception {
         /*
        如 果是第一次连接，则先放入集合
        If it's the first connection, put it in the collection first
         */

        Channel channel = NettyClient.createConnect(url);
        long start = TimeService.currentTimeMillis();
        while (!channel.isOpen()) {
            if (TimeService.currentTimeMillis() - start > Constants.MILLIS_PER_SECOND * 5) {
                throw new Exception("Failed to connect " + url);
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }
        return channel;
    }

    public static void createConnectData(Channel channel) {
        ConnectData connectData = new ConnectData((SocketChannel) channel);
        /*
        连接创建成功之后，启动处理连接通道中传输信息所需线程
        After the connection is created successfully, start the threads needed
        to process the transmission of information in the connection channel
        */
        connectData.getThreadPool().execute(new RequestByPeriodProcessor(connectData));
        connectData.getThreadPool().execute(new RequestByCountProcessor(connectData));
        connectData.getThreadPool().execute(new ResponseAutoProcessor(connectData));
        connectData.getThreadPool().execute(new ResponseAutoProcessor(connectData));
        CHANNEL_DATA_MAP.put(channel, connectData);
    }

    /**
     * 停止或断开一个连接,清除该连接相关信息
     * Stop or disconnect a connection
     */
    public static void disConnect(SocketChannel channel) {
        boolean isCached = false;
        String role = "";
        Iterator<Map.Entry<String, Channel>> entries = ROLE_CHANNEL_MAP.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Channel> entry = entries.next();
            if (channel.equals(entry.getValue())) {
                role = entry.getKey();
                entries.remove();
                isCached = true;
                break;
            }
        }
        if(!isCached){
            return;
        }
        ConnectData connectData = CHANNEL_DATA_MAP.remove(channel);
        connectData.setConnected(false);
        connectData.getThreadPool().shutdown();

        ROLE_MAP.remove(role);
        Log.info(role + "模块断开连接，当前在线模块列表" + ROLE_MAP);

        for (Map.Entry<String, Channel> entry : MSG_ID_KEY_CHANNEL_MAP.entrySet()) {
            if (channel.equals(entry.getValue())) {
                MSG_ID_KEY_CHANNEL_MAP.remove(entry.getKey());
                INVOKE_MAP.remove(entry.getKey());
            }
        }

        Iterator<Map.Entry<String, Channel>> msgEntries = MSG_ID_KEY_CHANNEL_MAP.entrySet().iterator();
        while (msgEntries.hasNext()) {
            Map.Entry<String, Channel> entry = entries.next();
            if (channel.equals(entry.getValue())) {
                INVOKE_MAP.remove(entry.getKey());
                msgEntries.remove();
            }
        }

        channel.close();
    }

    /**
     * 判断是否为正整数
     * Determine whether it is a positive integer
     *
     * @param string 待验证的值，Value to be verified
     * @return boolean
     */
    public static boolean isPureDigital(String string) {
        try {
            return Integer.parseInt(string) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void sendMessage(Channel channel, String message) {
//        TextWebSocketFrame frame = new TextWebSocketFrame(message);
//        channel.writeAndFlush(frame);
        channel.eventLoop().execute(() -> channel.writeAndFlush(new TextWebSocketFrame(message)));
    }

    public static void sendMessage(String moduleAbbr, Message message) throws Exception {
        sendMessage(getConnectByRole(moduleAbbr), JSONUtils.obj2json(message));
    }

    /**
     * 缓存链接信息
     * Cache link information
     */
    public synchronized static Channel cacheConnect(String role, Channel channel, boolean isSender) {
        if (!ROLE_CHANNEL_MAP.containsKey(role)
                || (isSender && role.compareTo(LOCAL.getModuleAbbreviation()) > 0)
                || (!isSender && role.compareTo(LOCAL.getModuleAbbreviation()) < 0)) {
            createConnectData(channel);
            ROLE_CHANNEL_MAP.put(role, channel);
            return channel;
        }
        return ROLE_CHANNEL_MAP.get(role);
    }
}
