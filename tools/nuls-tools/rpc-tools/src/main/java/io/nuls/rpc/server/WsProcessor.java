package io.nuls.rpc.server;

import io.nuls.rpc.handler.WebSocketHandler;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import org.java_websocket.WebSocket;

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
            synchronized (RuntimeInfo.REQUEST_QUEUE) {
                if (RuntimeInfo.REQUEST_QUEUE.size() > 0) {
                    objects = RuntimeInfo.REQUEST_QUEUE.get(0);
                    RuntimeInfo.REQUEST_QUEUE.remove(0);
                }
            }

            try {
                if (objects != null) {
                    WebSocket webSocket = (WebSocket) objects[0];
                    String message = (String) objects[1];
                    webSocket.send(WebSocketHandler.callCmd(message));
                }

                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
