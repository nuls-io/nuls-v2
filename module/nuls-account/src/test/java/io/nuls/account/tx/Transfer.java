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
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
    public void createAccount() throws NulsException {
        Account account = AccountTool.createAccount(1);
        System.out.println(HexUtil.encode(account.getPriKey()));
        System.out.println(HexUtil.encode(account.getPubKey()));
        System.out.println(account.getAddress().toString());
    }

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
        //String fromKey = "???";
        //byte[] from = AddressTool.getAddress("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        String fromKey = "???";
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
        chainId = 1;
        assetChainId = 1;
        assetId = 1;
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        chain.setConfig(configBean);

        Transaction tx = new Transaction();
        tx.setType(TxType.BLOCK_ACCOUNT);
        CoinData coinData = new CoinData();

        String fromStr = "NULSd6Hh5e4o3N3y6FnHeGhECFXrETqSzbapx";
        byte[] from = AddressTool.getAddress(fromStr);

        byte[] nonce;
        RpcResult request = JsonRpcUtil.request("https://api.nuls.io/jsonrpc", "getAccountBalance", List.of(chainId, assetChainId, assetId, fromStr));
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
        //File file = new File("/Users/pierreluo/Nuls/address_block_finally");
        //List<String> list = IOUtils.readLines(new FileInputStream(file), StandardCharsets.UTF_8.name());
        //System.out.println("read length: " + list.size());
        //Set<String> set = list.stream().map(a -> a.trim()).collect(Collectors.toSet());
        //System.out.println("deduplication length: " + set.size());
        //data.setAddresses(set.toArray(new String[set.size()]));
        data.setAddresses(new String[]{"NULSd6HgV3H5FecBPdXiY7r9d9cQa79KhaezY"});
        tx.setTxData(data.serialize());
        tx.setTime(System.currentTimeMillis() / 1000);
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        System.out.println(String.format("交易大小: %s", tx.size()));

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
    public void accountUnBlockMultiSignTest() throws Exception {
        chainId = 1;
        assetChainId = 1;
        assetId = 1;
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        chain.setConfig(configBean);

        Transaction tx = new Transaction();
        tx.setType(TxType.UNBLOCK_ACCOUNT);
        CoinData coinData = new CoinData();
        String fromStr = "NULSd6Hh5e4o3N3y6FnHeGhECFXrETqSzbapx";
        byte[] from = AddressTool.getAddress(fromStr);

        byte[] nonce;
        RpcResult request = JsonRpcUtil.request("https://api.nuls.io/jsonrpc", "getAccountBalance", List.of(chainId, assetChainId, assetId, fromStr));
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
                "NULSd6HgV3H5FecBPdXiY7r9d9cQa79KhaezY"
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
        String fromKey = "???";
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
    public void blockTest() {
        Set<String> nodes = new HashSet<>();
        for (int i=7396000;i<7399782;i++) {
            System.out.println(String.format("load block header: %s", i));
            RpcResult request = JsonRpcUtil.request("https://api.nuls.io/jsonrpc", "getHeaderByHeight", List.of(1, Long.valueOf(i)));
            Map result = (Map) request.getResult();
            String packingAddress = (String) result.get("packingAddress");
            Integer blockVersion = (Integer) result.get("blockVersion");
            if (blockVersion.intValue() < 11) {
                nodes.add(packingAddress);
            }
        }
        nodes.stream().forEach(n -> System.out.println(n));
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
