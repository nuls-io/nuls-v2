package io.nuls.transaction.rpc.cmd;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.parse.JSONUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/30
 */
public class TransactionCmdTest {

    protected int chainId = 12345;
    protected String version = "1.0";
    //protected String moduleCode = "ac";

    @BeforeClass
    public static void start() throws Exception {
        //WsServer.mockModule();
    }

    @Test
    public void txRegisterTest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("moduleCode", "ac");
        params.put("moduleValidator", "ac_accountTxValidate");
        List<Map> txRegisterList=new ArrayList<>();
        Map<String, Object> txParams = new HashMap<>();
        txParams.put("txType", "3");
        txParams.put("validator", "ac_aliasTxValidate");
        txParams.put("commit", "ac_aliasTxCommit");
        txParams.put("rollback", "ac_rollbackAlias");
        txParams.put("systemTx", false);
        txParams.put("unlockTx", false);
        txParams.put("verifySignature", true);
        txRegisterList.add(txParams);
        params.put("list",txRegisterList);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "tx_register", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_register");
        boolean value = (Boolean) result.get("value");
        assertTrue(value);
    }

}
