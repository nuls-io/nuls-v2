package io.nuls.core.rpc.netty.channel;

import io.netty.channel.socket.SocketChannel;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.RequestOnly;
import io.nuls.core.rpc.model.message.Message;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Link Foundation Class
 * Link base class
 *
 * @author tag
 * 2019/2/21
 */
public class ConnectData {

    private final SocketChannel channel;

    /**
     * Link closure and disconnection identification
     * Link Close Disconnection Identification
     */
    private boolean connected = true;

    /**
     * currentRequestOnlyQueue occupied memory size
     * Current memory size of RequestOnly queue
     * */
    private long requestOnlyQueueMemSize = 0;

    /**
     * The automatically processed response message obtained from the server
     * Response that need to be handled Automatically from the server
     */
    private final LinkedBlockingQueue<Response> responseAutoQueue = new LinkedBlockingQueue<>();

    /**
     * The client does not require corresponding requests
     * Client does not need corresponding requests
     */
    private final LinkedBlockingQueue<RequestOnly> requestOnlyQueue = new LinkedBlockingQueue<>(Constants.QUEUE_SIZE);

    /**
     * Request timed out
     * Request for timeout
     */
    private final List<String> timeOutMessageList = new ArrayList<>();

    /**
     * Last interface call time（Subscription interval specified time return will use）
     * Recent call time of interface
     * Key: Message
     * Value: Time(long)
     */
    private final Map<Message, Long> cmdInvokeTime = new ConcurrentHashMap<>();

    /**
     * Multiple response queues（Subscription based on time intervals/Period）,Message
     * Multiply called queue (Period).Message.
     */
    private final LinkedBlockingQueue<Object[]> requestPeriodLoopQueue = new LinkedBlockingQueue<>();

    /**
     * Multiple responses（Subscription based on the number of time triggers/Event count）,Message
     * Multiply called (Event count). Message.
     */
    private final List<Message> requestEventCountLoopList = new CopyOnWriteArrayList<>();

    /**
     * List of results to be returned from the current link subscription interface
     * Current Link Subscription Interface to Return Result List
     */
    private final LinkedBlockingQueue<Response> requestEventResponseQueue = new LinkedBlockingQueue<>();

    /**
     * Message subscription by timeIDPair with detailed key values
     * <p>
     * Key:MessageID
     * Value:Message
     */
    private final Map<String, Message> idToPeriodMessageMap = new ConcurrentHashMap<>();

    /**
     * Message subscription based on time triggered timesIDPair with detailed key values
     * <p>
     * Key:MessageID
     * Value:Message
     */
    private final Map<String, Message> idToEventMessageMap = new ConcurrentHashMap<>();

    /**
     * The initial number of times the interface is changed when subscribing to it
     * Initial number of interface changes when subscribing to an interface
     * <p>
     * Key:cmd_messageId
     * Value:The number of times the interface has changed during subscription/Number of interface changes at subscription time
     */
    private final Map<String, Integer> subscribeInitCount = new ConcurrentHashMap<>();


    /**
     * Event listening for storage connection disconnection
     */
    private final List<EventListener> closeEventListenerList = new ArrayList<>();

    /**
     * Determine whether the specified message is a subscription message and whether the data is returned at the specified interval time
     * Determines whether the specified message is a subscription message and returns entity at a specified interval
     *
     * @param messageId
     */
    private boolean periodMessageIsExist(String messageId) {
        return idToPeriodMessageMap.containsKey(messageId);
    }

    /**
     * Determine whether the specified message is a subscription message and the number of event triggers returns data
     * Determines whether the specified message is a subscription message and returns entity on the number of event triggers
     *
     * @param messageId
     */
    private boolean eventMessageIsExist(String messageId) {
        return idToEventMessageMap.containsKey(messageId);
    }

    /**
     * The thread required for processing messages in this link
     * The thread that the link needs to process the message
     */
    private final ExecutorService threadPool = ThreadUtils.createThreadPool(6, 100, new NulsThreadFactory("ServerProcessor"));

    /**
     * Subscription events（Number of interface changes）
     * Subscription events (number of interface changes)
     */
    public void subscribeByEvent(Message message, Request request) {
        ConnectManager.subscribeByEvent(this, message, request);
        idToEventMessageMap.put(message.getMessageID(), message);
        addSubscribeInitCount(message);
        requestEventCountLoopList.add(message);
    }

