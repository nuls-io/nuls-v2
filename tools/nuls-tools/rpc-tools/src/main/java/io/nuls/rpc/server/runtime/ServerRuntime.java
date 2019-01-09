package io.nuls.rpc.server.runtime;

import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.*;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.thread.RequestByCountProcessor;
import io.nuls.rpc.server.thread.RequestByPeriodProcessor;
import io.nuls.rpc.server.thread.RequestSingleProcessor;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.WebSocket;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务端运行时所需要的变量和方法
 * Variables and static methods required for server-side runtime
 *
 * @author tag
 * 2018/12/29
 * */
public class ServerRuntime {
    /**
     * 本模块是否可以启动服务（所依赖模块是否可以连接）
     * Can this module start the service? (Can the dependent modules be connected?)
     */
    public static boolean startService = true;

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
     * 其他模块连接到本地的连接结合
     * Other modules connect to local connection combinations
     *
     * key:WebSocket   value:ServerData
     * */
    public static final Map<WebSocket, WsData> SERVER_DATA_MAP = new ConcurrentHashMap<>();

    /**
     * 接口被那些Message订阅
     * Interfaces have been subscribed to by those Messages
     * Key:cmd
     * Value:订阅该接口的message队列/Subscribe to the message of the interface
     * */
    public static final Map<String, CopyOnWriteArrayList<Message>> CMD_SUBSCRIBE_MESSAGE_MAP = new ConcurrentHashMap<>();

    /**
     * 订阅接口的Message对应的连接
     * Connection corresponding to Message of Subscription Interface
     * Key：订阅消息/Subscribe message
     * Value:该订阅消息所属连接/
     * */
    public static final Map<Message, WsData> MESSAGE_TO_WSDATA_MAP = new ConcurrentHashMap<>();

    /**
     * 接口被订阅次数(事件方式)
     * Number of changes in the return value of the subscribed interface
     * Key: Cmd
     * Value: subscribe count
     */
    public static final Map<String,Integer> SUBSCRIBE_COUNT = new ConcurrentHashMap<>();

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
    public static String kernelUrl = "";


