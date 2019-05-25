package io.nuls.test;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallOtherModule {

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }
    @Test
    public void createAccount() {
        List<String> accountList = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, 1);
            params.put("count", 3);
            params.put("password", null);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            if (!cmdResp.isSuccess()) {
                return;
            }
            accountList = (List<String>) ((HashMap)((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
            System.out.println(accountList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
}
