package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Request;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.TimeService;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * 客户端运行时所需要的变量和方法
 * Variables and static methods required by client runtime
 *
 * @author tangyi
 * @date 2018/11/23
 */
public class ClientRuntime {

    /**
     * Key: 角色
     * Value：角色的连接信息
     * Key: role
     * Value: Connection information of the role
     */
    public static ConcurrentMap<String, Map> roleMap = new ConcurrentHashMap<>();

    /**
     * 从服务端获取的消息集合
     * Message set received from the server
     */
    public static final List<Message> SERVER_MESSAGE_QUEUE = Collections.synchronizedList(new ArrayList<>());

    /**
     * 调用远程方法时，可以设置自动回调的本地方法
     * Key：调用远程方法的messageId
     * Value：自动回调的本地方法
     * When calling a remote method, you can set the local method for automatic callback
     * Key: MessageId that calls remote methods
     * Value: Local method of automatic callback
     */
    static final Map<String, Object[]> INVOKE_MAP = new ConcurrentHashMap<>();

    /**
     * 自动调用本地方法的线程池
     * Thread pool that automatically calls local methods
     */
    static ExecutorService clientThreadPool = ThreadUtils.createThreadPool(5, 500, new NulsThreadFactory("handleResponse"));

    /**
     * 连接其他模块的客户端集合
     * Key: 连接地址(如: ws://127.0.0.1:8887)
     * Value：WsClient对象
     * Client Set Connecting Other Modules
     * Key: url(ex: ws://127.0.0.1:8887)
     * Value: WsClient object
     */
    private static ConcurrentMap<String, WsClient> wsClientMap = new ConcurrentHashMap<>();

    /**
     * messageId对应的客户端对象，用于取消订阅的Request
     * Key：messageId
     * Value：客户端对象
     * WsClient object corresponding to messageId, used to unsubscribe the Request
     * key: messageId
     * value: WsClient
     */
    static ConcurrentMap<String, WsClient> msgIdKeyWsClientMap = new ConcurrentHashMap<>();


    /**
     * 根据角色返回角色的连接信息
     * Return the role's connection information based on the role
     */
    static String getRemoteUri(String role) {
        Map map = roleMap.get(role);
        return map != null
                ? "ws://" + map.get(Constants.KEY_IP) + ":" + map.get(Constants.KEY_PORT)
                : null;
    }


    /**
     * 获取队列中的第一个元素，然后移除队列
     * Get the first item and remove
     *
     * @return 队列的第一个元素. The first item in SERVER_RESPONSE_QUEUE.
     */
    static synchronized Message firstItemInServerMessageQueue() {
        Message message = null;
        if (ClientRuntime.SERVER_MESSAGE_QUEUE.size() > 0) {
            message = ClientRuntime.SERVER_MESSAGE_QUEUE.get(0);
            ClientRuntime.SERVER_MESSAGE_QUEUE.remove(0);
        }
        return message;
    }


    /**
     * 根据url获取客户端对象
     * Get the WsClient object through the url
     */
    static WsClient getWsClient(String url) throws Exception {
        if (!wsClientMap.containsKey(url)) {
            /*
            如果是第一次连接，则先放入集合
            If it's the first connection, put it in the collection first
             */
            WsClient wsClient = new WsClient(url);
            wsClient.connect();
            long start = TimeService.currentTimeMillis();
            while (!wsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
                if (TimeService.currentTimeMillis() - start > Constants.TIMEOUT_TIMEMILLIS) {
                    throw new Exception("Failed to connect " + url);
                }
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            }
            wsClientMap.put(url, wsClient);
        }

        /*
        从Map中返回客户端对象
        Return WsClient objects from Map
         */
        return wsClientMap.get(url);
    }


    /**
     * 构造默认Request对象
     * Constructing a default Request object
     */
    public static Request defaultRequest() {
        Request request = new Request();
        request.setRequestAck("0");
        request.setSubscriptionEventCounter("0");
        request.setSubscriptionPeriod("0");
        request.setSubscriptionRange("0");
        request.setResponseMaxSize("0");
        request.setRequestMethods(new HashMap<>(1));
        return request;
    }
}
