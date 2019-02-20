package io.nuls.account.rpc.cmd;

import io.nuls.account.ServiceInitializer;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.base.data.Address;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class MultiSigAccountCmdTest {

    protected int chainId = 12345;

    protected String password = "a12345678";

    static MultiSignAccountService multiSignAccountService;
    static AccountService accountService;

    @BeforeClass
    public static void start() throws Exception {
        ServiceInitializer.initialize();
        accountService = SpringLiteContext.getBean(AccountService.class);
        multiSignAccountService = SpringLiteContext.getBean(MultiSignAccountService.class);
    }

    @Test
    public void createMultiSigAccountTest() throws Exception {
        //create 3 account
        List<Account> accountList = createAccount(3);
        Map<String, Object> params = new HashMap<>();
        List<String> pubKeys = new ArrayList<>();
        for (Account account:accountList ) {
            pubKeys.add(HexUtil.encode(account.getPubKey()));
            System.out.println(account.getAddress().getBase58());
        }
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("pubKeys", pubKeys);
        params.put("minSigns", 2);
        //create the multi sign accout
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSigAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createMultiSigAccount");
        assertNotNull(result);
        String address = (String) result.get("address");
        System.out.println("address: "+address);
        assertNotNull(address);
        int resultMinSigns = (int) result.get("minSigns");
        assertEquals(resultMinSigns,2);
        List<String> resultPubKeys = (List<String>) result.get("pubKeys");
        assertNotNull(resultPubKeys);
        assertEquals(pubKeys.size(),3);
    }

    @Test
    public void removeMultiSigAccountTest() throws Exception {
        //create 3 account
        MultiSigAccount multiSigAccount = createMultiSigAccount();
        removeMultiSigAccount(multiSigAccount.getAddress());
    }

    @Test
    public void importMultiSigAccountTest() throws Exception {
        //create
        MultiSigAccount multiSigAccount = createMultiSigAccount();
        //remove
        removeMultiSigAccount(multiSigAccount.getAddress());
        //import
        importMultiSigAccount(multiSigAccount);
    }

    public void removeMultiSigAccount(Address address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", address.getBase58());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_removeMultiSigAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_removeMultiSigAccount");
        assertTrue((boolean)result.get("value"));
    }

    public void importMultiSigAccount(MultiSigAccount  multiSigAccount) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", multiSigAccount.getAddress().getBase58());
        List<String> pubKeys = new ArrayList<>();
        for (byte[] tmp : multiSigAccount.getPubKeyList()) {
            pubKeys.add(HexUtil.encode(tmp));
        }
        params.put("pubKeys", pubKeys);
        params.put("minSigns", multiSigAccount.getM());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importMultiSigAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importMultiSigAccount");
        assertNotNull(result);
        String address = (String) result.get("address");
        assertEquals(multiSigAccount.getAddress().getBase58(),address);
    }

    public MultiSigAccount createMultiSigAccount() throws Exception {
        MultiSigAccount multiSigAccount = new MultiSigAccount();
        List<Account> accountList = createAccount(3);
        Map<String, Object> params = new HashMap<>();
        List<String> pubKeys = new ArrayList<>();
        List<byte[]> pubKeysBytesList = new ArrayList<>();
        for (Account account:accountList ) {
            pubKeys.add(HexUtil.encode(account.getPubKey()));
            pubKeysBytesList.add(account.getPubKey());
        }
        multiSigAccount.setChainId(chainId);
        multiSigAccount.setPubKeyList(pubKeysBytesList);
        multiSigAccount.setM((byte) 2);

        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", multiSigAccount.getChainId());
        params.put("pubKeys", pubKeys);
        params.put("minSigns", multiSigAccount.getM());
        //create the multi sign accout
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSigAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createMultiSigAccount");
        assertNotNull(result);
        String address = (String) result.get("address");
        multiSigAccount.setAddress(new Address(address));
        assertNotNull(address);
        int resultMinSigns = (int) result.get("minSigns");
        assertEquals(resultMinSigns,2);
        List<String> resultPubKeys = (List<String>) result.get("pubKeys");
        assertNotNull(resultPubKeys);
        assertEquals(pubKeys.size(),3);
        return multiSigAccount;
    }


    /**
     *
     * get the account
     *
     * */
    public List<Account> createAccount(int count) {
        List<Account> accountList = accountService.createAccount(chainId, count, password);
        assertNotNull(accountList);
        return accountList;
    }

}
