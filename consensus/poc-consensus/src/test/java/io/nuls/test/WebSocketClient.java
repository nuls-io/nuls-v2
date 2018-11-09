package io.nuls.test;

import io.nuls.rpc.cmd.CmdDispatcher;
import org.junit.Test;

public class WebSocketClient {
    @Test
    public void test() throws Exception{
        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
        System.out.println(CmdDispatcher.call("cs_createAgent", null, 1.0));
    }
}
