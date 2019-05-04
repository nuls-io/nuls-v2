/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.tx.base;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.transaction.TransferService;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2018/12/4
 */
public class BaseQuery {

    @BeforeClass
    public static void initClass() {
        Log.info("init log.");
        ServiceManager.init(chainId, Provider.ProviderType.RPC);
    }

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        chain = new Chain();
        chain.setConfig(new ConfigBean(assetId, chainId, 100000000L));
    }

    protected TransferService transferService = ServiceManager.get(TransferService.class);
    protected Chain chain;
    protected static int chainId = 2;
    protected static int assetId = 1;

    protected static String password = "nuls123456";//"nuls123456";

    protected String sender = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    protected String toAddress0  = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    protected String toAddress1  = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    protected String toAddress2  = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    protected String toAddress3  = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    protected String toAddress4  = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    protected String toAddress5  = "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM";
    protected String toAddress6  = "tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29";
    protected String toAddress7  = "tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf";
    protected String toAddress8  = "tNULSeBaMmbiCH5soCFasXnG4TwqknyTzYBM3S";
    protected String toAddress9  = "tNULSeBaMsUBLVxwoaswjWvghJyoUJfbfB6dja";
    protected String toAddress10 = "tNULSeBaMi5yGkDbDgKGGX8TGxYdDttZ4KhpMv";
    protected String toAddress11 = "tNULSeBaMqjttJV62GZ1iXVFDBudet3ey2aYSB";
    protected String toAddress12 = "tNULSeBaMgTcqskhNrE1ZSt3kZpdAv6B83npXE";
    protected String toAddress13 = "tNULSeBaMjcximfy1JEGzjxodNMjrjydWuiffr";
    protected String toAddress14 = "tNULSeBaMfMk3RGzotV3Dw788NFTP52ep7SMnJ";
    protected String toAddress15 = "tNULSeBaMj8XfWDjyKHZ1ybC3ShR8qKGyVKRcb";
    protected String toAddress16 = "tNULSeBaMqwycXLTWtjexSHHfa4jDTrVq9FMWE";
    protected String toAddress17 = "tNULSeBaMoixxbUovqmzPyJ2AwYFAX2evKbuy9";
    protected String toAddress18 = "tNULSeBaMvaRhahBAYkZKQFhiSqcC67UiRzoSA";
    protected String toAddress19 = "tNULSeBaMuk5jx12ZXhaf5HLgcAr3WCwUhRGfT";
    protected String toAddress20 = "tNULSeBaMqjT3y9bGz4gBeJ7FJujmxBDTGdNp1";
    protected String toAddress21 = "tNULSeBaMobzkpUc1zYcT67wheRPLg7cmas5A6";
    protected String toAddress22 = "tNULSeBaMjXxVzqB4T7zFoykRwfSZSD5ptAn4A";
    protected String toAddress23 = "tNULSeBaMpaiBiMHWfAeTzdXhnfJXPfwXwKikc";
    protected String toAddress24 = "tNULSeBaMrL5netZkTo9FZb86xGSk47kq6TRBR";
    protected String toAddress25 = "tNULSeBaMk52mfhacRWkmB98PrwCVXuEzCdQuk";
    protected String toAddress26 = "tNULSeBaMiKWTid5Gj3FoqBFP7WomUzgumVeKc";
    protected String toAddress27 = "tNULSeBaMvGmZSrFyQHptSL9yBCNSDfhWoxEHF";
    protected String toAddress28 = "tNULSeBaMkzsRE6qc9RVoeY6gHq8k1xSMcdrc7";
    protected String toAddress29 = "tNULSeBaMfXDQeT4MJZim1RusCJRPx5j9bMKQN";
    protected String toAddress30 = "tNULSeBaMj7QaB8mYBBvkhaT3jCrXEMCEcRfb1";
    protected String toAddress31 = "tNULSeBaMtgmrSYu98QwP1Mv8G5FwaMDkWSkuy";
    protected String toAddress32 = "tNULSeBaMh4VafNqp5TJSmV5ogdZviq1nbXBSu";
    protected String toAddress33 = "tNULSeBaMfCD8hK8inyEKDBZpuuBUjLdiKgwnG";
    protected String toAddress34 = "tNULSeBaMvQr8dVnk3f3DPvwCYX3ctTRtrTurD";

    protected String createHash = "002029ca32525f635a15c82c046114657c0d8a96a7163780ac6b425b2383b240bd56";
    protected String contractAddress   = "";
    protected String contractAddress0  = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    protected String contractAddress1  = "tNULSeBaNBhqzwK2yN9FuXmNWago7vLt64xggp";
    protected String contractAddress2  = "tNULSeBaN4ahTXVo5RH1DSnUV9tXpYm3JyBqXc";
    protected String contractAddress3  = "tNULSeBaNCCaTxg3KyWw6xsVpGVicJ2SSRAgEF";
    protected String contractAddress4  = "tNULSeBaMyXYWMvjmddsaLTa3dUwJKJ6Umo7eu";
    protected String contractAddress5  = "tNULSeBaN8YvMDC8bFk7ReHDGMDvnRfhCgYHcM";
    protected String contractAddress6  = "tNULSeBaN54ra3FjKDkMvWKSjEVEAh9UkyvCAC";
    protected String contractAddress7  = "tNULSeBaN7ib5inHZPiSiPf2RFUTAnm7v3zAyy";
    protected String contractAddress8  = "tNULSeBaNAKahVoAFixwdBWJXeA3S913uzha2p";
    protected String contractAddress9  = "tNULSeBaMyR8CTTQiJBawsTsH3xYFDuRktVwEo";
    protected String contractAddress10 = "tNULSeBaNAp4J76TzzpLshj9djAKYrc2aTC6MW";
    protected String contractAddress11 = "tNULSeBaN9Gws3c6ia3EvotK37QnayUQh9KVNV";
    protected String contractAddress12 = "tNULSeBaMwYp39ArJhm85KdDCS8KBVSRdFwCHt";
    protected String contractAddress13 = "tNULSeBaN96u4cGLa9cBjdjWm8Th8U1S4fe52f";
    protected String contractAddress14 = "tNULSeBaN3vgW4VSCbJe3ycApfWFDAdmfPEkob";
    protected String contractAddress15 = "tNULSeBaN4hV2aJ4PtrH4bHQ4uKxwLLbgY9ueV";
    protected String contractAddress16 = "tNULSeBaN7oj8ZsFeENwHvTt229X6SQL1Z5aGz";
    protected String contractAddress17 = "tNULSeBaNASJJ6zswjRkXtLYcuzm6SRT1V8B2p";
    protected String contractAddress18 = "tNULSeBaN14G6bHBfmeqPf1E2hnp3QjxJZpuVT";
    protected String contractAddress19 = "tNULSeBaMzeEEdBVLWg7kTaiKt3DtzfhNs7Lg5";
    protected String contractAddress20 = "tNULSeBaN5tTxQkc6CFs53VgGC5JHUf7VKteMy";
    protected String contractAddress21 = "tNULSeBaMwYCjrkCQxcKaaDRjmQ524HVYNnqAB";
    protected String contractAddress22 = "tNULSeBaN45K8896xgjspzeJWJQg2QrYtHrw4T";
    protected String contractAddress23 = "tNULSeBaNCCrUDnFAykgYM2NLFQow3y9bgg7k2";
    protected String contractAddress24 = "tNULSeBaN3Rx4PkFCwZQRCuSuST8DoHzb9cnH9";
    protected String contractAddress25 = "tNULSeBaMyDmJhSLhTUe9sgbyaeLjJP1gm5jKr";
    protected String contractAddress26 = "tNULSeBaNB27e5ooevy3GHsFmPY87WGJanjJcb";
    protected String contractAddress27 = "tNULSeBaN7XPP7iw6V6GVXXUCiNjYgthUPokgS";
    protected String contractAddress28 = "tNULSeBaMwX4j5T2xGKktm9SFEPDStwM8mUVYe";
    protected String contractAddress29 = "tNULSeBaN5pghSZfZESFuDTkJJc1MYPvvWYw22";
    protected String contractAddress30 = "tNULSeBaMyYNWdn3mbdsSmwN5sxk37JTEqtgRM";
    protected String contractAddress31 = "tNULSeBaMw4LDaSQ2NWiHEPPPdnXVeKHhZV4hz";
    protected String contractAddress32 = "tNULSeBaMx5VtE2EJTHtHueWQ1yA37EP1AGuia";
    protected String contractAddress33 = "tNULSeBaN7gkAGGdnj9hDkKgFDXDf6LnnbWpSG";
    protected String contractAddress34 = "tNULSeBaN4kWaxmgYq2oFMvQ9hq8UEdivvA7i7";
    protected String contractAddress_nrc20   = "tNULSeBaMzGVvtSpgB7dcERu7NZWU6Cf8gtnnP";
    protected String contractAddress_nrc200  = "tNULSeBaMzThBLi2gwarkgcEdKAT8twK4KF1Uf";
    protected String contractAddress_nrc201  = "tNULSeBaN8LYBqbDhfF7cW11iu9bk1QyjNNVK6";
    protected String contractAddress_nrc202  = "tNULSeBaN9TgWh4hteRMiWKNeEumnKPJCUTh53";
    protected String contractAddress_nrc203  = "tNULSeBaN5qn8B3UB2kyV6Rtv6cqbXnJZm9jAK";
    protected String contractAddress_nrc204  = "tNULSeBaN37SzKyZoERd4KyEMQCKT2sRazUC94";
    protected String contractAddress_nrc205  = "tNULSeBaN6pRoodbstDUem26sR3DcGmNomRmNp";
    protected String contractAddress_nrc206  = "tNULSeBaMzNhv7ekFeX6SS1fxEVZrBynMuFVeR";
    protected String contractAddress_nrc207  = "tNULSeBaMyXor2aJZ4TXjRjhJkw1T4DYxtzH4b";
    protected String contractAddress_nrc208  = "tNULSeBaN2Us7FG6UU2vwjn8winJc7aSvHRQWM";
    protected String contractAddress_nrc209  = "tNULSeBaMw31oFyEyKDUeHFdL7c45HMB5oZyka";
    protected String contractAddress_nrc2010 = "tNULSeBaMzChFy1Q6Ao5oF83oa8cSMEG3ZKfUd";
    protected String contractAddress_nrc2011 = "tNULSeBaN7xxv8xRAqr4QtYZkrHeoLzYTCLmRu";
    protected String contractAddress_nrc2012 = "tNULSeBaNCLwPhph7q4dj1acTgbyxGJd2FPYVT";
    protected String contractAddress_nrc2013 = "tNULSeBaN5SowmpaevWqTscBDAWpRCWPF6ZxGy";
    protected String contractAddress_nrc2014 = "tNULSeBaMy1ETawqkgLG7u1BfSuf5Go3Pc7uk7";
    protected String contractAddress_nrc2015 = "tNULSeBaN5U1GPWmPc8LJ2gpf15KXS5A2VkFaW";
    protected String contractAddress_nrc2016 = "tNULSeBaN8YgGzmJ9qeQFKsMb15HLE1gD2oBxy";
    protected String contractAddress_nrc2017 = "tNULSeBaMw9NQDXCymSjkuybC2haE5gygCLtRR";
    protected String contractAddress_nrc2018 = "tNULSeBaN2wu88LKfVmkoGUTuKXXrttG7QDowe";
    protected String contractAddress_nrc2019 = "tNULSeBaN8NUaHyUGDXQUUenb1iThYRzAqDJwK";
    protected String contractAddress_nrc2020 = "tNULSeBaMwj95C4P8FAoEM4fc5VsCHCKQ3rtZP";
    protected String contractAddress_nrc2021 = "tNULSeBaN1qbx2x15duGVAo5Qni7HScF9etXxQ";
    protected String contractAddress_nrc2022 = "tNULSeBaMzwQ2nhTEEfc2dh3H6JReb22h3HC4v";
    protected String contractAddress_nrc2023 = "tNULSeBaN2QK3vN8vAXynEaPewwztuwzH2NSUC";
    protected String contractAddress_nrc2024 = "tNULSeBaMvpgepaBn7cYRMFEmSdWVjp5ARBXh3";
    protected String contractAddress_nrc2025 = "tNULSeBaN3Y789JGTDTFQmAtyUHSGcZRwPADYD";
    protected String contractAddress_nrc2026 = "tNULSeBaN23MYckd68RvKGW2TazUryPnyYwkHg";
    protected String contractAddress_nrc2027 = "tNULSeBaN4W4U7XFbEyArJAJfNbjNwV8WP8eqo";
    protected String contractAddress_nrc2028 = "tNULSeBaN3Xck3iQ2NvC4nJCyrMQAmRrdeAxJb";
    protected String contractAddress_nrc2029 = "tNULSeBaMxHeQHAZhFi9PWvPLkVqXjDumfAdzJ";
    protected String contractAddress_nrc2030 = "tNULSeBaN4LgXZJ5DmLGGLdh7s14aPfREjs3wA";
    protected String contractAddress_nrc2031 = "tNULSeBaNAByj1ed6ZZas2huKc4pjfy931o7Sd";
    protected String contractAddress_nrc2032 = "tNULSeBaMz314AeVfETvL1ei9EqGPpeoc942W4";
    protected String contractAddress_nrc2033 = "tNULSeBaN3JCvpzFvPBBGA2vVfYWnEgG73AFfZ";
    protected String contractAddress_nrc2034 = "tNULSeBaN99Y5r2nVsguatRF9yuNJdpTvtwajW";

    protected String methodName = "";
    protected String tokenReceiver = "";

    protected String callHash = "0020874dca08dbf4784540e26c0c31f728a2c2fd2e18bf71c896d8f88955d53e77b7";
    protected String deleteHash = "0020b2c159dbdf784c2860ec97072feb887466aa50fc147a5b50388886caab113f9a";

    @Test
    public void getBlockHeader() throws NulsException, JsonProcessingException {
        BlockHeader blockHeader = BlockCall.getBlockHeader(chainId, 20L);
        Log.info("\nstateRoot is " + HexUtil.encode(ContractUtil.getStateRoot(blockHeader)) + ", " + blockHeader.toString());
    }

    @Test
    public void getBalance() throws Exception {
        Map<String, Object> balance0 = LedgerCall.getBalanceAndNonce(chain, toAddress7);
        Log.info("balance:{}", JSONUtils.obj2PrettyJson(balance0));
    }

    @Test
    public void base64ToBase58() {
        String base64Str = "AgACPJCMF36z+LvlLbuxYRwT48hQmgA=";
        byte[] bytes = Base64.getDecoder().decode(base64Str);
        Log.info("address is {}", AddressTool.getStringAddressByBytes(bytes));

    }

    /**
     * 获取账户创建的合约列表
     */
    @Test
    public void accountContracts() throws Exception {
        Map params = this.makeAccountContractsParams(sender, 1, 10);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, ACCOUNT_CONTRACTS, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(ACCOUNT_CONTRACTS));
        Assert.assertTrue(null != result);
        Log.info("accountContracts-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeAccountContractsParams(String address, int pageNumber, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("pageNumber", pageNumber);
        params.put("pageSize", pageSize);
        return params;
    }

    /**
     * 获取合约基本信息
     */
    @Test
    public void contractInfo() throws Exception {
        Map params = this.makeContractInfoParams("tNULSeBaNAsyKtqQRFPVQkxtiEch4hw4X6iYdZ");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_INFO, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_INFO));
        Assert.assertTrue(null != result);
        Log.info("contract_info-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeContractInfoParams(String contractAddress) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("contractAddress", contractAddress);
        return params;
    }

    /**
     * 获取合约执行结果
     */
    @Test
    public void contractResult() throws Exception {
        Map params = this.makeContractResultParams("48a41b8c4c1ac220126a281fcf8f5037539db5248c789b56821129e6e7e20410");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_RESULT, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_RESULT));
        Assert.assertTrue(null != result);
        Log.info("contractResult-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeContractResultParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", hash);
        return params;
    }

    /**
     * 获取合约交易
     */
    @Test
    public void contractTx() throws Exception {
        Map params = this.makeContractTxParams("e75a531a0220d5ff4e0386334a21d5a986b79f97bda6373127f84be39ba5dc9b");
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_TX, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_TX));
        Assert.assertTrue(null != result);
        Log.info("contractTx-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeContractTxParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", hash);
        return params;
    }


    /**
     * 查交易
     */
    @Test
    public void getTxClient() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", "3e7faf0939b131ccb018ce5b96761fb9178cbd247d781a8c1315a4e47c08630f");
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Map resultMap = (Map) record.get("tx_getTxClient");
        String txHex = (String) resultMap.get("tx");
        Assert.assertTrue(null != txHex);
        Transaction tx = new Transaction();
        tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
        Log.info("tx is {}", JSONUtils.obj2PrettyJson(tx));

    }

    @Test
    public void getTxRecord() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", sender);
        params.put("assetChainId", null);
        params.put("assetId", null);
        params.put("type", null);
        params.put("pageSize", null);
        params.put("pageNumber", null);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxs", params);
        Map record = (Map) dpResp.getResponseData();
        Log.info("Page<TransactionPO>:{}", JSONUtils.obj2PrettyJson(record));
    }

    public TransferService getTransferService() {
        return transferService;
    }

    public void setTransferService(TransferService transferService) {
        this.transferService = transferService;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public static int getChainId() {
        return chainId;
    }

    public static void setChainId(int chainId) {
        BaseQuery.chainId = chainId;
    }

    public static int getAssetId() {
        return assetId;
    }

    public static void setAssetId(int assetId) {
        BaseQuery.assetId = assetId;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        BaseQuery.password = password;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getToAddress0() {
        return toAddress0;
    }

    public void setToAddress0(String toAddress0) {
        this.toAddress0 = toAddress0;
    }

    public String getToAddress1() {
        return toAddress1;
    }

    public void setToAddress1(String toAddress1) {
        this.toAddress1 = toAddress1;
    }

    public String getToAddress2() {
        return toAddress2;
    }

    public void setToAddress2(String toAddress2) {
        this.toAddress2 = toAddress2;
    }

    public String getToAddress3() {
        return toAddress3;
    }

    public void setToAddress3(String toAddress3) {
        this.toAddress3 = toAddress3;
    }

    public String getToAddress4() {
        return toAddress4;
    }

    public void setToAddress4(String toAddress4) {
        this.toAddress4 = toAddress4;
    }

    public String getToAddress5() {
        return toAddress5;
    }

    public void setToAddress5(String toAddress5) {
        this.toAddress5 = toAddress5;
    }

    public String getToAddress6() {
        return toAddress6;
    }

    public void setToAddress6(String toAddress6) {
        this.toAddress6 = toAddress6;
    }

    public String getToAddress7() {
        return toAddress7;
    }

    public void setToAddress7(String toAddress7) {
        this.toAddress7 = toAddress7;
    }

    public String getToAddress8() {
        return toAddress8;
    }

    public void setToAddress8(String toAddress8) {
        this.toAddress8 = toAddress8;
    }

    public String getToAddress9() {
        return toAddress9;
    }

    public void setToAddress9(String toAddress9) {
        this.toAddress9 = toAddress9;
    }

    public String getToAddress10() {
        return toAddress10;
    }

    public void setToAddress10(String toAddress10) {
        this.toAddress10 = toAddress10;
    }

    public String getToAddress11() {
        return toAddress11;
    }

    public void setToAddress11(String toAddress11) {
        this.toAddress11 = toAddress11;
    }

    public String getToAddress12() {
        return toAddress12;
    }

    public void setToAddress12(String toAddress12) {
        this.toAddress12 = toAddress12;
    }

    public String getToAddress13() {
        return toAddress13;
    }

    public void setToAddress13(String toAddress13) {
        this.toAddress13 = toAddress13;
    }

    public String getToAddress14() {
        return toAddress14;
    }

    public void setToAddress14(String toAddress14) {
        this.toAddress14 = toAddress14;
    }

    public String getToAddress15() {
        return toAddress15;
    }

    public void setToAddress15(String toAddress15) {
        this.toAddress15 = toAddress15;
    }

    public String getToAddress16() {
        return toAddress16;
    }

    public void setToAddress16(String toAddress16) {
        this.toAddress16 = toAddress16;
    }

    public String getToAddress17() {
        return toAddress17;
    }

    public void setToAddress17(String toAddress17) {
        this.toAddress17 = toAddress17;
    }

    public String getToAddress18() {
        return toAddress18;
    }

    public void setToAddress18(String toAddress18) {
        this.toAddress18 = toAddress18;
    }

    public String getToAddress19() {
        return toAddress19;
    }

    public void setToAddress19(String toAddress19) {
        this.toAddress19 = toAddress19;
    }

    public String getToAddress20() {
        return toAddress20;
    }

    public void setToAddress20(String toAddress20) {
        this.toAddress20 = toAddress20;
    }

    public String getToAddress21() {
        return toAddress21;
    }

    public void setToAddress21(String toAddress21) {
        this.toAddress21 = toAddress21;
    }

    public String getToAddress22() {
        return toAddress22;
    }

    public void setToAddress22(String toAddress22) {
        this.toAddress22 = toAddress22;
    }

    public String getToAddress23() {
        return toAddress23;
    }

    public void setToAddress23(String toAddress23) {
        this.toAddress23 = toAddress23;
    }

    public String getToAddress24() {
        return toAddress24;
    }

    public void setToAddress24(String toAddress24) {
        this.toAddress24 = toAddress24;
    }

    public String getToAddress25() {
        return toAddress25;
    }

    public void setToAddress25(String toAddress25) {
        this.toAddress25 = toAddress25;
    }

    public String getToAddress26() {
        return toAddress26;
    }

    public void setToAddress26(String toAddress26) {
        this.toAddress26 = toAddress26;
    }

    public String getToAddress27() {
        return toAddress27;
    }

    public void setToAddress27(String toAddress27) {
        this.toAddress27 = toAddress27;
    }

    public String getToAddress28() {
        return toAddress28;
    }

    public void setToAddress28(String toAddress28) {
        this.toAddress28 = toAddress28;
    }

    public String getToAddress29() {
        return toAddress29;
    }

    public void setToAddress29(String toAddress29) {
        this.toAddress29 = toAddress29;
    }

    public String getToAddress30() {
        return toAddress30;
    }

    public void setToAddress30(String toAddress30) {
        this.toAddress30 = toAddress30;
    }

    public String getToAddress31() {
        return toAddress31;
    }

    public void setToAddress31(String toAddress31) {
        this.toAddress31 = toAddress31;
    }

    public String getToAddress32() {
        return toAddress32;
    }

    public void setToAddress32(String toAddress32) {
        this.toAddress32 = toAddress32;
    }

    public String getToAddress33() {
        return toAddress33;
    }

    public void setToAddress33(String toAddress33) {
        this.toAddress33 = toAddress33;
    }

    public String getToAddress34() {
        return toAddress34;
    }

    public void setToAddress34(String toAddress34) {
        this.toAddress34 = toAddress34;
    }

    public String getCreateHash() {
        return createHash;
    }

    public void setCreateHash(String createHash) {
        this.createHash = createHash;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractAddress0() {
        return contractAddress0;
    }

    public void setContractAddress0(String contractAddress0) {
        this.contractAddress0 = contractAddress0;
    }

    public String getContractAddress1() {
        return contractAddress1;
    }

    public void setContractAddress1(String contractAddress1) {
        this.contractAddress1 = contractAddress1;
    }

    public String getContractAddress2() {
        return contractAddress2;
    }

    public void setContractAddress2(String contractAddress2) {
        this.contractAddress2 = contractAddress2;
    }

    public String getContractAddress3() {
        return contractAddress3;
    }

    public void setContractAddress3(String contractAddress3) {
        this.contractAddress3 = contractAddress3;
    }

    public String getContractAddress4() {
        return contractAddress4;
    }

    public void setContractAddress4(String contractAddress4) {
        this.contractAddress4 = contractAddress4;
    }

    public String getContractAddress5() {
        return contractAddress5;
    }

    public void setContractAddress5(String contractAddress5) {
        this.contractAddress5 = contractAddress5;
    }

    public String getContractAddress6() {
        return contractAddress6;
    }

    public void setContractAddress6(String contractAddress6) {
        this.contractAddress6 = contractAddress6;
    }

    public String getContractAddress7() {
        return contractAddress7;
    }

    public void setContractAddress7(String contractAddress7) {
        this.contractAddress7 = contractAddress7;
    }

    public String getContractAddress8() {
        return contractAddress8;
    }

    public void setContractAddress8(String contractAddress8) {
        this.contractAddress8 = contractAddress8;
    }

    public String getContractAddress9() {
        return contractAddress9;
    }

    public void setContractAddress9(String contractAddress9) {
        this.contractAddress9 = contractAddress9;
    }

    public String getContractAddress10() {
        return contractAddress10;
    }

    public void setContractAddress10(String contractAddress10) {
        this.contractAddress10 = contractAddress10;
    }

    public String getContractAddress11() {
        return contractAddress11;
    }

    public void setContractAddress11(String contractAddress11) {
        this.contractAddress11 = contractAddress11;
    }

    public String getContractAddress12() {
        return contractAddress12;
    }

    public void setContractAddress12(String contractAddress12) {
        this.contractAddress12 = contractAddress12;
    }

    public String getContractAddress13() {
        return contractAddress13;
    }

    public void setContractAddress13(String contractAddress13) {
        this.contractAddress13 = contractAddress13;
    }

    public String getContractAddress14() {
        return contractAddress14;
    }

    public void setContractAddress14(String contractAddress14) {
        this.contractAddress14 = contractAddress14;
    }

    public String getContractAddress15() {
        return contractAddress15;
    }

    public void setContractAddress15(String contractAddress15) {
        this.contractAddress15 = contractAddress15;
    }

    public String getContractAddress16() {
        return contractAddress16;
    }

    public void setContractAddress16(String contractAddress16) {
        this.contractAddress16 = contractAddress16;
    }

    public String getContractAddress17() {
        return contractAddress17;
    }

    public void setContractAddress17(String contractAddress17) {
        this.contractAddress17 = contractAddress17;
    }

    public String getContractAddress18() {
        return contractAddress18;
    }

    public void setContractAddress18(String contractAddress18) {
        this.contractAddress18 = contractAddress18;
    }

    public String getContractAddress19() {
        return contractAddress19;
    }

    public void setContractAddress19(String contractAddress19) {
        this.contractAddress19 = contractAddress19;
    }

    public String getContractAddress20() {
        return contractAddress20;
    }

    public void setContractAddress20(String contractAddress20) {
        this.contractAddress20 = contractAddress20;
    }

    public String getContractAddress21() {
        return contractAddress21;
    }

    public void setContractAddress21(String contractAddress21) {
        this.contractAddress21 = contractAddress21;
    }

    public String getContractAddress22() {
        return contractAddress22;
    }

    public void setContractAddress22(String contractAddress22) {
        this.contractAddress22 = contractAddress22;
    }

    public String getContractAddress23() {
        return contractAddress23;
    }

    public void setContractAddress23(String contractAddress23) {
        this.contractAddress23 = contractAddress23;
    }

    public String getContractAddress24() {
        return contractAddress24;
    }

    public void setContractAddress24(String contractAddress24) {
        this.contractAddress24 = contractAddress24;
    }

    public String getContractAddress25() {
        return contractAddress25;
    }

    public void setContractAddress25(String contractAddress25) {
        this.contractAddress25 = contractAddress25;
    }

    public String getContractAddress26() {
        return contractAddress26;
    }

    public void setContractAddress26(String contractAddress26) {
        this.contractAddress26 = contractAddress26;
    }

    public String getContractAddress27() {
        return contractAddress27;
    }

    public void setContractAddress27(String contractAddress27) {
        this.contractAddress27 = contractAddress27;
    }

    public String getContractAddress28() {
        return contractAddress28;
    }

    public void setContractAddress28(String contractAddress28) {
        this.contractAddress28 = contractAddress28;
    }

    public String getContractAddress29() {
        return contractAddress29;
    }

    public void setContractAddress29(String contractAddress29) {
        this.contractAddress29 = contractAddress29;
    }

    public String getContractAddress30() {
        return contractAddress30;
    }

    public void setContractAddress30(String contractAddress30) {
        this.contractAddress30 = contractAddress30;
    }

    public String getContractAddress31() {
        return contractAddress31;
    }

    public void setContractAddress31(String contractAddress31) {
        this.contractAddress31 = contractAddress31;
    }

    public String getContractAddress32() {
        return contractAddress32;
    }

    public void setContractAddress32(String contractAddress32) {
        this.contractAddress32 = contractAddress32;
    }

    public String getContractAddress33() {
        return contractAddress33;
    }

    public void setContractAddress33(String contractAddress33) {
        this.contractAddress33 = contractAddress33;
    }

    public String getContractAddress34() {
        return contractAddress34;
    }

    public void setContractAddress34(String contractAddress34) {
        this.contractAddress34 = contractAddress34;
    }

    public String getContractAddress_nrc20() {
        return contractAddress_nrc20;
    }

    public void setContractAddress_nrc20(String contractAddress_nrc20) {
        this.contractAddress_nrc20 = contractAddress_nrc20;
    }

    public String getContractAddress_nrc200() {
        return contractAddress_nrc200;
    }

    public void setContractAddress_nrc200(String contractAddress_nrc200) {
        this.contractAddress_nrc200 = contractAddress_nrc200;
    }

    public String getContractAddress_nrc201() {
        return contractAddress_nrc201;
    }

    public void setContractAddress_nrc201(String contractAddress_nrc201) {
        this.contractAddress_nrc201 = contractAddress_nrc201;
    }

    public String getContractAddress_nrc202() {
        return contractAddress_nrc202;
    }

    public void setContractAddress_nrc202(String contractAddress_nrc202) {
        this.contractAddress_nrc202 = contractAddress_nrc202;
    }

    public String getContractAddress_nrc203() {
        return contractAddress_nrc203;
    }

    public void setContractAddress_nrc203(String contractAddress_nrc203) {
        this.contractAddress_nrc203 = contractAddress_nrc203;
    }

    public String getContractAddress_nrc204() {
        return contractAddress_nrc204;
    }

    public void setContractAddress_nrc204(String contractAddress_nrc204) {
        this.contractAddress_nrc204 = contractAddress_nrc204;
    }

    public String getContractAddress_nrc205() {
        return contractAddress_nrc205;
    }

    public void setContractAddress_nrc205(String contractAddress_nrc205) {
        this.contractAddress_nrc205 = contractAddress_nrc205;
    }

    public String getContractAddress_nrc206() {
        return contractAddress_nrc206;
    }

    public void setContractAddress_nrc206(String contractAddress_nrc206) {
        this.contractAddress_nrc206 = contractAddress_nrc206;
    }

    public String getContractAddress_nrc207() {
        return contractAddress_nrc207;
    }

    public void setContractAddress_nrc207(String contractAddress_nrc207) {
        this.contractAddress_nrc207 = contractAddress_nrc207;
    }

    public String getContractAddress_nrc208() {
        return contractAddress_nrc208;
    }

    public void setContractAddress_nrc208(String contractAddress_nrc208) {
        this.contractAddress_nrc208 = contractAddress_nrc208;
    }

    public String getContractAddress_nrc209() {
        return contractAddress_nrc209;
    }

    public void setContractAddress_nrc209(String contractAddress_nrc209) {
        this.contractAddress_nrc209 = contractAddress_nrc209;
    }

    public String getContractAddress_nrc2010() {
        return contractAddress_nrc2010;
    }

    public void setContractAddress_nrc2010(String contractAddress_nrc2010) {
        this.contractAddress_nrc2010 = contractAddress_nrc2010;
    }

    public String getContractAddress_nrc2011() {
        return contractAddress_nrc2011;
    }

    public void setContractAddress_nrc2011(String contractAddress_nrc2011) {
        this.contractAddress_nrc2011 = contractAddress_nrc2011;
    }

    public String getContractAddress_nrc2012() {
        return contractAddress_nrc2012;
    }

    public void setContractAddress_nrc2012(String contractAddress_nrc2012) {
        this.contractAddress_nrc2012 = contractAddress_nrc2012;
    }

    public String getContractAddress_nrc2013() {
        return contractAddress_nrc2013;
    }

    public void setContractAddress_nrc2013(String contractAddress_nrc2013) {
        this.contractAddress_nrc2013 = contractAddress_nrc2013;
    }

    public String getContractAddress_nrc2014() {
        return contractAddress_nrc2014;
    }

    public void setContractAddress_nrc2014(String contractAddress_nrc2014) {
        this.contractAddress_nrc2014 = contractAddress_nrc2014;
    }

    public String getContractAddress_nrc2015() {
        return contractAddress_nrc2015;
    }

    public void setContractAddress_nrc2015(String contractAddress_nrc2015) {
        this.contractAddress_nrc2015 = contractAddress_nrc2015;
    }

    public String getContractAddress_nrc2016() {
        return contractAddress_nrc2016;
    }

    public void setContractAddress_nrc2016(String contractAddress_nrc2016) {
        this.contractAddress_nrc2016 = contractAddress_nrc2016;
    }

    public String getContractAddress_nrc2017() {
        return contractAddress_nrc2017;
    }

    public void setContractAddress_nrc2017(String contractAddress_nrc2017) {
        this.contractAddress_nrc2017 = contractAddress_nrc2017;
    }

    public String getContractAddress_nrc2018() {
        return contractAddress_nrc2018;
    }

    public void setContractAddress_nrc2018(String contractAddress_nrc2018) {
        this.contractAddress_nrc2018 = contractAddress_nrc2018;
    }

    public String getContractAddress_nrc2019() {
        return contractAddress_nrc2019;
    }

    public void setContractAddress_nrc2019(String contractAddress_nrc2019) {
        this.contractAddress_nrc2019 = contractAddress_nrc2019;
    }

    public String getContractAddress_nrc2020() {
        return contractAddress_nrc2020;
    }

    public void setContractAddress_nrc2020(String contractAddress_nrc2020) {
        this.contractAddress_nrc2020 = contractAddress_nrc2020;
    }

    public String getContractAddress_nrc2021() {
        return contractAddress_nrc2021;
    }

    public void setContractAddress_nrc2021(String contractAddress_nrc2021) {
        this.contractAddress_nrc2021 = contractAddress_nrc2021;
    }

    public String getContractAddress_nrc2022() {
        return contractAddress_nrc2022;
    }

    public void setContractAddress_nrc2022(String contractAddress_nrc2022) {
        this.contractAddress_nrc2022 = contractAddress_nrc2022;
    }

    public String getContractAddress_nrc2023() {
        return contractAddress_nrc2023;
    }

    public void setContractAddress_nrc2023(String contractAddress_nrc2023) {
        this.contractAddress_nrc2023 = contractAddress_nrc2023;
    }

    public String getContractAddress_nrc2024() {
        return contractAddress_nrc2024;
    }

    public void setContractAddress_nrc2024(String contractAddress_nrc2024) {
        this.contractAddress_nrc2024 = contractAddress_nrc2024;
    }

    public String getContractAddress_nrc2025() {
        return contractAddress_nrc2025;
    }

    public void setContractAddress_nrc2025(String contractAddress_nrc2025) {
        this.contractAddress_nrc2025 = contractAddress_nrc2025;
    }

    public String getContractAddress_nrc2026() {
        return contractAddress_nrc2026;
    }

    public void setContractAddress_nrc2026(String contractAddress_nrc2026) {
        this.contractAddress_nrc2026 = contractAddress_nrc2026;
    }

    public String getContractAddress_nrc2027() {
        return contractAddress_nrc2027;
    }

    public void setContractAddress_nrc2027(String contractAddress_nrc2027) {
        this.contractAddress_nrc2027 = contractAddress_nrc2027;
    }

    public String getContractAddress_nrc2028() {
        return contractAddress_nrc2028;
    }

    public void setContractAddress_nrc2028(String contractAddress_nrc2028) {
        this.contractAddress_nrc2028 = contractAddress_nrc2028;
    }

    public String getContractAddress_nrc2029() {
        return contractAddress_nrc2029;
    }

    public void setContractAddress_nrc2029(String contractAddress_nrc2029) {
        this.contractAddress_nrc2029 = contractAddress_nrc2029;
    }

    public String getContractAddress_nrc2030() {
        return contractAddress_nrc2030;
    }

    public void setContractAddress_nrc2030(String contractAddress_nrc2030) {
        this.contractAddress_nrc2030 = contractAddress_nrc2030;
    }

    public String getContractAddress_nrc2031() {
        return contractAddress_nrc2031;
    }

    public void setContractAddress_nrc2031(String contractAddress_nrc2031) {
        this.contractAddress_nrc2031 = contractAddress_nrc2031;
    }

    public String getContractAddress_nrc2032() {
        return contractAddress_nrc2032;
    }

    public void setContractAddress_nrc2032(String contractAddress_nrc2032) {
        this.contractAddress_nrc2032 = contractAddress_nrc2032;
    }

    public String getContractAddress_nrc2033() {
        return contractAddress_nrc2033;
    }

    public void setContractAddress_nrc2033(String contractAddress_nrc2033) {
        this.contractAddress_nrc2033 = contractAddress_nrc2033;
    }

    public String getContractAddress_nrc2034() {
        return contractAddress_nrc2034;
    }

    public void setContractAddress_nrc2034(String contractAddress_nrc2034) {
        this.contractAddress_nrc2034 = contractAddress_nrc2034;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getTokenReceiver() {
        return tokenReceiver;
    }

    public void setTokenReceiver(String tokenReceiver) {
        this.tokenReceiver = tokenReceiver;
    }

    public String getCallHash() {
        return callHash;
    }

    public void setCallHash(String callHash) {
        this.callHash = callHash;
    }

    public String getDeleteHash() {
        return deleteHash;
    }

    public void setDeleteHash(String deleteHash) {
        this.deleteHash = deleteHash;
    }
}
