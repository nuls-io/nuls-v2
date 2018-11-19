package io.nuls.ledger.test.cmd;

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/19.
 */
public class CmdTest {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void before() throws Exception {
        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
    }

    @Test
    public void lg_test() throws Exception {
        double version = 1.0;
        String response = CmdDispatcher.call("lg_test", new Object[]{"123", "test"}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        logger.info("cmdResp {}", cmdResp);
    }
}
