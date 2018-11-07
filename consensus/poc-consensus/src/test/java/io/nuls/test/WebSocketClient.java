package io.nuls.test;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.cmd.CmdDispatcher;
import org.java_websocket.WebSocket;
import org.junit.Test;

public class WebSocketClient {
    @Test
    public void test() throws Exception{
        WsClient client = new WsClient("ws://127.0.0.1:8887");
        client.connect();
        while (!client.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
            System.out.println("还没有打开");
        }
        System.out.println("建立websocket连接");
        System.out.println(CmdDispatcher.call("cs_createAgent", null, 1.0));
        client.close();
    }
}
