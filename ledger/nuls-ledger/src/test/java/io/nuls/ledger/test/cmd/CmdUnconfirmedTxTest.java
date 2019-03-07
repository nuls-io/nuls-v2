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
import io.nuls.ledger.utils.LedgerUtils;
import io.nuls.ledger.utils.LoggerUtil;
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

/**
 * Created by ljs on 2019/01/06.
 */
public class CmdUnconfirmedTxTest {

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
//        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
    }

    public int chainId = 12345;
    int assetChainId = 12345;
    //    String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
    String address = "5MR_2CkbW7Bn1GpKkh4ZVfudxzTUNPdKp5Z";
    int assetId = 1;
    //入账金额
    BigInteger amount = BigInteger.valueOf(100000000000000L);

    Transaction buildTransaction(String fromAddr, String toAddr, BigInteger tranAmount) throws Exception {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(fromAddr));
        coinFrom.setAmount(tranAmount);
        coinFrom.setAssetsChainId(assetChainId);
        coinFrom.setAssetsId(assetId);
        coinFrom.setNonce(HexUtil.decode(getNonce(fromAddr)));
        coinFrom.setLocked((byte) 0);

        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(toAddr));
        coinTo.setAmount(tranAmount);
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
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
        tx.setTime(System.currentTimeMillis());
        return tx;
    }
    Transaction buildLockedTimeTransaction(String fromAddr, String toAddr, BigInteger tranAmount) throws Exception {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(fromAddr));
        coinFrom.setAmount(tranAmount);
        coinFrom.setAssetsChainId(assetChainId);
        coinFrom.setAssetsId(assetId);
        coinFrom.setNonce(HexUtil.decode(getNonce(fromAddr)));
        coinFrom.setLocked((byte) 0);

        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(toAddr));
        coinTo.setAmount(tranAmount);
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
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
        tx.setTime(System.currentTimeMillis());
        return tx;
    }

    Transaction buildUnLockedTimeTransaction(String fromAddr, String toAddr, BigInteger tranAmount) throws Exception {
        //封装交易执行
        String hash = "00202f4c51b04d06b338e9917e99ecb4dfe3b02147e7f206a4c3b3aaf7a1dd86aea1";
        String unLockNonce = LedgerUtils.getNonceStrByTxHash(hash);
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(fromAddr));
        coinFrom.setAmount(tranAmount);
        coinFrom.setAssetsChainId(assetChainId);
        coinFrom.setAssetsId(assetId);
        coinFrom.setNonce(HexUtil.decode(unLockNonce));
        coinFrom.setLocked((byte)-1);

        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(toAddr));
        coinTo.setAmount(tranAmount);
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
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
        tx.setTime(System.currentTimeMillis());
        return tx;
    }

    public Response getBalanceNonce(String address) throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        params.put("chainId", 12345);
        params.put("assetChainId", 12345);
        params.put("assetId", 1);
        params.put("address", address);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
        LoggerUtil.logger.info("response ={}", response);
        return response;
    }

    public String getNonce(String address) throws Exception {
        Response response = getBalanceNonce(address);
        String nonce = ((Map) ((Map) response.getResponseData()).get("getBalanceNonce")).get("nonce").toString();
        return nonce;
    }

    @Test
    public void testNonce() throws Exception {
        LoggerUtil.logger.info(getNonce("5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz"));
    }

    @Test
    public void testUncomfirmedTx() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        Transaction transaction = buildTransaction(address, "5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz", new BigInteger("200000000000"));
        params.put("chainId", chainId);
        params.put("txHex", transaction.hex());
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
        LoggerUtil.logger.info("response {}", response);
        LoggerUtil.logger.info("获取 address={},res={}", address, getBalanceNonce(address));
        LoggerUtil.logger.info("获取 address={},res={}", "5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz", getBalanceNonce("5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz"));
    }

    @Test
    public void testUncomfirmedLockedTx() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        Transaction transaction = buildLockedTimeTransaction(address, "5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz", new BigInteger("200000000000"));
        params.put("chainId", chainId);
        params.put("txHex", transaction.hex());
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
        LoggerUtil.logger.info("response {}", response);
        LoggerUtil.logger.info("获取 address={},res={}", address, getBalanceNonce(address));
        LoggerUtil.logger.info("获取 address={},res={}", "5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz", getBalanceNonce("5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz"));
    }
    @Test
    public void testUncomfirmedUnLockedTx() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        Transaction transaction = buildUnLockedTimeTransaction("5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz", "5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz", new BigInteger("200000000000"));
        params.put("chainId", chainId);
        params.put("txHex", transaction.hex());
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
        LoggerUtil.logger.info("response {}", response);
        LoggerUtil.logger.info("获取 address={},res={}", address, getBalanceNonce(address));
        LoggerUtil.logger.info("获取 address={},res={}", "5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz", getBalanceNonce("5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz"));
    }
}
