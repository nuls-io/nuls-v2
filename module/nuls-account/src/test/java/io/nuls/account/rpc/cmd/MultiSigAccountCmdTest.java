package io.nuls.account.rpc.cmd;

import io.nuls.account.ServiceInitializer;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.base.data.Address;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class MultiSigAccountCmdTest {

    protected int chainId = 2;
    protected int assetId = 1;

    protected String password = "nuls123456";

    static MultiSignAccountService multiSignAccountService;
    static AccountService accountService;
    Chain chain;

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
        }
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("pubKeys", pubKeys);
        params.put("minSigns", 2);
        //create the multi sign accout
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSignAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createMultiSignAccount");
        assertNotNull(result);
        String address = (String) result.get("address");
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
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address.getBase58());
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeMultiSignAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_removeMultiSignAccount");
        assertTrue((boolean)result.get("value"));
    }

    public void importMultiSigAccount(MultiSigAccount  multiSigAccount) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", multiSigAccount.getAddress().getBase58());
        List<String> pubKeys = new ArrayList<>();
        for (byte[] tmp : multiSigAccount.getPubKeyList()) {
            pubKeys.add(HexUtil.encode(tmp));
        }
        params.put("pubKeys", pubKeys);
        params.put("minSigns", multiSigAccount.getM());
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importMultiSignAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importMultiSignAccount");
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
        params.put(Constants.CHAIN_ID, multiSigAccount.getChainId());
        params.put("pubKeys", pubKeys);
        params.put("minSigns", multiSigAccount.getM());
        //create the multi sign accout
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSignAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createMultiSignAccount");
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
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId));
        List<Account> accountList = accountService.createAccount(chain, count, password);
        assertNotNull(accountList);
        return accountList;
    }

}
