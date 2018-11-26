package io.nuls.ledger.test.cmd;

import io.nuls.rpc.client.ClientRuntime;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/19.
 */
public class CmdTest {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void before() throws Exception {
        WsServer.mockModule();
        //CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
    }

    @Test
    public void lg_getBalance() throws Exception {
        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", "8096");
        params.put("address", "123");
        Response response = CmdDispatcher.requestAndResponse(ClientRuntime.ROLE_CM, "lg_getBalance", params);
        logger.info("response {}", response);
    }
}
