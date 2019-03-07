package io.nuls.transaction.rpc.cmd;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
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
        NoUse.mockModule();
    }

    @Test
    public void txRegisterTest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("moduleCode", "ac");
        params.put("moduleValidator", "ac_accountTxValidate");
        List<Map> txRegisterList = new ArrayList<>();
        Map<String, Object> txParams = new HashMap<>();
        txParams.put("txType", "3");
        txParams.put("validateCmd", "ac_aliasTxValidate");
        txParams.put("commitCmd", "ac_aliasTxCommit");
        txParams.put("rollbackCmd", "ac_rollbackAlias");
        txParams.put("systemTx", false);
        txParams.put("unlockTx", false);
        txParams.put("verifySignature", true);
        txRegisterList.add(txParams);
        params.put("list", txRegisterList);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_register", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_register");
        boolean value = (Boolean) result.get("value");
        assertTrue(value);
    }

    @Test
    public void txCommitTest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("txHex", "0300bce49bca67010029215635667168583533426d7835376755754736426268717a46784d4d54393339333006e588abe5908d320001173930042301b50e18cbcd1891499450f125de50851bd09a39300100010000000000000000000000000000000000000000");
        params.put("secondaryDataHex", "");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_commit", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_register");
        boolean value = (Boolean) result.get("value");
        assertTrue(value);
    }

    @Test
    public void getTxTest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("txHash", "00208cdfa154206b175072f9acc1ccc81ccc58f3104403f0df6dc4806be67bc30b02");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTx", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_getTx");
        String txHex = (String) result.get("txHex");
        Transaction tx = Transaction.getInstance(txHex);
        assertNotNull(tx);
    }

}
