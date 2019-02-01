package io.nuls.account.rpc.common;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.dto.AccountKeyStoreDto;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.rpc.call.LegerCmdCall;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author: EdwardChan
 * @description: 测试过程中需要的公共的方法
 * @date: Jan. 31th 2019
 */
public class CommonRpcOperation {


    protected static int chainId = 12345;
    protected static String password = "nuls123456";
    protected static String newPassword = "c12345678";
    protected static String version = "1.0";
    protected static String success = "1";


    /**
     * 创建一个账户
     */
    public static List<String> createAccount() {
        return createAccount(chainId, 1, password);
    }

    /**
     * 创建指定数量的账户
     **/
    public static List<String> createAccount(int count) {
        return createAccount(chainId, count, password);
    }

    public static List<String> createAccount(int chainId, int count, String password) {
        List<String> accountList = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("count", count);
            params.put("password", password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            if (!AccountConstant.SUCCESS_CODE.equals(cmdResp.getResponseStatus())) {
                return null;
            }
            accountList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountList;
    }

    public static Map<String,Object> getAccountByAddress(int chainId, String address) {
        HashMap accountMap = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);

            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountByAddress", params);
            if (!AccountConstant.SUCCESS_CODE.equals(cmdResp.getResponseStatus())) {
                return null;
            }
            accountMap = ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAccountByAddress"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountMap;
    }

    public static String setAlias(String address, String alias) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("password", password);
        params.put("alias", alias);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
        System.out.println("ac_setAlias result:" + JSONUtils.obj2json(cmdResp));
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setAlias");
        assertNotNull(result);
        String txHash = (String) result.get("txHash");
        assertNotNull(txHash);
        return txHash;
    }

    /**
     * 根据地址查询别名
     */
    public static String getAliasByAddress(String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", address);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAliasByAddress", params);
        System.out.println("ac_getAliasByAddress result:" + JSONUtils.obj2json(cmdResp));
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAliasByAddress");
        assertNotNull(result);
        String alias = (String) result.get("alias");
        return alias;
    }

    /**
     * 创建多签账户
     *
     *
     *
     * **/
    public static MultiSigAccount createMultiSigAccount() throws Exception {
        MultiSigAccount multiSigAccount = new MultiSigAccount();
        List<String> accountList = createAccount(3);
        Map<String, Object> params = new HashMap<>();
        List<String> pubKeys = new ArrayList<>();
        List<byte[]> pubKeysBytesList = new ArrayList<>();
        for (String address:accountList ) {
            Map<String,Object> accountMap = getAccountByAddress(chainId,address);
            //pubKeys.add(HexUtil.encode(account.getPubKey()));
            assertNotNull(accountMap);
            Object pubKeyHexObj = accountMap.get("pubkeyHex");
            assertNotNull(pubKeyHexObj);
            String pubKeyHex = pubKeyHexObj.toString();
            pubKeys.add(pubKeyHex);
        }
        multiSigAccount.setChainId(chainId);

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
        assertNotNull(address);
        multiSigAccount.setAddress(new Address(address));
        int resultMinSigns = (int) result.get("minSigns");
        assertEquals(resultMinSigns,2);
        List<Map<String,String>> resultPubKeys = (List<Map<String,String>>) result.get("pubKeys");
        assertNotNull(resultPubKeys);
        assertEquals(resultPubKeys.size(),3);
        for (Map<String,String> map : resultPubKeys) {
            pubKeysBytesList.add(HexUtil.decode(map.get("pubKey")));
        }
        multiSigAccount.setPubKeyList(pubKeysBytesList);
        return multiSigAccount;
    }
}
