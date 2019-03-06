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
import io.nuls.base.data.*;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.rpc.call.LedgerCall;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.Log;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class TestTx {

    static int chainId = 12345;
    static int assetChainId = 12345;
//    static String address20 = "QXpkrbKqShZfopyck5jBQSFgbP9cD3930";
//    static String address22 = "KS3wfAPFAmY8EwMFz21EXhJMXf8DV3930";
//    static String address3 = "Vxb3xxatcFFTZZe3wynX6CfAsvzAx3930";
//    static String address4 = "R9CxmNqtBDEm9iWX2Cod46QGCNE2M3930";
//    static String address5 = "LFkghywKjdE2G3SZUcTsMkzcJ7tda3930";
//    static String address6 = "QMwz71wTKgp9sZ8g44A9WNgXk11u23930";
//    static String address23 = "WEXAmsUJSNAvCx2zUaXziy3ZYX1em3930";
//    static String address8 = "TewWmmtBxuxN14FRazZXdtD9XuH313930";
//    static String address9 = "WodfCXTbJ22mPa35Y61yNTRh1x3zB3930";
//    static String address200 = "SPWAxuodkw222367N88eavYDWRraG3930";
//    static String address201 = "Rnt57eZnH8Dd7K3LudJXmmEutYJZD3930";
//    static String address202 = "XroY3cLWTfgKMRRRLCP5rhvo1gHY63930";


    static String address20 = "5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz";
    static String address21 = "5MR_2CbdqKcZktcxntG14VuQDy8YHhc6ZqW";
    static String address22 = "5MR_2Cj9tfgQpdeF7nDy5wyaGG6MZ35H3rA";
    static String address23 = "5MR_2CWKhFuoGVraaxL5FYY3RsQLjLDN7jw";
    static String address24 = "5MR_2CgwCFRoJ8KX37xNqjjR7ttYuJsg8rk";
    static String address25 = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
    static String address26 = "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu";
    static String address27 = "5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM";
    static String address28 = "5MR_2CfUsasd33vQV3HqGw6M3JwVsuVxJ7r";
    static String address29 = "5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF";

    static int assetId = 1;
    //入账金额
    static BigInteger amount = BigInteger.valueOf(1000000L * 100000000L);
    static String password = "nuls123456";

    private Chain chain;
    private Transaction tx;
    private CoinData coinData;

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        // Get information from kernel
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        chain = new Chain();
        chain.setConfig(new ConfigBean(12345, 1));
//        初始化token
//        addGenesisAsset();
    }



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
    }


    @Test
    public void importPriKeyTest() {
//        importPriKey("7e304e60c4e29c15382f76c0bb097bda28a1950b78871b6b7eb2bb4cc4ddeb49");//种子出块地址 5MR_2Cb86fpFbuY4Lici8MJStNxDFYH6kRB
//        importPriKey("70e871a2e637b4182dfbedc53e164182d266821f4824ab1a3a73055e9f252f98");//种子出块地址

//        importPriKey("00c299b105e2f9b260d7811d5cb94c713cc324e55831cb15a18454f7382f0a5f6e");//20 5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz
//        importPriKey("00c4a6b90d3f4eb7b50bc85fd0e99ccb717e148b4fde7462e14c590445e589588c");//21 5MR_2CbdqKcZktcxntG14VuQDy8YHhc6ZqW
//        importPriKey("009ad5018ed1fc162c5320b9ae496984dd10227086ad86ea954a209597ff9b7d3a");//22 5MR_2Cj9tfgQpdeF7nDy5wyaGG6MZ35H3rA
//        importPriKey("00c805d2d6d5e06f57fdfb1aff56ef3c2dd15eee88f36fa7d45d368c352ec5ec0d");//23 5MR_2CWKhFuoGVraaxL5FYY3RsQLjLDN7jw
//        importPriKey("00c77707b21eece6c1ce0b8add04db79dc846f36830effe5c5ae2aced00097fafb");//24 5MR_2CgwCFRoJ8KX37xNqjjR7ttYuJsg8rk
        importPriKey("00def3b0f4bfad2a6abb5f6957829e752a1a30806edc35e98016425d578fdc4e77", password);//25 5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo
        importPriKey("1c2b9fd4417c1aad8ae9f24c982ff294eb50a6462b873b79a879e805a9990346", password);//26 5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu
        importPriKey("00c98ecfd3777745270cacb9afba17ef0284769a83ff2adb4106b8a0baaec9452c");//27 5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM
        importPriKey("23848d45b4b34aca8ff24b00949a25a2c9175faf283675128e189eee8b085942");//28 5MR_2CfUsasd33vQV3HqGw6M3JwVsuVxJ7r
        importPriKey("009560d5ed6587822b7aee6f318f50b312c281e4f330b6990396881c6d3f870bc1");//29 5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF

        importPriKey("00fffd585ed08dddf0d034236aa1ea85abd2e4e69981617ee477adf6cdcf50f4d5");//打包地址 5MR_2Ch8CCnLwoLWFZ45pFEZSmo1C1pkPFA
    }

    @Test
    public void createAgentTx() throws Exception {
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(address23), assetChainId, assetId);
        System.out.println(balance.longValue());

        //组装创建节点交易
        Map agentTxMap = this.createAgentTx(address27, "5MR_2Ch8CCnLwoLWFZ45pFEZSmo1C1pkPFA");
        //调用接口
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
        Assert.assertTrue(null != result);
        Log.debug("{}", result.get("txHex"));
        System.out.println("transfer: " + result.get("txHex"));  //Thread.sleep(3000L);
    }


    /**
     * 委托节点
     */
    @Test
    public void depositToAgent() throws Exception {
        //组装委托节点交易
        String agentHash = "00203ce7a72a4370bce3d1fdefcb2e35364370bde670f5fde30474820abd55dbeaa7";
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put("chainId", chainId);
        dpParams.put("address", address27);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", 200000 * 100000000L);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
        HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
        String dpTxHex = (String) dpResult.get("txHex");
    }

    /**
     * 取消委托
     *
     * @throws Exception
     */
    @Test
    public void withdraw() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        //Address depositAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        params.put("address", address27);
        params.put("txHash", "0020467c4aea7653d17295b313c4002ddb41d882c22039d181fc474e7de6d5d0c06b");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void stopAgentTx() throws Exception {
        Map<String, Object> txMap = new HashMap();
        txMap.put("chainId", chainId);
        txMap.put("address", address27);
        txMap.put("password", "");
        //调用接口
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_stopAgent"));
        Assert.assertTrue(null != result);
        Log.debug("{}", result.get("txHex"));
        System.out.println("transfer: " + result.get("txHex"));  //Thread.sleep(3000L);
    }

    /**
     * 设置别名
     * @throws Exception
     */
    @Test
    public void alias() throws Exception {
        String alias = "charlie" + System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", address25);
        params.put("password", password);
        params.put("alias", alias);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
        System.out.println("ac_setAlias result:" + JSONUtils.obj2json(cmdResp));
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setAlias");
        assertNotNull(result);
        String txHash = (String) result.get("txHash");
        assertNotNull(txHash);
        Log.debug("alias-txHash{}", txHash);
    }

    /**
     * 转账
     * @throws Exception
     */
    @Test
    public void newCtx() throws Exception {
/*        BigInteger balance1 = LedgerCall.getBalance(chain, AddressTool.getAddress(address20), assetChainId, assetId);
        BigInteger balance2 = LedgerCall.getBalance(chain, AddressTool.getAddress(address21), assetChainId, assetId);
        System.out.println("address20: " + balance1.longValue());
        System.out.println("address21: " + balance2.longValue());
        CrossTxTransferDTO ctxTransfer = new CrossTxTransferDTO(chain.getChainId(),
                createFromCoinDTOList(), createToCoinDTOList(), "this is cross-chain transaction");
        //调接口
        String json = JSONUtils.obj2json(ctxTransfer);
        Map<String, Object> params = JSONUtils.json2map(json);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_createCtx", params);
        Assert.assertTrue(null != response.getResponseData());
        Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_createCtx");
        Assert.assertTrue(null != map);
        Log.debug("{}", map.get("value"));

        Thread.sleep(3000L);*/

        Map transferMap = this.createTransferTx();
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        Assert.assertTrue(null != result);
        Log.debug("{}", result.get("value"));
        Thread.sleep(4000L);
        //packableTxs();

    }

    @Test
    public void removeAccountTest() throws Exception {
        removeAccount("5MR_2Cb86fpFbuY4Lici8MJStNxDFYH6kRB");
        removeAccount(address26);
    }

    public void importPriKey(String priKey){
        importPriKey(priKey, null);
    }
    public void importPriKey(String priKey, String pwd){
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
            Log.debug("{}", address);
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void removeAccount(String address) throws Exception {
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

        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress("5MR_2CkYEhXKCmUWTEsWRTnaWgYE8kJdfd5"), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance));

        BigInteger balance2 = LedgerCall.getBalance(chain, AddressTool.getAddress(address21), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance2));
        BigInteger balance3 = LedgerCall.getBalance(chain, AddressTool.getAddress(address22), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance3));
        BigInteger balance4 = LedgerCall.getBalance(chain, AddressTool.getAddress(address24), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance4));
        BigInteger balance5 = LedgerCall.getBalance(chain, AddressTool.getAddress(address25), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance5));
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
        coinDTO.setAddress("2kX_2Ceu7cSmeV9ZSJaqqwwF27Jr6n7omyw");
        coinDTO.setAmount(new BigInteger("200000000"));

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(23);
        coinDTO2.setAddress("2kX_2CfowdpKsNtwCiTGM4L8PhQrF1b4Rki");
        coinDTO2.setAmount(new BigInteger("100000000"));
        List<CoinDTO> listTO = new ArrayList<>();
        listTO.add(coinDTO);
        listTO.add(coinDTO2);
        return listTO;
    }

    /**
     * 铸币交易
     *
     * @return
     * @throws IOException
     */
    private static Transaction buildTransaction(String address) throws IOException {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(amount);
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms = new ArrayList<>();
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        return tx;
    }

    static int height = 0;
    /**
     * 铸币
     *
     * @throws Exception
     */
    public static void addGenesisAsset(String address) throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        Log.debug("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTransaction(address);

        params.put("txHex", HexUtil.encode(transaction.serialize()));
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        Log.debug("response {}", response);

        params.put("isConfirmTx", true);
        List<String> list = new ArrayList<>();
        list.add(HexUtil.encode(transaction.serialize()));
        params.put("txHexList", list);
        params.put("blockHeight", height++);
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        Log.debug("response {}", response);
    }


    /**
     * 创建普通转账交易
     *
     * @return
     */
    private Map createTransferTx() {
        Map transferMap = new HashMap();
        transferMap.put("chainId", chainId);
        transferMap.put("remark", "transfer test");
        List<CoinDTO> inputs = new ArrayList<>();
        List<CoinDTO> outputs = new ArrayList<>();
        CoinDTO inputCoin1 = new CoinDTO();
        inputCoin1.setAddress(address26);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("10000000"));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1 = new CoinDTO();
        outputCoin1.setAddress(address27);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(new BigInteger("10000000"));
        outputs.add(outputCoin1);

        transferMap.put("inputs", inputs);
        transferMap.put("outputs", outputs);
        return transferMap;
    }

    /**
     * 创建节点
     */
    public Map createAgentTx(String agentAddr, String packingAddr) throws Exception {
        Address agentAddress = new Address(chainId, (byte) assetId, SerializeUtils.sha256hash160(agentAddr.getBytes()));
        Address rewardAddress = new Address(chainId, (byte) assetId, SerializeUtils.sha256hash160(agentAddr.getBytes()));
        Address packingAddress = new Address(chainId, (byte) assetId, SerializeUtils.sha256hash160(packingAddr.getBytes()));
        Map<String, Object> params = new HashMap<>();
        params.put("" +
                "", agentAddr);
        params.put("chainId", chainId);
        params.put("deposit", 20000 * 100000000L);
        params.put("commissionRate", 10);
        params.put("packingAddress", packingAddr);
        params.put("password", "");
        params.put("rewardAddress", agentAddr);
        return params;
//        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
//        System.out.println(cmdResp.getResponseData());
    }
}
