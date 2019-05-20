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

package io.nuls.contract.tx.nrc20;


import io.nuls.contract.basetest.ContractTest;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractNRC20TokenQueryTest extends BaseQuery {


    /**
     * 预创建合约
     */
    @Test
    public void preCreateContract() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test - 空气币";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makePreCreateParams(sender, contractCode, remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, PRE_CREATE, params);
        Log.info("pre_create-Response:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        Assert.assertTrue(cmdResp2.isSuccess());
    }

    private Map makePreCreateParams(String sender, byte[] contractCode, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("password", password);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractCode", HexUtil.encode(contractCode));
        params.put("args", args);
        params.put("remark", remark);
        return params;
    }


    /**
     * 估算创建合约的gas
     */
    @Test
    public void imputedCreateGas() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makeImputedCreateGasParams(sender, contractCode, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, IMPUTED_CREATE_GAS, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(IMPUTED_CREATE_GAS));
        Assert.assertTrue(null != result);
        Log.info("imputed_create_gas-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeImputedCreateGasParams(String sender, byte[] contractCode, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("contractCode", HexUtil.encode(contractCode));
        params.put("args", args);
        return params;
    }

    /**
     * 验证创建合约
     */
    @Test
    public void validateCreate() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makeValidateCreateParams(sender, contractCode, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, VALIDATE_CREATE, params);
        Log.info("validate_create-Response:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        Assert.assertTrue(cmdResp2.isSuccess());
    }

    private Map makeValidateCreateParams(String sender, byte[] contractCode, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractCode", HexUtil.encode(contractCode));
        params.put("args", args);
        return params;
    }


    /**
     * 验证调用合约
     */
    @Test
    public void validateCall() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "transfer";
        String methodDesc = "";
        String token = BigInteger.TEN.pow(8).toString();
        Map params = this.makeValidateCallParams(sender, value, contractAddress0, methodName, methodDesc, toAddress0, token);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, VALIDATE_CALL, params);
        Log.info("validateCall-Response:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        Assert.assertTrue(cmdResp2.isSuccess());
    }

    private Map makeValidateCallParams(String sender, BigInteger value, String contractAddress0, String methodName, String methodDesc, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractAddress", contractAddress0);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        return params;
    }

    /**
     * 估算调用合约的gas
     */
    @Test
    public void imputedCallGas() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "transfer";
        String methodDesc = "";
        String token = BigInteger.TEN.pow(8).toString();
        Map params = this.makeImputedCallGasParams(sender, value, contractAddress0, methodName, methodDesc, toAddress0, token);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, IMPUTED_CALL_GAS, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(IMPUTED_CALL_GAS));
        Assert.assertTrue(null != result);
        Log.info("imputed_call_gas-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeImputedCallGasParams(String sender, BigInteger value, String contractAddress0, String methodName, String methodDesc, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("contractAddress", contractAddress0);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        return params;
    }


    /**
     * 获取向合约地址转账的手续费
     */
    @Test
    public void transfer2ContractFee() throws Exception {
        BigInteger value = BigInteger.TEN.pow(8);
        String remark = "transfer 2 contract fee";
        Map params = this.makeTransferFeeParams(sender, contractAddress_nrc200, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TRANSFER_FEE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TRANSFER_FEE));
        Assert.assertTrue(null != result);
        Log.info("transfer2ContractFee-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeTransferFeeParams(String address, String toAddress, BigInteger amount, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("toAddress", toAddress);
        params.put("amount", amount);
        params.put("remark", remark);
        return params;
    }


    /**
     * token余额
     */
    @Test
    public void tokenBalance() throws Exception {
        Map params = this.makeTokenBalanceParams(contractAddress_nrc200, toAddress1);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TOKEN_BALANCE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TOKEN_BALANCE));
        Assert.assertTrue(null != result);
        Log.info("tokenBalance-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeTokenBalanceParams(String contractAddress0, String address) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress0);
        params.put("address", address);
        return params;
    }

    /**
     * 账户token资产
     */
    @Test
    public void tokenAssetsList() throws Exception {
        Map params = this.makeTokenAssetsListParams(sender, 1, 10);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TOKEN_ASSETS_LIST, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TOKEN_ASSETS_LIST));
        Assert.assertTrue(null != result);
        Log.info("tokenAssetsList-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeTokenAssetsListParams(String address, int pageNumber, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("pageNumber", pageNumber);
        params.put("pageSize", pageSize);
        return params;
    }

    /**
     * 账户token交易列表
     */
    @Test
    public void tokenTransferList() throws Exception {
        Map params = this.makeTokenTransferListParams(sender, 1, 10);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TOKEN_TRANSFER_LIST, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TOKEN_TRANSFER_LIST));
        Assert.assertTrue(null != result);
        Log.info("tokenAssetsList-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeTokenTransferListParams(String address, int pageNumber, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("pageNumber", pageNumber);
        params.put("pageSize", pageSize);
        return params;
    }


    /**
     * 调用合约视图方法
     */
    @Test
    public void invokeView() throws Exception {
        String methodName = "balanceOf";
        String methodDesc = "";
        Map params = this.makeInvokeViewParams(contractAddress0, methodName, methodDesc, toAddress0);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, INVOKE_VIEW, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(INVOKE_VIEW));
        Assert.assertTrue(null != result);
        Log.info("invoke_view-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    private Map makeInvokeViewParams(String contractAddress0, String methodName, String methodDesc, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress0);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        return params;
    }

    /**
     * 获取合约构造函数
     */
    @Test
    public void constructor() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        Map params = this.makeConstructorParams(contractCode);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONSTRUCTOR, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONSTRUCTOR));
        Assert.assertTrue(null != result);
        Log.info("constructor-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeConstructorParams(byte[] contractCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractCode", HexUtil.encode(contractCode));
        return params;
    }

    /**
     * 验证删除合约
     */
    @Test
    public void validateDelete() throws Exception {
        Map params = this.makeValidateDeleteParams(sender, contractAddress0);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, VALIDATE_DELETE, params);
        Log.info("validateDelete-Response:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        Assert.assertTrue(cmdResp2.isSuccess());
    }

    private Map makeValidateDeleteParams(String sender, String contractAddress0) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("contractAddress", contractAddress0);
        return params;
    }

    /**
     * 查交易模块的确认交易
     */
    @Test
    public void getConfirmTx() throws Exception {
        this.getTxCfmClient(callHash);
    }

    private void getTxCfmClient(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.info(JSONUtils.obj2PrettyJson(record));
    }

}
