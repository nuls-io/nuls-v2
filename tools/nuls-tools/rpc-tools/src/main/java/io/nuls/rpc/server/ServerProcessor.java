package io.nuls.rpc.server;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Request;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.WebSocket;

import java.util.Map;

/**
 * 处理客户端消息的线程
 * Threads handling client messages
 *
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
public class ServerProcessor implements Runnable {
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        while (ServerRuntime.CLIENT_MESSAGE_QUEUE.size() > 0) {
            /*
            获取队列中的第一个对象，如果是空，舍弃
            Get the first item of the queue, If it is an empty object, discard
             */
            Object[] objects = ServerRuntime.firstItemInClientMessageQueue();
            if (objects == null) {
                continue;
            }

            try {
                WebSocket webSocket = (WebSocket) objects[0];
                String msg = (String) objects[1];
                Message message = JSONUtils.json2pojo(msg, Message.class);

                /*
                根据MessageType进行不同处理
                Processing differently according to MessageType
                 */
                MessageType messageType = MessageType.valueOf(message.getMessageType());
                switch (messageType) {
                    case NegotiateConnection:
                        /*
                        握手消息，返回确认握手成功
                        If it is a handshake message, return confirmation that the handshake was successful
                         */
                        CmdHandler.negotiateConnectionResponse(webSocket);
                        break;
                    case Request:
                        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);

                        if (Constants.booleanString(true).equals(request.getRequestAck())) {
                            /*
                            如果需要一个Ack，则发送
                            Send Ack if needed
                             */
                            CmdHandler.ack(webSocket, message.getMessageId());

                            /*
                            Ack只发送一次（发送之后改变requestAck的值为0）
                            Ack is sent only once (change the value of requestAck to 0 after sending)
                             */
                            request.setRequestAck(Constants.booleanString(false));
                        }
                        message.setMessageData(request);

                        /*
                        Request，调用本地方法
                        If it is Request, call the local method
                         */
                        if (CmdHandler.response(webSocket, message.getMessageId(), request)) {
                            /*
                            需要继续发送，添加回队列
                            Need to continue sending, add back to queue
                             */
                            ServerRuntime.CLIENT_MESSAGE_QUEUE.add(new Object[]{webSocket, JSONUtils.obj2json(message)});
                        }
                        break;
                    case Unsubscribe:
                        /*
                        如果是取消订阅，从订阅列表中把Request移除
                        If unsubscribed, remove Request from the subscription list
                         */
                        CmdHandler.unsubscribe(webSocket, message);
                        break;
                    default:
                        break;
                }

                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}
