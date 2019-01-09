package io.nuls.rpc.client.runtime;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Message;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.thread.TimeService;
import org.java_websocket.WebSocket;
import io.nuls.rpc.client.WsClient;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 客户端运行时所需要的变量和方法
 * Variables and static methods required by client runtime
 *
 * @author tag
 * 2018/12/29
 * */
public class ClientRuntime {
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
     * 连接其他模块的客户端集合
     * Key: 连接地址(如: ws://127.0.0.1:8887), Value：WsClient对象
     * <p>
     * Client Set Connecting Other Modules
     * Key: url(ex: ws://127.0.0.1:8887), Value: WsClient object
     */
    public static final Map<String, WsClient> WS_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * messageId对应的客户端对象，用于取消订阅的Request
     * Key：messageId, Value：客户端对象
     * <p>
     * WsClient object corresponding to messageId, used to unsubscribe the Request
     * key: messageId, value: WsClient
     */
    public static final ConcurrentMap<String, WsClient> MSG_ID_KEY_WS_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 根据角色返回角色的连接信息
     * Return the role's connection information based on the role
     */
    public static String getRemoteUri(String role) {
        Map map = ROLE_MAP.get(role);
        return map == null
                ? null
                : "ws://" + map.get(Constants.KEY_IP) + ":" + map.get(Constants.KEY_PORT);
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


    /**
     * @param url 连接字符串，Connection Url
     * @return 与url对应的客户端对象，WsClient object corresponding to URL
     * @throws Exception 连接失败，Connect failed
     */
    public static WsClient getWsClient(String url) throws Exception {
        /*
        如果连接已存在，直接返回
        If the connection already exists, return directly
         */
        if(WS_CLIENT_MAP.containsKey(url) ){
            return WS_CLIENT_MAP.get(url);
        }
        /*
        如果是第一次连接，则先放入集合
        If it's the first connection, put it in the collection first
         */
        WsClient wsClient = new WsClient(url);
        wsClient.connect();
        long start = TimeService.currentTimeMillis();
        while (!wsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
            if (TimeService.currentTimeMillis() - start > Constants.MILLIS_PER_SECOND * 5) {
                throw new Exception("Failed to connect " + url);
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }
        /*
        创建并启动客户端需要的线程
        Create and start the threads needed by the client
         */
        wsClient.getResponseAutoThread().start();
        WS_CLIENT_MAP.put(url, wsClient);
        return wsClient;
    }

    /**
     * @param   role
     * @return 与url对应的客户端对象，WsClient object corresponding to URL
     * @throws Exception 连接失败，Connect failed
     */
    public static WsClient getWsClientByRole(String role) throws Exception {
        String url = getRemoteUri(role);
        if(StringUtils.isBlank(url)){
            throw new Exception("Connection module not started");
        }
        return getWsClient(url);
    }

    /**
     * 停止或断开一个连接,清除该连接相关信息
     * Stop or disconnect a connection
     * */
    public static void stopWsClient(WsClient client){
        String wsClientKey = null;
        for (Map.Entry<String,WsClient> entry:WS_CLIENT_MAP.entrySet()) {
            if(client.equals(entry.getValue())){
                wsClientKey = entry.getKey();
                WS_CLIENT_MAP.remove(wsClientKey);
                break;
            }
        }
        if(StringUtils.isBlank(wsClientKey)){
            return;
        }

        for (String role : ROLE_MAP.keySet()) {
            if(wsClientKey.equals(getRemoteUri(role))){
                ROLE_MAP.remove(role);
                break;
            }
        }

        for (Map.Entry<String,WsClient> entry:MSG_ID_KEY_WS_CLIENT_MAP.entrySet()) {
            if(client.equals(entry.getValue())){
                MSG_ID_KEY_WS_CLIENT_MAP.remove(entry.getKey());
            }
        }
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
}
