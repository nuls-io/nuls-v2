/**
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

package io.nuls.transaction;

import io.nuls.base.basic.AddressTool;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.model.dto.CrossTxTransferDTO;
import io.nuls.transaction.rpc.call.LedgerCall;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * @author: Charlie
 * @date: 2019/3/11
 */
public class TxValid {

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

    /** 空地址
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
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, 1024 * 1024, 1000, 20, 20000L, 60000L));
    }

    @Test
    public void transfer() throws Exception {
        for (int i = 0; i < 1; i++) {
            String hash = createTransfer(address28, address21);
//            String hash = createCtxTransfer();
            System.out.println("count:" + (i + 1));
            System.out.println("");
//            Thread.sleep(500L);
            //getTx(hash);
        }

//        Transfer transfer1 = new Transfer(address25, address21);
//        Thread thread1 = new Thread(transfer1);
//        thread1.start();
//        Transfer transfer2 = new Transfer(address26, address22);
//        Thread thread2 = new Thread(transfer2);
//        thread2.start();
//        Transfer transfer3 = new Transfer(address27, address23);
//        Thread thread3 = new Thread(transfer3);
//        thread3.start();
//        Transfer transfer4 = new Transfer(address28, address24);
//        Thread thread4 = new Thread(transfer4);
//        thread4.start();
//        try {
//            while (true) {
//                Thread.sleep(1000000000L);
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void mixedTransfer() throws Exception {
        String agentHash = null;
        String depositHash = null;
        String withdrawHash = null;
        String stopAgent = null;
        for (int i = 0; i < 20000; i++) {
            String hash = createTransfer(address25, address21);
            switch (i){
                case 0:
                    //创建节点
                    agentHash = createAgent(address29, address27);
                    break;
                case 4000:
                    //委托
                    depositHash = deposit(address29, agentHash);
                    break;
                case 16000:
                    //取消委托
                    withdrawHash = withdraw(address29, depositHash);
                    break;
                case 19000:
                    //停止节点
                    stopAgent = stopAgent(address29);
                    break;
            }

            System.out.println("count:" + (i + 1));
            System.out.println("");

        }
    }

    @Test
    public void getTx() throws Exception {
        getTxCfmClient("0020e87c15cc8509a46cf5c802641864dc609f87b4e1d6120a3de3d3d31e6075173a");
        getTxCfmClient("00209a152f8a93c05463c970a12b875cb23effb59b0dd177b47ec1e56fca0d3c80e8");//最后一条
    }

    private void getTx(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("txHash", hash);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTx", params);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("tx_getTx"));
        Assert.assertTrue(null != result);
        Log.debug("{}", JSONUtils.obj2PrettyJson(result));
       /* String txStr = (String) result.get("tx");
        Log.debug("getTx -hash:{}", ((Transaction)TxUtil.getInstanceRpcStr(txStr, Transaction.class)).getHash().getDigestHex());*/
    }


    @Test
    public void importPriKeyTest() {
        importPriKey("b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5", password);//种子出块地址 tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp
//        importPriKey("188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f", password);//种子出块地址 tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe
//        importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", password);//20 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
//        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//21 tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
//        importPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78", password);//22 tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24
        importPriKey("4100e2f88c3dba08e5000ed3e8da1ae4f1e0041b856c09d35a26fb399550f530", password);//23 tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD
        importPriKey("bec819ef7d5beeb1593790254583e077e00f481982bce1a43ea2830a2dc4fdf7", password);//24 tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL
        importPriKey("ddddb7cb859a467fbe05d5034735de9e62ad06db6557b64d7c139b6db856b200", password);//25 tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL
        importPriKey("4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a", password);//26 tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm
        importPriKey("3dadac00b523736f38f8c57deb81aa7ec612b68448995856038bd26addd80ec1", password);//27 tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1
        importPriKey("27dbdcd1f2d6166001e5a722afbbb86a845ef590433ab4fcd13b9a433af6e66e", password);//28 tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2
        importPriKey("76b7beaa98db863fb680def099af872978209ed9422b7acab8ab57ad95ab218b", password);//29 tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn

    }

    @Test
    public void createAgentTx() throws Exception {
        createAgent(address26, "tNULSeBaMoRVvrr9noCDWwNNe3ZAbCvRWEPtij");
    }
    private String createAgent(String address, String packing)  throws Exception{
        //组装创建节点交易
        Map agentTxMap = this.createAgentTx(address, packing);
        //调用接口
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
        Assert.assertTrue(null != result);
        String hash = (String) result.get("txHash");
        Log.debug("createAgent-txHash:{}", hash);
        return hash;
    }
    /**
     * 委托节点
     */
    @Test
    public void depositToAgent() throws Exception {
        //组装委托节点交易
        String hash = deposit(address26, "");
    }

    private String deposit(String address, String agentHash) throws Exception {
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put("chainId", chainId);
        dpParams.put("address", address);
        dpParams.put("password", password);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", 200000 * 100000000L);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
        HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
        String txHash = (String) dpResult.get("txHash");
        Log.debug("deposit-txHash:{}", txHash);
        return txHash;
    }

    /**
     * 取消委托
     *
     * @throws Exception
     */
    @Test
    public void withdraw() throws Exception {
        String hash = withdraw(address25, "0020b8a42eb4c70196189e607e9434fe09b595d5753711f21819113f40d64a1c82c1");
        Log.debug("withdraw-txHash:{}", hash);
    }

    private String withdraw(String address, String depositHash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("password", password);
        params.put("txHash", depositHash);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        HashMap dpResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_withdraw");
        String hash = (String) dpResult.get("txHash");
        Log.debug("withdraw-txHash:{}", hash);
        return hash;
    }

    @Test
    public void stopAgentTx() throws Exception {
        String hash = stopAgent(address25);
        Log.debug("stopAgent-txHash:{}", hash);
    }

    private String stopAgent(String address) throws Exception {
        Map<String, Object> txMap = new HashMap();
        txMap.put("chainId", chainId);
        txMap.put("address", address);
        txMap.put("password", password);
        //调用接口
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_stopAgent"));
        String txHash = (String) result.get("txHash");
        Log.debug("stopAgent-txHash:{}", txHash);
        return txHash;
    }

    /**
     * 设置别名
     *
     * @throws Exception
     */
    @Test
    public void alias() throws Exception {
        String alias = "charlie";
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", "tNULSeBaMigwBrvikwVwbhAgAxip8cTScwcaT8");
        params.put("password", password);
        params.put("alias", alias);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
        System.out.println("ac_setAlias result:" + JSONUtils.obj2json(cmdResp));
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setAlias");
        String txHash = (String) result.get("txHash");
        Log.debug("alias-txHash:{}", txHash);
    }

    /**
     * 查交易
     */
    @Test
    public void getTxRecord() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", address20);//tNULSeBaMk4LBr1y1tsneiHvy5H2Rc3Lns4QuN
        params.put("assetChainId", null);
        params.put("assetId", null);
        params.put("type", null);
        params.put("pageSize", null);
        params.put("pageNumber", null);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxs", params);
        Map record = (Map) dpResp.getResponseData();
        Log.debug("Page<TransactionPO>:{}", JSONUtils.obj2PrettyJson(record));
    }

    /**
     * 查交易
     */
    private void getTxClient(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.debug("{}", JSONUtils.obj2PrettyJson(record));
    }

    /**
     * 查交易
     */
    private void getTxCfmClient(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.debug(JSONUtils.obj2PrettyJson(record));
    }

    @Test
    public void getPriKeyByAddress() throws Exception {
        String prk = getPriKeyByAddress("tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL");
        System.out.println(prk);
    }

    /**
     * 导入keystore
     */
    @Test
    public void importAccountByKeystoreFile() {
        String address = importAccountByKeystoreFile("C:/Users/Administrator/Desktop/2.0测试配置和内容/tNULSeBaMigwBrvikwVwbhAgAxip8cTScwcaT8.keystore");
        Log.info("address:{}", address);
    }

    /**
     * 删除账户
     */
    @Test
    public void removeAccountTest() throws Exception {
        removeAccount("tNULSeBaMoRVvrr9noCDWwNNe3ZAbCvRWEPtij", password);
//        removeAccount(address20, password);
    }

    private String createTransfer(String addressFrom, String addressTo) throws Exception {
        Map transferMap = this.createTransferTx(addressFrom, addressTo);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
        if(!cmdResp.isSuccess()){
            return "fail";
        }
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        Assert.assertTrue(null != result);
        String hash = (String) result.get("value");
        Log.debug("{}", hash);
        return hash;
    }

    private String createCtxTransfer() throws Exception {
        CrossTxTransferDTO ctxTransfer = new CrossTxTransferDTO(chain.getChainId(),
                createFromCoinDTOList(), createToCoinDTOList(), "this is cross-chain transaction");
        //调接口
        String json = JSONUtils.obj2json(ctxTransfer);
        Map<String, Object> params = JSONUtils.json2map(json);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_createCtx", params);
        Assert.assertTrue(null != response.getResponseData());
        Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_createCtx");
        Assert.assertTrue(null != map);
        String hash = (String) map.get("value");
        Log.debug("{}", hash);
        return hash;
    }


    private String getPriKeyByAddress(String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("password", password);
        params.put("address", address);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
        String adr = (String) result.get("priKey");
        Log.debug("{}", adr);
        return address;
    }

    public static String importAccountByKeystoreFile(String filePath) {
        String address = null;
        try {
            File file = new File(filePath);
            byte[] bytes = copyToByteArray(file);
            String keyStoreStr = new String(bytes, "UTF-8");

            //AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(new String(HexUtil.decode(keyStoreHexStr)), AccountKeyStoreDto.class);

            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("keyStore", RPCUtil.encode(bytes));
            params.put("password", password);
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

    public void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);

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
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("password", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params);
        Log.debug("{}", JSONUtils.obj2json(cmdResp.getResponseData()));
    }

    @Test
    public void getBalance() throws Exception {

        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress("tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1"), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance));

     /*   BigInteger balance2 = LedgerCall.getBalance(chain, AddressTool.getAddress(address21), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance2));
        BigInteger balance3 = LedgerCall.getBalance(chain, AddressTool.getAddress(address22), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance3));
        BigInteger balance4 = LedgerCall.getBalance(chain, AddressTool.getAddress(address24), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance4));
        BigInteger balance5 = LedgerCall.getBalance(chain, AddressTool.getAddress(address25), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance5));*/
    }


    //    @Test
    public void packableTxs() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        long endTime = System.currentTimeMillis() + 10000L;
        System.out.println("endTime: " + endTime);
        params.put("endTimestamp", endTime);
        params.put("maxTxDataSize", 2 * 1024 * 1024L);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_packableTxs", params);
        Assert.assertTrue(null != response.getResponseData());
        Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_packableTxs");
        Assert.assertTrue(null != map);
        List<String> list = (List) map.get("list");
        Log.debug("packableTxs:");
        for (String s : list) {
            Log.debug(s);
        }
    }

    private List<CoinDTO> createFromCoinDTOList() {
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(assetChainId);
        coinDTO.setAddress(address25);
        coinDTO.setAmount(new BigInteger("200000000"));
        coinDTO.setPassword(password);

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(assetChainId);
        coinDTO2.setAddress(address26);
        coinDTO2.setAmount(new BigInteger("100000000"));
        coinDTO2.setPassword(password);
        List<CoinDTO> listFrom = new ArrayList<>();
        listFrom.add(coinDTO);
        listFrom.add(coinDTO2);
        return listFrom;
    }

    private List<CoinDTO> createToCoinDTOList() {
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(23);
        coinDTO.setAddress("NULSd6Hgfj4PyqSuYBEJth3zEG32sjYsUGsVA");
        coinDTO.setAmount(new BigInteger("200000000"));

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(23);
        coinDTO2.setAddress("NULSd6HggkGBiHrUAL4YGErUFiMb2DkB5QQus");
        coinDTO2.setAmount(new BigInteger("100000000"));
        List<CoinDTO> listTO = new ArrayList<>();
        listTO.add(coinDTO);
        listTO.add(coinDTO2);
        return listTO;
    }


    /**
     * 创建普通转账交易
     *
     * @return
     */
    private Map createTransferTx(String addressFrom, String addressTo) {
        Map transferMap = new HashMap();
        transferMap.put("chainId", chainId);
        transferMap.put("remark", "transfer test");
        List<CoinDTO> inputs = new ArrayList<>();
        List<CoinDTO> outputs = new ArrayList<>();
        CoinDTO inputCoin1 = new CoinDTO();
        inputCoin1.setAddress(addressFrom);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("100100000"));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1 = new CoinDTO();
        outputCoin1.setAddress(addressTo);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(new BigInteger("100000000"));
        outputs.add(outputCoin1);

        transferMap.put("inputs", inputs);
        transferMap.put("outputs", outputs);
        return transferMap;
    }

    /**
     * 创建节点
     */
    public Map createAgentTx(String agentAddr, String packingAddr) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentAddress", agentAddr);
        params.put("chainId", chainId);
        params.put("deposit", 20000 * 100000000L);
        params.put("commissionRate", 10);
        params.put("packingAddress", packingAddr);
        params.put("password", password);
        params.put("rewardAddress", agentAddr);
        return params;
    }

    /*
    @Test
    public void multiThreadCreateTx() {
        for (int i = 0; i < 1; i++) {
            Thread thread = new Thread(new CreateTxThread(), "MR" + i);
            thread.start();
        }
        try {
            while (true) {
                Thread.sleep(1000000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/
}
