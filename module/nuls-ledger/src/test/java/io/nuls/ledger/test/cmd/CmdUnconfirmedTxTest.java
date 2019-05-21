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
import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.ledger.test.constant.TestConfig;
import io.nuls.ledger.utils.LedgerUtil;
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
public class CmdUnconfirmedTxTest {

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
//        CmdDispatcher.syncKernel("ws://127.0.0.1:7771");
    }
    //    String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
    //入账金额
    BigInteger amount = BigInteger.valueOf(100000000000000L);
    String address = "tNULSeBaMfi17CxRHVqFZbSFGYeyRLHWw2ctho";
    String addressTo = "tNULSeBaMmp4U2k653V5FmmPf4HDECWK2ExYVr";
    Transaction buildTransaction(String fromAddr, String toAddr, BigInteger tranAmount) throws Exception {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(fromAddr));
        coinFrom.setAmount(tranAmount);
        coinFrom.setAssetsChainId(TestConfig.assetChainId);
        coinFrom.setAssetsId(TestConfig.assetId);
        coinFrom.setNonce(RPCUtil.decode(getNonce(fromAddr)));
        coinFrom.setLocked((byte) 0);

        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(toAddr));
        coinTo.setAmount(tranAmount);
        coinTo.setAssetsChainId(TestConfig.assetChainId);
        coinTo.setAssetsId(TestConfig.assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms = new ArrayList<>();
        coinFroms.add(coinFrom);
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        tx.setBlockHeight(1);
        tx.setTime(System.currentTimeMillis()/1000);
        return tx;
    }
    Transaction buildLockedTimeTransaction(String fromAddr, String toAddr, BigInteger tranAmount) throws Exception {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(fromAddr));
        coinFrom.setAmount(tranAmount);
        coinFrom.setAssetsChainId(TestConfig.assetChainId);
        coinFrom.setAssetsId(TestConfig.assetId);
        coinFrom.setNonce(RPCUtil.decode(getNonce(fromAddr)));
        coinFrom.setLocked((byte) 0);

        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(toAddr));
        coinTo.setAmount(tranAmount);
        coinTo.setAssetsChainId(TestConfig.assetChainId);
        coinTo.setAssetsId(TestConfig.assetId);
        coinTo.setLockTime(-1);
        List<CoinFrom> coinFroms = new ArrayList<>();
        coinFroms.add(coinFrom);
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        tx.setBlockHeight(1);
        tx.setTime(System.currentTimeMillis()/1000);
        return tx;
    }

    Transaction buildUnLockedTimeTransaction(String fromAddr, String toAddr, BigInteger tranAmount) throws Exception {
        //封装交易执行
        String hash = "00202f4c51b04d06b338e9917e99ecb4dfe3b02147e7f206a4c3b3aaf7a1dd86aea1";
        String unLockNonce = LedgerUtil.getNonceEncodeByTxHash(hash);
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(fromAddr));
        coinFrom.setAmount(tranAmount);
        coinFrom.setAssetsChainId(TestConfig.assetChainId);
        coinFrom.setAssetsId(TestConfig.assetId);
        coinFrom.setNonce(RPCUtil.decode(unLockNonce));
        coinFrom.setLocked((byte)-1);

        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(toAddr));
        coinTo.setAmount(tranAmount);
        coinTo.setAssetsChainId(TestConfig.assetChainId);
        coinTo.setAssetsId(TestConfig.assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms = new ArrayList<>();
        coinFroms.add(coinFrom);
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        tx.setBlockHeight(1);
        tx.setTime(System.currentTimeMillis()/1000);
        return tx;
    }

    public Response getBalanceNonce(String address) throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        params.put(Constants.CHAIN_ID, TestConfig.chainId);
        params.put("assetChainId",TestConfig.assetChainId);
        params.put("assetId", TestConfig.assetId);
        params.put("address", address);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
        Log.info("response ={}", response);
        return response;
    }

    public String getNonce(String address) throws Exception {
        Response response = getBalanceNonce(address);
        String nonce = ((Map) ((Map) response.getResponseData()).get("getBalanceNonce")).get("nonce").toString();
        return nonce;
    }

    @Test
    public void testNonce() throws Exception {
        Log.info(getNonce("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"));
    }

    @Test
    public void testUncomfirmedTx() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        Transaction transaction = buildTransaction(address, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", new BigInteger("200000000000"));
        params.put(Constants.CHAIN_ID, TestConfig.chainId);
        params.put("tx", RPCUtil.encode(transaction.serialize()));
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
        Log.info("response {}", response);
        Log.info("获取 address={},res={}", address, getBalanceNonce(address));
        Log.info("获取 address={},res={}", "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", getBalanceNonce("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"));
    }

    @Test
    public void testUncomfirmedLockedTx() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        Transaction transaction = buildLockedTimeTransaction(address, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", new BigInteger("200000000000"));
        params.put(Constants.CHAIN_ID, TestConfig.chainId);
        params.put("tx", RPCUtil.encode(transaction.serialize()));
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
        Log.info("response {}", response);
        Log.info("获取 address={},res={}", address, getBalanceNonce(address));
        Log.info("获取 address={},res={}", "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", getBalanceNonce("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"));
    }

    @Test
    public void testUncomfirmedUnLockedTx() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        Transaction transaction = buildUnLockedTimeTransaction("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", new BigInteger("200000000000"));
        params.put(Constants.CHAIN_ID, TestConfig.chainId);
        params.put("tx", RPCUtil.encode(transaction.serialize()));
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
        Log.info("response {}", response);
        Log.info("获取 address={},res={}", address, getBalanceNonce(address));
        Log.info("获取 address={},res={}", "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", getBalanceNonce("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"));
    }
}
