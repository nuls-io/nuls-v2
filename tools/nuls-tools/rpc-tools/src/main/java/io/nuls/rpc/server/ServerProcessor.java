package io.nuls.rpc.server;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Request;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.Map;

/**
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

                /*
                如果不是正确的Message对象，丢弃
                If it is not the correct Message object, discard
                 */
                Message message;
                try {
                    message = JSONUtils.json2pojo(msg, Message.class);
                } catch (IOException e) {
                    Log.error(e);
                    continue;
                }

                /*
                根据MessageType进行不同处理
                Processing differently according to MessageType
                 */
                MessageType messageType = MessageType.valueOf(message.getMessageType());
                switch (messageType) {
                    case NegotiateConnection:
                        /*
                        如果是握手消息，则返回确认握手成功
                        If it is a handshake message, return confirmation that the handshake was successful
                         */
                        CmdHandler.negotiateConnectionResponse(webSocket);
                        break;
                    case Request:
                        /*
                        如果是Request，则调用本地方法返回数据
                        If it is Request, call local method to return the data
                         */
                        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);

                        /*
                        如果需要一个Ack，则发送
                        Send Ack if needed
                         */
                        if (Constants.booleanString(true).equals(request.getRequestAck())) {
                            CmdHandler.ack(webSocket, message.getMessageId());
                        }

                        /*
                        调用本地方法
                        Call the local method
                         */
                        if (CmdHandler.response(webSocket, message)) {
                            synchronized (ServerRuntime.CLIENT_MESSAGE_QUEUE) {
                                /*
                                Ack只发送一次（发送之后改变requestAck的值为0）
                                Ack is sent only once (change the value of requestAck to 0 after sending)
                                 */
                                request.setRequestAck(Constants.booleanString(false));
                                message.setMessageData(request);
                                ServerRuntime.CLIENT_MESSAGE_QUEUE.add(new Object[]{webSocket, JSONUtils.obj2json(message)});
                            }
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
