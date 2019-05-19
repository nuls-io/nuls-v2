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
import io.nuls.ledger.test.constant.TestConfig;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.parse.JSONUtils;
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
    //    String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
    String address = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    //入账金额
    BigInteger amount = BigInteger.valueOf(100000000000000L);

    Transaction buildTransaction() throws IOException {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(amount);
        coinTo.setAssetsChainId(TestConfig.assetChainId);
        coinTo.setAssetsId(TestConfig.assetId);
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
        NoUse.mockModule();
    }


    @Test
    public void addGenesisAsset() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", TestConfig.chainId);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        logger.info("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTransaction();
        List<String> txList = new ArrayList<>();
        txList.add(RPCUtil.encode(transaction.serialize()));
        params.put("tx", RPCUtil.encode(transaction.serialize()));
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        logger.info("response {}", response);
        params.put("txList", txList);
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
        params.put("chainId", TestConfig.chainId);
        params.put("assetChainId", TestConfig.assetChainId);
        params.put("assetId", TestConfig.assetId);
//        params.put("address", address);
        params.put("address", "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm");

        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalance", params);
        logger.info("response {}", JSONUtils.obj2json(response));
    }
    @Test
    public void getBalanceNonce2() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        params.put("chainId", 100);
        params.put("assetChainId", 100);
        params.put("assetId", TestConfig.assetId);
//        params.put("address", address);
        params.put("address", "8CPcA7kaXSHbWb3GHP7bd5hRLFu8RZv57rY9w");

        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalance", params);
        logger.info("response {}", JSONUtils.obj2json(response));
    }
}
