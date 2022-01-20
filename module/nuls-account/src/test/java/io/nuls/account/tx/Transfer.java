/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.account.tx;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.account.model.bo.tx.txdata.AccountBlockData;
import io.nuls.account.model.dto.CoinDTO;
import io.nuls.account.util.AccountTool;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.TxUtil;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.io.IoUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.v2.model.dto.RpcResult;
import io.nuls.v2.util.HttpClientUtil;
import io.nuls.v2.util.JsonRpcUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Charlie
 * @date: 2019/4/22
 */
public class Transfer implements Runnable {

    static String address20 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String address21 = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    static String address22 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    static String address23 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    static String address24 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    static String address25 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    static String address26 = "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm";
    static String address27 = "tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1";
    static String address28 = "tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2";
    static String address29 = "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn";

    static int chainId = 2;
    static int assetChainId = 2;
    static int assetId = 1;
    static String version = "1.0";

    static String password = "nuls123456";//"nuls123456";

    private String addressFrom;

    private String addressTo;

    public Transfer(){}

    //public Transfer(String addressFrom, String addressTo) {
    //    this.addressFrom = addressFrom;
    //    this.addressTo = addressTo;
    //}

    //@Before
    //public void before() throws Exception {
    //    NoUse.mockModule();
    //    ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
    //}

    @Test
    public void createMultiSigAccountTest() throws Exception {
        //create 3 account
        List<Account> accountList = new ArrayList<>();
        accountList.add(AccountTool.createAccount(2));
        accountList.add(AccountTool.createAccount(2));
        accountList.add(AccountTool.createAccount(2));

        Map<String, Object> params = new HashMap<>();
        List<String> pubKeys = new ArrayList<>();
        for (Account account:accountList ) {
            System.out.println(HexUtil.encode(account.getPriKey()));
            pubKeys.add(HexUtil.encode(account.getPubKey()));
        }
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("pubKeys", pubKeys);
        params.put("minSigns", 2);
        //create the multi sign accout
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSignAccount", params);
        assertNotNull(cmdResp);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createMultiSignAccount");
        assertNotNull(result);
        String address = (String) result.get("address");
        assertNotNull(address);
        int resultMinSigns = (int) result.get("minSign");
        assertEquals(resultMinSigns,2);
        List<String> resultPubKeys = (List<String>) result.get("pubKeys");
        assertNotNull(resultPubKeys);
        assertEquals(pubKeys.size(),3);
    }

