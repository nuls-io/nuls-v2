package io.nuls.ledger.test.cmd;

import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.ledger.test.constant.TestConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CmdAssetRegTest {
    @Before
    public void before() throws Exception {
        NoUse.mockModule();
    }


    @Test
    public void getAssetRegInfoTest() throws Exception {
        // Build params map
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, TestConfig.chainId);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetRegInfo", params);
        Log.debug("response {}", JSONUtils.obj2json(response));
    }
}
