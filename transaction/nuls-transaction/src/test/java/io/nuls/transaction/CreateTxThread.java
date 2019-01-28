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
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.model.dto.CrossTxTransferDTO;
import io.nuls.transaction.rpc.call.LedgerCall;
import org.junit.Assert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2019-01-25
 */
public class CreateTxThread implements Runnable {
    static int chainId = 12345;
    static int assetChainId = 12345;
    static int assetId = 1;
    static String address1 = "QXpkrbKqShZfopyck5jBQSFgbP9cD3930";
    static String address2 = "KS3wfAPFAmY8EwMFz21EXhJMXf8DV3930";
    static String address3 = "LFkghywKjdE2G3SZUcTsMkzcJ7tda3930";
    static String address4 = "QMwz71wTKgp9sZ8g44A9WNgXk11u23930";
    static String password="nuls123456";

    private Chain chain;

    public CreateTxThread(){
        try {
            chain = new Chain();
            chain.setConfig(new ConfigBean(assetChainId, assetId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            creatTx();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void creatTx() throws Exception{
        for(int i = 0; i<999999; i++) {
            BigInteger balance1 = LedgerCall.getBalance(chain, AddressTool.getAddress(address1), assetChainId, assetId);
            BigInteger balance2 = LedgerCall.getBalance(chain, AddressTool.getAddress(address2), assetChainId, assetId);
            System.out.println("======= Thread : " + Thread.currentThread().getName() + "=======");
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

//            Thread.sleep(3000L);

//            Map transferMap1 = this.createTransferTx(address1, address2, password);
//            //调用接口
//            Response cmdResp1 = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap1);
//            HashMap result1 = (HashMap) (((HashMap) cmdResp1.getResponseData()).get("ac_transfer"));
//            Assert.assertTrue(null != result1);
//            Log.info("{}", result1.get("value"));
//
//            Map transferMap2 = this.createTransferTx(address2, address3, password);
//            //调用接口
//            Response cmdResp2 = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap2);
//            HashMap result2 = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("ac_transfer"));
//            Assert.assertTrue(null != result2);
//            Log.info("{}", result2.get("value"));
//
//            Map transferMap3 = this.createTransferTx(address3, address4, "");
//            //调用接口
//            Response cmdResp3 = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap3);
//            HashMap result3 = (HashMap) (((HashMap) cmdResp3.getResponseData()).get("ac_transfer"));
//            Assert.assertTrue(null != result3);
//            Log.info("{}", result3.get("value"));

            Map transferMap4 = this.createTransferTx(address4, address1, "");
            //调用接口
            Response cmdResp4 = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap4);
            HashMap result4 = (HashMap) (((HashMap) cmdResp4.getResponseData()).get("ac_transfer"));
            Assert.assertTrue(null != result4);
            Log.info("{}", result4.get("value"));
            Thread.sleep(4000L);
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
     * 创建普通转账交易
     * @return
     */
    private Map createTransferTx(String fromAddress, String toAddress, String pwd)
    {
        Map transferMap = new HashMap();
        transferMap.put("chainId",chainId);
        transferMap.put("remark","transfer test");
        List<CoinDTO> inputs=new ArrayList<>();
        List<CoinDTO> outputs=new ArrayList<>();
        CoinDTO inputCoin1=new CoinDTO();
        inputCoin1.setAddress(fromAddress);
        inputCoin1.setPassword(pwd);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("10000000"));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1=new CoinDTO();
        outputCoin1.setAddress(toAddress);
        outputCoin1.setPassword(pwd);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(new BigInteger("10000000"));
        outputs.add(outputCoin1);

        transferMap.put("inputs",inputs);
        transferMap.put("outputs",outputs);
        return transferMap;
    }
}
