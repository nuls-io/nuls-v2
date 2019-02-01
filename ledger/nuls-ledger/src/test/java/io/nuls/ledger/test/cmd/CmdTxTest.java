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
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
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
 * Created by ljs on 2019/01/06.
 */
public class CmdTxTest {

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
//        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
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
        int chainId = 8096;
        int assetChainId = 445;
        String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
        int assetId = 222;
        params.put("assetChainId", 445);
        params.put("address", address);
        params.put("assetId", 222);
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(BigInteger.valueOf(100));
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
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
        params.put("chainId", chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(HexUtil.encode(tx.serialize()));
        params.put("txHexList",txHexList);
        params.put("isConfirmTx",false);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        logger.info("response {}", response);

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
        int chainId = 8096;
        int assetChainId = 445;
        String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
        int assetId = 222;
        params.put("assetChainId", 445);
        params.put("address", address);
        params.put("assetId", 222);
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(BigInteger.valueOf(100));
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
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
        params.put("chainId", chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(HexUtil.encode(tx.serialize()));
        params.put("txHexList",txHexList);
        params.put("isConfirmTx",false);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        logger.info("response {}", response);

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
        int chainId = 8096;
        int assetChainId = 445;
        String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
        int assetId = 222;
        params.put("assetChainId", 445);
        params.put("address", address);
        params.put("assetId", 222);
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinFrom.setAddress(AddressTool.getAddress(address));
        coinFrom.setAmount(BigInteger.valueOf(29));
        coinFrom.setAssetsChainId(assetChainId);
        coinFrom.setAssetsId(assetId);
        coinFrom.setLocked((byte)0);
        coinFrom.setNonce(HexUtil.decode(nonce));
        List<CoinFrom> coinFroms =new ArrayList<>();
        coinFroms.add(coinFrom);
        List<CoinTo> coinTos =new ArrayList<>();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(6L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        params.put("chainId", chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(HexUtil.encode(tx.serialize()));
        params.put("txHexList",txHexList);
        params.put("isConfirmTx",false);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        logger.info("response {}", response);
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
        int chainId = 8096;
        int assetChainId = 445;
        String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
        int assetId = 222;
        params.put("assetChainId", 445);
        params.put("address", address);
        params.put("assetId", 222);
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(BigInteger.valueOf(100));
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
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
        params.put("chainId", chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(HexUtil.encode(tx.serialize()));
        params.put("txHexList",txHexList);
        params.put("isConfirmTx",true);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        logger.info("response {}", response);
    }
    /**
     * 测试含有coinFrom与coinTo的交易
     * @throws Exception
     */
    @Test
    public void commitConfirmTx2() throws Exception {
        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        int chainId = 8096;
        int assetChainId = 445;
        String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
        String addressTo = "LLbmaw1UNmKmd5PfuzP1Zm9dNuAnia01f";
        int assetId = 222;
        params.put("assetChainId", 445);
        params.put("address", address);
        params.put("assetId", 222);
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(address));
        coinFrom.setNonce(HexUtil.decode(nonce));
        coinFrom.setAssetsId(assetId);
        coinFrom.setAssetsChainId(assetChainId);
        coinFrom.setAmount(BigInteger.valueOf(21));
        coinFrom.setLocked((byte)0);
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(addressTo));
        coinTo.setAmount(BigInteger.valueOf(20));
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
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
        params.put("chainId", chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(HexUtil.encode(tx.serialize()));
        params.put("txHexList",txHexList);
        params.put("isConfirmTx",true);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        logger.info("response {}", response);
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
        int chainId = 8096;
        int assetChainId = 445;
        String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
        int assetId = 222;
        params.put("assetChainId", 445);
        params.put("address", address);
        params.put("assetId", 222);
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
        String nonce =  ((Map)((Map)response.getResponseData()).get("getNonce")).get("nonce").toString();
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(BigInteger.valueOf(100));
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
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
        params.put("chainId", chainId);
        List<String> txHexList = new ArrayList<>();
        txHexList.add(HexUtil.encode(tx.serialize()));
        params.put("txHexList",txHexList);
        params.put("isConfirmTx",true);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        logger.info("response {}", response);
    }
}
