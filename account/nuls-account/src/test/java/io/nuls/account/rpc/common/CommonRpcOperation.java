package io.nuls.account.rpc.common;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.TransferDto;
import io.nuls.base.data.Address;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.parse.JSONUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
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

            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountByAddress", params);
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
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
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
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAliasByAddress", params);
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
        //List<String> accountList = createAccount(3);
        List<String> accountList = List.of("5MR_2CkYEhXKCmUWTEsWRTnaWgYE8kJdfd5","5MR_2CcRgU3vDGp2uEG3rdzLdyMCbsiLFbJ","5MR_2CckymYvKM7NKpt6fpZproQYMtnGdaT");
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
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSigAccount", params);
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
/**
 *
 *
 * */
    public static String importAccountByKeystoreFile(String filePath) {
        String address = null;
        try {
            File file = new File(filePath);
            byte[] bytes = copyToByteArray(file);
            String keyStoreStr = new String(bytes,"UTF-8");

            //AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(new String(HexUtil.decode(keyStoreHexStr)), AccountKeyStoreDto.class);

            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("keyStore", HexUtil.encode(bytes));
            params.put("password", null);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByKeystore");
            address = (String) result.get("address");
            //assertEquals(accountList.get(0), address);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }

    public static byte[] copyToByteArray(File in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        InputStream input = null;
        try {
            input = new FileInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            int byteCount = 0;
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = input.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return out.toByteArray();
        } finally {
            try {
                input.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     *
     *
     * */
    public static String importAccountByPriKeyWithOverwrite(String address,String priKey,String password) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("priKey", priKey);
        params.put("password", password);
        params.put("overwrite", true);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
        assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
        String returnAddress = (String) result.get("address");
        assertNotNull(returnAddress);
        return returnAddress;

    }

    /**
     * 创建普通转账交易
     *
     * @return
     */
    public static TransferDto createTransferTx(String fromAddress, String toAddress, BigInteger amount) {
        TransferDto transferDto = new TransferDto();
        transferDto.setChainId(chainId);
        transferDto.setRemark("transfer test");
        List<CoinDto> inputs = new ArrayList<>();
        List<CoinDto> outputs = new ArrayList<>();
        CoinDto inputCoin1 = new CoinDto();
        inputCoin1.setAddress(fromAddress);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(1);
        inputCoin1.setAmount(amount);
        inputs.add(inputCoin1);

        CoinDto outputCoin1 = new CoinDto();
        outputCoin1.setAddress(toAddress);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(1);
        outputCoin1.setAmount(amount);
        outputs.add(outputCoin1);

        transferDto.setInputs(inputs);
        transferDto.setOutputs(outputs);
        return transferDto;
    }
}
