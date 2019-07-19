package io.nuls.transaction; /**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.utils.TxUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 多签地址,多签地址转账交易测试
 *
 * @author: Charlie
 * @date: 2019/7/12
 */
public class TxMultiSig {

    static String address20 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String address21 = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    static String address22 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    static String address23 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    static String address24 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    static String address25 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    static String address26 = "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm";
    static String address27 = "tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1";
    static String address28 = "tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2";
    static String address29 = "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn";


    /**
     * 空地址
     * tNULSeBaMm8Kp5u7WU5xnCJqLe8fRFD49aZQdK
     * tNULSeBaMigwBrvikwVwbhAgAxip8cTScwcaT8
     */
    private Chain chain;
    static int chainId = 2;
    static int assetChainId = 2;
    static int assetId = 1;
    static String version = "1.0";

    static String password = "nuls123456";//"nuls123456";


    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, 1024 * 1024, 1000, 20, 20000, 60000));
    }


    @Test
    public void importPriKeyTest() {
        importPriKey("b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5", password);//种子出块地址 tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp
//        importPriKey("188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f", password);//种子出块地址 tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe
        importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//20 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//21 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        importPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78", password);//22 tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24
        importPriKey("4100e2f88c3dba08e5000ed3e8da1ae4f1e0041b856c09d35a26fb399550f530", password);//23 tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD
        importPriKey("bec819ef7d5beeb1593790254583e077e00f481982bce1a43ea2830a2dc4fdf7", password);//24 tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL
        importPriKey("ddddb7cb859a467fbe05d5034735de9e62ad06db6557b64d7c139b6db856b200", password);//25 tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL
        importPriKey("4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a", password);//26 tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm
        importPriKey("3dadac00b523736f38f8c57deb81aa7ec612b68448995856038bd26addd80ec1", password);//27 tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1
        importPriKey("27dbdcd1f2d6166001e5a722afbbb86a845ef590433ab4fcd13b9a433af6e66e", password);//28 tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2
        importPriKey("76b7beaa98db863fb680def099af872978209ed9422b7acab8ab57ad95ab218b", password);//29 tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn
    }

    @Test
    public void importPriKey() {
        //创建多签账户的地址 3个
        importPriKey("ba5bc98030183e2680e15fc2defa97f24b45d6975dd8668ecc578596b6dd474d", password);// tNULSeBaMkUZYqaeFqe6cdx2gdBZxZh1fVcnM5 -pubkey: 035d975818dc2b0ed1b1fbafb80403a188d7bca27f07ac58dd63f15a3fdd5989b5
        importPriKey("32dae213420e32ec840a1467460b243cb7eb8e6fc15a37dd99252e87aa3bc3d1", password);// tNULSeBaMo8z73fktnukU3JsFFfogWgLd91uPM -pubkey: 026a4821178975d196d90a68d80e5838876a2b30f1018d304c4b814823f7275a60
        importPriKey("7cf2a34ee75b7560404c73b984cdf9d07f34941dbf0a61a971bdd8be73d10f9a", password);// tNULSeBaMti6qq57uGVncG1BgYQVSz3Yj4NVBi -pubkey: 02887a1e8bbb32a1885040849caf8ee194147c77ea4f227c18aad0b84ab79a3bf6

        //备用地址
        importPriKey("6d455b384b9dc81e6cf52b43ac2af5a90d5608a3419c0a062030a0e7978724e6", password);// tNULSeBaMmiWaeLshXCRNWBw5NpZNNvJHyG5aY -pubkey: 02dce420d8dd2c397c0fba283b5dde5558ce34d899dc740dac64e0bb72034838cb
        importPriKey("930f0be5000d7c8d5ca69e9ff8b6ab9ca7f8e27ebb3843b4fc991727ed8f3200", password);// tNULSeBaMsbHJStYcYdosBGphvdr5yFCeCPZ3L -pubkey: 03d9eb346464550ce5349825d43bd15b16df3d97a0a0771f1c03f1a0e283d29e5b
    }

    static String addressMultiSign = "tNULSeBaNMnYA7FArmcMoYSdb1qsvoXL9qSKJB";

    static String signAddress1 = "tNULSeBaMkUZYqaeFqe6cdx2gdBZxZh1fVcnM5";
    static String signAddress2 = "tNULSeBaMo8z73fktnukU3JsFFfogWgLd91uPM";
    static String signAddress3 = "tNULSeBaMti6qq57uGVncG1BgYQVSz3Yj4NVBi";

    static String address30 = "tNULSeBaMmiWaeLshXCRNWBw5NpZNNvJHyG5aY";
    static String address31 = "tNULSeBaMsbHJStYcYdosBGphvdr5yFCeCPZ3L";



    @Test
    public void createMultiSignAccount(){
        try {
            List<String> pubKeys = new ArrayList<>();
            pubKeys.add("035d975818dc2b0ed1b1fbafb80403a188d7bca27f07ac58dd63f15a3fdd5989b5");
            pubKeys.add("026a4821178975d196d90a68d80e5838876a2b30f1018d304c4b814823f7275a60");
            pubKeys.add(signAddress3);
            String address = createMultiSignAccount(pubKeys, 2);
            Log.info("{}", address);

        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Test //转账
    public void transfer() throws Exception{
//        String hash = createTransfer(address27, addressMultiSign,new BigInteger("100000000000000"));
//        String hash = createMultiSignTransfer(addressMultiSign, address30,new BigInteger("1000000000"), null, null);
        String hash = createMultiSignTransfer("charlie_m_sign", address31,new BigInteger("1300000000"), signAddress1, password);
    }


    @Test //签名
    public void signMultiSignTransactionTest() throws Exception {
        String rs = signMultiSignTransaction(signAddress2, password,
                "02000894315d03616263008c0117020003975bd16ec54ecc5ed595ac4b0666d395256073c802000100a0f37d4d0000000000000000000000000000000000000000000000000000000008f68dc28895e50c79000117020001ccdb0403e38cdd6351629a81686562929397140002000100006d7c4d000000000000000000000000000000000000000000000000000000000000000000000000d2020321035d975818dc2b0ed1b1fbafb80403a188d7bca27f07ac58dd63f15a3fdd5989b521026a4821178975d196d90a68d80e5838876a2b30f1018d304c4b814823f7275a602102887a1e8bbb32a1885040849caf8ee194147c77ea4f227c18aad0b84ab79a3bf621035d975818dc2b0ed1b1fbafb80403a188d7bca27f07ac58dd63f15a3fdd5989b5473045022100f2dbae29634b2895fa5df51ed4a946283eb2b477a8b4183c2ccde009e2ecdda902206e92794da293d3dd7a3252547f88441cd7d2bd142b4a8d8cd90bb620de4aae6f");
    }

    @Test //设置别名
    public void setMultiSigAlias() throws Exception{

//        String rs = alias(addressMultiSign, "charlie_m_sign", signAddress1, password);
        String rs = alias(addressMultiSign, "charlie_m_sign", null, null);
    }

    @Test
    public void balance() throws Exception {
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(addressMultiSign), assetChainId, assetId);
        System.out.println(balance.longValue());
        BigInteger balance2 = LedgerCall.getBalance(chain, AddressTool.getAddress(addressMultiSign), 200, assetId);
        System.out.println(balance2.longValue());
        BigInteger balance3 = LedgerCall.getBalance(chain, AddressTool.getAddress(address30), assetChainId, assetId);
        System.out.println(balance3.longValue());
        BigInteger balance4 = LedgerCall.getBalance(chain, AddressTool.getAddress(address30), 200, assetId);
        System.out.println(balance4.longValue());
    }

    @Test //查多签账户
    public void getMAccount() throws Exception {
        getMultiSigAccount(addressMultiSign);
    }

    @Test //是否是多签账户创建者之一
    public void isMultiSignAccountBuilder() throws Exception {
        isMultiSignAccountBuilder(addressMultiSign, "tNULSeBaMmiWaeLshXCRNWBw5NpZNNvJHyG5aY");
    }

    @Test
    public void remove() throws Exception {
//        removeAccount("tNULSeBaMkUZYqaeFqe6cdx2gdBZxZh1fVcnM5", password);
//        removeAccount("tNULSeBaMo8z73fktnukU3JsFFfogWgLd91uPM", password);
        removeMultiSigAccount(addressMultiSign);
    }

    public String createMultiSignAccount(List<String> pubKeys, int minSign) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("pubKeys", pubKeys);
        params.put("minSigns", minSign);
        //create the multi sign accout
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSignAccount", params);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createMultiSignAccount");
        assertNotNull(result);
        String address = (String) result.get("address");
        assertNotNull(address);
        int resultMinSigns = (int) result.get("minSign");
        assertEquals(resultMinSigns,minSign);
        List<String> resultPubKeys = (List<String>) result.get("pubKeys");
        assertNotNull(resultPubKeys);
        assertEquals(pubKeys.size(),pubKeys.size());
        return address;
    }

    /**
     * 创建3个账户,并获取公私钥,为创建多签账户准备数据
     */
    @Test
    public void initAccount() {
        try {
            List<String> addrList = createAccount(chainId, 3, password);
            for(String address : addrList){
                String prikey = getPrivateKey(address, password);
                String pubkey = getPublicKey(address, password);
                Log.info("address:{} -prikey:{} -pubkey:{}", address, prikey, pubkey);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private Boolean isMultiSignAccountBuilder(String address, String pubKey) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("pubKey", pubKey);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_isMultiSignAccountBuilder", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_isMultiSignAccountBuilder");
        Boolean value = (Boolean) result.get("value");
        Log.info("{}", value);
        return value;
    }



    private String getMultiSigAccount(String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getMultiSignAccount", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getMultiSignAccount");
        String value = (String) result.get("value");
        if(null == value){
            Log.info("null");
            return value;
        }
        MultiSigAccount multiSigAccount = TxUtil.getInstanceRpcStr(value, MultiSigAccount.class);
//        Log.info("{}",JSONUtils.obj2json(multiSigAccount));
        Log.info("address: {}", multiSigAccount.getAddress().getBase58());
        for(byte[] pk : multiSigAccount.getPubKeyList()){
            Log.info("pubkey: {}", HexUtil.encode(pk));
        }
        Log.info("M: {}", multiSigAccount.getM());
        Log.info("alias: {}", multiSigAccount.getAlias());
        return value;
    }


    private String getPrivateKey(String address, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("password", password);
        params.put("address", address);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
        String priKey = (String) result.get("priKey");
        return priKey;
    }

    public String getPublicKey(String address, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("password", password);
        params.put("address", address);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPubKey", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPubKey");
        String pubKey = (String) result.get("pubKey");
        return pubKey;
    }

    public static List<String> createAccount(int chainId, int count, String password) {
        List<String> accountList = null;
        Response cmdResp = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("count", count);
            params.put("password", password);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params, 60000L);
            accountList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
        } catch (Exception e) {
            Log.error("cmdResp:{}", cmdResp);
            e.printStackTrace();
        }
        return accountList;
    }

    /**
     * 导入账户私钥
     *
     * @param priKey
     * @param pwd
     */
    public void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            params.put("priKey", priKey);
            params.put("password", pwd);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
            Log.debug("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void removeAccount(String address, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("password", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params);
        Log.debug("{}", JSONUtils.obj2json(cmdResp.getResponseData()));
    }

    public void removeMultiSigAccount(String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeMultiSignAccount", params);
        Log.debug("{}", JSONUtils.obj2json(cmdResp.getResponseData()));
    }


    private String createMultiSignTransfer(String addressFrom, String addressTo, BigInteger amount, String signAddress, String signAddressPwd) throws Exception {
        Map transferMap = this.createMultiSignTransferTx(addressFrom, addressTo, amount, signAddress, signAddressPwd);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSignTransfer", transferMap);
        if (!cmdResp.isSuccess()) {
            return "fail";
        }
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_createMultiSignTransfer"));
        Assert.assertTrue(null != result);
        String tx = (String) result.get("tx");
        Log.info("{}", tx);
        String txHash = (String) result.get("txHash");
        Log.info("{}", txHash);
        boolean completed = (boolean) result.get("completed");
        Log.info("{}", completed);
        if(completed){
            return txHash;
        }
        return tx;
    }

    private String createTransfer(String addressFrom, String addressTo, BigInteger amount) throws Exception {
        Map transferMap = this.createMultiSignTransferTx(addressFrom, addressTo, amount, null, null);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
        if (!cmdResp.isSuccess()) {
            return "fail";
        }
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        Assert.assertTrue(null != result);
        String hash = (String) result.get("value");
        Log.debug("{}", hash);
        return hash;
    }


    private String signMultiSignTransaction(String address, String password, String txStr) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("tx", txStr);
        params.put("signAddress", address);
        params.put("signPassword", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signMultiSignTransaction", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_signMultiSignTransaction");
        String tx = (String) result.get("tx");
        Log.info("{}", tx);
        String txHash = (String) result.get("txHash");
        Log.info("{}", txHash);
        boolean completed = (boolean) result.get("completed");
        Log.info("{}", completed);
        if(completed){
            return txHash;
        }
        return tx;
    }

    private String alias(String addressMultiSign, String alias, String signAddress, String pwd) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", addressMultiSign);
        params.put("alias", alias);
        params.put("signAddress", signAddress);
        params.put("signPassword", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setMultiSignAlias", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setMultiSignAlias");
        String tx = (String) result.get("tx");
        Log.info("{}", tx);
        String txHash = (String) result.get("txHash");
        Log.info("{}", txHash);
        boolean completed = (boolean) result.get("completed");
        Log.info("{}", completed);
        if(completed){
            return txHash;
        }
        return tx;
    }

    /**
     *
     *
     * @return
     */
    private Map createMultiSignTransferTx(String addressFrom, String addressTo, BigInteger amount, String signAddress, String signAddressPwd) {
        Map transferMap = new HashMap();
        transferMap.put("chainId", chainId);
        transferMap.put("remark", "abc");
        List<CoinDTO> inputs = new ArrayList<>();
        List<CoinDTO> outputs = new ArrayList<>();
        CoinDTO inputCoin1 = new CoinDTO();
        inputCoin1.setAddress(addressFrom);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("100000").add(amount));
        inputs.add(inputCoin1);

//        CoinDTO inputCoin2 = new CoinDTO();
//        inputCoin2.setAddress(addressFrom);
//        inputCoin2.setPassword(password);
//        inputCoin2.setAssetsChainId(200);
//        inputCoin2.setAssetsId(assetId);
//        BigInteger toOtherAsset = new BigInteger("100000000");
//        inputCoin2.setAmount(new BigInteger("100000").add(toOtherAsset));
//        inputs.add(inputCoin2);


        CoinDTO outputCoin1 = new CoinDTO();
        outputCoin1.setAddress(addressTo);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(amount);
        outputs.add(outputCoin1);
//
//        CoinDTO outputCoin2 = new CoinDTO();
//        outputCoin2.setAddress(addressTo);
//        outputCoin2.setPassword(password);
//        outputCoin2.setAssetsChainId(200);
//        outputCoin2.setAssetsId(assetId);
//        outputCoin2.setAmount(toOtherAsset);
//        outputs.add(outputCoin2);

        transferMap.put("inputs", inputs);
        transferMap.put("outputs", outputs);
        transferMap.put("signAddress", signAddress);
        transferMap.put("signPassword",signAddressPwd);
        return transferMap;
    }

}
