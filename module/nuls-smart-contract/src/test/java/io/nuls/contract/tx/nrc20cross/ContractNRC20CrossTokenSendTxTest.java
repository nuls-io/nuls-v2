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

package io.nuls.contract.tx.nrc20cross;


import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.mock.basetest.ContractTest;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractNRC20CrossTokenSendTxTest extends BaseQuery {

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        //sender = toAddress32;
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nuls-cross-chain-nrc20-test.jar").getFile());
        //InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/nuls-cross-chain-nrc20/target/nuls-cross-chain-nrc20-test.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create cross token";
        String name = "cct";
        String symbol = "CCT";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "8";
        Map params = this.makeCreateParams(sender, contractCode, "cct", remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map) (map.get("contractResult"))).get("success"));
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(map));
    }

    /**
     * token转账
     */
    @Test
    public void tokenTransfer() throws Exception {
        BigInteger value = BigInteger.TEN.pow(10);
        String remark = "token transfer to " + contractAddress;
        Map params = this.makeTokenTransferParams(sender, contractAddress, contractAddress_nrc20, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TOKEN_TRANSFER, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TOKEN_TRANSFER));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    private Integer getAssetId(String contractAddress) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("contractAddress", contractAddress);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetContractAssetId", parameters);
        Map result = (HashMap) (((HashMap) response.getResponseData()).get("getAssetContractAssetId"));
        assertTrue(response, result);
        Integer assetId = Integer.parseInt(result.get("assetId").toString());
        return assetId;
    }

    private void mainNetAssetReg(Integer assetId) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", sender);
        parameters.put("assetId", assetId);
        parameters.put("password", "nuls123456");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_mainNetAssetReg", parameters);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    @Test
    public void getAssetRegInfo() throws Exception {
        String cmd = "getAssetRegInfo";
        Map<String, Object> params = new HashMap<>(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("assetType", ContractConstant.TOKEN_ASSET_TYPE);
        Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, cmd, params);
        if (!callResp.isSuccess()) {
            Log.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, callResp.getResponseErrorCode(), callResp.getResponseComment());
            return;
        }
        System.out.println(JSONUtils.obj2PrettyJson(callResp));
    }

    @Test
    public void getAssetContractAddressTest() throws Exception {
        // Build params map
        Map<String,Object> params = new HashMap<>();
        params.put("chainId",2);
        params.put("assetId",2);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetContractAddress", params);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    @Test
    public void temp() throws Exception {
        System.out.println(getAssetId("tNULSeBaN6e4ANHRsextm4bY5e8HpDCWWe7eby"));
    }

    /**
     * 1. 流程 - 创建NRC20合约
     *          创建系统合约
     *          向NRC20合约设置系统合约参数
     *          向链管理模块注册跨链
     */
    @Test
    public void testCreateProcessor() throws Exception {
        String nrc20 = createCrossNrc20Contract();
        String sys = createCrossSystemContract();
        this.invokeCall(sender, BigInteger.ZERO, nrc20, "setSystemContract", null, "remark", sys);
        TimeUnit.SECONDS.sleep(1);
        Integer assetId = this.getAssetId(nrc20);
        this.mainNetAssetReg(assetId);
        Log.info("nrc20 is [{}], assetId is [{}], sys is [{}]", nrc20, assetId, sys);

    }

    /**
     * 2. 调用合约 - 调用token跨链转账
     */
    @Test
    public void callTransferCrossChain() throws Exception {
        contractAddress_nrc20 = "tNULSeBaMxMQEBEzEVwX3ZE7shuzuD9uEn45dW";
        methodName = "transferCrossChain";
        BigInteger value = BigInteger.ZERO;
        String methodDesc = "";
        String remark = "call contract test - token跨链转账";
        String to = "XXOOdjJQw4LJdjtCd5Gda17FCNgSgTcPUUdSA";
        String token = BigInteger.valueOf(8_0000_0000L).toString();
        String[] args = {to, token};
        Map params = this.makeCallParams(sender, value, contractAddress_nrc20, methodName, methodDesc, remark, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    private String createCrossNrc20Contract() throws Exception {
        Log.info("开始创建跨链特性的NRC20合约");
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nuls-cross-chain-nrc20-test.jar").getFile());
        //InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/nuls-cross-chain-nrc20/target/nuls-cross-chain-nrc20-test.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create cross token";
        String name = "cct";
        String symbol = "CCT";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "8";
        Map params = this.makeCreateParams(sender, contractCode, "cct", remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map) (map.get("contractResult"))).get("success"));
        return contractAddress;
    }

    private String createCrossSystemContract() throws Exception {
        Log.info("开始创建跨链特性的System合约");
        InputStream in = new FileInputStream(ContractTest.class.getResource("/cross-token-system-contract-test1.jar").getFile());
        //InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/cross-token-system-contract/target/cross-token-system-contract-test1.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create cross system contract";
        Map params = this.makeCreateParams(sender, contractCode, "sys", remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map) (map.get("contractResult"))).get("success"));
        return contractAddress;
    }

    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 流程: 测试合约内部临时存储
     */
    @Test
    public void testContractRepositoryProcessor() throws Exception {
        String nrc20 = createTempNrc20Contract();
        String temp = createTempContract();
        this.invokeCall(sender, BigInteger.ZERO, nrc20, "setSystemContract", null, "remark", temp);
        TimeUnit.SECONDS.sleep(1);
        Log.info("nrc20 is [{}], temp is [{}]", nrc20, temp);
        String to = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        String token = BigInteger.valueOf(8_0000_0000L).toString();
        String[] args = {to, token};
        this.invokeCall(sender, BigInteger.ZERO, nrc20, "transferCrossChain123", null, "remark", args);
    }

    @Test
    public void test() throws Exception {
        // tNULSeBaNC716ifZZz7ZxnXfAbQE7k6VBpm8PD
        // tNULSeBaMz8qLFzkUMXTzWDG4KzdaEg3kPtpSd
        String nrc20 = "tNULSeBaNC716ifZZz7ZxnXfAbQE7k6VBpm8PD";
        //String temp = "tNULSeBaMz8qLFzkUMXTzWDG4KzdaEg3kPtpSd";
        //this.invokeCall(sender, BigInteger.ZERO, nrc20, "setSystemContract", null, "remark", temp);
        String to = "XXOOdjJQw4LJdjtCd5Gda17FCNgSgTcPUUdSA";
        String token = BigInteger.valueOf(8_0000_0000L).toString();
        String[] args = {to, token};
        this.invokeCall(sender, BigInteger.ZERO, nrc20, "transferCrossChain123", null, "remark", args);
    }

    private String createTempNrc20Contract() throws Exception {
        Log.info("开始创建测试合约存储的NRC20合约");
        InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-storage-test/target/contract-vm-storage-test-test1.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create cross token";
        String name = "cct";
        String symbol = "CCT";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "8";
        Map params = this.makeCreateParams(sender, contractCode, "cct", remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map) (map.get("contractResult"))).get("success"));
        return contractAddress;
    }

    private String createTempContract() throws Exception {
        Log.info("开始创建Temp合约");
        InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/mavenplugin/target/maven-plugin-test.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create temp contract";
        Map params = this.makeCreateParams(sender, contractCode, "temp", remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map) (map.get("contractResult"))).get("success"));
        return contractAddress;
    }
}
