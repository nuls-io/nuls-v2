package io.nuls.test;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.WsServer;
import org.junit.BeforeClass;

import java.util.HashMap;
import java.util.Map;

public class RpcTest {
    protected int chainId = 12345;
    protected  String success = "1";
    @BeforeClass
    public static void start() throws Exception {
        WsServer.mockModule();
    }

    public void getRoundInfo() throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getRoundInfo", params);
        if(success.equals(cmdResp.getResponseStatus())){

        }
    }
}
