package io.nuls.transaction;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.rpc.call.LedgerCall;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.LOG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCommonUtil {

    public static final int CHAIN_ID = 2;
    public static final int ASSET_CHAIN_ID = CHAIN_ID;
    public static final int ASSET_ID = 1;
    public static final String VERSION = "1.0";
    public static final String PASSWORD = "nuls123456";
    public static String SOURCE_ADDRESS = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
//    public static String SOURCE_ADDRESS = "tNULSeBaMth1hiT6khgSjDqazSEpfNxM5D1Mqs";

    public static List<String> createAccounts(int num) throws Exception {
        LOG.info("##########create " + num + " accounts##########");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < num / 100; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, VERSION);
            params.put(Constants.CHAIN_ID, CHAIN_ID);
            params.put("count", 100);
            params.put("password", PASSWORD);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            assertTrue(response.isSuccess());
            list.addAll((List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list"));
            assertEquals(100 * (i + 1), list.size());
        }
        if (num % 100 > 0) {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, VERSION);
            params.put(Constants.CHAIN_ID, CHAIN_ID);
            params.put("count", num % 100);
            params.put("password", PASSWORD);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            assertTrue(response.isSuccess());
            list.addAll((List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list"));
            assertEquals(num, list.size());
        }
        return list;
    }

    public static BigInteger getBalance(Chain chain, String address) throws Exception {
        return LedgerCall.getBalance(chain, AddressTool.getAddress(address), CHAIN_ID, ASSET_ID);
    }

    public static void importAccountByKeystorePath() {
        try {
            File path = new File("C:\\Users\\alvin\\Desktop\\alpha3");
            for (File file : path.listFiles()) {
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, VERSION);
                params.put(Constants.CHAIN_ID, CHAIN_ID);
                params.put("keyStore", RPCUtil.encode(Files.readString(file.toPath()).getBytes()));
                params.put("password", PASSWORD);
                params.put("overwrite", true);
                Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
                assertTrue(cmdResp.isSuccess());
            }
            getAccountList().forEach(e -> System.out.println(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAccountList() throws Exception {
        Map params = new HashMap();
        params.put("chainId", CHAIN_ID);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountList", params);
        Object o = ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAccountList")).get("list");
        List<String> result = new ArrayList<>();
        List list = (List) o;
        for (Object o1 : list) {
            Map map = (Map) o1;
            String address = (String) map.get("address");
            result.add(address);
        }
        return result;
    }

    public static Response transfer(String from, String to, String s) throws Exception {
        BigInteger bigInteger = new BigInteger(s);
        if (bigInteger.compareTo(new BigInteger("10000000")) < 0) {
            return MessageUtil.newSuccessResponse("");
        }
        Map transferMap = getTxMap(from, to, s);
        return ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
    }

    public static Map getTxMap(String from, String to, String s) {
        Map transferMap = new HashMap();
        transferMap.put("chainId", CHAIN_ID);
        transferMap.put("remark", "transfer test");
        List<CoinDTO> inputs = new ArrayList<>();
        List<CoinDTO> outputs = new ArrayList<>();
        CoinDTO inputCoin1 = new CoinDTO();
        inputCoin1.setAddress(from);
        inputCoin1.setPassword(PASSWORD);
        inputCoin1.setAssetsChainId(ASSET_CHAIN_ID);
        inputCoin1.setAssetsId(ASSET_ID);
        inputCoin1.setAmount(new BigInteger(s));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1 = new CoinDTO();
        outputCoin1.setAddress(to);
        outputCoin1.setPassword(PASSWORD);
        outputCoin1.setAssetsChainId(ASSET_CHAIN_ID);
        outputCoin1.setAssetsId(ASSET_ID);
        outputCoin1.setAmount(new BigInteger(s));
        outputs.add(outputCoin1);
        transferMap.put("inputs", inputs);
        transferMap.put("outputs", outputs);
        return transferMap;
    }


    public static boolean queryTxs(List<String> hashList) throws Exception {
        boolean result = true;
        for (String hash : hashList) {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, CHAIN_ID);
            params.put("txHash", hash);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTx", params);
            assertTrue(response.isSuccess());
            Map map = (Map) response.getResponseData();
            Map tx = (Map) map.get("tx_getConfirmedTx");
            String txStr = tx.get("tx").toString();
            Transaction transaction = new Transaction();
            transaction.parse(new NulsByteBuffer(RPCUtil.decode(txStr)));
            if (!hash.equals(transaction.getHash().toHex())) {
                LOG.info("hash-{} not exist", hash);
                result = false;
            }
        }
        return result;
    }

    public static boolean queryTx(String hash, boolean confirmed) throws Exception {
        boolean result = true;
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, CHAIN_ID);
        params.put("txHash", hash);
        String cmd;
        if (confirmed) {
            cmd = "tx_getConfirmedTx";
        } else {
            cmd = "tx_getTx";
        }
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, cmd, params);
        assertTrue(response.isSuccess());
        Map map = (Map) response.getResponseData();
        Map tx = (Map) map.get(cmd);
        Object tx1 = tx.get("tx");
        if (tx1 == null) {
            LOG.info("hash-{} not exist", hash);
            return false;
        }
        String txStr = tx1.toString();
        Transaction transaction = new Transaction();
        transaction.parse(new NulsByteBuffer(RPCUtil.decode(txStr)));
        if (!hash.equals(transaction.getHash().toHex())) {
            LOG.info("hash-{} not exist", hash);
            result = false;
        }
        return result;
    }


    public static void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, VERSION);
            params.put(Constants.CHAIN_ID, CHAIN_ID);

            params.put("priKey", priKey);
            params.put("password", pwd);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            assertTrue(cmdResp.isSuccess());
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
            LOG.info("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map createAgentTx(String agentAddr, String packingAddr) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentAddress", agentAddr);
        params.put(Constants.CHAIN_ID, CHAIN_ID);
        params.put("deposit", 20000 * 100000000L);
        params.put("commissionRate", 10);
        params.put("packingAddress", packingAddr);
        params.put("password", PASSWORD);
        params.put("rewardAddress", agentAddr);
        return params;
    }

    public static void removeAccount(String address, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, VERSION);
        params.put(Constants.CHAIN_ID, CHAIN_ID);
        params.put("address", address);
        params.put("password", password);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params);
        assertTrue(response.isSuccess());
        LOG.info("{}", JSONUtils.obj2json(response.getResponseData()));
    }

    public static void checkBalance(Chain chain, String address, String amount) throws Exception {
        BigInteger balance = getBalance(chain, address);
        BigInteger integer = new BigInteger(amount);
        int compare = balance.compareTo(integer);
        switch (compare) {
            case -1:
                transfer(SOURCE_ADDRESS, address, integer.subtract(balance).toString());
                break;
            case 1:
                transfer(address, SOURCE_ADDRESS, balance.subtract(integer).toString());
                break;
            case 0:
        }

    }

    public void getAgentList() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, CHAIN_ID);
        params.put("pageNumber", 1);
        params.put("pageSize", 10);
        params.put("keyWord", "");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentList", params);
        Object o = ((HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_getAgentList")).get("list");
        LOG.info("list:{}", o);
    }

    public static boolean setAlias(Chain chain, String address) throws Exception {
        LOG.info("##########设置别名##########");
        BigInteger balance = getBalance(chain, address);
        LOG.info(address + "-----balance:{}", balance);
        {
            String alias = "jyc_" + System.currentTimeMillis();
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, CHAIN_ID);
            params.put("address", address);
            params.put("password", PASSWORD);
            params.put("alias", alias);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
            return response.isSuccess();
        }
    }

}
