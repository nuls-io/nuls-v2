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

import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import static io.nuls.transaction.utils.LoggerUtil.Log;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
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
//    static String address1 = "QXpkrbKqShZfopyck5jBQSFgbP9cD3930";
//    static String address2 = "KS3wfAPFAmY8EwMFz21EXhJMXf8DV3930";
//    static String address3 = "LFkghywKjdE2G3SZUcTsMkzcJ7tda3930";
//    static String address4 = "R9CxmNqtBDEm9iWX2Cod46QGCNE2M3930";
//
//    static String password = "nuls123456";
//    static String address6 = "QMwz71wTKgp9sZ8g44A9WNgXk11u23930";
//    static String address5 = "LFkghywKjdE2G3SZUcTsMkzcJ7tda3930";
//    static String address7 = "WEXAmsUJSNAvCx2zUaXziy3ZYX1em3930";
//    static String address9 = "WodfCXTbJ22mPa35Y61yNTRh1x3zB3930";
//    //static String address10 = "SPWAxuodkw222367N88eavYDWRraG3930";
//    static String address11 = "Rnt57eZnH8Dd7K3LudJXmmEutYJZD3930";
//    static String address12 = "XroY3cLWTfgKMRRRLCP5rhvo1gHY63930";

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
//            System.out.println("======= Thread : " + Thread.currentThread().getName() + "=======");
//            CrossTxTransferDTO ctxTransfer = new CrossTxTransferDTO(chain.getChainId(),
//                    createFromCoinDTOList(), createToCoinDTOList(), "this is cross-chain transaction");
//            //调接口
//            String json = JSONUtils.obj2json(ctxTransfer);
//            Map<String, Object> params = JSONUtils.json2map(json);
//            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_createCtx", params);
//            Assert.assertTrue(null != response.getResponseData());
//            Map map = (HashMap) ((HashMap) response.getResponseData()).get("tx_createCtx");
//            Assert.assertTrue(null != map);
//            Log.info("{}", map.get("value"));

//            createTransfer();
            createTransfer();

            Thread.sleep(3500L);
        }
    }
    private void createTransfer() throws Exception {
        Map transferMap = this.createTransferTx(address24, "5MR_4bgJiPmxN4mZV2C89thSEdJ8qWnm9Xi", null);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("value"));
    }

    private void createNewAddressTransfer() throws Exception {
        String addrFrom = createAccount();
        TestTx.addGenesisAsset(addrFrom);
        String AddrTo = address20;//createAccount();
        Map transferMap = this.createTransferTx(addrFrom, AddrTo, null);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("value"));
    }

    public static String createAccount() {
        List<String> accountList = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("version", "1.0");
            params.put("chainId", chainId);
            params.put("count", 1);
            params.put("password", "");
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);

            accountList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
            return accountList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<CoinDTO> createFromCoinDTOList() {
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(assetChainId);
        coinDTO.setAddress(address20);
        coinDTO.setAmount(new BigInteger("200000000"));
        coinDTO.setPassword("nuls123456");

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(assetChainId);
        coinDTO2.setAddress(address21);
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
        coinDTO.setAddress(address23);
        coinDTO.setAmount(new BigInteger("200000000"));

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(8964);
        coinDTO2.setAddress(address24);
        coinDTO2.setAmount(new BigInteger("100000000"));
        List<CoinDTO> listTO = new ArrayList<>();
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
