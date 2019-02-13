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
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.model.dto.CrossTxTransferDTO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.service.TxService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertNotNull;

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class TestTx {

    static int chainId = 12345;
    static int assetChainId = 12345;
    static String address1 = "QXpkrbKqShZfopyck5jBQSFgbP9cD3930";
    static String address2 = "KS3wfAPFAmY8EwMFz21EXhJMXf8DV3930";
    static String address3 = "Vxb3xxatcFFTZZe3wynX6CfAsvzAx3930";
    static String address4 = "R9CxmNqtBDEm9iWX2Cod46QGCNE2M3930";
    static String address5 = "LFkghywKjdE2G3SZUcTsMkzcJ7tda3930";
    static String address6 = "QMwz71wTKgp9sZ8g44A9WNgXk11u23930";
    static String address7 = "WEXAmsUJSNAvCx2zUaXziy3ZYX1em3930";
    static String address8 = "TewWmmtBxuxN14FRazZXdtD9XuH313930";
    static String address9 = "WodfCXTbJ22mPa35Y61yNTRh1x3zB3930";
    static String address10 = "SPWAxuodkw222367N88eavYDWRraG3930";
    static String address11 = "Rnt57eZnH8Dd7K3LudJXmmEutYJZD3930";
    static String address12 = "XroY3cLWTfgKMRRRLCP5rhvo1gHY63930";


    static String address20 = "H3eriRPPdbSMxXfg5MFYVfGmypNma3930";
    static String address21 = "H9jzu275LW7qUPo4boZoN611Hc2DE3930";
    static String address22 = "Hev98WnFwR55FJffop8H2J24VJe5y3930";
    static String address23 = "HgmTfwiFhTLNuz2sRLgz3BrXcyY9F3930";
    static String address24 = "JHwrmyKbu4KmSxy27HctqSG8aQqdY3930";
    static String address25 = "JtM2x9hyUPfUQCfNnZZb4XG1eciS13930";
    static String address26 = "JyBjVrGPbpr4smwbwUzDokQz2F7Gw3930";
    static String address27 = "K8vyxqeu6dyfR35XcdqNZK4fW9h2N3930";
    static String address28 = "KKQmeMGKfkkmQF5onWBY487zHdB7Q3930";
    static String address29 = "KMNPqwARu77qAL4UCkd5Vwvj5PAtw3930";

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
        CmdDispatcher.syncKernel();
        chain = new Chain();
        chain.setConfig(new ConfigBean(12345, 1));
//        初始化token
//        addGenesisAsset();
    }

    @Test
    public void test() throws Exception {
        addGenesisAsset(address1);
        addGenesisAsset(address2);
        addGenesisAsset(address3);
        addGenesisAsset(address4);
        addGenesisAsset(address5);
        addGenesisAsset(address6);
        addGenesisAsset(address7);
        addGenesisAsset(address8);
        addGenesisAsset(address9);
        addGenesisAsset(address10);
        addGenesisAsset(address11);
        addGenesisAsset(address12);

        addGenesisAsset(address20);
        addGenesisAsset(address21);
        addGenesisAsset(address22);
        addGenesisAsset(address23);
        addGenesisAsset(address24);
        addGenesisAsset(address25);
        addGenesisAsset(address26);
        addGenesisAsset(address27);
        addGenesisAsset(address28);
        addGenesisAsset(address29);
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(address4), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance));

        BigInteger balance2 = LedgerCall.getBalance(chain, AddressTool.getAddress(address6), assetChainId, assetId);
        System.out.println(JSONUtils.obj2PrettyJson(balance2));
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


    /**
     * 转账
     * @throws Exception
     */
    @Test
    public void newCtx() throws Exception {
        BigInteger balance1 = LedgerCall.getBalance(chain, AddressTool.getAddress(address1), assetChainId, assetId);
        BigInteger balance2 = LedgerCall.getBalance(chain, AddressTool.getAddress(address2), assetChainId, assetId);
//            BigInteger balance3 = LedgerCall.getBalance(chain, AddressTool.getAddress(address3), assetChainId, assetId);
//            BigInteger balance4 = LedgerCall.getBalance(chain, AddressTool.getAddress(address4), assetChainId, assetId);
        System.out.println("address1: " + balance1.longValue());
        System.out.println("address2: " + balance2.longValue());
//            System.out.println("address3: " + balance3.longValue());
//            System.out.println("address4: " + balance4.longValue());
        CrossTxTransferDTO ctxTransfer = new CrossTxTransferDTO(chain.getChainId(),
                createFromCoinDTOList(), createToCoinDTOList(), "this is cross-chain transaction");
        //调接口
        String json = JSONUtils.obj2json(ctxTransfer);
        Map<String, Object> params = JSONUtils.json2map(json);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_createCtx", params);
        Assert.assertTrue(null != response.getResponseData());
        Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_createCtx");
        Assert.assertTrue(null != map);
        Log.info("{}", map.get("value"));

        Thread.sleep(3000L);

      /*  Map transferMap = this.createTransferTx();
        //调用接口
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("value"));
        Thread.sleep(4000L);
        //packableTxs();*/

    }

    @Test
    public void createAgentTx() throws Exception {
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(address7), assetChainId, assetId);
        System.out.println(balance.longValue());

        //组装创建节点交易
        Map agentTxMap = this.createAgentTx(address9, address1);
        //调用接口
        Response cmdResp2 = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("txHex"));
        System.out.println("transfer: " + result.get("txHex"));  //Thread.sleep(3000L);
    }

    @Test
    public void stopAgentTx() throws Exception {

        //组装创建节点交易
        //Map agentTxMap=this.createAgentTx(address9, address1);
        Map<String, Object> txMap = new HashMap();
        txMap.put("chainId", chainId);
        txMap.put("address", address9);
        txMap.put("password", "");
        //调用接口
        Response cmdResp2 = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_stopAgent"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("txHex"));
        System.out.println("transfer: " + result.get("txHex"));  //Thread.sleep(3000L);
    }


    /**
     * 委托节点交易创建
     */
    @Test
    public void depositToAgent() throws Exception {
        //组装委托节点交易
        String agentHash = "0020cd550e025eae244dc62b101379c7694e6c7e25a06fd607c29efbd7499f82bb62";
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put("chainId", chainId);
        dpParams.put("address", address10);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", 20000 * 100000000L);
        Response dpResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
        HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
        String dpTxHex = (String) dpResult.get("txHex");
    }

    /**
     * 退出共识
     *
     * @throws Exception
     */
    @Test
    public void withdraw() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        //Address depositAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        params.put("address", address10);
        params.put("txHash", "00208bb9f6ec49c0013c7d6a868733510ce29b0433566634ec6c4e8d02f9af3d63ce");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        System.out.println(cmdResp.getResponseData());
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
        params.put("address", address1);
        params.put("password", password);
        params.put("alias", alias);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setAlias", params);
        System.out.println("ac_setAlias result:" + JSONUtils.obj2json(cmdResp));
        assertNotNull(cmdResp);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setAlias");
        assertNotNull(result);
        String txHash = (String) result.get("txHash");
        assertNotNull(txHash);
        Log.info("alias-txHash{}", txHash);
    }


    //    @Test
    public void packableTxs() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        long endTime = System.currentTimeMillis() + 10000L;
        System.out.println("endTime: " + endTime);
        params.put("endTimestamp", endTime);
        params.put("maxTxDataSize", 2 * 1024 * 1024L);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_packableTxs", params);
        Assert.assertTrue(null != response.getResponseData());
        Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_packableTxs");
        Assert.assertTrue(null != map);
        List<String> list = (List) map.get("list");
        Log.info("packableTxs:");
        for (String s : list) {
            Log.info(s);
        }
    }

    private List<CoinDTO> createFromCoinDTOList() {
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(assetChainId);
        coinDTO.setAddress(address1);
        coinDTO.setAmount(new BigInteger("200000000"));
        coinDTO.setPassword("nuls123456");

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(assetChainId);
        coinDTO2.setAddress(address2);
        coinDTO2.setAmount(new BigInteger("100000000"));
        coinDTO2.setPassword("nuls123456");
        List<CoinDTO> listFrom = new ArrayList<>();
        listFrom.add(coinDTO);
        listFrom.add(coinDTO2);
        return listFrom;
    }

    private List<CoinDTO> createToCoinDTOList() {
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(8964);
        coinDTO.setAddress("VatuPuZeEc1YJ21iasZH6SMAD2VNL0423");
        coinDTO.setAmount(new BigInteger("200000000"));

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(8964);
        coinDTO2.setAddress("K7gb72AMXhymt8wBH3fwBUqSwf4EX0423");
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

    /**
     * 铸币
     *
     * @throws Exception
     */
    public static void addGenesisAsset(String address) throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        Log.info("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTransaction(address);

        params.put("txHex", HexUtil.encode(transaction.serialize()));
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        Log.info("response {}", response);

        params.put("isConfirmTx", true);
        List<String> list = new ArrayList<>();
        list.add(HexUtil.encode(transaction.serialize()));
        params.put("txHexList", list);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        Log.info("response {}", response);
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
        inputCoin1.setAddress(address1);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("10000000"));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1 = new CoinDTO();
        outputCoin1.setAddress(address2);
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
        params.put("agentAddress", agentAddr);
        params.put("chainId", chainId);
        params.put("deposit", 20000 * 100000000L);
        params.put("commissionRate", 10);
        params.put("packingAddress", packingAddr);
        params.put("password", "");
        params.put("rewardAddress", agentAddr);
        return params;
//        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
//        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void importPriKeyTest() {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);

            params.put("priKey", "00d1e4d568489995f4b45789f372a1c23823241ccb6a6709d164658384fdaaa822");
//            params.put("priKey", "008e7861842b2ca881326f40482e905f0119a608d90f7aa60ac80f06bf0c196d09");
//            params.put("priKey", "00e97ec0c01607f58bc692c604739c63ebf5aed2e9d01b9f427be4aa1cf53b4dea");
//            params.put("priKey", "00ae4df86385da0cce8b72fb463fc07d8767af36ad2cc78baf624b8815234bcc71");
//            params.put("priKey", "7b06ab2a94bf81eeea2606b6f87406ddb0b45f0f2a4d9a676c56ca81b3706c36");
//            params.put("priKey", "00fed14ccaa20c41107b7a00b279960235d981e698a717234c63f6fa25cbc5abcb");
//            params.put("priKey", "00c97dab006f1d973ad9d7f38a79125c24a1ebcdb3a687a3b4f84ddda97548e116");
//            params.put("priKey", "00adee90f664e1575c371f38a801449ac5324ba406e9931fb0cf51babc0d23e09b");
//            params.put("priKey", "008878a0099b7393b59a6bd728d97abbe6b593af1603dc3043550bcc8fc61ffbe6");
//            params.put("priKey", "211eb443e477cff92610b9eb733d71de61a75fe56f7253be91b715f2004eb409");
//            params.put("priKey", "6dec834b2556df23bb0fbc3607720f462be7ef8d28a34caf3847d91b8c9c6f93");
//            params.put("priKey", "00af59aa43536f6162a7166cdc1a389b32be0a06bc06f71a601a92e08fd2788dfe");
            params.put("password", "");
            params.put("overwrite", true);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
//            assertEquals(accountList.get(0), address);
            //账户已存在，不覆盖，返回错误提示  If the account exists, it will not be covered,return error message.
            params.put("overwrite", false);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
