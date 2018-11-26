package io.nuls.ledger.test;

import io.nuls.rpc.server.WsServer;
import org.junit.Before;
import org.junit.Test;

public class AppTest {

    @Before
    public void before() {

    }

    @Test
    public void test() {
        try {
            WsServer.mockKernel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
