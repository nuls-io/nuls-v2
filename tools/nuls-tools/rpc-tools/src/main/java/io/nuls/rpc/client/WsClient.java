package io.nuls.rpc.client;

import io.nuls.rpc.model.message.Ack;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.client.thread.ResponseAutoProcessor;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * WebSocketClient的实现类
 * Implementation Class of WebSocketClient
 *
 * @author tag
 * 2018/12/29
 * */
public class WsClient extends WebSocketClient {
    /**
     * 链接关闭断开标识
     * Link Close Disconnection Identification
     * */
    private boolean connected = true;

    /**
     * 从服务端得到的握手确认
     * Handshake confirmation(NegotiateConnectionResponse) from the server
     */
    private final Queue<Message> negotiateResponseQueue = new ConcurrentLinkedQueue<>();

    /**
     * 从服务端得到的请求确认
     * Request confirmation(Ack) from the server
     */
    private final Queue<Ack> ackQueue = new ConcurrentLinkedQueue<>();

    /**
     * 从服务端得到的需要手动处理的应答消息
     * Response that need to be handled manually from the server
     */
    private final Queue<Response> responseManualQueue = new ConcurrentLinkedQueue<>();

    /**
     * 从服务端得到的自动处理的应答消息
     * Response that need to be handled Automatically from the server
     */
    private final Queue<Response> responseAutoQueue = new ConcurrentLinkedQueue<>();

    /**
     * 处理消息的线程池，现在只有一个处理线程暂时不需要线程池
     * Thread pool for message processing
    private final ExecutorService threadPool = ThreadUtils.createThreadPool(1, 100, new NulsThreadFactory("Processor"));
     * */
    private final Thread responseAutoThread = new Thread(new ResponseAutoProcessor(this));

    public WsClient(String url) throws URISyntaxException {
        super(new URI(url));
    }

    @Override
    public void onOpen(ServerHandshake shake) {
    }

    /**
     * 初步处理收到的消息，根据不同类型放入不同队列中
     * Preliminary processing of received messages, push to different queues according to different types
     *
     * @param msg 收到的消息 / Received messages
     */
    @Override
    public void onMessage(String msg) {
        try {
            /*
            收到的所有消息都放入队列，等待其他线程处理
            All messages received are queued, waiting for other threads to process
             */
            Message message = JSONUtils.json2pojo(msg, Message.class);
            switch (MessageType.valueOf(message.getMessageType())) {
                case NegotiateConnectionResponse:
                    negotiateResponseQueue.offer(message);
                    break;
                case Ack:
                    Ack ack = JSONUtils.map2pojo((Map) message.getMessageData(), Ack.class);
                    ackQueue.offer(ack);
                    break;
                case Response:
                    Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                    /*
                    Response：还要判断是否需要自动处理
                    Response: Determines whether automatic processing is required
                     */
                    if (ClientRuntime.INVOKE_MAP.containsKey(response.getRequestId())) {
                        responseAutoQueue.offer(response);
                    } else {
                        responseManualQueue.offer(response);
                    }
                    break;
                default:
                    break;
            }

        } catch (IOException e) {
            Log.error(e);
        }
    }


    @Override
    public void onClose(int paramInt, String paramString, boolean paramBoolean) {
        connected = false;
        ClientRuntime.stopWsClient(this);
    }

    @Override
    public void onError(Exception e) {
        Log.error(e);
    }

    /**
     * @return 第一条握手确认消息，The first handshake confirmed message
     */
    public Message firstMessageInNegotiateResponseQueue() {
        return negotiateResponseQueue.poll();
    }

    /**
     * @return 第一条确认消息，The first ack message
     */
    public Ack firstMessageInAckQueue() {
        return ackQueue.poll();
    }

    /**
     * @return 第一条需要手动处理的Response消息，The first Response message that needs to be handled manually
     */
    public Response firstMessageInResponseManualQueue() {
        return responseManualQueue.poll();
    }

    /**
     * @return 第一条需要自动处理的Response消息，The first Response message that needs to be handled automatically
     */
    public Response firstMessageInResponseAutoQueue() {
        return responseAutoQueue.poll();
    }

    public Queue<Message> getNegotitateResponseQueue() {
        return negotiateResponseQueue;
    }

    public Queue<Ack> getAckQueue() {
        return ackQueue;
    }

    public Queue<Response> getResponseManualQueue() {
        return responseManualQueue;
    }


    public Queue<Response> getResponseAutoQueue() {
        return responseAutoQueue;
    }

    public Queue<Message> getNegotiateResponseQueue() {
        return negotiateResponseQueue;
    }

    public Thread getResponseAutoThread() {
        return responseAutoThread;
    }

    public boolean isConnected() {
        return connected;
    }
}
