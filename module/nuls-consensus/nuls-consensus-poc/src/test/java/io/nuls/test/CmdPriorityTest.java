package io.nuls.test;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CmdPriorityTest {
    private  int chainId = 2;
    private  String success = "1";

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    /**
     * 获取当前轮次信息
     * */
    public void cmdPriorityTest() throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        Response cmdResp;
        for (int i =0;i<10000;i++){
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getWholeInfo", params);
            System.out.println(cmdResp.getResponseData());
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getConsensusConfig", params);
            System.out.println(cmdResp.getResponseData());
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getRoundInfo", params);
            System.out.println(cmdResp.getResponseData());
        }
    }
}
