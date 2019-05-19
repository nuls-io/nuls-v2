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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ljs on 2019/01/06.
 */
public class CmdTxTest {
    String address = "tNULSeBaMfi17CxRHVqFZbSFGYeyRLHWw2ctho";
    String addressTo = "tNULSeBaMmp4U2k653V5FmmPf4HDECWK2ExYVr";
    @Before
    public void before() throws Exception {
        NoUse.mockModule();
//        CmdDispatcher.syncKernel("ws://127.0.0.1:7771");
    }

    /**
     * 测试只有coinTo的交易
     * @throws Exception
     */
    @Test
    public void commitUnConfirmTx() throws Exception {
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
        tx.setBlockHeight(6L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        params.put("chainId", TestConfig.chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(RPCUtil.encode(tx.serialize()));
        params.put("txList",txHexList);
        params.put("isConfirmTx",false);
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        LoggerUtil.logger().info("response {}", response);

    }

    /**
     * 测试只有coinTo的交易
     * @throws Exception
     */
    @Test
    public void commitUnConfirmTx3() throws Exception {
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
        tx.setBlockHeight(6L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        params.put("chainId", TestConfig.chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(RPCUtil.encode(tx.serialize()));
        params.put("txList",txHexList);
        params.put("isConfirmTx",false);
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        LoggerUtil.logger().info("response {}", response);

    }

    /**
     * 测试只有coinFrom的交易
     * @throws Exception
     */
    @Test
    public void commitUnConfirmTx2() throws Exception {
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
        CoinTo coinTo = new CoinTo();
        coinFrom.setAddress(AddressTool.getAddress(address));
        coinFrom.setAmount(BigInteger.valueOf(29));
        coinFrom.setAssetsChainId(TestConfig.assetChainId);
        coinFrom.setAssetsId(TestConfig.assetId);
        coinFrom.setLocked((byte)0);
        coinFrom.setNonce(RPCUtil.decode(nonce));
        List<CoinFrom> coinFroms =new ArrayList<>();
        coinFroms.add(coinFrom);
        List<CoinTo> coinTos =new ArrayList<>();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(6L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        params.put("chainId", TestConfig.chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(RPCUtil.encode(tx.serialize()));
        params.put("txList",txHexList);
        params.put("isConfirmTx",false);
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        LoggerUtil.logger().info("response {}", response);
    }


    /**
     * 测试只有coinTo的交易
     * @throws Exception
     */
    @Test
    public void commitConfirmTx() throws Exception {
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
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        params.put("chainId", TestConfig.chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(RPCUtil.encode(tx.serialize()));
        params.put("txList",txHexList);
        params.put("isConfirmTx",true);
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        LoggerUtil.logger().info("response {}", response);
    }
    /**
     * 测试含有coinFrom与coinTo的交易
     * @throws Exception
     */
    //    String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";

    //入账金额
    BigInteger amount = BigInteger.valueOf(100000000000L);
    public Transaction buildTx2() throws Exception {
        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
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
        tx.setBlockHeight(0L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
       return tx;
    }
    @Test
    public void commitConfirmTx2() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", TestConfig.chainId);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        LoggerUtil.logger().info("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTx2();
        List<String> txHexList = new ArrayList<>();
        txHexList.add(RPCUtil.encode(transaction.serialize()));
        params.put("tx",RPCUtil.encode(transaction.serialize()));
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        LoggerUtil.logger().info("response {}", response);
        params.put("txList",txHexList);
        params.put("blockHeight",1);
        params.put("isConfirmTx",true);
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        LoggerUtil.logger().info("response {}", response);
    }
    /**
     * 测试只有coinTo的交易
     * @throws Exception
     */
    @Test
    public void commitConfirmTx3() throws Exception {
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
        List<String> txHexList = new ArrayList<>();
        txHexList.add(RPCUtil.encode(tx.serialize()));
        params.put("txList",txHexList);
        params.put("isConfirmTx",true);
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        LoggerUtil.logger().info("response {}", response);
    }

    @Test
    public void goBlockCommit(){
        Transaction tx = null;
        try {
            Map<String,Object> params = new HashMap<>();
            params.put("chainId", TestConfig.assetChainId);
            params.put("blockHeight",0);
            params.put("addressChainId", TestConfig.chainId);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "goBatchCommitTest", params);
            LoggerUtil.logger().info("response {}", response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testFreezeCommit(){

        try {
            for(int i=0;i<1000000;i++) {
                Map<String, Object> params = new HashMap<>();
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "batchAccountStatesTest", params);
                LoggerUtil.logger().info("response {}", response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testFreezeCommit2(){

        try {
            for(int i=0;i<1000000;i++) {
                Map<String, Object> params = new HashMap<>();
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "batchAccountStatesTest2", params);
                LoggerUtil.logger().info("response {}", response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
