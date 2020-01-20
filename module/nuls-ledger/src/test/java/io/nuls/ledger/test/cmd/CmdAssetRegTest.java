package io.nuls.ledger.test.cmd;

import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
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
        params.put(Constants.CHAIN_ID, 1);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetRegInfo", params);
        Log.debug("response {}", JSONUtils.obj2json(response));
    }
    @Test
    public void chainAssetTxRegTest() throws Exception {
        // Build params map
        Map<String,Object> params = new HashMap<>();
        params.put("assetSymbol","ljs");
        params.put("assetName","ljs");
        params.put("initNumber",500000000);
        params.put("decimalPlace",8);
        params.put("txCreatorAddress","NULSd6HgXQht4JybnU8pScuqbsiRxTH6rr3do");
        params.put("assetOwnerAddress","NULSd6HggU7nLpj6GUP7F4c8Dr9GfRiDYcb1j");
        params.put("password","nuls123456");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "chainAssetTxReg", params);
        Log.debug("response {}", JSONUtils.obj2json(response));
    }

    @Test
    public void getAssetRegInfoByHashTest() throws Exception {
        // Build params map
        Map<String,Object> params = new HashMap<>();
        params.put("chainId",1);
        params.put("txHash","6d7fce97c656834dad86f2c47d16f2373db006b49d836a98701d8273c45264fc");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetRegInfoByHash", params);
        Log.debug("response {}", JSONUtils.obj2json(response));
    }
    @Test
    public void getAssetContractAddressTest() throws Exception {
        // Build params map
        Map<String,Object> params = new HashMap<>();
        params.put("chainId",1);
        params.put("assetId",2);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetContractAddress", params);
        Log.debug("response {}", JSONUtils.obj2json(response));
    }
    @Test
    public void getAssetRegInfoByAssetIdTest() throws Exception {
        // Build params map
        Map<String,Object> params = new HashMap<>();
        params.put("chainId",1);
        params.put("assetId",2);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetRegInfoByAssetId", params);
        Log.debug("response {}", JSONUtils.obj2json(response));
    }
    @Test
    public void getAssetContractAssetIdTest() throws Exception {
        // Build params map
        Map<String,Object> params = new HashMap<>();
        params.put("contractAddress","NULSd6HgYEvN6e8kVdrJ3pBzPFq1T6p6j6pjv");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetContractAssetId", params);
        Log.debug("response {}", JSONUtils.obj2json(response));
    }

}
