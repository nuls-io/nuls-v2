package io.nuls.eventbus.runtime;

import io.nuls.rpc.client.WsClient;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * @author naveen
 */
public class EventBusRuntime {

    public static final Queue<Object[]> CLIENT_SYNC_QUEUE = new ConcurrentLinkedQueue<>();
    public static final Queue<Object[]> EVENT_DISPATCH_QUEUE = new ConcurrentLinkedQueue<>();
    public static final Queue<Object[]> SEND_AND_RETRY_QUEUE = new ConcurrentLinkedQueue<>();

    public static Object[] firstObjArrInClientSyncQueue(){ return firstObjArrInQueue(CLIENT_SYNC_QUEUE);}

    public static Object[] firstObjArrInEventDispatchQueue(){ return firstObjArrInQueue(EVENT_DISPATCH_QUEUE);}

    public static Object[] firstObjArrInRetryQueue(){ return firstObjArrInQueue(SEND_AND_RETRY_QUEUE);}

    private static synchronized Object[] firstObjArrInQueue(Queue<Object[]> objectsQueue) {
        return objectsQueue.poll();
    }
}
