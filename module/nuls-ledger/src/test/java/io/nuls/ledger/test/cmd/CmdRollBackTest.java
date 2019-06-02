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

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.ledger.test.constant.TestConfig;
import io.nuls.ledger.utils.LedgerUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/01/11
 **/
public class CmdRollBackTest {
    //    String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
    String address = "tNULSeBaMfi17CxRHVqFZbSFGYeyRLHWw2ctho";
    String addressTo = "tNULSeBaMmp4U2k653V5FmmPf4HDECWK2ExYVr";
    //入账金额
    BigInteger amount = BigInteger.valueOf(100000000000L);

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
    }

    Transaction buildTx() throws IOException {

        double version = 1.0;
        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        params.put("assetChainId", TestConfig.assetChainId);
        params.put("address", address);
        params.put("assetId", TestConfig.assetId);
        params.put(Constants.CHAIN_ID, TestConfig.chainId);
        String nonce = "0000000000000000";
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(AddressTool.getAddress(address));
        coinFrom.setNonce(LedgerUtil.getNonceDecode(nonce));
        coinFrom.setAssetsId(TestConfig.assetId);
        coinFrom.setAssetsChainId(TestConfig.assetChainId);
        coinFrom.setAmount(BigInteger.valueOf(21));
        coinFrom.setLocked((byte) 0);
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(addressTo));
        coinTo.setAmount(BigInteger.valueOf(20));
        coinTo.setAssetsChainId(TestConfig.assetChainId);
        coinTo.setAssetsId(TestConfig.assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms = new ArrayList<>();
        coinFroms.add(coinFrom);
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        return tx;
    }

    public void createTestTx1() {

    }

    public void createTestTx2() {

    }


    @Test
    public void rollbackTx() {
        Transaction tx = null;
        try {
            tx = buildTx();
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, TestConfig.chainId);
            List<String> txList = new ArrayList<>();
            txList.add(RPCUtil.encode(tx.serialize()));
            params.put("txList", txList);
            params.put("blockHeight", 1);
            params.put("isConfirmTx", true);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "rollBackConfirmTx", params);
            Log.info("response {}", response);
        } catch (IOException e) {
            Log.error(e);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Test
    public void getSnapshot() {
        Transaction tx = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, TestConfig.chainId);
            params.put("blockHeight", 1);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getSnapshot", params);
            Log.info("response {}", response);
        } catch (IOException e) {
            Log.error(e);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Test
    public void getBlock() {
        Transaction tx = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, TestConfig.chainId);
            params.put("blockHeight", 44);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBlock", params);
            Log.info("response {}", response);
        } catch (IOException e) {
            Log.error(e);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Test
    public void getBlockHeight() {
        Transaction tx = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, TestConfig.chainId);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBlockHeight", params);
            Log.info("response {}", response);
        } catch (IOException e) {
            Log.error(e);
        } catch (Exception e) {
            Log.error(e);
        }
    }

}
