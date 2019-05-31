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

import io.nuls.core.exception.NulsException;
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
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.transaction.TestCommonUtil.*;
import static io.nuls.transaction.utils.LoggerUtil.LOG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class TestJyc {

    private static Chain chain;

    @BeforeClass
    public static void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
        chain = new Chain();
        chain.setConfig(new ConfigBean(CHAIN_ID, ASSET_ID, 1024 * 1024, 1000, 20, 20000, 60000));
    }

    @Test
    public void name() throws Exception {
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", PASSWORD);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        boolean success = transfer(SOURCE_ADDRESS, "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp", "1000000000").isSuccess();
        System.out.println("transfer-" + success);
        boolean alias = setAlias(chain, "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp");
        System.out.println("setAlias-" + alias);
    }

    /**
     * 导入种子节点
     */
    @Test
    public void importSeed() {
//        importPriKey("14a37507d42e474b45e7f2914c4fc317bbf3a428f6d9a398f5719a3be6bb74b1", PASSWORD);//tNULSeBaMjESuVomqR74SbUmTHwQGEKAeE9awT
//        importPriKey("60bdc4d03a10de2f86f351f2e7cecc2d306b7150265e19727148f1c51bec2fd8", PASSWORD);//tNULSeBaMtsumpXhfEZBU2pMEz7SHLcx5b2TQr
//        importPriKey("7769721125746a25ebd8cbd8f2b39c54dfb82eefd918cd6d940580bed2a758d1", PASSWORD);//tNULSeBaMkwmNkUJGBkdAkUaddbTnQ1tzBUqkT
//        importPriKey("6420b85c05334451688dfb5d01926bef98699c9e914dc262fcc3f625c04d2fd5", PASSWORD);//tNULSeBaMhwGMdTsVZC6Gg8ad5XA8CjZpR95MK
//        importPriKey("146b6920c0992bd7f3a434651462fe47f446c385636d35d2085035b843458467", PASSWORD);//tNULSeBaMqt2J3V8TdY69Gwb2yPCpeRaHn5tW6
        importPriKey("b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5", PASSWORD);//tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp
//        importPriKey("188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f", PASSWORD);//tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe
//        importPriKey("9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b", PASSWORD);//tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
//        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", PASSWORD);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
//        importPriKey("8212e7ba23c8b52790c45b0514490356cd819db15d364cbe08659b5888339e78", PASSWORD);//tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24
//        importPriKey("4100e2f88c3dba08e5000ed3e8da1ae4f1e0041b856c09d35a26fb399550f530", PASSWORD);//tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD
//        importPriKey("bec819ef7d5beeb1593790254583e077e00f481982bce1a43ea2830a2dc4fdf7", PASSWORD);//tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL
//        importPriKey("ddddb7cb859a467fbe05d5034735de9e62ad06db6557b64d7c139b6db856b200", PASSWORD);//tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL
//        importPriKey("4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a", PASSWORD);//tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm
//        importPriKey("3dadac00b523736f38f8c57deb81aa7ec612b68448995856038bd26addd80ec1", PASSWORD);//tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1
//        importPriKey("27dbdcd1f2d6166001e5a722afbbb86a845ef590433ab4fcd13b9a433af6e66e", PASSWORD);//tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2
//        importPriKey("76b7beaa98db863fb680def099af872978209ed9422b7acab8ab57ad95ab218b", PASSWORD);//tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn
//        importPriKey("00a6eef7b91c645525bb8410f2a79e1299a69d0d7ef980068434b6aca90ab6d9", PASSWORD);//tNULSeBaMiAQSiqXHBUypfMGZzcroe12W4SFbi
    }

    /**
     * 加入共识
     * 普通转账
     * 创建节点
     * 委托
     *
     * @throws NulsException
     */
    @Test
    public void joinConsensus() throws Exception {
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", PASSWORD);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        BigInteger balance = getBalance(chain, SOURCE_ADDRESS);
        LOG.info(SOURCE_ADDRESS + "-----balance:{}", balance);
        List<String> accountList;
        try {
            LOG.info("##################################################");
            String agentAddress = "";
            String packingAddress = "";
            String agentHash = "";
            String depositHash = "";
            {
                LOG.info("1.##########新建两个地址，一个作为节点地址，一个作为打包地址##########");
                //新建两个地址
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, VERSION);
                params.put(Constants.CHAIN_ID, CHAIN_ID);
                params.put("count", 2);
                params.put("password", PASSWORD);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                assertTrue(response.isSuccess());
                accountList = (List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list");
                agentAddress = accountList.get(0);
                packingAddress = accountList.get(1);
                LOG.info("agentAddress-{{}}", agentAddress);
                LOG.info("packingAddress-{{}}", packingAddress);
            }
            {
                LOG.info("2.##########从创世块地址转账给新创建的地址##########");
                Map transferMap = new HashMap();
                transferMap.put("chainId", CHAIN_ID);
                transferMap.put("remark", "transfer test");
                List<CoinDTO> inputs = new ArrayList<>();
                List<CoinDTO> outputs = new ArrayList<>();
                CoinDTO inputCoin1 = new CoinDTO();
                inputCoin1.setAddress(SOURCE_ADDRESS);
                inputCoin1.setPassword(PASSWORD);
                inputCoin1.setAssetsChainId(CHAIN_ID);
                inputCoin1.setAssetsId(ASSET_ID);
                inputCoin1.setAmount(new BigInteger("25000700000000"));
                inputs.add(inputCoin1);

                CoinDTO outputCoin1 = new CoinDTO();
                outputCoin1.setAddress(agentAddress);
                outputCoin1.setPassword(PASSWORD);
                outputCoin1.setAssetsChainId(CHAIN_ID);
                outputCoin1.setAssetsId(ASSET_ID);
                outputCoin1.setAmount(new BigInteger("25000100000000"));
                outputs.add(outputCoin1);

                CoinDTO outputCoin2 = new CoinDTO();
                outputCoin2.setAddress(packingAddress);
                outputCoin2.setPassword(PASSWORD);
                outputCoin2.setAssetsChainId(CHAIN_ID);
                outputCoin2.setAssetsId(ASSET_ID);
                outputCoin2.setAmount(new BigInteger("500000000"));
                outputs.add(outputCoin2);
                transferMap.put("inputs", inputs);
                transferMap.put("outputs", outputs);

                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
                assertTrue(response.isSuccess());
                HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                LOG.info("transfer hash:{}", result.get("value"));
                LOG.info("transfer from {} to {}", SOURCE_ADDRESS, agentAddress);
                LOG.info("transfer from {} to {}", SOURCE_ADDRESS, packingAddress);
            }

            Thread.sleep(15000);
            {
                LOG.info("3.##########创建节点##########");
                BigInteger agentBalance = getBalance(chain, agentAddress);
                LOG.info(agentAddress + "-----balance:{}", agentBalance);
                assertEquals(new BigInteger("25000100000000"), agentBalance);
                BigInteger packingBalance = getBalance(chain, packingAddress);
                LOG.info(packingAddress + "-----balance:{}", packingBalance);
                assertEquals(new BigInteger("500000000"), packingBalance);
                //创建节点
                Map agentTxMap = createAgentTx(agentAddress, packingAddress);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
                assertTrue(response.isSuccess());
                Map map = (HashMap) (((HashMap) response.getResponseData()).get("cs_createAgent"));
                agentHash = (String) map.get("txHash");
                LOG.info("createAgent-txHash:{}", agentHash);
            }

            Thread.sleep(15000);
            {
                LOG.info("4.##########进行委托##########");
                Map<String, Object> dpParams = new HashMap<>();
                dpParams.put(Constants.CHAIN_ID, CHAIN_ID);
                dpParams.put("address", agentAddress);
                dpParams.put("password", PASSWORD);
                dpParams.put("agentHash", agentHash);
                dpParams.put("deposit", 200000 * 100000000L);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
                assertTrue(response.isSuccess());
                HashMap dpResult = (HashMap) ((HashMap) response.getResponseData()).get("cs_depositToAgent");
                depositHash = (String) dpResult.get("txHash");
                LOG.info("deposit-txHash:{}", depositHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 包含各种交易
     * 普通转账
     * 设置别名
     * 创建节点
     * 委托
     * 取消委托
     * 删除节点
     * 黄牌
     * 红牌
     *
     * @throws NulsException
     */
    @Test
    public void allInOne() throws Exception {
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", PASSWORD);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        BigInteger balance = getBalance(chain, SOURCE_ADDRESS);
        LOG.info(SOURCE_ADDRESS + "-----balance:{}", balance);
        List<String> accountList;
        try {
            while (true) {
                LOG.info("##################################################");
                String agentAddress = "";
                String packingAddress = "";
                String agentHash = "";
                String depositHash = "";
                {
                    LOG.info("1.##########新建两个地址，一个作为节点地址，一个作为打包地址##########");
                    //新建两个地址
                    Map<String, Object> params = new HashMap<>();
                    params.put(Constants.VERSION_KEY_STR, VERSION);
                    params.put(Constants.CHAIN_ID, CHAIN_ID);
                    params.put("count", 2);
                    params.put("password", PASSWORD);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                    assertTrue(response.isSuccess());
                    accountList = (List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list");
                    agentAddress = accountList.get(0);
                    packingAddress = accountList.get(1);
                    LOG.info("agentAddress-{{}}", agentAddress);
                    LOG.info("packingAddress-{{}}", packingAddress);
                }
                {
                    LOG.info("2.##########从创世块地址转账给新创建的地址##########");
                    Map transferMap = new HashMap();
                    transferMap.put("chainId", CHAIN_ID);
                    transferMap.put("remark", "transfer test");
                    List<CoinDTO> inputs = new ArrayList<>();
                    List<CoinDTO> outputs = new ArrayList<>();
                    CoinDTO inputCoin1 = new CoinDTO();
                    inputCoin1.setAddress(SOURCE_ADDRESS);
                    inputCoin1.setPassword(PASSWORD);
                    inputCoin1.setAssetsChainId(CHAIN_ID);
                    inputCoin1.setAssetsId(ASSET_ID);
                    inputCoin1.setAmount(new BigInteger("25000700000000"));
                    inputs.add(inputCoin1);

                    CoinDTO outputCoin1 = new CoinDTO();
                    outputCoin1.setAddress(agentAddress);
                    outputCoin1.setPassword(PASSWORD);
                    outputCoin1.setAssetsChainId(CHAIN_ID);
                    outputCoin1.setAssetsId(ASSET_ID);
                    outputCoin1.setAmount(new BigInteger("25000100000000"));
                    outputs.add(outputCoin1);

                    CoinDTO outputCoin2 = new CoinDTO();
                    outputCoin2.setAddress(packingAddress);
                    outputCoin2.setPassword(PASSWORD);
                    outputCoin2.setAssetsChainId(CHAIN_ID);
                    outputCoin2.setAssetsId(ASSET_ID);
                    outputCoin2.setAmount(new BigInteger("500000000"));
                    outputs.add(outputCoin2);
                    transferMap.put("inputs", inputs);
                    transferMap.put("outputs", outputs);

                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
                    assertTrue(response.isSuccess());
                    HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                    LOG.info("transfer hash:{}", result.get("value"));
                    LOG.info("transfer from {} to {}", SOURCE_ADDRESS, agentAddress);
                    LOG.info("transfer from {} to {}", SOURCE_ADDRESS, packingAddress);
                }

                Thread.sleep(5000);
                {
                    LOG.info("3.##########给新创建的地址设置别名##########");
                    BigInteger agentBalance = getBalance(chain, agentAddress);
                    LOG.info(agentAddress + "-----balance:{}", agentBalance);
                    assertEquals(new BigInteger("25000100000000"), agentBalance);
                    BigInteger packingBalance = getBalance(chain, packingAddress);
                    LOG.info(packingAddress + "-----balance:{}", packingBalance);
                    assertEquals(new BigInteger("500000000"), packingBalance);
                    {
                        setAlias(chain, agentAddress);
                        setAlias(chain, packingAddress);
                    }
                    Thread.sleep(15000);
                    LOG.info("4.##########创建节点##########");
                    //创建节点
                    Map agentTxMap = createAgentTx(agentAddress, packingAddress);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
                    assertTrue(response.isSuccess());
                    Map map = (HashMap) (((HashMap) response.getResponseData()).get("cs_createAgent"));
                    agentHash = (String) map.get("txHash");
                    LOG.info("createAgent-txHash:{}", agentHash);
                }

                Thread.sleep(15000);
                {
                    LOG.info("5.##########进行委托##########");
                    Map<String, Object> dpParams = new HashMap<>();
                    dpParams.put(Constants.CHAIN_ID, CHAIN_ID);
                    dpParams.put("address", agentAddress);
                    dpParams.put("password", PASSWORD);
                    dpParams.put("agentHash", agentHash);
                    dpParams.put("deposit", 200000 * 100000000L);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
                    assertTrue(response.isSuccess());
                    HashMap dpResult = (HashMap) ((HashMap) response.getResponseData()).get("cs_depositToAgent");
                    depositHash = (String) dpResult.get("txHash");
                    LOG.info("deposit-txHash:{}", depositHash);
                }

                Thread.sleep(60000);
                {
                    LOG.info("6.##########取消委托##########");
                    //取消委托
                    Map<String, Object> params = new HashMap<>();
                    params.put(Constants.CHAIN_ID, CHAIN_ID);
                    params.put("address", agentAddress);
                    params.put("password", PASSWORD);
                    params.put("txHash", depositHash);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
                    assertTrue(response.isSuccess());
                    HashMap dpResult = (HashMap) ((HashMap) response.getResponseData()).get("cs_withdraw");
                    String hash = (String) dpResult.get("txHash");
                    LOG.info("withdraw-txHash:{}", hash);
                }

                Thread.sleep(60000);
                {
                    LOG.info("7.##########再次委托##########");
                    //再次委托
                    Map<String, Object> dpParams = new HashMap<>();
                    dpParams.put(Constants.CHAIN_ID, CHAIN_ID);
                    dpParams.put("address", agentAddress);
                    dpParams.put("password", PASSWORD);
                    dpParams.put("agentHash", agentHash);
                    dpParams.put("deposit", 200000 * 100000000L);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
                    assertTrue(response.isSuccess());
                    HashMap dpResult = (HashMap) ((HashMap) response.getResponseData()).get("cs_depositToAgent");
                    depositHash = (String) dpResult.get("txHash");
                    LOG.info("deposit-txHash:{}", depositHash);
                }

                Thread.sleep(60000);
                {
                    LOG.info("8.##########删除节点账户，制造黄牌##########");
                    Map<String, Object> params = new HashMap<>();
                    params.put(Constants.VERSION_KEY_STR, VERSION);
                    params.put(Constants.CHAIN_ID, CHAIN_ID);
                    params.put("address", packingAddress);
                    params.put("password", PASSWORD);
                    Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
                    HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
                    String priKey = (String) result.get("priKey");
                    removeAccount(packingAddress, PASSWORD);
                    Thread.sleep(60000);
                    LOG.info("9.##########导入节点账户，重新加入共识##########");
                    importPriKey(priKey, PASSWORD);
                    Thread.sleep(60000);
                }

                {
                    LOG.info("10.##########删除节点##########");
                    //停止节点
                    Map<String, Object> txMap = new HashMap();
                    txMap.put("chainId", CHAIN_ID);
                    txMap.put("address", agentAddress);
                    txMap.put("password", PASSWORD);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
                    assertTrue(response.isSuccess());
                    Map result = (HashMap) (((HashMap) response.getResponseData()).get("cs_stopAgent"));
                    String txHash = (String) result.get("txHash");
                    LOG.info("stopAgent-txHash:{}", txHash);
                }

                Thread.sleep(60000);
                {
                    LOG.info("11.##########查询刚才创建的地址余额并返还给创世块地址##########");
                    BigInteger agentBalance = getBalance(chain, agentAddress);
                    LOG.info(agentAddress + "-----balance:{}", agentBalance);
                    BigInteger packingBalance = getBalance(chain, packingAddress);
                    LOG.info(packingAddress + "-----balance:{}", packingBalance);
                    Map transferMap = new HashMap();
                    transferMap.put("chainId", CHAIN_ID);
                    transferMap.put("remark", "transfer test");
                    List<CoinDTO> inputs = new ArrayList<>();
                    List<CoinDTO> outputs = new ArrayList<>();
                    CoinDTO inputCoin1 = new CoinDTO();
                    inputCoin1.setAddress(agentAddress);
                    inputCoin1.setPassword(PASSWORD);
                    inputCoin1.setAssetsChainId(CHAIN_ID);
                    inputCoin1.setAssetsId(ASSET_ID);
                    inputCoin1.setAmount(agentBalance.subtract(new BigInteger("100000000")));
                    inputs.add(inputCoin1);
                    CoinDTO inputCoin2 = new CoinDTO();
                    inputCoin2.setAddress(packingAddress);
                    inputCoin2.setPassword(PASSWORD);
                    inputCoin2.setAssetsChainId(CHAIN_ID);
                    inputCoin2.setAssetsId(ASSET_ID);
                    inputCoin2.setAmount(packingBalance.subtract(new BigInteger("100000000")));
                    inputs.add(inputCoin2);

                    CoinDTO outputCoin1 = new CoinDTO();
                    outputCoin1.setAddress(SOURCE_ADDRESS);
                    outputCoin1.setPassword(PASSWORD);
                    outputCoin1.setAssetsChainId(CHAIN_ID);
                    outputCoin1.setAssetsId(ASSET_ID);
                    outputCoin1.setAmount(agentBalance.add(packingBalance).subtract(new BigInteger("200000000")));
                    outputs.add(outputCoin1);
                    transferMap.put("inputs", inputs);
                    transferMap.put("outputs", outputs);

                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
                    HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                    LOG.info("ac_transfer hash:{}", result.get("value"));
                    assertTrue(response.isSuccess());
                }
                Thread.sleep(60000);
                BigInteger agentBalance = getBalance(chain, agentAddress);
                LOG.info(agentAddress + "---balance:{}", agentBalance);
                BigInteger packingBalance = getBalance(chain, packingAddress);
                LOG.info(packingAddress + "---balance:{}", packingBalance);
                LOG.info("##################################################");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 压力测试，发起大量普通转账
     *
     * @throws Exception
     */
    @Test
    public void pressureTest() throws Exception {
        int total = 10_000_0000;
        int count = 1000;
        LOG.info("1.##########check or create " + count + " accounts##########");
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", PASSWORD);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        List<String> accountList = getAccountList();
        LOG.info("already have " + accountList.size() + " accounts");
        if (accountList.size() < count) {
            List<String> list = createAccounts(count - accountList.size());
            accountList.addAll(list);
        }
        for (String e : accountList) {
            checkBalance(chain, e, "10000000000");
        }
        Thread.sleep(10000);
        testRunning(total, count, accountList);
    }

    private void testRunning(int total, int count, List<String> accountList) throws Exception {
        LOG.info("##########" + count + " accounts Transfer to each other##########");
        //100个地址之间互相转账
        for (int j = 0; j < total / count; j++) {
            for (int i = 0; i < count; i++) {
                String from = accountList.get(i % count);
                String to = accountList.get((i + 1) % count);
                Response response = transfer(from, to, "100000000");
                assertTrue(response.isSuccess());
            }
            LOG.info("##########" + j + " round end##########");
            Thread.sleep(1000);
        }
    }

    /**
     * 稳定性测试
     */
    @Test
    public void stabilityTest() throws Exception {
        {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, VERSION);
            params.put(Constants.CHAIN_ID, CHAIN_ID);
            params.put("address", SOURCE_ADDRESS);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountByAddress", params);

            String address = JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("ac_getAccountByAddress"));
            LOG.info("address-{}" + address);
            BigInteger balance = getBalance(chain, SOURCE_ADDRESS);
            LOG.info(SOURCE_ADDRESS + "-----balance:{}", balance);
        }
        int total = 100_000_000;
        int count = 500;
        List<String> accountList = new ArrayList<>();
        LOG.info("##################################################");
        {
            LOG.info("1.##########create " + count + " accounts##########");
            int loop = count / 100 == 0 ? 1 : count / 100;
            for (int i = 0; i < loop; i++) {
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, VERSION);
                params.put(Constants.CHAIN_ID, CHAIN_ID);
                params.put("count", count < 100 ? count : 100);
                params.put("password", PASSWORD);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                assertTrue(response.isSuccess());
                accountList.addAll((List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list"));
            }
            assertEquals(count, accountList.size());
            for (String account : accountList) {
                LOG.info("address-{}", account);
            }
        }
        {
            LOG.info("2.##########transfer from seed address to " + count + " accounts##########");
            for (int i = 0, accountListSize = accountList.size(); i < accountListSize; i++) {
                String account = accountList.get(i);
                Response response = transfer(SOURCE_ADDRESS, account, "100000000000");
                assertTrue(response.isSuccess());
                HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                LOG.info(i + "---transfer from {} to {}, hash:{}", SOURCE_ADDRESS, account, result.get("value"));
                Thread.sleep(100);
            }
        }
        Thread.sleep(60000);
        List<String> hashList = new ArrayList<>();
        int intervel = 100;
        {
            LOG.info("3.##########" + count + " accounts Transfer to each other##########");
            //100个地址之间互相转账
            for (int j = 0; j < total / count; j++) {
                for (int i = 0; i < count; i++) {
                    String from = accountList.get(i % count);
                    String to = accountList.get((i + 1) % count);

                    Response response = transfer(from, to, "100000000");
                    assertTrue(response.isSuccess());
                    HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                    String hash = result.get("value").toString();
                    hashList.add(hash);
                    LOG.info("transfer from {} to {}, hash:{}", from, to, hash);
                }
                LOG.info("##########" + j + " round end##########");
                Thread.sleep(intervel * 100);
                intervel--;
                intervel = intervel < 1 ? 100 : intervel;
            }
        }
        Thread.sleep(100000);
        {
            while (true) {
                for (Iterator<String> iterator = hashList.iterator(); iterator.hasNext(); ) {
                    String hash = iterator.next();
                    if (queryTx(hash, true)) {
                        iterator.remove();
                    }
                }
                if (hashList.size() == 0) {
                    break;
                }
                LOG.info("remain " + hashList.size() + " hash not verify");
                Thread.sleep(10000);
            }
        }
    }

    /**
     * 发交易从1发到5000
     */
    @Test
    public void blockSaveTest() throws Exception {
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", PASSWORD);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        int total = 100_000_000;
        int count = 10;
        List<String> accountList = new ArrayList<>();
        LOG.info("##################################################");
        {
            LOG.info("1.##########create " + count + " accounts##########");
            int loop = count / 100 == 0 ? 1 : count / 100;
            for (int i = 0; i < loop; i++) {
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, VERSION);
                params.put(Constants.CHAIN_ID, CHAIN_ID);
                params.put("count", count < 100 ? count : 100);
                params.put("password", PASSWORD);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                assertTrue(response.isSuccess());
                accountList.addAll((List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list"));
            }
            assertEquals(count, accountList.size());
        }
        {
            LOG.info("2.##########transfer from seed address to " + count + " accounts##########");
            for (int i = 0, accountListSize = accountList.size(); i < accountListSize; i++) {
                String account = accountList.get(i);
                Response response = transfer(SOURCE_ADDRESS, account, "5000000000");
                assertTrue(response.isSuccess());
                HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                LOG.info(i + "---transfer from {} to {}, hash:{}", SOURCE_ADDRESS, account, result.get("value"));
            }
        }
        Thread.sleep(20000);
        {
            LOG.info("3.##########" + count + " accounts Transfer to each other##########");
            //100个地址之间互相转账
            int num = 0;//发了多少个交易
            int limit = 1;
            for (int j = 0; j < total / count; j++) {
                for (int i = 0; i < count; i++) {
                    String from = accountList.get(i % count);
                    String to = accountList.get((i + 1) % count);
                    Response response = transfer(from, to, "100000000");
                    assertTrue(response.isSuccess());
                    num++;
                    if (num == limit) {
                        if (count == limit) {
                            limit = 1;
                        }
                        if (total == limit) {
                            return;
                        }
                        LOG.info("send " + num + " tx");
                        num = 0;
                        limit++;
                        Thread.sleep(10000);
                    }
                }
            }
        }
    }

    /**
     * 发交易从1发到5000
     */
    @Test
    public void blockSaveTest1() throws Exception {
        int total = 100_000_000;
        List<String> accountList = getAccountList();
        int count = accountList.size();
        LOG.info("##################################################");
        {
            LOG.info("3.##########" + count + " accounts Transfer to each other##########");
            //100个地址之间互相转账
            int num = 0;//发了多少个交易
            int limit = 1;
            for (int j = 0; j < total / count; j++) {
                for (int i = 0; i < count; i++) {
                    String from = accountList.get(i % count);
                    String to = accountList.get((i + 1) % count);
                    Response response = transfer(from, to, "100000000");
                    assertTrue(response.isSuccess());
                    num++;
                    if (num == limit) {
                        if (count == limit) {
                            limit = 1;
                        }
                        if (total == limit) {
                            return;
                        }
                        LOG.info("send " + num + " tx");
                        num = 0;
                        limit++;
                        Thread.sleep(10000);
                    }
                }
            }
        }
    }
}
