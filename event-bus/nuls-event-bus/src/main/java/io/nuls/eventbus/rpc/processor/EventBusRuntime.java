package io.nuls.eventbus.rpc.processor;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.info.Constants;
import io.nuls.tools.thread.TimeService;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class EventBusRuntime {

    public static final Queue<Object[]> CLIENT_SYNC_QUEUE = new ConcurrentLinkedQueue<>();
    public static final Queue<Object[]> EVENT_DISPATCH_QUEUE = new ConcurrentLinkedQueue<>();

    public static ConcurrentMap<String, Map<String,String>> subscribedRoleInfoMap = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, WsClient> subscribedClientMap = new ConcurrentHashMap<>();

    public static Object[] firstObjArrInClientSyncQueue(){ return firstObjArrInQueue(CLIENT_SYNC_QUEUE);}

    public static Object[] firstObjArrInEventDispatchQueue(){ return firstObjArrInQueue(EVENT_DISPATCH_QUEUE);}

    private static synchronized Object[] firstObjArrInQueue(Queue<Object[]> objectsQueue) {
        Object[] objects = objectsQueue.peek();
        objectsQueue.poll();
        return objects;
    }

    static String getRemoteUri(String role) {
        Map map = subscribedRoleInfoMap.get(role);
        return map != null
                ? "ws://" + map.get(Constants.KEY_IP) + ":" + map.get(Constants.KEY_PORT)
                : null;
    }

    static WsClient getWsClient(String url) throws Exception {
        if (!subscribedClientMap.containsKey(url)) {

            /*TODO need to change WsClient constructor to public or
            need to use ClientRuntime.getWsClient(url) but this method is package private
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
            subscribedClientMap.put(url, wsClient);wsClient.close();
        }
        return subscribedClientMap.get(url);
    }
}
