package io.nuls.rpc.server.runtime;

import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import org.java_websocket.WebSocket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 做为服务器端时，一个连接里面包含的数据
 * As a server, a connection contains data
 *
 * @author tag
 * 2018/1/2
 * */
public class WsData {
    /**
     * 与某个客户端的连接
     * Connection with a client
     * */
    private WebSocket webSocket;

    /**
     * 当前链接状态
     * Current link status
     * */
    private boolean connected = true;

    /**
     * 接口最近调用时间（订阅间隔指定时间返回会用到）
     * Recent call time of interface
     * Key: Message
     * Value: Time(long)
     */
    private final Map<Message, Long> cmdInvokeTime = new ConcurrentHashMap<>();

    /**
     * 单次响应队列，Message
     * Single called queue.Message.
     */
    private final LinkedBlockingQueue<Message> requestSingleQueue = new LinkedBlockingQueue<>();

    /**
     * 多次响应队列（根据时间间隔订阅/Period），Message
     * Multiply called queue (Period).Message.
     */
    private final LinkedBlockingQueue<Message> requestPeriodLoopQueue = new LinkedBlockingQueue<>();

    /**
     * 多次响应（根据时间触发次数订阅/Event count），Message
     * Multiply called (Event count). Message.
     */
    private final List<Message> requestEventCountLoopList = new CopyOnWriteArrayList<>();

    /**
     * 当前链接订阅接口待返回结果列表
     * Current Link Subscription Interface to Return Result List
     * */
    private final LinkedBlockingQueue<Response> requestEventResponseQueue = new LinkedBlockingQueue<>();

    /**
     * 按时间订阅消息的消息ID与详细键值对
     *
     * Key:MessageID
     * Value:Message
     * */
    private final Map<String, Message> idToPeriodMessageMap = new ConcurrentHashMap<>();

    /**
     * 按时间触发次数订阅消息的消息ID与详细键值对
     *
     * Key:MessageID
     * Value:Message
     * */
    private final Map<String, Message> idToEventMessageMap = new ConcurrentHashMap<>();

    /**
     * 订阅接口时接口被改变的初始次数
     * Initial number of interface changes when subscribing to an interface
     *
     * Key:cmd_messageId
     * Value:订阅时接口已改变次数/Number of interface changes at subscription time
     * */
    private final Map<String, Integer> subscribeInitCount = new ConcurrentHashMap<>();

    /**
     * 该链接处理消息的需要的线程
     * The thread that the link needs to process the message
     * */
    private final ExecutorService threadPool = ThreadUtils.createThreadPool(3, 50, new NulsThreadFactory("ServerProcessor"));


    /**
     * 判断指定消息是否为订阅消息，且是按指定间隔时间返回数据
     * Determines whether the specified message is a subscription message and returns data at a specified interval
     *
     * @param messageId
     * */
    private boolean periodMessageIsExist(String messageId){
        return idToPeriodMessageMap.containsKey(messageId);
    }

    /**
     * 判断指定消息是否为订阅消息，且是事件触发次数返回数据
     * Determines whether the specified message is a subscription message and returns data on the number of event triggers
     *
     * @param messageId
     * */
    private boolean eventMessageIsExist(String messageId){
        return idToEventMessageMap.containsKey(messageId);
    }

    /**
     * 订阅事件（接口改变次数）
     * Subscription events (number of interface changes)
     * */
    public void subscribeByEvent(Message message){
        ServerRuntime.subscribeByEvent(this,message);
        requestEventCountLoopList.add(message);
        idToEventMessageMap.put(message.getMessageId(),message);
        addSubscribeInitCount(message);
    }

    /**
     * 取消订阅 / unsubscribe
     *
     * @param messageId
     * */
    public void unsubscribe(String messageId){
        Message message;
        if(periodMessageIsExist(messageId)){
            message = idToPeriodMessageMap.remove(messageId);
            cmdInvokeTime.remove(message);
            requestPeriodLoopQueue.remove(message);
        }else if(eventMessageIsExist(messageId)){
            message = idToEventMessageMap.remove(messageId);
            requestEventCountLoopList.remove(message);
            removeSubscribeInitCount(message);
            ServerRuntime.unsubscribeByEvent(message);
        }
    }

    /**
     * 添加订阅接口初始次数
     * Initial number of subscription interfaces added
     * */
    public void addSubscribeInitCount(Message message){
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (Object method : request.getRequestMethods().keySet()) {
            String cmd = (String)method;
            String key = ServerRuntime.getSubscribeKey(message.getMessageId(),cmd);
            if(!subscribeInitCount.containsKey(key)){
                subscribeInitCount.put(key, ServerRuntime.getCmdChangeCount(cmd));
            }
        }
    }

    /**
     * 刪除订阅接口初始次数
     * Initial number of subscription interfaces added
     * */
    public void removeSubscribeInitCount(Message message){
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);
        for (Object method : request.getRequestMethods().keySet()) {
            String cmd = (String)method;
            String key = ServerRuntime.getSubscribeKey(message.getMessageId(),cmd);
            if(subscribeInitCount.containsKey(key)){
                subscribeInitCount.remove(key);
            }
        }
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public Map<Message, Long> getCmdInvokeTime() {
        return cmdInvokeTime;
    }

    public LinkedBlockingQueue<Message> getRequestSingleQueue() {
        return requestSingleQueue;
    }

    public LinkedBlockingQueue<Message> getRequestPeriodLoopQueue() {
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

    public ExecutorService getThreadPool() {
        return threadPool;
    }
}
