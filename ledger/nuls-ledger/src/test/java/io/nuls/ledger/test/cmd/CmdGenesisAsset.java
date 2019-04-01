/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.ledger.test.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.model.ByteUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/01/14
 **/
public class CmdGenesisAsset {
    public int chainId = 2;
    int assetChainId = 2;
    //    String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
    String address = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    int assetId = 1;
    //入账金额
    BigInteger amount = BigInteger.valueOf(100000000000000L);

    Transaction buildTransaction() throws IOException {
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
//        coinFroms.add(coinFrom);
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        tx.setBlockHeight(0);
        tx.setTime(500000000000000L);
        return tx;
    }


    final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void before() throws Exception {
//        NoUse.mockModule();
    }


    @Test
    public void addGenesisAsset() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        logger.info("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTransaction();
        List<String> txHexList = new ArrayList<>();
        txHexList.add(HexUtil.encode(transaction.serialize()));
        params.put("txHex", HexUtil.encode(transaction.serialize()));
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        logger.info("response {}", response);
        params.put("txHexList", txHexList);
        params.put("blockHeight", 0);
        params.put("isConfirmTx", true);
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        logger.info("response {}", response);
    }

    @Test
    public void getBalanceNonce() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        params.put("chainId", 2);
        params.put("assetChainId", 2);
        params.put("assetId", 1);
        params.put("address", address);
//        params.put("address", "LLbmaw1UNmKmd5PfuzP1Zm9dNuAnia01f");

        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
        logger.info("response {}", response);
    }

    @Test
    public void testHex() throws IOException {
        TranList list = new TranList();
        for (int i = 0; i < 10000; i++) {
            Transaction tx = buildTransaction();
            list.getTxs().add(tx);
        }
        byte[] bytes = list.serialize();
        long time1 = System.currentTimeMillis();
        String hex0 = HexUtil.encode(bytes);
        long time2 = System.currentTimeMillis();
        System.out.println("HexUtil.byteToHex useTime=" + (time2 - time1) + "==StringLenght=" + hex0.length());
        String hex = HexUtil.encode(bytes);
        long time3 = System.currentTimeMillis();
        System.out.println("HexUtil.encode useTime=" + (time3 - time2) + "===StringLenght=" + hex.length());
        String s = ByteUtils.asString(bytes);
        long time4 = System.currentTimeMillis();
        System.out.println(" ByteUtils.asString useTime=" + (time4 - time3) + "===StringLenght" + s.length());

    }
    @Test
    public void testHex2() throws IOException {
         String t="dfss3234234";
        System.out.println(RPCUtil.encode(RPCUtil.decode(t)));


    }
}