    public static void setKernelUrl(String url) {
        kernelUrl = url;
    }

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
                } else {
                    throw new Exception(Constants.CMD_DUPLICATE + ":" + cmdDetail.getMethodName() + "-" + cmdDetail.getVersion());
                }
            }
        }
        LOCAL.getApiMethods().sort(Comparator.comparingDouble(CmdDetail::getVersion));
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
            count = CMD_CHANGE_COUNT.get(cmd)+1;
            CMD_CHANGE_COUNT.put(cmd, count );
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
            return 0;
        }
    }

    /**
     * 获取连接数据
     * Get connection data
     *
     * @param webSocket
     * @param webSocket
     * @return ServerData
     * */
    public static WsData getWsData(WebSocket webSocket){
        if(!SERVER_DATA_MAP.isEmpty() && SERVER_DATA_MAP.keySet().contains(webSocket)){
            return  SERVER_DATA_MAP.get(webSocket);
        }
        WsData wsData = new WsData();
        wsData.setWebSocket(webSocket);
        wsData.getThreadPool().execute(new RequestSingleProcessor(wsData));
        wsData.getThreadPool().execute(new RequestByPeriodProcessor(wsData));
        wsData.getThreadPool().execute(new RequestByCountProcessor(wsData));
        SERVER_DATA_MAP.put(webSocket,wsData);
        return wsData;
    }

    /**
     * Cmd订阅次数减1
     * Subscription times minus 1
     *
     * @param cmd
     * */
    public static void subscribeCountMinus(String cmd){
        if(SUBSCRIBE_COUNT.containsKey(cmd)){
            int count = SUBSCRIBE_COUNT.get(cmd)-1;
            if(count <= 0){
                SUBSCRIBE_COUNT.remove(cmd);
                CMD_CHANGE_COUNT.remove(cmd);
            }
            SUBSCRIBE_COUNT.put(cmd,count);
        }
    }

    /**
     * 取消订阅
     * Subscription times minus 1
     *
     * @param message
     * */
    public static void subscribeCountMinus(Message message){
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (Object method : request.getRequestMethods().keySet()) {
            String cmd = (String)method;
            subscribeCountMinus(cmd);
        }
    }

    /**
     * Cmd订阅次数加1
     * Subscription times add 1
     *
     * @param cmd
     * */
    public static void subscribeCountAdd(String cmd){
        if(!SUBSCRIBE_COUNT.containsKey(cmd)){
            SUBSCRIBE_COUNT.put(cmd,1);
        }
        int count = SUBSCRIBE_COUNT.get(cmd)+1;
        SUBSCRIBE_COUNT.put(cmd,count);
    }

    /**
     * 订阅
     * Subscription times add 1
     *
     * @param message
     * */
    public static void subscribeCountAdd(Message message){
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (Object method : request.getRequestMethods().keySet()) {
            String cmd = (String)method;
            subscribeCountAdd(cmd);
        }
    }

    /**
     * 订阅接口（按接口改变次数）
     * Subscription interface (number of changes per interface)
     *
     * @param wsData   链接信息
     * @param message  订阅消息
     * */
    public static void subscribeByEvent(WsData wsData, Message message){
        MESSAGE_TO_WSDATA_MAP.put(message,wsData);
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (String method:request.getRequestMethods().keySet()) {
            if(CMD_SUBSCRIBE_MESSAGE_MAP.containsKey(method)){
                CMD_SUBSCRIBE_MESSAGE_MAP.get(method).add(message);
            }else{
                CopyOnWriteArrayList<Message> messageList = new CopyOnWriteArrayList<>();
                messageList.add(message);
                CMD_SUBSCRIBE_MESSAGE_MAP.put(method,messageList);
            }
            subscribeCountAdd(method);
        }
    }

    /**
     * 取消订阅接口（按接口改变次数）
     * Unsubscribe interface (number of changes per interface)
     *
     * @param message    取消的订阅消息
     * */
    public static void unsubscribeByEvent(Message message){
        MESSAGE_TO_WSDATA_MAP.remove(message);
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (String method:request.getRequestMethods().keySet()) {
            if(CMD_SUBSCRIBE_MESSAGE_MAP.containsKey(method)){
                CMD_SUBSCRIBE_MESSAGE_MAP.get(method).remove(message);
            }
            subscribeCountMinus(method);
        }
    }

    /**
     * 订阅接口被调用，判断订阅该接口的事件是否触发
     * @param cmd        Command of remote method
     * @param response   Response
     */
    public static void eventTrigger(String cmd, Response response){
        /*
        找到订阅该接口的Message和WsData,然后判断订阅该接口的Message事件是否触发
         */
        CopyOnWriteArrayList<Message> messageList = CMD_SUBSCRIBE_MESSAGE_MAP.get(cmd);
        for (Message message:messageList) {
            WsData wsData = MESSAGE_TO_WSDATA_MAP.get(message);
            String key = getSubscribeKey(message.getMessageId(),cmd);
            if(wsData.getSubscribeInitCount().containsKey(key)){
                int initCount = wsData.getSubscribeInitCount().get(key);
                Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
                long eventCount = Long.parseLong(request.getSubscriptionEventCounter());
                int changeCount = addCmdChangeCount(cmd);
                if((changeCount - initCount)%eventCount == 0){
                    try {
                        response.setRequestId(message.getMessageId());
                        wsData.getRequestEventResponseQueue().put(response);
                    }catch (InterruptedException e){
                        Log.error(e);
                    }
                }
            }
        }
    }

    /**
     * 封装真正的返回结果
     * Encapsulate the true return result
     * */
    public static Response getRealResponse(String cmd,String messageId,Response response){
        Response realResponse = new Response();
        realResponse.setRequestId(messageId);
        realResponse.setResponseStatus(response.getResponseStatus());
        realResponse.setResponseComment(response.getResponseComment());
        realResponse.setResponseMaxSize(response.getResponseMaxSize());
        realResponse.setResponseData(response.getResponseData());
        return realResponse;
    }

    public static String getSubscribeKey(String messageId,String cmd){
        return cmd + "_" + messageId;
    }

    /**
     * 更新模块是否可启动状态
     * Update module bootable status
     * */
    public static void updateStatus(){
        if(!startService){
            for (String role : ServerRuntime.LOCAL.getDependencies().keySet()){
                if(!ClientRuntime.ROLE_MAP.containsKey(role)){
                    return;
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
}
