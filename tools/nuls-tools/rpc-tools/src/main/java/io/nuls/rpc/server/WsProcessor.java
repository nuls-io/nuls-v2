package io.nuls.rpc.server;

import io.nuls.rpc.handler.CmdHandler;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.message.MessageType;
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
public class WsProcessor implements Runnable {
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

        while (RuntimeInfo.REQUEST_QUEUE.size() > 0) {

            Object[] objects = null;
            /*
            Get the first item of the queue.
            First in, first out
             */
            synchronized (RuntimeInfo.REQUEST_QUEUE) {
                if (RuntimeInfo.REQUEST_QUEUE.size() > 0) {
                    objects = RuntimeInfo.REQUEST_QUEUE.get(0);
                    RuntimeInfo.REQUEST_QUEUE.remove(0);
                }
            }

            try {
                if (objects == null) {
                    continue;
                }

                WebSocket webSocket = (WebSocket) objects[0];
                String msg = (String) objects[1];

                Map<String, Object> messageMap;
                try {
                    messageMap = JSONUtils.json2map(msg);
                } catch (IOException e) {
                    Log.error(e);
                    Log.error("Message【" + msg + "】 doesn't match the rule, Discard!");
                    continue;
                }

                MessageType messageType = MessageType.valueOf(messageMap.get("messageType").toString());
                switch (messageType) {
                    case NegotiateConnection:
                        CmdHandler.negotiateConnectionResponse(webSocket);
                        break;
                    case Request:
                        if (CmdHandler.response(webSocket, messageMap)) {
                            synchronized (RuntimeInfo.REQUEST_QUEUE) {
                                RuntimeInfo.REQUEST_QUEUE.add(objects);
                            }
                        }
                        break;
                    case Unsubscribe:
                        CmdHandler.unsubscribe();
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