    @Test
    public void accountBlockTest() throws Exception {
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        chain.setConfig(configBean);

        Transaction tx = new Transaction();
        tx.setType(TxType.BLOCK_ACCOUNT);
        CoinData coinData = new CoinData();
        //String fromKey = "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b";
        //byte[] from = AddressTool.getAddress("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        String fromKey = "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75";
        byte[] from = AddressTool.getAddress("tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD");
        byte[] nonce = TxUtil.getBalanceNonce(chain, assetChainId, assetId, from).getNonce();
        if(null == nonce){
            nonce = HexUtil.decode("0000000000000000");
        }
        coinData.addFrom(new CoinFrom(
                from,
                assetChainId,
                assetId,
                new BigDecimal("0.001").movePointRight(8).toBigInteger(),
                nonce,
                (byte) 0
        ));
        coinData.addTo(new CoinTo(
                from,
                assetChainId,
                assetId,
                BigInteger.ZERO,
                (byte) 0
        ));
        tx.setCoinData(coinData.serialize());
        AccountBlockData data = new AccountBlockData();
        data.setAddresses(new String[]{
                "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24",
                "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD",
                "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL",
                "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL"
        });
        tx.setTxData(data.serialize());
        tx.setTime(System.currentTimeMillis() / 1000);

        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        //根据密码获得ECKey get ECKey from Password
        ECKey ecKey =  ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(fromKey)));
        byte[] signBytes = SignatureUtil.signDigest(tx.getHash().getBytes(), ecKey).serialize();
        P2PHKSignature signature = new P2PHKSignature(signBytes, ecKey.getPubKey()); // TxUtil.getInstanceRpcStr(signatureStr, P2PHKSignature.class);
        p2PHKSignatures.add(signature);
        //交易签名
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        tx.setTransactionSignature(transactionSignature.serialize());
        Response response = this.newTx(tx);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    @Test
    public void accountBlockMultiSignTest() throws Exception {
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        chain.setConfig(configBean);

        Transaction tx = new Transaction();
        tx.setType(TxType.BLOCK_ACCOUNT);
        CoinData coinData = new CoinData();

        String fromStr = "tNULSeBaNE8nFpFo6qYiPiNHSbsGyKSceJLwQt";
        byte[] from = AddressTool.getAddress(fromStr);

        byte[] nonce;
        RpcResult request = JsonRpcUtil.request("http://beta.api.nuls.io/jsonrpc", "getAccountBalance", List.of(chainId, assetChainId, assetId, fromStr));
        Map result = (Map) request.getResult();
        String nonceStr = (String) result.get("nonce");
        if(null == nonceStr){
            nonce = HexUtil.decode("0000000000000000");
        } else {
            nonce = HexUtil.decode(nonceStr);
        }
        coinData.addFrom(new CoinFrom(
                from,
                assetChainId,
                assetId,
                new BigDecimal("0.1").movePointRight(8).toBigInteger(),
                nonce,
                (byte) 0
        ));
        coinData.addTo(new CoinTo(
                from,
                assetChainId,
                assetId,
                BigInteger.ZERO,
                (byte) 0
        ));
        tx.setCoinData(coinData.serialize());
        AccountBlockData data = new AccountBlockData();
        data.setAddresses(new String[]{
                "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM",
                "tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29",
                "tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf",
                "tNULSeBaMmbiCH5soCFasXnG4TwqknyTzYBM3S",
                "tNULSeBaMsUBLVxwoaswjWvghJyoUJfbfB6dja",
                "tNULSeBaMmahC2LGeasKWTeXZcm9rkzsPUMeuc",
                "tNULSeBaMpo7gKm5QPtmRijw41Z5QE6j2GMuxC",
                "tNULSeBaMrUZo5DodqSUxsvyc2ZpnY6CixntZQ",
                "tNULSeBaMjSpf89cPKLKEQBZC9empzKph3ChrD",
                "tNULSeBaMsLTm79mqYRRb2mcFwQwA1qyxv6P4v",
                "tNULSeBaMqmUcqLwNjbwPu36yS8kZr96VpTCXu",
                "tNULSeBaMfZz2WqVTugiFSH4iAJ9Aj8HsiWCDS",
                "tNULSeBaMpbZm6kmxZnh9L8zMfyLbKAxSAukhW",
                "tNULSeBaMkt96G9UNGSi57MDpsFgVM1fBEC3xg",
                "tNULSeBaMfLLEj4S6SJSv5heDG4q3PSBtqbtV4",
                "tNULSeBaMiBnT5eaApoxXAx7ha3a3PdiczirQM",
                "tNULSeBaMvYBNZrRcHfj3e9rYqWV3n2mpDGjTK",
                "tNULSeBaMntZBWKNA9Gr6rLLpMtJDi4EoHS7MS",
                "tNULSeBaMu6g5aYtG56ugWxzgTJeV2ejcWjS8g",
                "tNULSeBaMs35nryuuAvewfgHLFBeMtLbVdUQvA",
                "tNULSeBaMuH5kCbpRXrFga2evkTC8kbQorVb4r",
                "tNULSeBaMvKqbpuWACegmxK18d8TotsQuh4N5f",
                "tNULSeBaMjf25Vtvd13Qb6P64nbrRXsNXNxhzU",
                "tNULSeBaMnPzLH3eWNAi93YaqHY8ghoo3HSsxB",
                "tNULSeBaMm4wHdUGhPWXqcUv1uMnVjBHaAWhyw",
                "tNULSeBaMs8ARjJYv43fk8ELRz1K7Gbj6bFCm2",
                "tNULSeBaMvAeuJqw1qwPWBigSHzSvSXxeGWzMi",
                "tNULSeBaMrrntsToT52gzXV7JVCBnT6Zvw123F",
                "tNULSeBaMiMyvJ8QYtcZ5Ay5w65Qwp78HV2Mhe",
                "tNULSeBaMoaVoa4wYyK8Sz5MhAkf6Z3J1FLXmx",
                "tNULSeBaMimJNxiRnvHnQcyASWdGafpZ7PDipL",
                "tNULSeBaMntdKF5gw8Mzg1JdHetLVscQQHGsv5",
                "tNULSeBaMrw5FD3nkD7Xnz2KWco51Mzz8XndeF",
                "tNULSeBaMunUXoJBQpBBqPYUtQrJtBe6zNjfE9",
                "tNULSeBaMpk7b9x3F2xsbs9dwE1dR1U4XCY4Yt",
                "tNULSeBaMnTWwYEg63kJAhwZo6EqKoC8F2YYv2",
                "tNULSeBaMrQHhgTAyJPBu1fNHQqoDEyubNYgoS",
                "tNULSeBaMn2afdfpiEVnLLM22vAJrj4oDzGRxu",
                "tNULSeBaMv6wyb8ZtHyrjG8cLTpTiXs1rBEYLr",
                "tNULSeBaMkYTVsd8jMf1AEHXj2qz6BqF1U6WJf",
                "tNULSeBaMfu2mm7WKZLcDRkWd5wWC3Zdr2oxYU",
                "tNULSeBaMrZ7vSnDuhDeBni1DCbgcKcR5zmWqJ",
                "tNULSeBaMqA3utJzfwZgYKEHKt35qZoAeGMzKR",
                "tNULSeBaMumTjdy7hFgCxKAPmUroDM3x77oG9w",
                "tNULSeBaMte8tvgwWBQuGSyZNy5X8aUQHaZUyP",
                "tNULSeBaMhazW38TLZSS7b55Pf9Z4tPasA1exL",
                "tNULSeBaMtALQbXQwAuCQAgE9TCbF745ToX4vb",
                "tNULSeBaMjxNnB6F91znyXsnYDWCpT8VHCk9FW",
                "tNULSeBaMh3WFcsM9spWSzEyueU56pRgAVawXA",
                "tNULSeBaMuSPf8qMVjRUKEwu1gLQ9YpEcJvCLj",
                "tNULSeBaMkTQpXHAqv4tHfEjMMPvQjTPPrVMFx",
                "tNULSeBaMo3g4M65tNiG29vXL1RJnqVfgYague",
                "tNULSeBaMj9TzSLF6daiPqzNz7DKfK2peaUqmD",
                "tNULSeBaMjeF1BGKT7DMGDPftx4fwQyFBWHgfe",
                "tNULSeBaMnEaBat4UXUGRCQWso3ANkmDe3pz8K",
                "tNULSeBaMtiaoqLqPDDZLQiX4xGey6gqidWWcY",
                "tNULSeBaMviyBiiEjQCbZgNQKbZd6MtXJPRnaT",
                "tNULSeBaMfUJrJ3k9ic9RQnc8Y8nEUZne34qcG",
                "tNULSeBaMiSjhPfvdaKCREtjCswDRi4WSpx15P",
                "tNULSeBaMvRgTF1exL1pqpt1x4iCwhzP3Ka7cb",
                "tNULSeBaMsU7Q5j7w9ScMNKRRKWEHfeD2PyuXd",
                "tNULSeBaMi1xRceTGF2VWioiwLWsroq6ufBNts",
                "tNULSeBaMs9wEhRym4gNqDwDwQt3U1DJf7uCHy",
                "tNULSeBaMnoWrgcCmknCtAYZ6ZiCJKc2EePYwd",
                "tNULSeBaMrs6KnvzXRiwQ9PooGpCc5q8KYWtTH",
                "tNULSeBaMr2oqDh63t1ATPghubUxkNFonTycos",
                "tNULSeBaMkYTg2aS8CThjxee49QoSW1NZyaA6r",
                "tNULSeBaMhQHFWRYnf7QGbj7AU2kBqzxuSqEaY",
                "tNULSeBaMqiMgCnAedK4iPYYnZjdJgHN7oDjHX",
                "tNULSeBaMjzFWbY37hKhMKvH4zVnUuLXdHimjB",
                "tNULSeBaMg7AVkSs5JnKULc2BQ1KnHSTQszZrb",
                "tNULSeBaMg8Y9EniLU3bKAWLvkeeGLBAMCKN1c",
                "tNULSeBaMqzTnGd47CyqttqKAQPUGxWwUAYD2Y",
                "tNULSeBaMqpmNbPL6nBVWMPqFZtgoUyHDiUkbq",
                "tNULSeBaMqc3peALkWhupPAgfoBXki1XFGaiwm",
                "tNULSeBaMu8gqAc1fPSwcdHo7BRLAycdE3jpiF",
                "tNULSeBaMriiCb8r5U6FAqzgTYuM4dVhiNdwua",
                "tNULSeBaMhEkFmCH3vjiJkFsD7kQUgfzmL88Es",
                "tNULSeBaMsfm8p4iaLXHDUChFKFtqwqpDsBxim",
                "tNULSeBaMhF63he1gYNdt8ZLNZrAuXTN6zbUpH",
                "tNULSeBaMgKXy7S9RTvuvgCavYFn1MzihTkLcz",
                "tNULSeBaMpC6qHqzdhDsrEqrStBTbaNc5BroAx",
                "tNULSeBaMoigbVKySdsMxTGuvCgzCPcHy4NxwL",
                "tNULSeBaMqQ4B3mFddBMtoCeunitXf4uNUrw64",
                "tNULSeBaMjomid47EQqFximBNKGSXPJui6TTSd",
                "tNULSeBaMtcfHB72Hd7ZznRkePJiR1SAJ6AJJV",
                "tNULSeBaMrj3xc956DR4ZeWsNxjZ98wSw9yL5D",
                "tNULSeBaMvAZVvpYBgmxhr42h2MFECu4CpCA9g",
                "tNULSeBaMsXTLqxyL89JwxAV4joA396JwyWEAW",
                "tNULSeBaMoabiDDp7LV5Eg2s5ZC5DvpAWmo9ea",
                "tNULSeBaMtupiuW2x6FRs6Ui8Co1jiCUMKsAyC",
                "tNULSeBaMmBskqTyjkvKpZh5TDidNito8Tcf74",
                "tNULSeBaMi9tDhDQtHR63ujJmyqhRpzE9MovPN",
                "tNULSeBaMuv6sSj8xKhBHwN9spECbNzZdtwnd8",
                "tNULSeBaMiPJEPQgN29TtJ7w4ym9zNYBdRnrPR",
                "tNULSeBaMjvV1i8FSsjmJPusCEkAM7ZMcALKRH",
                "tNULSeBaMnPMaZph6WX4J5oWXPLx7KcWt47YUT",
                "tNULSeBaMhtt2F5Acq12tQDjNcwJpDb7vVfJVw",
                "tNULSeBaMnQt8BDFER8cbVGsPh9WZsxB5q7VND",
                "tNULSeBaMtyxQmmzk6iY83tJ3kjLGxJLKAs8Ej",
                "tNULSeBaMj3TQBrmpeonMj5ZLJrmSqT5SrKBve",
                "tNULSeBaMhjZ1UAJCNKgCSG957MuWNxADWkchK",
                "tNULSeBaMsWQPfXrS8JiYnzqVqVGrVDxugYiXx",
                "tNULSeBaMi5EpyZV3d7PgJzXFAgEqsfRAYSQ2z",
                "tNULSeBaMm5TXMvxWv4wa3i9bbxDvGhkQch9uc",
                "tNULSeBaMu4QaGoeJoer5QeGWEm9D4Ee53gDf6",
                "tNULSeBaMsqv2B69TwoW6djnSouNXyDH37MtdK",
                "tNULSeBaMiyqrcfcy8bYZKeXWHTe4PRSUgGVYz",
                "tNULSeBaMtpwBbbrZ7bYhN8D1XnjNSRb5wVRU6",
                "tNULSeBaMseyZ2mvq7ppS3zfEGSiUhHR1kTC4f",
                "tNULSeBaMfpjoeMfNvVXJUXTqjcVKVAybMr7uX",
                "tNULSeBaMqSW8Y9Rjv8VXdsEyhrt2adCaWrNpr",
                "tNULSeBaMjXLHhMNjkSqJ1qthKbPBmSVYrU58a",
                "tNULSeBaMtYmhFowou6b9zP913RnbyHW7kRAQq",
                "tNULSeBaMuTEkD3NwuCnrv8UkfPj4wZj2PDGfU",
                "tNULSeBaMgpCSLUeqJmzwFbk23AXEDtbdnG9Lv",
                "tNULSeBaMfoeqZqatkBS9emk28HWacRkfJakCH",
                "tNULSeBaMfTBvZWtjKTbsPCLfSkxnLUV2cpzJM",
                "tNULSeBaMkYJw65t1fHgdUv2pjzB7Vm5HhNhgB",
                "tNULSeBaMjS7k9rCAeMAWpdXyYLriEd1YcWZkr",
                "tNULSeBaMu6BxBCxTKycx5jPc47qPLoxnyxrkW",
                "tNULSeBaMhCKxbHnD6H8He4RGLc7QARhAgjQZy",
                "tNULSeBaMthzbGhijcBw5BRzLVuiRcVo5zX9av",
                "tNULSeBaMqeK9H7tw9SFWk6szREvmqg9cHQeRM",
                "tNULSeBaMuAEjB62JSGST1GaNSASYLiYEyxpoT",
                "tNULSeBaMkyngHqa9oqope4vqU3aHeE13dBzLX",
                "tNULSeBaMfcoT8bnBcL5DAwpvYh1km8DYcrKZt",
                "tNULSeBaMoZDWdPHvBnTXZW4NedMnukLU9zW6p",
                "tNULSeBaMmE8kzrv2Y1KpwdLuRxNwaNGx19x9g",
                "tNULSeBaMfS5rCFMcvnR29RscuENujtUZYH7HK",
                "tNULSeBaMqtFzCbdDVFfWytQDE5s55didxzSo5",
                "tNULSeBaMvFqJxhoZqzPYrRqJdEiHqAeRQDRTG",
                "tNULSeBaMo1EbjDVWhjkA7657RC7ahN7uHWGdW",
                "tNULSeBaMt2MpEvJd9cd7145oBf5jVqRuWr6Es",
                "tNULSeBaMiPTZkdZqYgDzYMx5ZMgBZQrQJ5WxK",
                "tNULSeBaMgkgdpCRNN4MzMXG1JmbX9jZfd8sqS",
                "tNULSeBaMsoZbd2PYaG8jR7xHKD1KZg9qp6UQb",
                "tNULSeBaMgUS6w8NC3CiVFQTN5RDk5ebvgHLtS",
                "tNULSeBaMqNV3spCyP6PxBk67mEbR5RECtPXjp",
                "tNULSeBaMm4wT6EHBYZ6zkW1skzskPKrueiTtk",
                "tNULSeBaMfpnoGHmmBgLcbLomXBZgH2vo68P8b",
                "tNULSeBaMsPcrJpW95q4dbBEc9yLJb87N8Rta2",
                "tNULSeBaMoLapBKbdtLUZVirE1e8BKnKFSPXpJ",
                "tNULSeBaMkhapvyiXUJMo4k6GGTxVVszGFRzhS",
                "tNULSeBaMhDJU2SXfwtj9W8Q39XcGevdh5VUQ9",
                "tNULSeBaMjNbm7bMdde3yPMDvcX8rmbubqSfFr",
                "tNULSeBaMfTFsutdpU2jPt4tGkBkU2S8jaYHYd",
                "tNULSeBaMuzmB9XkM1knGd273oDwLnhL1VQjTM",
                "tNULSeBaMmEvpf763rYd9hkDBaxiDTb25TTiUV",
                "tNULSeBaMkKmQAMd8Ujhxo7vB7SnWQiiP5M11S",
                "tNULSeBaMuiRfkBmMq8UduWpA7dscZ3V5X2yvG",
                "tNULSeBaMkJpotR11e1J8atyzYGrLqfrYMTEip",
                "tNULSeBaMojezR6asCbvdJeGeqx3AQdNSUDYKx",
                "tNULSeBaMg7E5Ep2H6ytA8hytcKEpbC3J3xBNe",
                "tNULSeBaMhBrb28G7UeQNQShcBKwnaUfCanzqJ",
                "tNULSeBaMnQSw2M86mL42edmh2Vctiz5ZSEEcG",
                "tNULSeBaMvCcqgvPzLJgVHPeZSLYgK6TX5atap",
                "tNULSeBaMvSFbWez4KHL8Az9Tr5hnHnbvSFQJb",
                "tNULSeBaMtNdEcNLf2gxaYBSRq5u36M4c6L7tP",
                "tNULSeBaMrogkg12xzQBkLjGAqdnxXPxcPsJMb",
                "tNULSeBaMqWWcZ5nu5i2FA15aV4Y9WBUKD9iB4",
                "tNULSeBaMorpbMwqNZxmnuBrHFYruCB6YFCnMQ",
                "tNULSeBaMn6fbN54GcJg74GfqqFe7Fb6XN284E",
                "tNULSeBaMi73Hix1Safn5NkZfv1kmfkSpqRTST",
                "tNULSeBaMtzXSuHdXLp3Jvx56kfwWhRMGdg2nS",
                "tNULSeBaMvDdBMnso67UKCtWDWBAtyU4ppcoeD",
                "tNULSeBaMpKLz3PPEMAwWyPQsRHwCLRzvnKfvs",
                "tNULSeBaMhvV5RunTAW2RZxcgi6HfWB7Kc1R6L",
                "tNULSeBaMrmxkwDHZfMNtYDcCUVk1TSu9Kjspr",
                "tNULSeBaMtFFcQNryNLG6LP5BCFFabrziQneqB",
                "tNULSeBaMrWAUWvNU742qqs6rF8NSGP1AyXufd",
                "tNULSeBaMfQU4tyw3cq5xsGwAxRXvR6ujjjrq9",
                "tNULSeBaMkVESdNbYfPFBkNiy7VTbYf4twoQVA",
                "tNULSeBaMmnKU42FMADBmCdUkhi4sBE6GKvZKd",
                "tNULSeBaMr6SEVAEGypGVjQSnjsYhbQoysd7z6",
                "tNULSeBaMiKwyWpkEKUCEyDg5KXAmBkANZDP1E",
                "tNULSeBaMjUuHfp33du3sELCkj5zcsqeh1kyWa",
                "tNULSeBaMq7kN3utAxZhXMCJfZvu455VSxJ1Af",
                "tNULSeBaMmYy3RNdjAJn6xuhFjBUaudd17QwmH",
                "tNULSeBaMkoxVYptcTAM9JBz9s2phWKcebcwhf",
                "tNULSeBaMhWuTbtkjZyJC1DqTsTFHSjZDF7GKp",
                "tNULSeBaMozCJU4xSvv1vz12uey73Wmgj5JgYc",
                "tNULSeBaMgsLpn4pdoPoibsYYC1ZXZDFga4kPP",
                "tNULSeBaMt49XG7MnwcggKW548UxKp4RAK2ZVX",
                "tNULSeBaMpYMwdwF1evX4M98N6ipARAb8eNQbS",
                "tNULSeBaMocZKLRnE7JGoVGAKHp3cbMwVeCRZs",
                "tNULSeBaMiBEtv3GDFZxcBD8wFtQSKJhtf2pV2",
                "tNULSeBaMhT3TF1ix5dXGAJnjJNikRyz4DuShT",
                "tNULSeBaMgXkdVYy4PSETGYfgwvVHEH3oCeD8A",
                "tNULSeBaMhU4eLyqA2b8KfHr3x83KFM224PwnK",
                "tNULSeBaMtqjDWpwy5uvqnG1UpXjdWAWiZFvaa",
                "tNULSeBaMf2yyz9baoJEgdAQrMSoroNGMznNuu",
                "tNULSeBaMm2WcHCmvcM37amWWzmHF8ixVjGYXe",
                "tNULSeBaMjt3rNxeETwKGvtCaBDFT6ujNdsZdz",
                "tNULSeBaMopwHBVhEt3XyJ4vdWA6rRigg5D3fe",
                "tNULSeBaMqjJyavZrSW1t6GgryVtDko6KnNteZ",
                "tNULSeBaMjBRt8Dy8fZxnohStXnSCFEoBWTEtJ",
                "tNULSeBaMnHLQ9mSmV1s6hWVykM8X3geoLxJSs",
                "tNULSeBaMnLoGZubYtpmbpCTqDnRf31eF3XjpR",
                "tNULSeBaMrZqR5d8pUBowcdYQT4iffQNLyKxMw",
                "tNULSeBaMpWsqnmb8YT5qg8NeRgsP7EJY4YjWt",
                "tNULSeBaMtbsGjuYHQSxGrzSSQKgE6mgSjgyhQ",
                "tNULSeBaMhUYYz616Vbsbu1hCFp9A1jc927jYC",
                "tNULSeBaMfpCKxJBYxuxM57Vri4ahYri8oH2A1",
                "tNULSeBaMkCyRhTfMGPgZVyMk9D3NhssT568Ey",
                "tNULSeBaMkvkDKY9kHEGkT2cPeH9kWpaK711gp",
                "tNULSeBaMsm2MQVUSwvTKCkS9FybxnyC9SW9Xi",
                "tNULSeBaMpe3fjWucNMcXgHtcBmMCCHc8EQGiL",
                "tNULSeBaMoaUo1wDiLeicMyV5ua5BLiQKjHNE8",
                "tNULSeBaMiEXN9jm6UhHH4TcKhEKdjWeAASwzt",
                "tNULSeBaMpJ454jv4ub1WxHiFkLSQAiohDBCdP",
                "tNULSeBaMupHbEXGJNzGQCt9yt3kCHMhB4Z76E",
                "tNULSeBaMmMwcFBnL8sS7GqADxT9TWcP4AHNEs",
                "tNULSeBaMihdTCTbTMvWv4rminTeHbwEixq6uH",
                "tNULSeBaMpxaojNgpmCoQq6bjNSsYzrG84URhf",
                "tNULSeBaMuCCmDY2dvHFiHmMcUkUjdZUJdf6Uj",
                "tNULSeBaMhAY1XUQ4Ys1DsMg647ZyDTh37zVcL",
                "tNULSeBaMtecXXRASkHnPFUpib15L25VAzqaT8",
                "tNULSeBaMgYiVhrEikXXuRz1Rjmdgo6RuSFyf1",
                "tNULSeBaMg8Xkc9bVJzDbtnZS1xsTCjXT3R4Bz",
                "tNULSeBaMsaSh1FSWLND8UrY3kM1eDiS3Nj8pK",
                "tNULSeBaMuGQhTWiZCUTv2gBftYK6aU5zFxGEa",
                "tNULSeBaMsKmxHMNBXo4N6yeLp2jzW26RV3KWs",
                "tNULSeBaMgZohCZpMMvnfTNzMrqGJsR2QsKEYq",
                "tNULSeBaMh5QMiy4rS7aH9jD4ML3PiSeFKxgyG",
                "tNULSeBaMov2rYAkKYj3cn9mbR4ueJpTb4H9Q5",
                "tNULSeBaMnmoeXxUKpB3rN4Gtm6tDGmugPavuv",
                "tNULSeBaMqw43TiZzNYLS16sUh2AgCc8mXDkD2",
                "tNULSeBaMtxtTEmtUcswFvFjE1xbgr7cMwhN6W",
                "tNULSeBaMiuWQMenj7UohRcbqikNuMiyuVhbAD",
                "tNULSeBaMjtqnXiMStS8153t7akP4rLLfkPof3",
                "tNULSeBaMpPMSxDTcrZNsFcjdUwwWuy6gTmiNo",
                "tNULSeBaMptDMz6nGuRyKnaaSPYVcNSSZkfdfS",
                "tNULSeBaMo1FJyMW75hQdBfgBpeKUgeRiDkHh8",
                "tNULSeBaMikmLcap2CTNjqZr1yi6cwPFytERFX",
                "tNULSeBaMqH4JAK7KgW75me6ZQDTFktvGKPAxX",
                "tNULSeBaMombLmuKPakGx3A9rATbjpotMx9138",
                "tNULSeBaMveR67FJcKC73nUasXYGCunCGRiQZe",
                "tNULSeBaMfXucFmjEgpRzZMjfMuriDizSV3ag6",
                "tNULSeBaMvfjQWS4gc11VsGAqS2C8Z9wp9CrQg",
                "tNULSeBaMtD8hS5JF5uZ1jMxTGzxD7b7m3kqhe",
                "tNULSeBaMhnY3VkQsuEH8zhtQDVZ6u25CCyaqx",
                "tNULSeBaMgmJ86tiBrBrHxD5rvDNf298VGC5Uh",
                "tNULSeBaMk6ucgosNEd6BP7oRtkNNaqMF64eZg",
                "tNULSeBaMtCE5ZjskpPvZNqMBYidBWPv7ErfAf",
                "tNULSeBaMmRNbqvBUcj4rQXYK84bFBkt78f9jJ",
                "tNULSeBaMouyxays968A4RhSxiF4URCcEAMHiC",
                "tNULSeBaMrDRG8oKe7kaE1onvgaAdyYs8dzVvi",
                "tNULSeBaMfkUbmPJXkG4tyTJRLQ1z2nSUieKMQ",
                "tNULSeBaMpXLrmy4gA7eqDgmzKazYkDsd6b3Ps",
                "tNULSeBaMo7QQe1mB7Dyb6uhNr6YtiaUkTHhYr",
                "tNULSeBaMkSUgEweKChREBgfwBQEHcofM9jJ5v",
                "tNULSeBaMrUfywgSMJQoKT5pm1oVJzKLEshcPm",
                "tNULSeBaMpWX4hpsUSrs4i93hHtTbD7cJv1iWe",
                "tNULSeBaMqWUj7AycL1vrdfhbb3cZT9WLJokEL",
                "tNULSeBaMn9vFYtpDfDMdLMmAZMcM9XwnhwQvB",
                "tNULSeBaMpF75QoLEAKBNjnyPhHtYKu87kmGgS",
                "tNULSeBaMr4SQUBVTDRpnXf3dd4xUT4FcVqGY1",
                "tNULSeBaMpmTGrpb8namUHVPaZGb38K6tCbV7y",
                "tNULSeBaMoi1sAc3W4t3QJvusn8NCBwGoR7ADD",
                "tNULSeBaMrTpo9fq3uxa58UrqiXkYv7T2Yrjdd",
                "tNULSeBaMqS9NciKBSoUouPtzNQB6Yv3LxUceE",
                "tNULSeBaMhLnN3PZbCAoNyF9WT9Q1i7eBvvSzC",
                "tNULSeBaMi5mSdV891rpKgUoXppEZijKUyLQGZ",
                "tNULSeBaMqfrQZnkvudv3EL3CC2bZFmTmuK9t2",
                "tNULSeBaMoCUou5U4WLRvYZUz9T2SeMeeDkggs",
                "tNULSeBaMmnp86Go6ZN2Ahj5JBKD3AmcQJnycG",
                "tNULSeBaMg4rTHvgvvhcg9tjX7WZRzrTAGe9xY",
                "tNULSeBaMgzJFFhtmbYxBb44mjLTufYxhiuM2w",
                "tNULSeBaMsZaZoZk1sqM8G5dRyhf7JNAHFqRqZ",
                "tNULSeBaMsZSnj4kTGsjDrq3bPXevPtufuswkz",
                "tNULSeBaMhmcnGnXoJb3ndNoAkPuggRcQQHkTh",
                "tNULSeBaMkcgaazHZEESEBviQmjUT3FWsZAPcd",
                "tNULSeBaMt2bmEWifYdfhk5VXvfT2ArGREnLcf",
                "tNULSeBaMu3Dc5k6ADnpeWiNejEAnpVmS99x3m",
                "tNULSeBaMfea6k8QA6Nxnw8tX3GJQeGiDntA2c",
                "tNULSeBaMnBG9WbtJB7iGwAeFYV9wXJMYNFasd",
                "tNULSeBaMjsdTrjiWZoyrx2i3bU4udTvipotWR",
                "tNULSeBaMo8SzDu8wr53A8z54FsLXAG88EGLcv",
                "tNULSeBaMmtehwDEoD3NZwdBjyrncL3D9AhNcE",
                "tNULSeBaMo1ogS8amEjKK4ew89XsTAmunFXxVL",
                "tNULSeBaMiBF9Hq751DqHBpz2f7tvum1hqTrKy",
                "tNULSeBaMuEWFCQNHEwQhwEKxEfHYqnunL4uTQ",
                "tNULSeBaMmGRsUo5Nf4BgBaWBfwoYLoGg1FJkn",
                "tNULSeBaMgJ9A4bomUbzEak3jD2eZau7uRPXSN",
                "tNULSeBaMrbMZHtWQ1y1ZAwxh7ucFnJsLhpBRV",
                "tNULSeBaMqNHEVjmmWqdNX9kBg6yNMxStx6vVC",
                "tNULSeBaMfdyhgyBVJwwDSGqxBkYyQq3NpWQhZ",
                "tNULSeBaMvW9MtQimSMExqCPeX3A2h3A28TLcf",
                "tNULSeBaMtQ561JF7CdmeT7DAKSwNoGQJAaGE9",
                "tNULSeBaMixp8EpxQCEXoE8GQnE1RorqpH15oZ",
                "tNULSeBaMtA78jZUCGujoSs3xxvvaUpaiQ6pSg",
                "tNULSeBaMk7RwH5XzUj2CaQKnG1adzFqtnNgyf",
                "tNULSeBaMtSJdD1kspSKFWHghc9usfGvwKuEuf",
                "tNULSeBaMsAo9DQE9UwvugUNh7tt6c7hdVs4iE",
                "tNULSeBaMqraf5fLK8BebK1T5kGwFcGAd5gA84",
                "tNULSeBaMiAtRWT6DvQuhKmLxm9ApjNyBfxhWZ",
                "tNULSeBaMfKqEuC1WHjtLwnxyEz5piAQJRizUd",
                "tNULSeBaMvTJeyWKx3gMyduMVmg2NiZabFQbL4",
                "tNULSeBaMq5QPpxud4L66ELMJvSwnh4Y2Fvge7",
                "tNULSeBaMsLnKG1yi3JB6bXwNh5hbqDRw5sYca",
                "tNULSeBaMgsfaLmdaNicD9B4kie5BAT2ypTUYa",
                "tNULSeBaMk9beGGouFuW5vThzByLwmJxoDXvVx",
                "tNULSeBaMhwyCDrHaDBMFn4qXiXadDDXLk9jhg",
                "tNULSeBaMjD7N8oCnfsXk5qjG2BCsfWqs8ZD9N",
                "tNULSeBaMtXaMAF3abqksykcbtXSi257BfUFY4",
                "tNULSeBaMm4q4QRpyXjz8fP4WaybzeyhdVKsEK",
                "tNULSeBaMjexhnVMEyQpEnQyXiGCtKLFNHgaJ2",
                "tNULSeBaMj1VRnmVYCgV2YG51vMvTW2fsBqAk7",
                "tNULSeBaMv2CDEpMEuJd4hTMfDaqLz9dgM7UgC",
                "tNULSeBaMoBTWZ8YjkJdiNWaKN4HdgjNFWWGmg",
                "tNULSeBaMuP9Uu3KB9TSNR8cxjZEmL8jaSkQBs",
                "tNULSeBaMmwqZA3HRg7tpxV68JYH4S7ikrjC93",
                "tNULSeBaMnLXBwZCR7MACM5BuoUYwLhVz4y7RV",
                "tNULSeBaMopdybaR3biV4hTbpWzpcjEqNGVm4r",
                "tNULSeBaMrdttGE3qNeA8XLUs7jTsDRiJW1ZQ6",
                "tNULSeBaMnLpMpHBZZjZLPgK3uv2vHNrd2tUy6",
                "tNULSeBaMr3Q5T2wLNWPrytyH383LckJj8ETW1",
                "tNULSeBaMkq7xfLV2vdmemVDU934AEvndfo1qb",
                "tNULSeBaMtPGn45f8aZP4Kk4svmdASyCehBCcG",
                "tNULSeBaMmSxzgW9nPAHoxhJgW9gVsoLwFwFVb",
                "tNULSeBaMshmtPc2Xon2NkNS1fLPm2XWrm4XgC",
                "tNULSeBaMpCvkwW3XefsjrzhAGVP8KDdSu3wfK",
                "tNULSeBaMs6j3ToLqHiFNTPUE6hoY9YjS5pGKm",
                "tNULSeBaMi3ADKu2nXkWE3AqSjpFy2k6j4FrpJ",
                "tNULSeBaMni3gTB9BWu2cUGRymEp7X82ZBy3mh",
                "tNULSeBaMn5DiAgX2t5xJ1a9X3xSAkqDRi2K4j",
                "tNULSeBaMm4kLbjP4R6tsEoBkBTRzmrpCwuLsY",
                "tNULSeBaMvNS4gJ2qicaythnMqRrr8c2nHwhoS",
                "tNULSeBaMfsiAkSHXBjQUZFkSWEDakH2MtDnYK",
                "tNULSeBaMkv8tkJ6ttFJUtCTj9PFDv7evqnmG8",
                "tNULSeBaMiC9YLmijMZNsMGynWrN3t3JBiQ4Dg",
                "tNULSeBaMnuhqQs9cKmBCqPdpBiMeAs9bvRJsb",
                "tNULSeBaMrxpxVFusCLe8JZJEe8hWrZr3PM9HB",
                "tNULSeBaMqFMKLCCYs2dSyeq8XsRRnzfMRTxuF",
                "tNULSeBaMnrkRPFVoyGgif9KmfqChYo47tVBWG",
                "tNULSeBaMs5d9bDa63JPxMjga9TdJbgWA3SDof",
                "tNULSeBaMnJugh6GQTzw6chJx3Urhw6KesAHXh",
                "tNULSeBaMngEGc2GCdKaQ3u7vDkp8H2BevxfeY",
                "tNULSeBaMfuz5CbVUMs9QsRpw2oAwB2eaRM2oo",
                "tNULSeBaMnpgjmVkBpKQan4u68CPBThZfRXQws",
                "tNULSeBaMkJAResbc7aPVygAd1aXan7EaXRmqm",
                "tNULSeBaMvfczADisSVhN7a7uekv4hxwUndrwD",
                "tNULSeBaMt2z5Meptb7f436caVpZGgzugxds5x",
                "tNULSeBaMowYrnEhaaPDfW9J6qshNLFoBfbRfY",
                "tNULSeBaMka1CKCtaRHrYUr4ux7SE4eAenHdZ6",
                "tNULSeBaMqoU3XLmVZyKHUohwji8Jym86YyeJU",
                "tNULSeBaMikdz7LHS5F7Z7LqLR9YnwmDPKRXMR",
                "tNULSeBaMvUbBMDpjJHuYyCBUDfyqg2CWpcZin",
                "tNULSeBaMuW77xE3fJRnikE16cMiLUmWVHWufK",
                "tNULSeBaMmPp8f4gcqsAnnL6LNMxpBzZxruaz9",
                "tNULSeBaMfNtkiinU7ng65MAy8dJ6KEuhHFzw8",
                "tNULSeBaMrxapbBbtd6dG7QtaTEePeJkgeLAqK",
                "tNULSeBaMjvoZvRrFgJqAQzfLxA9CACkgWnqUi",
                "tNULSeBaMiXqxtENZuuKcdzz67Ua2Dzy6EKsup",
                "tNULSeBaMrLsG5ezq9ZDMSassQHzVrkpENxSqr",
                "tNULSeBaMo7mfp5a7jpTVNSJUi5Jgj5AYbnPX6",
                "tNULSeBaMhecxiBzwg1TVtVFMLHrmE1y6ZLWVy",
                "tNULSeBaMjpLBvyxXhVePggZ1NdkpeboQhZmy2",
                "tNULSeBaMkKk93GctPmfbQiu9pp2ggNurQEKJi",
                "tNULSeBaMo5SN8h9QFbWAh2cNgkvzHDgUV6ncX",
                "tNULSeBaMh73FkEjKNxJVYDjTtGtYuKnhuFGbT",
                "tNULSeBaMsPohLuyHriS1HvAPkQSXmq59WPcSs",
                "tNULSeBaMfcAimhKK4GYPWcM3SYHii4dPKHkid",
                "tNULSeBaMmuGSW51weVcgzRUa4HvkK1M2d9MvX",
                "tNULSeBaMmqJ62rTdcHfvYASnFa1Yh6sRFQPYq",
                "tNULSeBaMsc3weWcoYQ5qyZuiMF1vwfrk4SCjY",
                "tNULSeBaMr6v5nR4YKxNzKub5AunuZKakG2gkr",
                "tNULSeBaMpEuVW4MpWcDoYFDwX1oSFXv3rF5Q2",
                "tNULSeBaMjZ7dLep9nxvgsvmUvyJdhMtix95AR",
                "tNULSeBaMtUnrBEyqDRRp5wDcj1RPYiiz3JfMA",
                "tNULSeBaMqikCittqiogDbNEt9oiBX1Kq71sFm",
                "tNULSeBaMktEFPExgoQEmtz3DNaZa8hWGLMbeY",
                "tNULSeBaMnqFMuReUYuEdGECrp5g5vfCoDLyDF",
                "tNULSeBaMhqcZ3VjNB6AioRoAQ4FLrebRZE3gK",
                "tNULSeBaMvNDNiDHK6tJ7kQ6WHiJeAVfGDwyAV",
                "tNULSeBaMuFCZAtkotpe3cRZfERsgjnTqdNV9a",
                "tNULSeBaMmzBF8HnUmPMSEtvctkVNHkvidE6YU",
                "tNULSeBaMqiYmoj3mYVG3LC4Tjv2H8ExUsehxB",
                "tNULSeBaMrEABVBxoexV9g9i1q7kcwgPS8WQ2e",
                "tNULSeBaMhbWaZBxfhWGL33hmfXGaVGoYzfLQh",
                "tNULSeBaMr9KPb7Fq7ozsoNbW26NFo3my6bBPU",
                "tNULSeBaMgMMSgBkUFABiHcUY5YJ2aYYpP8MPk",
                "tNULSeBaMftNjG2oGAMpau8FGCyB4bir6aVpXq",
                "tNULSeBaMgMLVVjAPNju9wJCGgA9jb22bByZWL",
                "tNULSeBaMf6wCKPCawDZw91soYKoMeXKEEWJnU",
                "tNULSeBaMgdv6woCcg3pMV4TdWNgS1Mxwsx1Mf",
                "tNULSeBaMpXaQPwYefmWAvGEBRBdH3jg8zajpY",
                "tNULSeBaMsFLSgzBk4mPsWoZmtSorJgVEywLcT",
                "tNULSeBaMqRS1hF1Yfj3KWNXCB2Zd5yd9uBjR2",
                "tNULSeBaMkXd7jqaFm9WsPoGyRLaw5M2QkHEG8",
                "tNULSeBaMooQgNrFgjDu9fiTccGsq6m7aM2AsJ",
                "tNULSeBaMfTnxS6Tq2BCdRaLjxKCNTZDEqrMFE",
                "tNULSeBaMfPy7ohmPwRgzd8PDD3Bfdqs9WtUbL",
                "tNULSeBaMu6diY2etjG4i6TrQF67Ubw5bXqVSU",
                "tNULSeBaMsRCtA1Yih9P2uxfgotSRf8VY59hEU",
                "tNULSeBaMj8GQENv4mvAUSJKEx6GbRWvPjJDmU",
                "tNULSeBaMfMBPQugTjTjd5Ab8PsGcuYiBmJvMR",
                "tNULSeBaMtuuqdGPPYrEK8RZyCVQjXmJ84h565",
                "tNULSeBaMvXrDk1Hn3W8oWTzCLucfT9yLHdYKx",
                "tNULSeBaMizh4FonspawLgK1XMNsRgC6LprThc",
                "tNULSeBaMkDGRXwcEYVMLTDT7xVRTvATMNFkDx",
                "tNULSeBaMkJzrVA5hLj1W98AzcKXhbU2hsq22f",
                "tNULSeBaMuEBjRJp5MhWW56idWiHCNNZHFZtYX",
                "tNULSeBaMhdAmLN9v1hP9m9AvyKyYzZiaZkTba",
                "tNULSeBaMuzw7vZe1FABLpfh9AgtDNknuuhjE9",
                "tNULSeBaMo3SAs2k4tZy78n3oARRA133aqd4Ce",
                "tNULSeBaMruemmdFvXBwgWcSg2iKCe5C4xCDnM",
                "tNULSeBaMjrTDP92hmRe8faLXu2e6iWAiDsZFe",
                "tNULSeBaMh3tmmoDe8AVEc6HrZ1qnvo1bbcjJC",
                "tNULSeBaMnvowU8YydagAn6DmPQnj4wXYpDEv8",
                "tNULSeBaMmQz2wUUKd8aCLF2x1HmmPq3vv4DpA",
                "tNULSeBaMuEPtJpLk2m7mqyEcJRe2U2USNfrEz",
                "tNULSeBaMpBy26qhhxUvRm3tyL2s4YHgKsA5Ha",
                "tNULSeBaMfjbz9pFkJCoh3R9EiNYETDqNpug7R",
                "tNULSeBaMfJiJnYh8GmxvcGXsyTTcs5DqtC7cf",
                "tNULSeBaMm6cnatbSay7A1L26gBVE9ietQVqTu",
                "tNULSeBaMhYVhLe2gDP1fqGMK6ScMy1x82yq4m",
                "tNULSeBaMruzibNvXwC7iRxHt5cHNbzQcU2iTf",
                "tNULSeBaMgYBARQE2evJny819CimVdnkSoPiZR",
                "tNULSeBaMh22BtYTPeX4m7HuxjhieBMa9zohVj",
                "tNULSeBaMkq6TgVTsApY3AKrg5Hi8oHBnM3jpN",
                "tNULSeBaMn9Q9WJR3H2T7cdt5ghDJfY7K4RMyC",
                "tNULSeBaMsXWvEh3wJVSokCdufPJ7ZZ7ii62Z4",
                "tNULSeBaMpfDDMPbnX8X2vVVsyTLJBA94p4Q9o",
                "tNULSeBaMgNAJmDtokL5SHwst7U5iYTExF1SkE",
                "tNULSeBaMoNJSFWdWrPg33maKUem6KMN3CoYdw",
                "tNULSeBaMueuUhttYGr5UDkNfgMyd7f3QoxhPY",
                "tNULSeBaMjJFZXJUsyDtk6T7YYqPz7bF71kZN2",
                "tNULSeBaMgTfXpTLUfaJFPWbcLcQdjAoximgwk",
                "tNULSeBaMtHQ5FQ3WXw6joV4MAzDAEzLqCvF7a",
                "tNULSeBaMuRmfMWfsFj9PRXmehk7ahYhzwwSgN",
                "tNULSeBaMnnfFkZ2xzzkwZwf6vtCBmT2SDhnLF",
                "tNULSeBaMsBf6cvpTQyVFoZ6zbasTSzfVzArQt",
                "tNULSeBaMgY4eRPtUUtNHva84fndrXKJD3RgZR",
                "tNULSeBaMu8EetPtED2NZpzw5Xh45HgHmLFiE1",
                "tNULSeBaMpu9UTCx7DGPx5ZnwwxysFLfvGXHjX",
                "tNULSeBaMjASP9LZS7fP7zsgf5pLdkyAk7kFz7",
                "tNULSeBaMhnJgEH7mDJCv6tPzzgkryhgFWYfXX",
                "tNULSeBaMijBJdwSfjCkfnPdTKikCawenoiRRi",
                "tNULSeBaMm8dpQ5rWzd62CT3FPH4N59yjwyYrM",
                "tNULSeBaMhMJrJCARRc7oNJXpberjVFJqEE23Z",
                "tNULSeBaMgw5ngJLNmtssxvWVH51TgD82mWPJF",
                "tNULSeBaMtSzWiypBDu379yZCJnKAeaHxUQ8Q5",
                "tNULSeBaMfx9jzsmBMpeAC3aQJMoj2DsBnG8s9",
                "tNULSeBaMtfGzvRk1VeEzV9rC8gdG5Ajkt9hbQ",
                "tNULSeBaMiAsAWtEV5BZ616Ra1aUMC2RAxATys",
                "tNULSeBaMhGZ4jfG7pwd219wBaDpyhfqr4dRJf",
                "tNULSeBaMsUH2TmnneAscjva8ZG2oinGFUna8S",
                "tNULSeBaMp2w4WaamEyTMzwGoi1kBh9J1qYBUL",
                "tNULSeBaMt9iMD3LWA9x3TV1i7DTbPhEzzucyZ",
                "tNULSeBaMv7KhdpkNdZtvw2Jwye9BComh5mNhz",
                "tNULSeBaMiBd7pSgiV3G3HhYH3HzuAE9qYvgHK",
                "tNULSeBaMsZBbyVddBKuaBaahx5Jye3vqDt3EU",
                "tNULSeBaMquM9XZQzJ1UjqdH3vejuA82PNqZTq",
                "tNULSeBaMtEVCpxxCyQFpFrw9Yy5P5SL9jYrzj",
                "tNULSeBaMgbwXahyVV3dkMCMi6JKApRv3BYMBZ",
                "tNULSeBaMoDhZ5KtUxCUcydXuVXqRJL2mZVh59",
                "tNULSeBaMfZwkqKtAz2xJ8Sdf8gCFYGZiTbRjs",
                "tNULSeBaMmqPUZfftqhuq9oau3GEXm9PqhTfL8",
                "tNULSeBaMtS1AqJsYZLoPoK61562sLZX3Rs89i",
                "tNULSeBaMgfMyrukha6RYmcKhnd5QXFUxyVbkZ",
                "tNULSeBaMfyuGNWtLqUNZyWFshk84JvZSmkPgh",
                "tNULSeBaMrSKe3eFvCy1YGvSHCCefV9ynQAfGZ",
                "tNULSeBaMhFrdDEcLHY3FTm91DwsvDhRifo76R",
                "tNULSeBaMtHf12yKU9wrcA5zaDhM72zEZ5GULo",
                "tNULSeBaMuXHDidBXs3Zyrv4t2Txtx6kUqMfhC",
                "tNULSeBaMhof9kRRJ9cTD1qpgazMiWyyBQzLDF",
                "tNULSeBaMhddEpJKf97L5raMhPCRxTtheLcRq1",
                "tNULSeBaMgheFSW4wV1kxCvCGn3JXxFFrD4S3b",
                "tNULSeBaMfbCwNW1FtjgJg4QTaSj3J9x5LMvbq",
                "tNULSeBaMgMpUfGnUm8W9bag2zWCAvZHhYwW29",
                "tNULSeBaMtxQK5QJ7auZtwrSnbDFKhKLsNPtDa",
                "tNULSeBaMnUfzCcopT8nGNH9Qrpj8Ed8iDsj42",
                "tNULSeBaMrM7u7uHFWQTu1vJ15JYCb6fCsciyn",
                "tNULSeBaMiKJuqVpSK6WACYjMcKY7y1CKMc55q",
                "tNULSeBaMm55vYpHP2UsiC5xxHWv8Rdxj7cBG5",
                "tNULSeBaMmjEtzXXNsRDuRDUn1SJwwoxcfgyKA",
                "tNULSeBaMvUjQJy7uMuQSRishuJD3YoU7ah3iE",
                "tNULSeBaMfizoyYNY7s3t9e8wrp5k82wBpTK7X",
                "tNULSeBaMi3vr4wCm97JUD1WAy3nRX85KK5Nxz",
                "tNULSeBaMhVizBH958eXqMYyGTHmKEGeoCFHh7",
                "tNULSeBaMqvwRCrA9Rnf2XaNRQ5S1ofypPzqt5",
                "tNULSeBaMtUjsUxZKDtMg2dkjLK2xM4VAY16xT",
                "tNULSeBaMpvJYYhoQkai4SweJQwquoRHKGjLJu",
                "tNULSeBaMqZftLMAss9Gp9xTtqtZzu97KL8kBY",
                "tNULSeBaMr8r9Gheq5Wfrs6xE1MWirxmLf2F2K",
                "tNULSeBaMuo8UM53rzsiuB4UBeGaQQEyEFRh42",
                "tNULSeBaMizfvUaE1j13FG95kU1h82EuNREYfu",
                "tNULSeBaMpWf6NFjmH4fLYP22LW8E9tkPuyJ1U",
                "tNULSeBaMgpska5Q5KAL51r1f9L21tn29xt9cZ",
                "tNULSeBaMidKU2EHui3SgLbZBvkPgWvysQkaAH",
                "tNULSeBaMuycHaW4JnW6yzaZra2okSBCw7q1n9",
                "tNULSeBaMpgrxd8rwV1QYaVoKT2uwPKjAczm9T",
                "tNULSeBaMnYVHpfvHR7wpeH8FkcZa6UKnPkHsE",
                "tNULSeBaMg1ZMaGnBPxk4vUe27Qo4z6sbbbFRM",
                "tNULSeBaMjZF3cT3g3cEkTeF9syPrAUGGv144q",
                "tNULSeBaMtmKTUJNxKGbjCxXrazTZanaDcoAjs",
                "tNULSeBaMf12mUMoHkqu6mCRfRVHYEBJm8XKe7",
                "tNULSeBaMghrB8jKVtvWFfD5Hhz9gvyNoAZdqe",
                "tNULSeBaMknvht7X8jpH1zVHA5XbNL6SJh84Va",
                "tNULSeBaMfYzoe3qHWwqAoQ8nzbiLN7HSGngvq",
                "tNULSeBaMm3NNsS5MAgssFaKSyLr1e23mpeW8E",
                "tNULSeBaMqZvocj1NABj1e7XyqLwzcDsxCKN9S",
                "tNULSeBaMtoDMjGjneU7swcnHt2nc66EAmxMUq",
                "tNULSeBaMfEgLeaUYtJ7xG5dhyGM3VinSqqGAU",
                "tNULSeBaMr4PBWiBTpeCVscsR6X92TosFCw1qj",
                "tNULSeBaMs3xrKvg81hwhKmNxa5hxE3EfCwhuT",
                "tNULSeBaMgKNMZzXj2RyAtGWmP1zFjH3GAVuro",
                "tNULSeBaMhsByLiVQmhfhvWgb882aByRLggNT2",
                "tNULSeBaMg18MoQyfVbRnFnzHFCDYf2MkAq7fk",
                "tNULSeBaMhbZVRRdtWebD6kQf5pAn3iESKjEMB",
                "tNULSeBaMkhmZxtsbggjha2P34dnHWKncQto4s",
                "tNULSeBaMiZLdwwzapMcuEaxqSDJwSWXYB5xFY",
                "tNULSeBaMoYM9Kus5bcyUYVsHpsiJai4R7tZrw",
                "tNULSeBaMvH6ihtMwo7RNykDdA1RefLfWbnG4k",
                "tNULSeBaMs2rrcd9dbkjrVSxxbrv7dJc18SNxa",
                "tNULSeBaMqM6xSprPpWc1t6Kvz2gzq2PvzW1b8",
                "tNULSeBaMuhMkb8gqszuRwgZLYWQ5giLePXiPa",
                "tNULSeBaMvS2ZDcwC5mhm1Z8MnqTBZZN9hxeXi",
                "tNULSeBaMiMw7veA1nccbem8YUaRxhLhoGkDzW",
                "tNULSeBaMnW8CmD3YkNhT8K4MuJpGHnwGvLdDd",
                "tNULSeBaMsgCEwk6ahqbtgtWumjYMoZqH7LMJ7",
                "tNULSeBaMqd5HzX1SdyX1hKtSodGMchwNH5Ugy",
                "tNULSeBaMiM6N4PBCRq7dzgc7jSQXgBd6DaBu5",
                "tNULSeBaMf27TjrLxUZLFyS6b7XDjd3qHhE3KD",
                "tNULSeBaMf4tEB78kT1RLFa4fNmuLF1dP1suS3",
                "tNULSeBaMhyh12cwj8s9j4p3k2vbxVY8pAgobF",
                "tNULSeBaMv28xs5a5aVkffWvrEZX4oXYUv8Par",
                "tNULSeBaMoswemXT9s4Hc11Hkqrrnub2YZ43Jy",
                "tNULSeBaMq6xB5WbZypvA5Koo1JiLrkmaGmhtV",
                "tNULSeBaMoaDYR833QRw7TYHNjbvDrdHGfDHgA",
                "tNULSeBaMsxj9hVcTaHtE9ia9Xne5WZFzNxv3x",
                "tNULSeBaMsiKomHaYN9GySJwREzDqFYoXtsV6H",
                "tNULSeBaMi268w6gegv7ze1RNFzFWnWvDECCod",
                "tNULSeBaMhQ5i2YU8VgX4cYwEVTe9pTnXBLBg1",
                "tNULSeBaMv9ZxbStYrjqxtVMY7sFuqGeDk2Y67",
                "tNULSeBaMgR9crjiyogJxUWRLJBdo3KMaUGvMm",
                "tNULSeBaMmyUve8bJqkdjPxkn26s6hcnJ2jiTa",
                "tNULSeBaMofQXJG7r1AxfSgNq7RcbVfmaWM9hm",
                "tNULSeBaMrxEqZN3B87cc4or5JrMrFctWHugQs",
                "tNULSeBaMgt6aywG28cCrkyRyikjtvHNnR1z6M",
                "tNULSeBaMupi4TjHuVaYhPCBBHd6KCukKGYp7Z",
                "tNULSeBaMv3CRbH4F1vV6e3qpREjmQHv54SNZ4",
                "tNULSeBaMudQKrUMSAiWTShQWssR5RtKKyY6gh",
                "tNULSeBaMpUpbexMqFT5zVoCWugkB97YHxMZSr",
                "tNULSeBaMgJyxXbDdW29PSWmwYPbLwhfwyyTNr",
                "tNULSeBaMvamQWa44W8H4VMq6QPKYawiHdrNb3",
                "tNULSeBaMfxwMtPwq8AuAPHbMhxD1hxoTkEV5U",
                "tNULSeBaMrQ69hyHDUhP2etZFL6jhhj6Sqk9aD",
                "tNULSeBaMg35vLn53Mvo5dG9YYpBoFZ9RXqBbB",
                "tNULSeBaMpLcjZbQbGsBmLCFBmxLSNZxAex7va",
                "tNULSeBaMqM92ndgqbzYGkVnJc336HEjUyeV4r",
                "tNULSeBaMsqGLd57akrmbnCnBawwhTv6NKw6uV",
                "tNULSeBaMkZwMcC89Vv9WckRniR5CT3PQ8ErxC",
                "tNULSeBaMjcbkeidMg8G9YFj1vTXej7sCiQhk4",
                "tNULSeBaMhbfJcMxjZTKsMafPjKzi2gU33KU3Y",
                "tNULSeBaMmbsKWL3iUJ7LPGghr8FjXN9TUCTwF",
                "tNULSeBaMuP67yCUYDDHvjskhJN52jNQHtP5HF",
                "tNULSeBaMvKWcqqWgbyUBFrRsVA1MStZ9TSsMY",
                "tNULSeBaMkuChtQpvRBA5ShRkWrJBd6j4Z9XSK",
                "tNULSeBaMopbSDde1rfXGu7FSgSDozWMyQf3QW",
                "tNULSeBaMpWrvxwBLxmeshAKtPs5N5rhMoc58N",
                "tNULSeBaMsGycScR38YmCbWq9mFj5VHkiUGr53",
                "tNULSeBaMrnfV2iQJmXfnyU2UhoBSmwHAwxNCn",
                "tNULSeBaMidZ7Ho6BR588JkUUMJMgbbJ88drdn",
                "tNULSeBaMun4bFpXbZqdF78fHvntCg9eMXLQJK",
                "tNULSeBaMu4CCBaL9nPKQxiSfmLZPmD2VgXCq6",
                "tNULSeBaMfS9oqnbheFs47t7gMLRGsXKYaT5ux",
                "tNULSeBaMkk3puk5FAtWbC4vgwGXtUUQ6gKGGD",
                "tNULSeBaMrPtJ7RLRGyVyiS8HjA95HtHauyeaU",
                "tNULSeBaMnrEM7Nphmh5JARXTHFJSXqNUNX7En",
                "tNULSeBaMqiF6W1seQ8uG1cCM4bSGhwB6GfHfJ",
                "tNULSeBaMsBkgVQru3LmiWWKCapdJoAYnhmfsj",
                "tNULSeBaMv233MWeNjRVEFTL3zDeXBMoGKxwPg",
                "tNULSeBaMgfcsurJVTeg1UJvkYabuegt6Lii6E",
                "tNULSeBaMjusbPyEeckUpRc3XBSTGZgBCPNmXn",
                "tNULSeBaMfSsSNdFmudaUiGmJnvW2kGivynxQv",
                "tNULSeBaMkgpyKqtvcp1zuf45zyUTj5RxLqsvK",
                "tNULSeBaMqA3PKhj1QrFGQVSb4hkDH5e4RBMkV",
                "tNULSeBaMpohhiLvANN4qsC2xx3rryWCy5n3Ki",
                "tNULSeBaMvPU3yJsCcpYLDNYSYq65MKuH1LGDQ",
                "tNULSeBaMiPAG2xtxQvUoT2MU4ubF4xayKNwLg",
                "tNULSeBaMvCDVQK9RiZi9UpyrwenVB5w1kMWa4",
                "tNULSeBaMnSmvxY3yoyeVkzHqZcAYbMFXyd7nq",
                "tNULSeBaMoXLZFkTAXC2xLJ6egfNt7jV4hWKz4",
                "tNULSeBaMfd1dtyWM1DXxTGqMhkCTry1cejeMX",
                "tNULSeBaMoZxxS8bDYyFpezhC3ubzi9vyeb5Pb",
                "tNULSeBaMg9gogmpM9zmDTPhqcdytAUQtfMzJi",
                "tNULSeBaMkNDHtep1AsHev7x5tomTj46EPp1NT",
                "tNULSeBaMkR6Rrh3G99V11vtLy12buoPSrpshw",
                "tNULSeBaMk288x2B7wYwn3RkCST5KdbRZR3ohH",
                "tNULSeBaMfXYsjhdBziVGfWdczMuAFYW2mFCW5",
                "tNULSeBaMoccXEUXpb1vFa2UAbCeaFuuPMPrRA",
                "tNULSeBaMhLTxx3uXWFUWVgaGrWCd2tM14Y38a",
                "tNULSeBaMrBy1a6S9YDw6tKZh716RgKdqdmEwu",
                "tNULSeBaMutky8ShkD4t6Kq8GC7B55xVAHsAyn",
                "tNULSeBaMtPhi2DcnBpefXE7UxsHvfybfgGQ7q",
                "tNULSeBaMqPTdgo6C7xHAXx2gYbz3a1U5qso5g",
                "tNULSeBaMpKYE7dnnm7aQ8eB3XesaKTEFc47kd",
                "tNULSeBaMfLEVV2TWnUehhPTUeM7yDTXTfVpF1",
                "tNULSeBaMqEJF6QPaYuUajPuQdGHvco11tv1J9",
                "tNULSeBaMp5PUGC9BVnp5XVde4jDp2sSPCi6ao",
                "tNULSeBaMkmuk7gxLtiiKi54e4h9QK9SFBCbzZ",
                "tNULSeBaMt1NM7US9gw24a6hG3evStCJDvaiwP",
                "tNULSeBaMuo686xNFt4nD8143urcyt414M3KyU",
                "tNULSeBaMgqLEBx1tZAeSm2fbyc81CgzprzRXD",
                "tNULSeBaMjU52P16LdDVeCRB83QnLAGSEzvkb8",
                "tNULSeBaMtdhs9fib5geZbDU5LFPt5NQvAmrRm",
                "tNULSeBaMomweQLFNWg8VTxCstaXnLTQEeESun",
                "tNULSeBaMnAaWMHkuwkMhSJ9KCuEF2jvCLxWAs",
                "tNULSeBaMpTnCUDnEsjBtybSauaFmBuUWfKi34",
                "tNULSeBaMpkNUr1rtJaSSUdrL5PNNasen3UX2n",
                "tNULSeBaMrQ4RTH9tRcq18YqfVoVVkkGUR3RAz",
                "tNULSeBaMkyLxd5Kz35CLDMiFWqQKzhuym1Q7s",
                "tNULSeBaMh3gzFC8fevdxUCcapk1SD8crefZMq",
                "tNULSeBaMj6tYP3KCNr8X44JhW6gbsqCvYprrb",
                "tNULSeBaMiSyUYhtD4cwyuLPHfhu4n6Gz6GVan",
                "tNULSeBaMsVzThaSfSWdnPgMTi2pwi5GmTWvSo",
                "tNULSeBaMuwLsHXr5qLNFtYTQBz5CvqHP3PxsH",
                "tNULSeBaMos6bvo5TTfcV2QfvWswASSudM5umB",
                "tNULSeBaMkBZX43dyCmqn7yTET13FxXmH2HgXf",
                "tNULSeBaMhtgtZnR9mchs5dczzv5f2Tt4enMRu",
                "tNULSeBaMfnfGBg3mKNFPPcRyhiVuBk5cnN4ps",
                "tNULSeBaMkEoS4ZmjZSaMter65Ymt24xcugqZq",
                "tNULSeBaMqCAePM1PwdEoXr6JBAwk5WCnhR7ZB",
                "tNULSeBaMkUGZLujTyHcsw1jNE4aJkTGXuvEjv",
                "tNULSeBaMkRTrR938eQcyRRNzm3VWwtwz8zY1J",
                "tNULSeBaMtkj5uxyJjU8m8SCorEBYaxeutjKtM",
                "tNULSeBaMpx7j9bTCbJF6PfV3AuHR5Yn7wu4uu",
                "tNULSeBaMsvqNMaXTrLjt4nxMuL7t8upB5RWLS",
                "tNULSeBaMgt6krz7z99fzUSAZ7FAyFePQbExi2",
                "tNULSeBaMn9fQTBKpzhjYD2LXzv2r3h53ih5Yd",
                "tNULSeBaMfzSPa2vXzyupdAxQ53tZFpFpfDd98",
                "tNULSeBaMuqLRuLPvvBrx5dfvNwx9nuJoCWxwa",
                "tNULSeBaMpNyFcbHLuNpRrozkDcWT9Vs42UXtw",
                "tNULSeBaMkhp5DjGah73ZErzvDYbVcdpN9om6V",
                "tNULSeBaMgKZwb2eGA4WnCjVKwWLN6BkEg1B9r",
                "tNULSeBaMssAytfdLFkMJDstHdCg76pbUTfpEq",
                "tNULSeBaMmEhcu5bgVXHLkgtthwpGJ45qgw2G1",
                "tNULSeBaMohv8aZ5L9heVV8efzLRzJ72aijhrv",
                "tNULSeBaMfJFtc328ZpAAsF5DQYyTR4riV8z7T",
                "tNULSeBaMuni6sNzoYGhmUPcX4PpsuaRy4Xpwt",
                "tNULSeBaMijPRRbTmwRXztM2o6JyA3sVUqHzc4",
                "tNULSeBaMk3pXFivBJnqTanTSr6PhkqkV2Uy2y",
                "tNULSeBaMiAoMVzVTUvovrYQqYATQUPayQ5MJK",
                "tNULSeBaMf5GyiiuJy1BAQJvbZZM8hhWZP4PP7",
                "tNULSeBaMoFG4fY86wPHonNHLDg45Xa96SS5VC",
                "tNULSeBaMhrPV8PGTNiRgiB32bDuQyC6SwxPqu",
                "tNULSeBaMhURiJqXwWgyPk2wRRm7pqHZuvfbVd",
                "tNULSeBaMhz6NZPzVgJHpy6rHgPE2zgh48WFnG",
                "tNULSeBaMtkTjELaLRzeRRHsvKPeSMJi7Z9tJp",
                "tNULSeBaMijKLJ1n2oWxcUKSFoDjKWaYUcXH2P",
                "tNULSeBaMptdNscecVgr23sSJgx8UVkeszcHXZ",
                "tNULSeBaMohGQaJAYVcS9ovpqgF6yMtmxdqDLC",
                "tNULSeBaMoi6NrBu3KQRttsVkeASjmyDGkPRK3",
                "tNULSeBaMrtS5P4v4cRRYYXKBgTmFNY4GXdfe7",
                "tNULSeBaMhQzJtn3MCepoMngAR9wo9mnQ4puzX",
                "tNULSeBaMuCifcksew966298LKdDRXA1wycEri",
                "tNULSeBaMtzwTv9rg4CyuXEZahZiNs39Xttczo",
                "tNULSeBaMp3e3uiWuYirWKaoFHYuAd3WLyatha",
                "tNULSeBaMhkT38YVfzWNkn7S3eMjtwv7QahJFz",
                "tNULSeBaMuFhEHyZRm7mXM5QxqsapyVVCgJEjh",
                "tNULSeBaMiLjsQuPFdc8QRURYU7xXopxTZkocU",
                "tNULSeBaMfS9D3JnxchZhZA8AdsnRayVnuwJnX",
                "tNULSeBaMqFuDcfCv8WKsBWuq5YUwLdbHXxFCk",
                "tNULSeBaMjHP5AhreEeeNwQpphdcdLxg1eqBqH",
                "tNULSeBaMj9TX5KMzg11UWXy465Lpqh883dvvF",
                "tNULSeBaMjKLiKXkDTdRSQnN8JXNmnA6rA2wYQ",
                "tNULSeBaMkC1mkMSPNUj13ifmyXNtgpcsumi5k",
                "tNULSeBaMuNiUecuanV1BqfwhH2t5NJKEhj1kC",
                "tNULSeBaMmeMaztowhBqNqrJQfqw45bqtvn1Zp",
                "tNULSeBaMkkz2UssG7yxnHxR7N9rUnKgBe2rQo",
                "tNULSeBaMfEfx7Xcvmx5Bb7SKuiCK9Fdsykqo2",
                "tNULSeBaMmKqEgQ767VThm6V6HGEfTptnjCdtM",
                "tNULSeBaMhfB4zxVA9zgDyk4yge3wKC1CvXuWd",
                "tNULSeBaMvhvohninNhfpgULP8WScNAxE8BeMM",
                "tNULSeBaMnJ32Pjfv55p5i3hundAPbchj51fnJ",
                "tNULSeBaMfBE3wSLpKV92NgToDWjo7Kr913smW",
                "tNULSeBaMpM4qi41GKv4yMbXRWSbAv9YAjhH9V",
                "tNULSeBaMpQzCGEaqfhmKavA5AF7E5Ce4qGMdH",
                "tNULSeBaMfwbqWYHBPRQHRJPaDsd2TvGGTebRz",
                "tNULSeBaMhMMAnJgAsBvRXfS3tNHT2xL3GPzS1",
                "tNULSeBaMgcfNog4eZNBksbu4EKb4PZCdswkjE",
                "tNULSeBaMfAb9GAUVNYnqJWFLxsg7C8xRkAkRd",
                "tNULSeBaMsLpWDcg5R3vowsKztHh7gVUn1bC9X",
                "tNULSeBaMjogD7thpjmZh8BixJueriZq5k5XQz",
                "tNULSeBaMuiY7Zc4FEDy9Z7uGjUPPQHUEMZKb9",
                "tNULSeBaMmiHhcHAFU4sLxCPmRr5dN9CdmbkXP",
                "tNULSeBaMrwn7iPKPrhg1QAZS159K1JVqxxQNf",
                "tNULSeBaMpCrMVnsKjwjnfTjuZUPf8e6PnuuUq",
                "tNULSeBaMiiszLkUSrnsd3JDpgZskHuGrETm8R",
                "tNULSeBaMuzDEePTnDmcDjXPchmqGJhY3D6MMQ",
                "tNULSeBaMnWNYKQLt18vTmenen7dMtoVeYmNM6",
                "tNULSeBaMsMi8cuhiupNzN6nqeB4DoA9N6EcGZ",
                "tNULSeBaMoc6ubuH55G9tuTP2XHqyfKnvJwD8R",
                "tNULSeBaMu8dtKhsDYxqY6uYSQsVaAwWEZCgRV",
                "tNULSeBaMrzybT5UGTm4BUJ9aaYhLmb4rh5WXG",
                "tNULSeBaMgPqUswe8YJBtJohogDsJ41maPwFcV",
                "tNULSeBaMjLBEjhgG4ih3jp485V3cMbLMJWpt2",
                "tNULSeBaMu29D8wZngo8XB7sjssmUsdGfnRjxL",
                "tNULSeBaMiCMKBTrWBFAUF1CN1iinQ8wKn9dWo",
                "tNULSeBaMokM7Fxmcq4GvWW5wz7YdznQYzeZZb",
                "tNULSeBaMjpMgj3kWEzmsbHbA1f2aZcQXs2cZb",
                "tNULSeBaMtH56mw9tZeaikWac94kSk6w4jM3Nc",
                "tNULSeBaMfc7iW65qqEn2PgRg2f6ch6yPeKYJM",
                "tNULSeBaMk1HEkBZ3G75hm1kYG7NqEvfDZXfyp",
                "tNULSeBaMos7mZoNNaYQjHnGvwYnR6RrBJvsaM",
                "tNULSeBaMrpHFChkjruf7AoDCQKb8zj5xU83Wq",
                "tNULSeBaMjwJi6wqQNGFCi6mSbf7XXPbZHwR3X",
                "tNULSeBaMphzU1eVrcApemwcMgyPYB15LkVWFv",
                "tNULSeBaMnDMhAFQdtY13kBbVwRgYc2y4gTt5P",
                "tNULSeBaMtgzLvfhFzC4gGBXbxoN13oiWZ3jKF",
                "tNULSeBaMhrk9x4s9gz4k9aMsLLgpjrUhsKQPk",
                "tNULSeBaMs3XhfT8bKj1pQkbbpbDxhJshU8Bms",
                "tNULSeBaMuaR6BWsv2xUuayBLdVz1iD4QgxDyN",
                "tNULSeBaMnAFVLMZvQ7YjAoZuQH6twNgZ7xvHx",
                "tNULSeBaMkG3mw83qz4cGmk4QQTTTFa5wi9ErM",
                "tNULSeBaMuYmpj46iJS3NVMi3An76X5favWq91",
                "tNULSeBaMfsqu6DitDnSsXhsckymgK6DEKcdo6",
                "tNULSeBaMtc8Fhqty3JHaH5LAcNY3igWoCL36S",
                "tNULSeBaMpp4ULknRMEYqhr5SP5PqPYRSTXHQX",
                "tNULSeBaMoKPYT2hoWR278QJPiY63yqr9jZwNH",
                "tNULSeBaMhKUK75s5cJaKhfoxVBk1zEVAy6uPE",
                "tNULSeBaMuE1V5FGdCzDWSyNADHvEzQp9WVVWr",
                "tNULSeBaMkgrW1YbfqCBtvim7GTLg1gD951YmD",
                "tNULSeBaMkvgzDNvbKQE2Ud4b6kF8SVvSoU1p1",
                "tNULSeBaMsUabrmoScrKjvmtYFA4azixj8u8GR",
                "tNULSeBaMhLqyX99W416zyHHys53U3RFth9EWg",
                "tNULSeBaMfXVdvvahpD7Fwtbb74xUvFcUiqbrV",
                "tNULSeBaMmsQG2EKsiZNZMFEjU9qpDkhK8tdJX",
                "tNULSeBaMkH4m6Sup4qGYXjcKfbf7UF8iebs42",
                "tNULSeBaMrK7gfG7VGbKbHWZKuKY9rSBaxqg5j",
                "tNULSeBaMhA5PAxwwaZd5RJ6oXr9G1Xai7nRYJ",
                "tNULSeBaMr1LbjvMJNCUUEN6XSeFS4BdEKBh64",
                "tNULSeBaMiBw3N9z8JrEDJYJmoPRZeeAnBGAfr",
                "tNULSeBaMvVuGLsGmJgDhswrFtmKZZo7Jc9ntM",
                "tNULSeBaMp8565GyTMLfjUK1zPF4kpQxGu7sox",
                "tNULSeBaMmzh4VwfCHSqd7skUSovswM7yrGEB6",
                "tNULSeBaMoEQiZeUa7vmxVvHhoN3CiW6EE1EBj",
                "tNULSeBaMtEoh2mkJVqXRRyPpVc6BSEFgs6gHD",
                "tNULSeBaMgDvLKMCTjUyPxxxqWZ6xUK32y9TKe",
                "tNULSeBaMgpoHU59Mq3ifJsbZfpBjwhpbjmLJc",
                "tNULSeBaMsjH2R7BZxLYxi5rgvEq5XfXyz9ksq",
                "tNULSeBaMi3H2Ehx7t3hPWxwz6xmm2KcfpobMA",
                "tNULSeBaMp5QfGMX3oecRmcSAE6xv8EwudSGt8",
                "tNULSeBaMhuGukadWXdoWevE5t3MNhQKAsHiFF",
                "tNULSeBaMqPU4pq61cmBxW9mBeDojYLur2z9WT",
                "tNULSeBaMftNQKemwEdFBwZBEsVoXjnBAkyHvP",
                "tNULSeBaMf2gRfqsqRU8Ke1198iFug4C5vN3os",
                "tNULSeBaMuvvYDji2EzMFaV2gbtchRmDgEK7fR",
                "tNULSeBaMiE5kntKZkGsA4qRuknbg2da3w33KL",
                "tNULSeBaMmAeo9281qaVWLAW8GT1dmgkrKPa2P",
                "tNULSeBaMguNdeYBDtH3t5z6LT1fbwiLCd9iUA",
                "tNULSeBaMnuJwi1KzWG6Sd6rtHbafZmPBuFCDx"
        });
        tx.setTxData(data.serialize());
        tx.setTime(System.currentTimeMillis() / 1000);
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

        String[] pubkeys = new String[]{
                "0225a6a872a4110c9b9c9a71bfdbe896e04bc83bb9fe38e27f3e18957d9b2a25ad",
                "029f8ab66d157ddfd12d89986833eb2a8d6dc0d92c87da12225d02690583ae1020",
                "02784d89575c16f9407c7218f8ca6c6a80d44023cd37796fc5458cbce1ede88adb",
                "020aee2c9cde73f50c5e2eef756b92aeb138bc3cda3438b31a68b56f16004bebf8",
                "02b2e32f94116d2364af6f06ae9af7f58824b0d3a57fca9170b1a36b665aa93195"};
        List<String> pubkeyList = Arrays.asList(pubkeys);
        List<byte[]> collect = pubkeyList.stream().map(p -> HexUtil.decode(p)).collect(Collectors.toList());
        MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
        transactionSignature.setM((byte) 3);
        transactionSignature.setPubKeyList(collect);
        tx.setTransactionSignature(transactionSignature.serialize());

        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        List<String> priKeyList = new ArrayList<>();
        priKeyList.add("???");
        for (String pri : priKeyList) {
            ECKey eckey = ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(pri)));
            P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByEckey(tx, eckey);
            p2PHKSignatures.add(p2PHKSignature);
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        }
        tx.setTransactionSignature(transactionSignature.serialize());
        System.out.println(HexUtil.encode(tx.serialize()));
        //Response response = this.newTx(tx);
        //System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    @Test
    public void txMultiSignTest() throws Exception {
        String pri = "???";
        String filePath = "???";
        String txHex = IoUtils.readBytesToString(new File(filePath));
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(txHex), 0);

        MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
        transactionSignature.parse(tx.getTransactionSignature(), 0);

        List<P2PHKSignature> p2PHKSignatures = transactionSignature.getP2PHKSignatures();

        ECKey eckey = ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(pri)));
        P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByEckey(tx, eckey);
        p2PHKSignatures.add(p2PHKSignature);
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        tx.setTransactionSignature(transactionSignature.serialize());
        System.out.println(HexUtil.encode(tx.serialize()));
    }

    @Test
    public void getAllBlockAccount() throws Exception {
        // ac_getAllBlockAccount
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAllBlockAccount", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }

    @Test
    public void accountUnBlockTest() throws Exception {
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        chain.setConfig(configBean);

        Transaction tx = new Transaction();
        tx.setType(TxType.UNBLOCK_ACCOUNT);
        CoinData coinData = new CoinData();
        String fromKey = "477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75";
        byte[] from = AddressTool.getAddress("tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD");
        byte[] nonce = TxUtil.getBalanceNonce(chain, assetChainId, assetId, from).getNonce();
        if(null == nonce){
            nonce = HexUtil.decode("0000000000000000");
        }
        coinData.addFrom(new CoinFrom(
                from,
                assetChainId,
                assetId,
                new BigDecimal("0.001").movePointRight(8).toBigInteger(),
                nonce,
                (byte) 0
        ));
        coinData.addTo(new CoinTo(
                from,
                assetChainId,
                assetId,
                BigInteger.ZERO,
                (byte) 0
        ));
        tx.setCoinData(coinData.serialize());
        AccountBlockData data = new AccountBlockData();
        data.setAddresses(new String[]{
                "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24",
                "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD"
        });
        tx.setTxData(data.serialize());
        tx.setTime(System.currentTimeMillis() / 1000);

        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        //根据密码获得ECKey get ECKey from Password
        ECKey ecKey =  ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(fromKey)));
        byte[] signBytes = SignatureUtil.signDigest(tx.getHash().getBytes(), ecKey).serialize();
        P2PHKSignature signature = new P2PHKSignature(signBytes, ecKey.getPubKey()); // TxUtil.getInstanceRpcStr(signatureStr, P2PHKSignature.class);
        p2PHKSignatures.add(signature);
        //交易签名
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        tx.setTransactionSignature(transactionSignature.serialize());
        Response response = this.newTx(tx);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    @Test
    public void accountUnBlockMultiSignTest() throws Exception {
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        chain.setConfig(configBean);

        Transaction tx = new Transaction();
        tx.setType(TxType.UNBLOCK_ACCOUNT);
        CoinData coinData = new CoinData();
        byte[] from = AddressTool.getAddress("tNULSeBaNNgHMQAwzaJU4rtXD4WEhiRrnrnZWo");
        byte[] nonce = TxUtil.getBalanceNonce(chain, assetChainId, assetId, from).getNonce();
        if(null == nonce){
            nonce = HexUtil.decode("0000000000000000");
        }
        coinData.addFrom(new CoinFrom(
                from,
                assetChainId,
                assetId,
                new BigDecimal("0.001").movePointRight(8).toBigInteger(),
                nonce,
                (byte) 0
        ));
        coinData.addTo(new CoinTo(
                from,
                assetChainId,
                assetId,
                BigInteger.ZERO,
                (byte) 0
        ));
        tx.setCoinData(coinData.serialize());
        AccountBlockData data = new AccountBlockData();
        data.setAddresses(new String[]{
                "tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29",
                "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM",
                "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL"
        });
        tx.setTxData(data.serialize());
        tx.setTime(System.currentTimeMillis() / 1000);
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

        String[] pubkeys = new String[]{"02f01ab55fff126dd22f7b13671829c1663a167f62553d9ac9a490785c72b38f42", "02721315241ba2511f757fffea4534fdef912e8c74a4f0df416809c9080a5393e6", "037623bbe485c3089180722114b524ec72a75a4f055b82ab25e28b5f03619d86cd"};
        List<String> pubkeyList = Arrays.asList(pubkeys);
        List<byte[]> collect = pubkeyList.stream().map(p -> HexUtil.decode(p)).collect(Collectors.toList());
        MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        transactionSignature.setM((byte) 2);
        transactionSignature.setPubKeyList(collect);

        List<String> priKeyList = new ArrayList<>();
        priKeyList.add("dc9514bbb1b19337f39f2b90b39a4087a531e842727c3a1fa77c2e20fb8c7ce5");
        priKeyList.add("a4757aebd10331b52fd8bc3a4c79ac025187bc65f6c4d944a07f7b77a9ff9161");
        priKeyList.add("d5aa0f4b360a913fb01fd257e22a67916ac842467fb241ca692a60e4d85511b3");
        for (String pri : priKeyList) {
            ECKey eckey = ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(pri)));
            P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByEckey(tx, eckey);
            p2PHKSignatures.add(p2PHKSignature);
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        }
        tx.setTransactionSignature(transactionSignature.serialize());
        Response response = this.newTx(tx);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    @Override
    public void run() {
        try {
            NulsHash hash = null;
            for (int i = 0; i < 1; i++) {
                hash = transfer(hash);
                System.out.println("count:" + (i + 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NulsHash transfer(NulsHash hash) throws Exception{
        //Map transferMap = CreateTx.createTransferTx(addressFrom, addressTo, new BigInteger("1000000000"));
        Map transferMap = CreateTx.createAssetsTransferTx(addressFrom, addressTo);
        Transaction tx = CreateTx.assemblyTransaction((List<CoinDTO>) transferMap.get("inputs"),
                (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
        newTx(tx);
        LoggerUtil.LOG.info("hash:" + tx.getHash().toHex());
//        LoggerUtil.LOG.info("count:" + (i + 1));
//        LoggerUtil.LOG.info("");
//        System.out.println("hash:" + hash.toHex());
        return tx.getHash();
    }


    private Response newTx(Transaction tx)  throws Exception{
        Map<String, Object> params = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
        params.put(Constants.VERSION_KEY_STR, RpcConstant.TX_NEW_VERSION);
        params.put(RpcConstant.TX_CHAIN_ID, chainId);
        params.put(RpcConstant.TX_DATA, RPCUtil.encode(tx.serialize()));
        return ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
    }
}
