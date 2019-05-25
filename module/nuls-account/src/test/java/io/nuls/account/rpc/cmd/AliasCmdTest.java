package io.nuls.account.rpc.cmd;

import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.rpc.call.LedgerCmdCall;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.constant.TxType;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class AliasCmdTest {

    //protected static AccountService accountService;

    protected int chainId = 2;
    protected String password = "nuls123456";
    protected String newPassword = "c12345678";
    protected double version2 = 1.0;
    protected String version = "1.0";

    //static AccountService accountService;

    @BeforeClass
    public static void start() throws Exception {
//        ServiceInitializer.initialize();
//        accountService = SpringLiteContext.getBean(AccountService.class);
        NoUse.mockModule();
    }

    /**
     * get an account
     */
    public String createAnAccount() {
        String address = null;
        List<String> accountList = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("count", 1);
            params.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            if (!cmdResp.isSuccess()) {
                return null;
            }
            accountList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(accountList);
        address = accountList.get(0);
        assertNotNull(address);
        return address;
    }

    /**
     * create transaction
     */
    public Transaction createTransaction() throws Exception {
        String address = createAnAccount();
        Transaction transaction = new Transaction();
        transaction.setType(TxType.ACCOUNT_ALIAS);
        Alias alias = new Alias();
        alias.setAddress(AddressTool.getAddress(address));
        alias.setAlias("alias_" + System.currentTimeMillis());
        transaction.setTxData(alias.serialize());
        return transaction;
    }

    /**
     * get an account
     */
    public Map<String, Object> createAliasTxCommitParam() throws Exception {
        Transaction transaction = createTransaction();
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHex", RPCUtil.encode(transaction.serialize()));
        //TODO How to get secondaryDataHex?
        params.put("secondaryDataHex", "111234134adfadfadfadfad");
        return params;
    }

    /**
     * get an account
     */
    public Response aliasTxCommit(Map<String, Object> params) throws Exception {
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_aliasTxCommit", params);
        return cmdResp;
    }


    @Test
    public void setAliasTest() throws Exception {
        //create an account for test
        //String address = createAnAccount();
        String address="tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("password", password);
        params.put("alias", "alias_2019");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setAlias");
        assertNotNull(result);
        String fee = (String) result.get("txHash");
        assertNotNull(fee);
        BigInteger balance = LedgerCmdCall.getBalance(chainId, chainId, 1, address);
        System.out.println(balance.longValue());
    }

    @Test
    public void getAliasFeeTest() throws Exception {
        //create an account for test
        String address = createAnAccount();
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("alias", "alias_" + System.currentTimeMillis());
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAliasFee", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAliasFee");
        assertNotNull(result);
        String fee = (String) result.get("fee");
        assertNotNull(fee);
        //TODO EdwardChan check the maxAmount
    }


    @Test
    public void getAliasByAddressTest() throws Exception {
        //create account
        //get the aliasfee
        //set the alias
        //get the alias by address
        int count = 1;
        String address="SPWAxuodkw222367N88eavYDWRraG3930";
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAliasByAddress", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAliasByAddress");
        assertNotNull(result);
        String alias = (String) result.get("alias");
        assertNotNull(alias);
    }

    @Test
    public void isAliasUsableTest() throws Exception {
        //verify the alias which is usable
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("alias", "alias_" + System.currentTimeMillis());
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_isAliasUsable", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_isAliasUsable");
        assertNotNull(result);
        Boolean value = (Boolean) result.get("value");
        assertTrue(value);
        //verify the alias which is not usable
        //TODO


    }

    @Test
    public void accountTxValidateTest() throws Exception {

    }

    @Test
    public void aliasTxValidateTest() throws Exception {

    }

    @Test
    public void aliasTxCommitTest() throws Exception {
        Map<String, Object> params = createAliasTxCommitParam();
        Response cmdResp = aliasTxCommit(params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_aliasTxCommit");
        assertNotNull(result);
        Boolean value = (Boolean) result.get("value");
        assertTrue(value);
    }

    @Test
    public void rollbackAliasTest() throws Exception {
        Map<String, Object> aliasTxCommitParams = createAliasTxCommitParam();
        Response aliasTxCommitCmdResp = aliasTxCommit(aliasTxCommitParams);
        assertNotNull(aliasTxCommitCmdResp);
        HashMap aliasTxCommitResult = (HashMap) ((HashMap) aliasTxCommitCmdResp.getResponseData()).get("ac_aliasTxCommit");
        assertNotNull(aliasTxCommitResult);
        Boolean aliasTxCommitResultValue = (Boolean) aliasTxCommitResult.get("value");
        assertTrue(aliasTxCommitResultValue);

        Map<String, Object> rollbackParams = new HashMap<>();
        rollbackParams.put(Constants.VERSION_KEY_STR, "1.0");
        rollbackParams.put(Constants.CHAIN_ID, chainId);
        rollbackParams.put("txHex", aliasTxCommitParams.get("txHex").toString());
        rollbackParams.put("secondaryDataHex", "111234134adfadfadfadfad");
        Response rollbackCmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_aliasTxRollback", rollbackParams);
        assertNotNull(rollbackCmdResp);
        HashMap rollbackResult = (HashMap) ((HashMap) rollbackCmdResp.getResponseData()).get("ac_aliasTxRollback");
        assertNotNull(rollbackResult);
        Boolean rollbackValue = (Boolean) rollbackResult.get("value");
        assertTrue(rollbackValue);
    }

}
