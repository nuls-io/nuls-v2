package io.nuls.rpc.server;

import io.nuls.rpc.handler.WebSocketHandler;
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

        while (RuntimeInfo.requestQueue.size() > 0) {

            Object[] objects = null;
            synchronized (RuntimeInfo.requestQueue) {
                if (RuntimeInfo.requestQueue.size() > 0) {
                    objects = RuntimeInfo.requestQueue.get(0);
                    RuntimeInfo.requestQueue.remove(0);
                }
            }

//            WebSocket webSocket = null;
//            String message = null;
            try {
                if (objects != null) {
                    WebSocket webSocket = (WebSocket) objects[0];
                    String message = (String) objects[1];
//                    if (webSocket != null && message != null) {
                    webSocket.send(WebSocketHandler.callCmd(message));
//                    }
                }

                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                //RuntimeInfo.requestQueue.add(new Object[]{webSocket, message});
            }
        }

    }
}
