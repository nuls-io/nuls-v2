package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Request;
import io.nuls.tools.thread.TimeService;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

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
     * 从服务端获取的Message，根据类型放入不同队列中
     * Message received from server, and placed in different queues according to type
     */
    public static final Queue<Message> NEGOTIATE_RESPONSE_QUEUE = new ConcurrentLinkedQueue<>();
    public static final Queue<Message> ACK_QUEUE = new ConcurrentLinkedQueue<>();
    public static final Queue<Message> RESPONSE_MANUAL_QUEUE = new ConcurrentLinkedQueue<>();
    public static final Queue<Message> RESPONSE_AUTO_QUEUE = new ConcurrentLinkedQueue<>();

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
     * 连接其他模块的客户端集合
     * Key: 连接地址(如: ws://127.0.0.1:8887)
     * Value：WsClient对象
     * Client Set Connecting Other Modules
     * Key: url(ex: ws://127.0.0.1:8887)
     * Value: WsClient object
     */
    static Map<String, WsClient> wsClientMap = new ConcurrentHashMap<>();

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
     * @return 第一条握手确认消息，The first handshake confirmed message
     */
    static Message firstMessageInNegotiateResponseQueue() {
        return firstMessageInQueue(NEGOTIATE_RESPONSE_QUEUE);
    }

    /**
     * @return 第一条确认消息，The first ack message
     */
    static Message firstMessageInAckQueue() {
        return firstMessageInQueue(ACK_QUEUE);
    }

    /**
     * @return 第一条需要手动处理的Response消息，The first Response message that needs to be handled manually
     */
    static Message firstMessageInResponseManualQueue() {
        return firstMessageInQueue(RESPONSE_MANUAL_QUEUE);
    }

    /**
     * @return 第一条需要自动处理的Response消息，The first Response message that needs to be handled automatically
     */
    static Message firstMessageInResponseAutoQueue() {
        return firstMessageInQueue(RESPONSE_AUTO_QUEUE);
    }

    /**
     * 获取队列中的第一个元素，然后从队列中移除
     * Get the first item and remove
     *
     * @return 队列的第一个元素. The first item in queue.
     */
    private static synchronized Message firstMessageInQueue(Queue<Message> messageQueue) {
        Message message = messageQueue.peek();
        messageQueue.poll();
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