    /**
     * Unsubscribe / unsubscribe
     *
     * @param messageId
     */
    public void unsubscribe(String messageId) {
        Message message;
        if (periodMessageIsExist(messageId)) {
            message = idToPeriodMessageMap.remove(messageId);
            cmdInvokeTime.remove(message);
            requestPeriodLoopQueue.remove(message);
        } else if (eventMessageIsExist(messageId)) {
            message = idToEventMessageMap.remove(messageId);
            requestEventCountLoopList.remove(message);
            removeSubscribeInitCount(message);
            ConnectManager.unsubscribeByEvent(message);
        }
    }

    /**
     * Add subscription interface initial count
     * Initial number of subscription interfaces added
     */
    public void addSubscribeInitCount(Message message) {
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (Object method : request.getRequestMethods().keySet()) {
            String cmd = (String) method;
            String key = ConnectManager.getSubscribeKey(message.getMessageID(), cmd);
            if (!subscribeInitCount.containsKey(key)) {
                subscribeInitCount.put(key, ConnectManager.getCmdChangeCount(cmd));
            }
        }
    }

    /**
     * Delete subscription interface initial count
     * Initial number of subscription interfaces added
     */
    public void removeSubscribeInitCount(Message message) {
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (Object method : request.getRequestMethods().keySet()) {
            String cmd = (String) method;
            String key = ConnectManager.getSubscribeKey(message.getMessageID(), cmd);
            if (subscribeInitCount.containsKey(key)) {
                subscribeInitCount.remove(key);
            }
        }
    }

    public ConnectData(SocketChannel channel) {
        this.channel = channel;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        if (!connected) {
            requestEventResponseQueue.clear();
            requestEventResponseQueue.offer(new Response());
            responseAutoQueue.clear();
            responseAutoQueue.offer(new Response());
            requestPeriodLoopQueue.clear();
            emitCloseEvent(); //Broadcast connection closure event
        }
    }

    public Map<Message, Long> getCmdInvokeTime() {
        return cmdInvokeTime;
    }

    public LinkedBlockingQueue<Object[]> getRequestPeriodLoopQueue() {
        return requestPeriodLoopQueue;
    }

    public List<Message> getRequestEventCountLoopList() {
        return requestEventCountLoopList;
    }

    public LinkedBlockingQueue<Response> getRequestEventResponseQueue() {
        return requestEventResponseQueue;
    }

    public Map<String, Message> getIdToPeriodMessageMap() {
        return idToPeriodMessageMap;
    }

    public Map<String, Message> getIdToEventMessageMap() {
        return idToEventMessageMap;
    }

    public Map<String, Integer> getSubscribeInitCount() {
        return subscribeInitCount;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public LinkedBlockingQueue<Response> getResponseAutoQueue() {
        return responseAutoQueue;
    }

    public List<String> getTimeOutMessageList() {
        return timeOutMessageList;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public LinkedBlockingQueue<RequestOnly> getRequestOnlyQueue() {
        return requestOnlyQueue;
    }

    public long getRequestOnlyQueueMemSize() {
        return requestOnlyQueueMemSize;
    }

    public void setRequestOnlyQueueMemSize(long requestOnlyQueueMemSize) {
        this.requestOnlyQueueMemSize = requestOnlyQueueMemSize;
    }

    public boolean requestOnlyQueueReachLimit(){
        return Constants.QUEUE_MEM_LIMIT_SIZE <= this.requestOnlyQueueMemSize;
    }

    public void addRequestOnlyQueueMemSize(long requestOnlyMemSize) {
        this.requestOnlyQueueMemSize += requestOnlyMemSize;
    }

    public void subRequestOnlyQueueMemSize(long requestOnlyMemSize) {
        this.requestOnlyQueueMemSize -= requestOnlyMemSize;
    }

    /**
     * Listening for connection closure events
     *
     * @param eventListener
     */
    public void addCloseEvent(EventListener eventListener) {
        this.closeEventListenerList.add(eventListener);
    }

    /**
     * Trigger connection closure event
     */
    public void emitCloseEvent() {
        this.closeEventListenerList.forEach(f -> f.apply());
    }

}
