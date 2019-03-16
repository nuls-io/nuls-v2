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

package io.nuls.contract.tx;


import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractSendTxTest {

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

    private Chain chain;
    static int chainId = 12345;
    static int assetChainId = 12345;
    static int assetId = 1;
    static String version = "1.0";

    static String password = "nuls123456";//"nuls123456";

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, 100000000L));
    }

    @Test
    public void importPriKeyTest() {
        importPriKey("00def3b0f4bfad2a6abb5f6957829e752a1a30806edc35e98016425d578fdc4e77", password);//25 5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo
        importPriKey("1c2b9fd4417c1aad8ae9f24c982ff294eb50a6462b873b79a879e805a9990346", password);//26 5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu
        importPriKey("00c98ecfd3777745270cacb9afba17ef0284769a83ff2adb4106b8a0baaec9452c", password);//27 5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM
        importPriKey("23848d45b4b34aca8ff24b00949a25a2c9175faf283675128e189eee8b085942", password);//28 5MR_2CfUsasd33vQV3HqGw6M3JwVsuVxJ7r
        importPriKey("009560d5ed6587822b7aee6f318f50b312c281e4f330b6990396881c6d3f870bc1", password);//29 5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF

        importPriKey("00fffd585ed08dddf0d034236aa1ea85abd2e4e69981617ee477adf6cdcf50f4d5", password);//打包地址 5MR_2Ch8CCnLwoLWFZ45pFEZSmo1C1pkPFA
    }

    @Test
    public void createAgentTx() throws Exception {
        //组装创建节点交易
        Map agentTxMap = this.createAgentTx(address27, address26);
        //调用接口
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
        Assert.assertTrue(null != result);
        String hash =  (String)result.get("txHash");
        Log.debug("createAgent-txHash:{}", hash);
    }

    /**
     * 委托节点
     */
    @Test
    public void depositToAgent() throws Exception {
        //组装委托节点交易
        String agentHash = "00205989de73d03c4a0560d4caa436d387738bbd2baccf4ce3440d6cbfae1149e63d";
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put("chainId", chainId);
        dpParams.put("address", address27);
        dpParams.put("password", password);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", 200000 * 100000000L);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
        HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
        String txHash = (String) dpResult.get("txHash");
        Log.debug("deposit-txHash:{}", txHash);
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
        params.put("address", address27);
        params.put("password", password);
        params.put("txHash", "0020b8a42eb4c70196189e607e9434fe09b595d5753711f21819113f40d64a1c82c1");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        HashMap dpResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_withdraw");
        String hash = (String) dpResult.get("txHash");
        Log.debug("withdraw-txHash:{}", hash);
    }

    @Test
    public void stopAgentTx() throws Exception {
        Map<String, Object> txMap = new HashMap();
        txMap.put("chainId", chainId);
        txMap.put("address", address27);
        txMap.put("password", password);
        //调用接口
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_stopAgent"));
        String txHash = (String) result.get("txHash");
        Log.debug("stopAgent-txHash:{}", txHash);
    }

    /**
     * 查交易
     */
    @Test
    public void getTxRecord() throws Exception{
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", "5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF");
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
    private void getTxClient(String hash) throws Exception{
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
    private void getTxCfmClient(String hash) throws Exception{
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.debug("", JSONUtils.obj2PrettyJson(record));
    }

    /**
     *  删除账户
     */
    @Test
    public void removeAccountTest() throws Exception {
        removeAccount("5MR_2Cb86fpFbuY4Lici8MJStNxDFYH6kRB", password);
        removeAccount(address26, password);
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

}
