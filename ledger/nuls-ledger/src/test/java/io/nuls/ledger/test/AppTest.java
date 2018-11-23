package io.nuls.ledger.test;

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.server.WsServer;
import org.junit.Before;
import org.junit.Test;

public class AppTest {

    private int port;
    private WsServer server;

    @Before
    public void before() {
        this.port = 8887;
        try {
            server = new WsServer(port);
            // 注意，下面这句话不要改，模拟实现在"io.nuls.rpc.cmd.kernel"中
            server.init("kernel", null, "io.nuls.rpc.cmd.kernel");
            server.start();
            CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {

    }
}
