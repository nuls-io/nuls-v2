/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.test.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.crypto.HexUtil;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * 捣乱数据测试
 * Created by ljs on 2019/01/10.
 */
public class CmdWrongDataTest {
    @Before
    public void before() throws Exception {
        NoUse.mockModule();
    }

    /**
     * 校验孤儿交易，校验nonce双花
     * @throws Exception
     */
    @Test
    public void validateCoinDataWrong() throws Exception {
        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        int chainId = 8096;
        int assetChainId = 445;
        String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
        int assetId = 222;
//        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
//        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinFrom.setAddress(AddressTool.getAddress(address));
        coinFrom.setAmount(BigInteger.valueOf(50));
        coinFrom.setAssetsChainId(assetChainId);
        coinFrom.setAssetsId(assetId);
        coinFrom.setNonce(HexUtil.decode("AAAAAAAA"));
        coinFrom.setLocked((byte)0);
        List<CoinFrom> coinFroms =new ArrayList<>();
//        coinFroms.add(coinFrom);
        List<CoinTo> coinTos =new ArrayList<>();
        coinFroms.add(coinFrom);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setCoinData(coinData.serialize());
        params.put("chainId", chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(HexUtil.encode(tx.serialize()));
        params.put("txHexList",txHexList);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        logger.info("response {}", response);
    }


    /**
     * 测试批量校验
     * @throws Exception
     */
    @Test
    public void validateCoinDataWrong2() throws Exception {
        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        int chainId = 8096;
        int assetChainId = 445;
        String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
        int assetId = 222;
//        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
//        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinFrom.setAddress(AddressTool.getAddress(address));
        coinFrom.setAmount(BigInteger.valueOf(50));
        coinFrom.setAssetsChainId(assetChainId);
        coinFrom.setAssetsId(assetId);
        coinFrom.setNonce(HexUtil.decode("AAAAAAAA"));
        coinFrom.setLocked((byte)0);
        List<CoinFrom> coinFroms =new ArrayList<>();
//        coinFroms.add(coinFrom);
        List<CoinTo> coinTos =new ArrayList<>();
        coinFroms.add(coinFrom);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setCoinData(coinData.serialize());
        params.put("chainId", chainId);
        params.put("txHex",HexUtil.encode(tx.serialize()));
        params.put("isBatchValidate",true);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        logger.info("response {}", response);
    }
    @Test
   public void test2(){
       System.out.println(String.format("address %s,nonce %s","dfsfs","3333"));
   }
}
