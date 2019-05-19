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
import io.nuls.base.data.*;
import io.nuls.ledger.test.constant.TestConfig;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ljs on 2019/01/06.
 */
public class CmdValidateTest {
    @Before
    public void before() throws Exception {
        NoUse.mockModule();
//        CmdDispatcher.syncKernel("ws://127.0.0.1:7771");
    }
    String address = "tNULSeBaMfi17CxRHVqFZbSFGYeyRLHWw2ctho";
    String addressTo = "tNULSeBaMmp4U2k653V5FmmPf4HDECWK2ExYVr";
    @Test
    public void validateCoinData() throws Exception {
        double version = 1.0;

        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)

//        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
//        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(BigInteger.valueOf(100));
        coinTo.setAssetsChainId(TestConfig.assetChainId);
        coinTo.setAssetsId(TestConfig.assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms =new ArrayList<>();
//        coinFroms.add(coinFrom);
        List<CoinTo> coinTos =new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setCoinData(coinData.serialize());
        params.put("chainId", TestConfig.chainId);
        params.put("tx",RPCUtil.encode(tx.serialize()));
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        LoggerUtil.logger().info("response {}", response);
    }


    @Test
    public void bathValidateBegin() throws Exception {
        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)

//        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
//        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        params.put("chainId", TestConfig.chainId);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        LoggerUtil.logger().info("response {}", response);
    }

    @Test
    public void bathValidatePerTx() throws Exception {
        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
//        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
//        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(BigInteger.valueOf(100));
        coinTo.setAssetsChainId(TestConfig.assetChainId);
        coinTo.setAssetsId(TestConfig.assetId);
        coinTo.setLockTime(-1);
        List<CoinFrom> coinFroms =new ArrayList<>();
//        coinFroms.add(coinFrom);
        List<CoinTo> coinTos =new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        params.put("chainId", TestConfig.chainId);
        params.put("tx",RPCUtil.encode(tx.serialize()));
        params.put("isBatchValidate",true);
        Response  response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        LoggerUtil.logger().info("response {}", response);
    }

    @Test
    public void bathValidatePerTx2() throws Exception {
        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)

        params.put("assetChainId", TestConfig.assetChainId);
        params.put("address", address);
        params.put("assetId", TestConfig.assetId);
        params.put("chainId", TestConfig.chainId);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(address));
        coinFrom.setNonce(RPCUtil.decode(nonce));
        coinFrom.setAssetsId(TestConfig.assetId);
        coinFrom.setAssetsChainId(TestConfig.assetChainId);
        coinFrom.setAmount(BigInteger.valueOf(21));
        coinFrom.setLocked((byte)0);
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(addressTo));
        coinTo.setAmount(BigInteger.valueOf(20));
        coinTo.setAssetsChainId(TestConfig.assetChainId);
        coinTo.setAssetsId(TestConfig.assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms =new ArrayList<>();
        coinFroms.add(coinFrom);
        List<CoinTo> coinTos =new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(2L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        params.put("chainId", TestConfig.chainId);
        params.put("tx",RPCUtil.encode(tx.serialize()));
        params.put("isBatchValidate",true);
          response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        LoggerUtil.logger().info("response {}", response);
    }
}
