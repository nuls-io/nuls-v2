package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * @author tangyi
 * @date 2018/11/23
 * @description
 */
public class ClientRuntime {


    /**
     * remote module information
     * key: module name/code
     */
    public static ConcurrentMap<String, Map> roleMap = new ConcurrentHashMap<>();

    /**
     * The response of the cmd invoked through RPC
     */
    static final List<Map> CALLED_VALUE_QUEUE = Collections.synchronizedList(new ArrayList<>());

    public static final Map<String, Object[]> INVOKE_MAP = new HashMap<>();

    static ExecutorService clientThreadPool = ThreadUtils.createThreadPool(5, 500, new NulsThreadFactory("handleResponse"));

    /**
     * WsClient object that communicates with other modules
     * key: uri(ex: ws://127.0.0.1:8887)
     * value: WsClient
     */
    private static ConcurrentMap<String, WsClient> wsClientMap = new ConcurrentHashMap<>();

    /**
     * Get the WsClient object through the url
     */
    static WsClient getWsClient(String uri) throws Exception {

        if (!wsClientMap.containsKey(uri)) {
            WsClient wsClient = new WsClient(uri);
            wsClient.connect();
            Thread.sleep(1000);
            if (wsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
                wsClientMap.put(uri, wsClient);
            } else {
                Log.info("Failed to connect " + uri);
            }
        }
        return wsClientMap.get(uri);
    }

    /**
     * WsClient object that communicates with other modules
     * Used to unsubscribe
     * key: messageId
     * value: WsClient
     */
    static ConcurrentMap<String, WsClient> msgIdKeyWsClientMap = new ConcurrentHashMap<>();

    /**
     * Get the url of the module that provides the cmd through the cmd
     * The resulting url may not be unique, returning all found
     */
    static String getRemoteUri(String role) {
        Map map = roleMap.get(role);
        return map != null
                ? "ws://" + map.get(Constants.KEY_IP) + ":" + map.get(Constants.KEY_PORT)
                : null;
    }
}
