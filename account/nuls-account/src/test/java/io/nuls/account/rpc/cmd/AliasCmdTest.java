package io.nuls.account.rpc.cmd;

import io.nuls.account.ServiceInitializer;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AccountService;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class AliasCmdTest {

    //protected static AccountService accountService;

    protected int chainId = 12345;
    protected String password = "a12345678";
    protected String newPassword = "c12345678";
    protected double version2 = 1.0;
    protected String version = "1.0";

    static AccountService accountService;

    @BeforeClass
    public static void start() throws Exception {
        ServiceInitializer.initialize();
        accountService = SpringLiteContext.getBean(AccountService.class);
    }

    /**
     *
     * get an account
     *
     * */
    public Account createAnAccount() {
        Account account = null;
        List<Account> accountList = accountService.createAccount(chainId, 1, password);
        assertNotNull(accountList);
        account = accountList.get(0);
        assertNotNull(account);
        return account;
    }

    /**
     *
     * create transaction
     *
     * */
    public Transaction createTransaction() throws Exception {
        Account account = createAnAccount();
        Transaction transaction = new Transaction();
        transaction.setType(AccountConstant.TX_TYPE_ACCOUNT_ALIAS);
        Alias alias = new Alias();
        alias.setAddress(account.getAddress().getAddressBytes());
        alias.setAlias("alias_" + System.currentTimeMillis());
        transaction.setTxData(alias.serialize());
        return transaction;
    }

    /**
     *
     * get an account
     *
     * */
    public Map<String, Object> createAliasTxCommitParam() throws Exception {
        Transaction transaction = createTransaction();
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("txHex", transaction.hex());
        //TODO How to get secondaryDataHex?
        params.put("secondaryDataHex", "111234134adfadfadfadfad");
        return params;
    }

    /**
     * get an account
     *
     * */
    public Response aliasTxCommit(Map<String, Object> params) throws Exception {
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_aliasTxCommit", params);
        return cmdResp;
    }


    @Test
    public void setAliasTest() throws Exception {
        //create an account for test
        Account account = createAnAccount();
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", account.getAddress().getBase58());
        params.put("password", password);
        params.put("alias", "alias_" + System.currentTimeMillis());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setAlias");
        assertNotNull(result);
        String fee = (String) result.get("txHash");
        assertNotNull(fee);
    }

    @Test
    public void getAliasFeeTest() throws Exception {
        //create an account for test
        Account account = createAnAccount();
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", account.getAddress().getBase58());
        params.put("alias", "alias_" + System.currentTimeMillis());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAliasFee", params);
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
        System.out.println(accountService);
        int count = 1;
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("alias", "alias_" + System.currentTimeMillis());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAliasFee", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAliasFee");
        assertNotNull(result);
        String fee = (String) result.get("fee");
        assertNotNull(fee);
        assertNotNull(cmdResp);
    }

    @Test
    public void isAliasUsableTest() throws Exception {
        //verify the alias which is usable
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("alias", "alias_" + System.currentTimeMillis());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_isAliasUsable", params);
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
        rollbackParams.put("chainId", chainId);
        rollbackParams.put("txHex", aliasTxCommitParams.get("txHex").toString());
        rollbackParams.put("secondaryDataHex", "111234134adfadfadfadfad");
        Response rollbackCmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_aliasTxRollback", rollbackParams);
        assertNotNull(rollbackCmdResp);
        HashMap rollbackResult = (HashMap) ((HashMap) rollbackCmdResp.getResponseData()).get("ac_aliasTxRollback");
        assertNotNull(rollbackResult);
        Boolean rollbackValue = (Boolean) rollbackResult.get("value");
        assertTrue(rollbackValue);
    }

}
