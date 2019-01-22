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
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
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

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class TxFlowTest {

    static int chainId = 12345;
    static int assetChainId = 12345;
    static String address1 = "QXpkrbKqShZfopyck5jBQSFgbP9cD3930";
    static String address2 = "KS3wfAPFAmY8EwMFz21EXhJMXf8DV3930";
    static String address3 = "Vxb3xxatcFFTZZe3wynX6CfAsvzAx3930";
    static String address4 = "R9CxmNqtBDEm9iWX2Cod46QGCNE2M3930";
    static int assetId = 1;
    //入账金额
    static BigInteger amount = BigInteger.valueOf(100000000000L);
    static String password="nuls123456";

    private Chain chain;
    private Transaction tx;
    private CoinData coinData;

    @Before
    public void before() throws Exception{
        NoUse.mockModule();
        chain = new Chain();
        chain.setConfig(new ConfigBean(12345, 1));
//        初始化token
        addGenesisAsset();
    }


    @Test
    public void newCtx() throws Exception{
        for(int i = 0; i<5; i++) {
            BigInteger balance1 = LedgerCall.getBalance(chain, AddressTool.getAddress(address1), assetChainId, assetId);
            BigInteger balance2 = LedgerCall.getBalance(chain, AddressTool.getAddress(address2), assetChainId, assetId);
            BigInteger balance3 = LedgerCall.getBalance(chain, AddressTool.getAddress(address3), assetChainId, assetId);
            BigInteger balance4 = LedgerCall.getBalance(chain, AddressTool.getAddress(address4), assetChainId, assetId);
            System.out.println("address1: " + balance1.longValue());
            System.out.println("address2: " + balance2.longValue());
            System.out.println("address3: " + balance3.longValue());
            System.out.println("address4: " + balance4.longValue());
            CrossTxTransferDTO ctxTransfer = new CrossTxTransferDTO(chain.getChainId(),
                    createFromCoinDTOList(), createToCoinDTOList(), "this is cross-chain transaction");
            //普通转账
            //调接口
            String json = JSONUtils.obj2json(ctxTransfer);
            Map<String, Object> params = JSONUtils.json2map(json);
            Response response = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_createCtx", params);
            Assert.assertTrue(null != response.getResponseData());
            Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_createCtx");
            Assert.assertTrue(null != map);
            Log.info("{}", map.get("value"));

            Map transferMap = this.createTransferTx();
            //调用接口
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
            HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
            Assert.assertTrue(null != result);
            Log.info("{}", result.get("value"));
            Thread.sleep(3000L);
        }
        //packableTxs();


    }

//    @Test
    public void packableTxs() throws Exception{
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        long endTime = System.currentTimeMillis() + 10000L;
        System.out.println("endTime: " + endTime);
        params.put("endTimestamp", endTime);
        params.put("maxTxDataSize",2 * 1024 * 1024L);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_packableTxs", params);
        Assert.assertTrue(null != response.getResponseData());
        Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_packableTxs");
        Assert.assertTrue(null != map);
        List<String> list = (List)map.get("list");
        Log.info("packableTxs:");
        for(String s : list){
            Log.info(s);
        }
    }

    private List<CoinDTO> createFromCoinDTOList(){
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
        List< CoinDTO > listFrom = new ArrayList<>();
        listFrom.add(coinDTO);
        listFrom.add(coinDTO2);
        return listFrom;
    }

    private List<CoinDTO> createToCoinDTOList(){
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
        List< CoinDTO > listTO = new ArrayList<>();
        listTO.add(coinDTO);
        listTO.add(coinDTO2);
        return listTO;
    }

    /**
     * 铸币交易
     * @return
     * @throws IOException
     */
    private static Transaction buildTransaction() throws IOException {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address3));
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
     * @throws Exception
     */
    public static void addGenesisAsset() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        Log.info("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTransaction();
        params.put("txHex", HexUtil.encode(transaction.serialize()));
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        Log.info("response {}", response);

        params.put("isConfirmTx",true);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        Log.info("response {}", response);
    }

    //连续交易测试
    @Test
    public void contineCtx() throws Exception{
        String address="LU6eNP3pJ5UMn5yn8LeDE3Pxeapsq3930";
        for(int i = 0; i<3; i++) {
            BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(address), assetChainId, assetId);
            System.out.println(balance.longValue());
            //组装普通转账交易
            Map transferMap = this.createTransferTx();
            //调用接口
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
            HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
            Assert.assertTrue(null != result);
            Log.info("{}", result.get("value"));
            System.out.println("transfer: "+result.get("value"));

            //组装创建节点交易
            Map agentTxMap=this.createAgentTx();
            //调用接口
//            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
//            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("cs_createAgent"));
//            Assert.assertTrue(null != result);
//            Log.info("{}", result.get("txHex"));
//            System.out.println("transfer: "+result.get("txHex"));

            //Thread.sleep(3000L);
        }
        //packableTxs();
    }

    /**
     * 创建普通转账交易
     * @return
     */
    private Map createTransferTx()
    {
        Map transferMap = new HashMap();
        transferMap.put("chainId",chainId);
        transferMap.put("remark","transfer test");
        List<CoinDTO> inputs=new ArrayList<>();
        List<CoinDTO> outputs=new ArrayList<>();
        CoinDTO inputCoin1=new CoinDTO();
        inputCoin1.setAddress(address1);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("10000000"));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1=new CoinDTO();
        outputCoin1.setAddress(address2);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(new BigInteger("10000000"));
        outputs.add(outputCoin1);

        transferMap.put("inputs",inputs);
        transferMap.put("outputs",outputs);
        return transferMap;
    }

    /**
     * 创建节点
     * */
    public Map createAgentTx()throws Exception{
        Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        Address rewardAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmgd".getBytes()));
        Address packingAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fegfgqmd".getBytes()));
        Map<String,Object> params = new HashMap<>();
        params.put("agentAddress",agentAddress.getBase58());
        params.put("chainId",1);
        params.put("deposit",20000);
        params.put("commissionRate",10);
        params.put("packingAddress",packingAddress.getBase58());
        params.put("password","");
        params.put("rewardAddress",rewardAddress.getBase58());
        return params;
//        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
//        System.out.println(cmdResp.getResponseData());
    }

}
