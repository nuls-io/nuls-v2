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
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.rpc.call.LedgerCall;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static io.nuls.transaction.utils.LoggerUtil.LOG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class TestJyc {
    private static String sourceAddress = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    private static Chain chain;
    private static int chainId = 2;
    private static int assetChainId = 2;
    private static int assetId = 1;
    private static String version = "1.0";
    private static String password = "nuls123456";

    /**
     * 模拟测试 module
     *
     * @throws Exception
     */
    @BeforeClass
    public static void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, 1024*1024,1000,20,20000,60000));
    }

    @Test
    public void test() throws Exception {
        {
//            balance("tNULSeBaMnrs6JKrCy6TQdzYJZkMbloZJDng7QAsD");
            removeAccount("tNULSeBaMrcW3H8KwKefbs2SZR5pJJySPjQsuo", password);
            removeAccount("tNULSeBaMumFNjGGSxoXRtknNUXBm6DdKZ8yTQ", password);
            removeAccount("tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", password);
        }
    }

    private BigInteger balance(String address) throws Exception {
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(address), chainId, assetId);
        LOG.debug(address + "-----balance:{}", balance);
        return balance;
    }

    /**
     * 导入种子节点
     */
    @Test
    public void importSeed() {
        importPriKey("188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f", password);//种子出块地址 tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe
//        importPriKey("14a37507d42e474b45e7f2914c4fc317bbf3a428f6d9a398f5719a3be6bb74b1", password); //tNULSeBaMjESuVomqR74SbUmTHwQGEKAeE9awT      32
//        importPriKey("60bdc4d03a10de2f86f351f2e7cecc2d306b7150265e19727148f1c51bec2fd8", password); //tNULSeBaMtsumpXhfEZBU2pMEz7SHLcx5b2TQr      192

//        importPriKey("7769721125746a25ebd8cbd8f2b39c54dfb82eefd918cd6d940580bed2a758d1", password); //tNULSeBaMkwmNkUJGBkdAkUaddbTnQ1tzBUqkT      248
//        importPriKey("6420b85c05334451688dfb5d01926bef98699c9e914dc262fcc3f625c04d2fd5", password); //tNULSeBaMhwGMdTsVZC6Gg8ad5XA8CjZpR95MK      247
//        importPriKey("146b6920c0992bd7f3a434651462fe47f446c385636d35d2085035b843458467", password); //tNULSeBaMqt2J3V8TdY69Gwb2yPCpeRaHn5tW6      135
//        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
    }

    private List<String> getAccountList() throws Exception {
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountList", null);
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

    @Test
    public void getAgentList() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("pageNumber", 1);
        params.put("pageSize", 10);
        params.put("keyWord", "");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentList", params);
        Object o = ((HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_getAgentList")).get("list");
        LOG.debug("list:{}", o);
    }

    /**
     * 加入共识
     *  普通转账
     *  创建节点
     *  委托
     *
     * @throws NulsException
     */
    @Test
    public void joinConsensus() throws NulsException {
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(sourceAddress), chainId, assetId);
        LOG.debug(sourceAddress + "-----balance:{}", balance);
        List<String> accountList;
        try {
            LOG.debug("##################################################");
            String agentAddress = "";
            String packingAddress = "";
            String agentHash = "";
            String depositHash = "";
            {
                LOG.debug("1.##########新建两个地址，一个作为节点地址，一个作为打包地址##########");
                //新建两个地址
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, version);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("count", 2);
                params.put("password", password);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                assertTrue(response.isSuccess());
                accountList = (List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list");
                agentAddress = accountList.get(0);
                packingAddress = accountList.get(1);
                LOG.debug("agentAddress-{{}}", agentAddress);
                LOG.debug("packingAddress-{{}}", packingAddress);
            }
            {
                LOG.debug("2.##########从创世块地址转账给新创建的地址##########");
                Map transferMap = new HashMap();
                transferMap.put("chainId", chainId);
                transferMap.put("remark", "transfer test");
                List<CoinDTO> inputs = new ArrayList<>();
                List<CoinDTO> outputs = new ArrayList<>();
                CoinDTO inputCoin1 = new CoinDTO();
                inputCoin1.setAddress(sourceAddress);
                inputCoin1.setPassword(password);
                inputCoin1.setAssetsChainId(chainId);
                inputCoin1.setAssetsId(assetId);
                inputCoin1.setAmount(new BigInteger("25000700000000"));
                inputs.add(inputCoin1);

                CoinDTO outputCoin1 = new CoinDTO();
                outputCoin1.setAddress(agentAddress);
                outputCoin1.setPassword(password);
                outputCoin1.setAssetsChainId(chainId);
                outputCoin1.setAssetsId(assetId);
                outputCoin1.setAmount(new BigInteger("25000100000000"));
                outputs.add(outputCoin1);

                CoinDTO outputCoin2 = new CoinDTO();
                outputCoin2.setAddress(packingAddress);
                outputCoin2.setPassword(password);
                outputCoin2.setAssetsChainId(chainId);
                outputCoin2.setAssetsId(assetId);
                outputCoin2.setAmount(new BigInteger("500000000"));
                outputs.add(outputCoin2);
                transferMap.put("inputs", inputs);
                transferMap.put("outputs", outputs);

                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
                assertTrue(response.isSuccess());
                HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                LOG.debug("transfer hash:{}", result.get("value"));
                LOG.debug("transfer from {} to {}", sourceAddress, agentAddress);
                LOG.debug("transfer from {} to {}", sourceAddress, packingAddress);
            }

            Thread.sleep(15000);
            {
                LOG.debug("3.##########创建节点##########");
                BigInteger agentBalance = LedgerCall.getBalance(chain, AddressTool.getAddress(agentAddress), chainId, assetId);
                LOG.debug(agentAddress + "-----balance:{}", agentBalance);
                assertEquals(new BigInteger("25000100000000"), agentBalance);
                BigInteger packingBalance = LedgerCall.getBalance(chain, AddressTool.getAddress(packingAddress), chainId, assetId);
                LOG.debug(packingAddress + "-----balance:{}", packingBalance);
                assertEquals(new BigInteger("500000000"), packingBalance);
                //创建节点
                Map agentTxMap = this.createAgentTx(agentAddress, packingAddress);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
                assertTrue(response.isSuccess());
                Map map = (HashMap) (((HashMap) response.getResponseData()).get("cs_createAgent"));
                agentHash = (String) map.get("txHash");
                LOG.debug("createAgent-txHash:{}", agentHash);
            }

            Thread.sleep(15000);
            {
                LOG.debug("4.##########进行委托##########");
                Map<String, Object> dpParams = new HashMap<>();
                dpParams.put(Constants.CHAIN_ID, chainId);
                dpParams.put("address", agentAddress);
                dpParams.put("password", password);
                dpParams.put("agentHash", agentHash);
                dpParams.put("deposit", 200000 * 100000000L);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
                assertTrue(response.isSuccess());
                HashMap dpResult = (HashMap) ((HashMap) response.getResponseData()).get("cs_depositToAgent");
                depositHash = (String) dpResult.get("txHash");
                LOG.debug("deposit-txHash:{}", depositHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 包含各种交易
     *  普通转账
     *  设置别名
     *  创建节点
     *  委托
     *  取消委托
     *  删除节点
     *  黄牌
     *  红牌
     *
     * @throws NulsException
     */
    @Test
    public void allInOne() throws NulsException {
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(sourceAddress), chainId, assetId);
        LOG.debug(sourceAddress + "-----balance:{}", balance);
        List<String> accountList;
        try {
            while (true) {
                LOG.debug("##################################################");
                String agentAddress = "";
                String packingAddress = "";
                String agentHash = "";
                String depositHash = "";
                {
                    LOG.debug("1.##########新建两个地址，一个作为节点地址，一个作为打包地址##########");
                    //新建两个地址
                    Map<String, Object> params = new HashMap<>();
                    params.put(Constants.VERSION_KEY_STR, version);
                    params.put(Constants.CHAIN_ID, chainId);
                    params.put("count", 2);
                    params.put("password", password);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                    assertTrue(response.isSuccess());
                    accountList = (List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list");
                    agentAddress = accountList.get(0);
                    packingAddress = accountList.get(1);
                    LOG.debug("agentAddress-{{}}", agentAddress);
                    LOG.debug("packingAddress-{{}}", packingAddress);
                }
                {
                    LOG.debug("2.##########从创世块地址转账给新创建的地址##########");
                    Map transferMap = new HashMap();
                    transferMap.put("chainId", chainId);
                    transferMap.put("remark", "transfer test");
                    List<CoinDTO> inputs = new ArrayList<>();
                    List<CoinDTO> outputs = new ArrayList<>();
                    CoinDTO inputCoin1 = new CoinDTO();
                    inputCoin1.setAddress(sourceAddress);
                    inputCoin1.setPassword(password);
                    inputCoin1.setAssetsChainId(chainId);
                    inputCoin1.setAssetsId(assetId);
                    inputCoin1.setAmount(new BigInteger("25000700000000"));
                    inputs.add(inputCoin1);

                    CoinDTO outputCoin1 = new CoinDTO();
                    outputCoin1.setAddress(agentAddress);
                    outputCoin1.setPassword(password);
                    outputCoin1.setAssetsChainId(chainId);
                    outputCoin1.setAssetsId(assetId);
                    outputCoin1.setAmount(new BigInteger("25000100000000"));
                    outputs.add(outputCoin1);

                    CoinDTO outputCoin2 = new CoinDTO();
                    outputCoin2.setAddress(packingAddress);
                    outputCoin2.setPassword(password);
                    outputCoin2.setAssetsChainId(chainId);
                    outputCoin2.setAssetsId(assetId);
                    outputCoin2.setAmount(new BigInteger("500000000"));
                    outputs.add(outputCoin2);
                    transferMap.put("inputs", inputs);
                    transferMap.put("outputs", outputs);

                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
                    assertTrue(response.isSuccess());
                    HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                    LOG.debug("transfer hash:{}", result.get("value"));
                    LOG.debug("transfer from {} to {}", sourceAddress, agentAddress);
                    LOG.debug("transfer from {} to {}", sourceAddress, packingAddress);
                }

                Thread.sleep(60000);
                {
                    LOG.debug("3.##########给新创建的地址设置别名##########");
                    BigInteger agentBalance = LedgerCall.getBalance(chain, AddressTool.getAddress(agentAddress), chainId, assetId);
                    LOG.debug(agentAddress + "-----balance:{}", agentBalance);
                    assertEquals(new BigInteger("25000100000000"), agentBalance);
                    BigInteger packingBalance = LedgerCall.getBalance(chain, AddressTool.getAddress(packingAddress), chainId, assetId);
                    LOG.debug(packingAddress + "-----balance:{}", packingBalance);
                    assertEquals(new BigInteger("500000000"), packingBalance);
                    {
                        String alias = "jyc_" + System.currentTimeMillis();
                        Map<String, Object> params = new HashMap<>();
                        params.put(Constants.VERSION_KEY_STR, "1.0");
                        params.put(Constants.CHAIN_ID, chainId);
                        params.put("address", agentAddress);
                        params.put("password", password);
                        params.put("alias", alias);
                        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
                        assertTrue(response.isSuccess());
                        HashMap result = (HashMap) ((HashMap) response.getResponseData()).get("ac_setAlias");
                        String txHash = (String) result.get("txHash");
                        LOG.debug("agentAddress alias-txHash:{}", txHash);
                    }
                    {
                        String alias = "jyc_" + System.currentTimeMillis();
                        Map<String, Object> params = new HashMap<>();
                        params.put(Constants.VERSION_KEY_STR, "1.0");
                        params.put(Constants.CHAIN_ID, chainId);
                        params.put("address", packingAddress);
                        params.put("password", password);
                        params.put("alias", alias);
                        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
                        assertTrue(response.isSuccess());
                        HashMap result = (HashMap) ((HashMap) response.getResponseData()).get("ac_setAlias");
                        String txHash = (String) result.get("txHash");
                        LOG.debug("packingAddress alias-txHash:{}", txHash);
                    }
                    Thread.sleep(60000);
                    LOG.debug("4.##########创建节点##########");
                    //创建节点
                    Map agentTxMap = this.createAgentTx(agentAddress, packingAddress);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
                    assertTrue(response.isSuccess());
                    Map map = (HashMap) (((HashMap) response.getResponseData()).get("cs_createAgent"));
                    agentHash = (String) map.get("txHash");
                    LOG.debug("createAgent-txHash:{}", agentHash);
                }

                Thread.sleep(60000);
                {
                    LOG.debug("5.##########进行委托##########");
                    Map<String, Object> dpParams = new HashMap<>();
                    dpParams.put(Constants.CHAIN_ID, chainId);
                    dpParams.put("address", agentAddress);
                    dpParams.put("password", password);
                    dpParams.put("agentHash", agentHash);
                    dpParams.put("deposit", 200000 * 100000000L);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
                    assertTrue(response.isSuccess());
                    HashMap dpResult = (HashMap) ((HashMap) response.getResponseData()).get("cs_depositToAgent");
                    depositHash = (String) dpResult.get("txHash");
                    LOG.debug("deposit-txHash:{}", depositHash);
                }

                Thread.sleep(60000);
                {
                    LOG.debug("6.##########取消委托##########");
                    //取消委托
                    Map<String, Object> params = new HashMap<>();
                    params.put(Constants.CHAIN_ID, chainId);
                    params.put("address", agentAddress);
                    params.put("password", password);
                    params.put("txHash", depositHash);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
                    assertTrue(response.isSuccess());
                    HashMap dpResult = (HashMap) ((HashMap) response.getResponseData()).get("cs_withdraw");
                    String hash = (String) dpResult.get("txHash");
                    LOG.debug("withdraw-txHash:{}", hash);
                }

                Thread.sleep(60000);
                {
                    LOG.debug("7.##########再次委托##########");
                    //再次委托
                    Map<String, Object> dpParams = new HashMap<>();
                    dpParams.put(Constants.CHAIN_ID, chainId);
                    dpParams.put("address", agentAddress);
                    dpParams.put("password", password);
                    dpParams.put("agentHash", agentHash);
                    dpParams.put("deposit", 200000 * 100000000L);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
                    assertTrue(response.isSuccess());
                    HashMap dpResult = (HashMap) ((HashMap) response.getResponseData()).get("cs_depositToAgent");
                    depositHash = (String) dpResult.get("txHash");
                    LOG.debug("deposit-txHash:{}", depositHash);
                }

                Thread.sleep(60000);
                {
                    LOG.debug("8.##########删除节点账户，制造黄牌##########");
                    Map<String, Object> params = new HashMap<>();
                    params.put(Constants.VERSION_KEY_STR, version);
                    params.put(Constants.CHAIN_ID, chainId);
                    params.put("address", packingAddress);
                    params.put("password", password);
                    Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
                    HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
                    String priKey = (String) result.get("priKey");
                    removeAccount(packingAddress, password);
                    Thread.sleep(60000);
                    LOG.debug("9.##########导入节点账户，重新加入共识##########");
                    importPriKey(priKey, password);
                    Thread.sleep(60000);
                }

                {
                    LOG.debug("10.##########删除节点##########");
                    //停止节点
                    Map<String, Object> txMap = new HashMap();
                    txMap.put("chainId", chainId);
                    txMap.put("address", agentAddress);
                    txMap.put("password", password);
                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
                    assertTrue(response.isSuccess());
                    Map result = (HashMap) (((HashMap) response.getResponseData()).get("cs_stopAgent"));
                    String txHash = (String) result.get("txHash");
                    LOG.debug("stopAgent-txHash:{}", txHash);
                }

                Thread.sleep(60000);
                {
                    LOG.debug("11.##########查询刚才创建的地址余额并返还给创世块地址##########");
                    BigInteger agentBalance = LedgerCall.getBalance(chain, AddressTool.getAddress(agentAddress), chainId, assetId);
                    LOG.debug(agentAddress + "-----balance:{}", agentBalance);
                    BigInteger packingBalance = LedgerCall.getBalance(chain, AddressTool.getAddress(packingAddress), chainId, assetId);
                    LOG.debug(packingAddress + "-----balance:{}", packingBalance);
                    Map transferMap = new HashMap();
                    transferMap.put("chainId", chainId);
                    transferMap.put("remark", "transfer test");
                    List<CoinDTO> inputs = new ArrayList<>();
                    List<CoinDTO> outputs = new ArrayList<>();
                    CoinDTO inputCoin1 = new CoinDTO();
                    inputCoin1.setAddress(agentAddress);
                    inputCoin1.setPassword(password);
                    inputCoin1.setAssetsChainId(chainId);
                    inputCoin1.setAssetsId(assetId);
                    inputCoin1.setAmount(agentBalance.subtract(new BigInteger("100000000")));
                    inputs.add(inputCoin1);
                    CoinDTO inputCoin2 = new CoinDTO();
                    inputCoin2.setAddress(packingAddress);
                    inputCoin2.setPassword(password);
                    inputCoin2.setAssetsChainId(chainId);
                    inputCoin2.setAssetsId(assetId);
                    inputCoin2.setAmount(packingBalance.subtract(new BigInteger("100000000")));
                    inputs.add(inputCoin2);

                    CoinDTO outputCoin1 = new CoinDTO();
                    outputCoin1.setAddress(sourceAddress);
                    outputCoin1.setPassword(password);
                    outputCoin1.setAssetsChainId(chainId);
                    outputCoin1.setAssetsId(assetId);
                    outputCoin1.setAmount(agentBalance.add(packingBalance).subtract(new BigInteger("200000000")));
                    outputs.add(outputCoin1);
                    transferMap.put("inputs", inputs);
                    transferMap.put("outputs", outputs);

                    Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
                    HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                    LOG.debug("ac_transfer hash:{}", result.get("value"));
                    assertTrue(response.isSuccess());
                }
                Thread.sleep(60000);
                BigInteger agentBalance = LedgerCall.getBalance(chain, AddressTool.getAddress(agentAddress), chainId, assetId);
                LOG.debug(agentAddress + "---balance:{}", agentBalance);
                BigInteger packingBalance = LedgerCall.getBalance(chain, AddressTool.getAddress(packingAddress), chainId, assetId);
                LOG.debug(packingAddress + "---balance:{}", packingBalance);
                LOG.debug("##################################################");
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
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", sourceAddress);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountByAddress", params);

            String address = JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("ac_getAccountByAddress"));
            LOG.debug("address-{}" + address);
            BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(sourceAddress), chainId, assetId);
            LOG.debug(sourceAddress + "-----balance:{}", balance);
        }
        int total = 1000_000;
        int count = 1000;
        testRunning(total, count);
    }

    /**
     * 压力测试，发起大量普通转账
     *
     * @throws Exception
     */
    @Test
    public void pressureTestMultiThread() throws Exception {
        {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", sourceAddress);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountByAddress", params);

            String address = JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("ac_getAccountByAddress"));
            LOG.debug("address-{}" + address);
            BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(sourceAddress), chainId, assetId);
            LOG.debug(sourceAddress + "-----balance:{}", balance);
        }
        int total = 1000_000;
        int count = 500;
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        class worker implements Runnable {
            private CountDownLatch latch;

            public worker(CountDownLatch latch) {
                this.latch = latch;
            }

            @Override
            public void run() {
                try {
                    testRunning(total, count);
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i < threadCount; i++) {
            new Thread(new worker(latch)).start();
        }
        latch.await();
    }

    private void testRunning(int total, int count) throws Exception {
        List<String> accountList = new ArrayList<>();
        LOG.debug("##################################################");
        {
            LOG.debug("1.##########create " + count + " accounts##########");
            for (int i = 0; i < count / 100; i++) {
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, version);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("count", 100);
                params.put("password", password);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                assertTrue(response.isSuccess());
                accountList.addAll((List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list"));
                assertEquals(100 * (i + 1), accountList.size());
            }
        }
        List<String> hashList = new ArrayList<>();
        {
            LOG.debug("2.##########transfer from seed address to " + count + " accounts##########");
            for (int i = 0, accountListSize = accountList.size(); i < accountListSize; i++) {
                String account = accountList.get(i);
                Response response = transfer(sourceAddress, account, chainId, "50000000000");
                assertTrue(response.isSuccess());
                HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                String hash = result.get("value").toString();
                hashList.add(hash);
                Thread.sleep(1);
            }
        }
        Thread.sleep(60000);

        {
            boolean b = queryTxs(hashList);
            LOG.debug("all tx exist-{}" + b);
            assertTrue(b);
        }
        hashList.clear();
        {
            LOG.debug("3.##########" + count + " accounts Transfer to each other##########");
            //100个地址之间互相转账
            for (int j = 0; j < total / count; j++) {
                for (int i = 0; i < count; i++) {
                    String from = accountList.get(i % count);
                    String to = accountList.get((i + 1) % count);

                    Response response = transfer(from, to, chainId, "100000000");
                    assertTrue(response.isSuccess());
                    HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                    String hash = result.get("value").toString();
                    hashList.add(hash);
//                    LOG.debug("transfer from {} to {}, hash:{}", from, to, hash);
                }
                LOG.debug("##########" + j + " round end##########");
                Thread.sleep(1000);
            }
        }
        Thread.sleep(120000);
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
                LOG.debug("remain " + hashList.size() + " hash not verify");
                Thread.sleep(10000);
            }
            LOG.debug("all txs exist");
        }
    }


    /**
     * 稳定性测试
     */
    @Test
    public void stabilityTest() throws Exception {
        {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", sourceAddress);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountByAddress", params);

            String address = JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("ac_getAccountByAddress"));
            LOG.debug("address-{}" + address);
            BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(sourceAddress), chainId, assetId);
            LOG.debug(sourceAddress + "-----balance:{}", balance);
        }
        int total = 100_000_000;
        int count = 500;
        List<String> accountList = new ArrayList<>();
        LOG.debug("##################################################");
        {
            LOG.debug("1.##########create " + count + " accounts##########");
            int loop = count/100 == 0 ? 1 : count/100;
            for (int i = 0; i < loop; i++) {
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, version);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("count", count<100?count:100);
                params.put("password", password);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                assertTrue(response.isSuccess());
                accountList.addAll((List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list"));
            }
            assertEquals(count, accountList.size());
            for (String account : accountList) {
                LOG.debug("address-{}", account);
            }
        }
        {
            LOG.debug("2.##########transfer from seed address to " + count + " accounts##########");
            for (int i = 0, accountListSize = accountList.size(); i < accountListSize; i++) {
                String account = accountList.get(i);
                Response response = transfer(sourceAddress, account, assetChainId, "100000000000");
                assertTrue(response.isSuccess());
                HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                LOG.debug(i + "---transfer from {} to {}, hash:{}", sourceAddress, account, result.get("value"));
                Thread.sleep(100);
            }
        }
        Thread.sleep(60000);
        List<String> hashList = new ArrayList<>();
        int intervel = 100;
        {
            LOG.debug("3.##########" + count + " accounts Transfer to each other##########");
            //100个地址之间互相转账
            for (int j = 0; j < total/count; j++) {
                for (int i = 0; i < count; i++) {
                    String from = accountList.get(i % count);
                    String to = accountList.get((i + 1) % count);

                    Response response = transfer(from, to, chainId, "100000000");
                    assertTrue(response.isSuccess());
                    HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                    String hash = result.get("value").toString();
                    hashList.add(hash);
                    LOG.debug("transfer from {} to {}, hash:{}", from, to, hash);
                }
                LOG.debug("##########" + j + " round end##########");
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
                LOG.debug("remain " + hashList.size() + " hash not verify");
                Thread.sleep(10000);
            }
        }
    }

    private void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            params.put("priKey", priKey);
            params.put("password", pwd);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            assertTrue(cmdResp.isSuccess());
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
            LOG.debug("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map createAgentTx(String agentAddr, String packingAddr) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentAddress", agentAddr);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("deposit", 20000 * 100000000L);
        params.put("commissionRate", 10);
        params.put("packingAddress", packingAddr);
        params.put("password", password);
        params.put("rewardAddress", agentAddr);
        return params;
    }

    private void removeAccount(String address, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("password", password);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params);
        assertTrue(response.isSuccess());
        LOG.debug("{}", JSONUtils.obj2json(response.getResponseData()));
    }

    private boolean queryTxs(List<String> hashList) throws Exception {
        boolean result = true;
        for (String hash : hashList) {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, chainId);
            params.put("txHash", hash);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTx", params);
            assertTrue(response.isSuccess());
            Map map = (Map) response.getResponseData();
            Map tx = (Map) map.get("tx_getConfirmedTx");
            String txStr = tx.get("tx").toString();
            Transaction transaction = new Transaction();
            transaction.parse(new NulsByteBuffer(RPCUtil.decode(txStr)));
            if (!hash.equals(transaction.getHash().toHex())) {
                LOG.debug("hash-{} not exist", hash);
                result = false;
            }
        }
        return result;
    }

    private boolean queryTx(String hash, boolean confirmed) throws Exception {
        boolean result = true;
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
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
            LOG.debug("hash-{} not exist", hash);
            return false;
        }
        String txStr = tx1.toString();
        Transaction transaction = new Transaction();
        transaction.parse(new NulsByteBuffer(RPCUtil.decode(txStr)));
        if (!hash.equals(transaction.getHash().toHex())) {
            LOG.debug("hash-{} not exist", hash);
            result = false;
        }
        return result;
    }


    /**
     * 发交易从1发到5000
     */
    @Test
    public void blockSaveTest() throws Exception {
        importPriKey("477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75", password);//tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
        balance("tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD");
        int total = 100_000_000;
        int count = 10;
        List<String> accountList = new ArrayList<>();
        LOG.debug("##################################################");
        {
            LOG.debug("1.##########create " + count + " accounts##########");
            int loop = count/100 == 0 ? 1 : count/100;
            for (int i = 0; i < loop; i++) {
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, version);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("count", count<100?count:100);
                params.put("password", password);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                assertTrue(response.isSuccess());
                accountList.addAll((List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list"));
            }
            assertEquals(count, accountList.size());
        }
        {
            LOG.debug("2.##########transfer from seed address to " + count + " accounts##########");
            for (int i = 0, accountListSize = accountList.size(); i < accountListSize; i++) {
                String account = accountList.get(i);
                Response response = transfer(sourceAddress, account, assetChainId, "5000000000");
                assertTrue(response.isSuccess());
                HashMap result = (HashMap) (((HashMap) response.getResponseData()).get("ac_transfer"));
                LOG.debug(i + "---transfer from {} to {}, hash:{}", sourceAddress, account, result.get("value"));
            }
        }
        Thread.sleep(20000);
        {
            LOG.debug("3.##########" + count + " accounts Transfer to each other##########");
            //100个地址之间互相转账
            int num = 0;//发了多少个交易
            int limit = 1;
            for (int j = 0; j < total/count; j++) {
                for (int i = 0; i < count; i++) {
                    String from = accountList.get(i % count);
                    String to = accountList.get((i + 1) % count);
                    Response response = transfer(from, to, chainId, "100000000");
                    assertTrue(response.isSuccess());
                    num++;
                    if (num == limit) {
                        if (count == limit) {
                            limit = 1;
                        }
                        if (total == limit) {
                            return;
                        }
                        LOG.debug("send " + num + " tx");
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
        LOG.debug("##################################################");
        {
            LOG.debug("3.##########" + count + " accounts Transfer to each other##########");
            //100个地址之间互相转账
            int num = 0;//发了多少个交易
            int limit = 1;
            for (int j = 0; j < total/count; j++) {
                for (int i = 0; i < count; i++) {
                    String from = accountList.get(i % count);
                    String to = accountList.get((i + 1) % count);
                    Response response = transfer(from, to, chainId, "100000000");
                    assertTrue(response.isSuccess());
                    num++;
                    if (num == limit) {
                        if (count == limit) {
                            limit = 1;
                        }
                        if (total == limit) {
                            return;
                        }
                        LOG.debug("send " + num + " tx");
                        num = 0;
                        limit++;
                        Thread.sleep(10000);
                    }
                }
            }
        }
    }

    private Response transfer(String from, String to, int chainId, String s) throws Exception {
        Map transferMap = getTxMap(from, to, chainId, s);
        return ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
    }

    private Map getTxMap(String from, String to, int chainId, String s) {
        Map transferMap = new HashMap();
        transferMap.put("chainId", chainId);
        transferMap.put("remark", "transfer test");
        List<CoinDTO> inputs = new ArrayList<>();
        List<CoinDTO> outputs = new ArrayList<>();
        CoinDTO inputCoin1 = new CoinDTO();
        inputCoin1.setAddress(from);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger(s));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1 = new CoinDTO();
        outputCoin1.setAddress(to);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(new BigInteger(s));
        outputs.add(outputCoin1);
        transferMap.put("inputs", inputs);
        transferMap.put("outputs", outputs);
        return transferMap;
    }

    @Test
    public void importAccountByKeystorePath() {
        try {
            File path = new File("C:\\Users\\alvin\\Desktop\\alpha3\\keystore\\backup");
            for (File file : path.listFiles()) {
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, version);
                params.put(Constants.CHAIN_ID, chainId);
                params.put("keyStore", RPCUtil.encode(Files.readString(file.toPath()).getBytes()));
                params.put("password", password);
                params.put("overwrite", true);
                Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
                assertTrue(cmdResp.isSuccess());
            }
            getAccountList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
