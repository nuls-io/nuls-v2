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

package io.nuls.contract.tx.contractcallcontract;


import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.crypto.HexUtil;
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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractCallContractQueryTest extends BaseQuery {

    /**
     * 验证创建合约
     */
    @Test
    public void validateCreate() throws Exception {
        InputStream in = new FileInputStream(ContractCallContractQueryTest.class.getResource("/contract_call_contract").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        Map params = this.makeValidateCreateParams(sender, contractCode);
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
     * 估算创建合约的gas
     */
    @Test
    public void imputedCreateGas() throws Exception {
        InputStream in = new FileInputStream(ContractCallContractQueryTest.class.getResource("/contract_call_contract").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        Map params = this.makeImputedCreateGasParams(sender, contractCode);
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
     * 验证调用合约 - 合约内部转账
     */
    @Test
    public void validateCall() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "multyForAddress";
        String methodDesc = "";
        String address1 = toAddress1;
        String address2 = toAddress2;
        String value1 = BigInteger.TEN.pow(8).toString();
        String value2 = BigInteger.TEN.pow(10).toString();
        Map params = this.makeValidateCallParams(sender, value, contractAddress0, methodName, methodDesc, address1, value1, address2, value2);
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
     * 验证调用合约 - 合约调用合约
     */
    @Test
    public void validateCall_contractCallContract() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "callContractWithReturnValue";
        String methodDesc = "";

        String _methodName = "transfer";
        String _token = BigInteger.TEN.pow(8).toString();
        String[] _args = new String[]{toAddress1, _token};
        BigInteger _value = value;
        Map params = this.makeValidateCallParams(sender, value, contractAddress0, methodName, methodDesc,
                contractAddress_nrc200, _methodName, _args, _value);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, VALIDATE_CALL, params);
        Log.info("validateCall-Response:{}", JSONUtils.obj2PrettyJson(cmdResp2));
        Assert.assertTrue(cmdResp2.isSuccess());
    }

    /**
     * 估算调用合约的gas - 合约内部转账
     */
    @Test
    public void imputedCallGas() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "multyForAddress";
        String methodDesc = "";
        String address1 = toAddress1;
        String address2 = toAddress2;
        String value1 = BigInteger.TEN.pow(8).toString();
        String value2 = BigInteger.TEN.pow(10).toString();
        Map params = this.makeImputedCallGasParams(sender, value, contractAddress0, methodName, methodDesc, address1, value1, address2, value2);
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
     * 估算调用合约的gas - 合约调用合约
     */
    @Test
    public void imputedCallGas_contractCallContract() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "callContractWithReturnValue";
        String methodDesc = "";

        String _methodName = "transfer";
        String _token = BigInteger.TEN.pow(8).toString();
        String[] _args = new String[]{toAddress1, _token};
        BigInteger _value = value;
        Map params = this.makeImputedCallGasParams(sender, value, contractAddress0, methodName, methodDesc,
                contractAddress_nrc200, _methodName, _args, _value);

        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, IMPUTED_CALL_GAS, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(IMPUTED_CALL_GAS));
        Assert.assertTrue(null != result);
        Log.info("imputed_call_gas-result:{}", JSONUtils.obj2PrettyJson(result));
    }


    /**
     * 获取向合约地址转账的手续费
     */
    @Test
    public void transfer2ContractFee() throws Exception {
        BigInteger value = BigInteger.TEN.pow(11);
        String remark = "transfer 2 contract fee";
        Map params = this.makeTransferFeeParams(sender, contractAddress0, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TRANSFER_FEE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TRANSFER_FEE));
        Assert.assertTrue(null != result);
        Log.info("transfer2ContractFee-result:{}", JSONUtils.obj2PrettyJson(result));
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
        Map params = this.makeTokenBalanceParams(contractAddress_nrc200, contractAddress0);
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
        Log.info(invokeView(contractAddress0, methodName, toAddress0));
    }

    /**
     * 获取合约构造函数
     */
    @Test
    public void constructor() throws Exception {
        InputStream in = new FileInputStream(ContractCallContractQueryTest.class.getResource("/contract_call_contract").getFile());
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
