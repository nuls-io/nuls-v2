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

package io.nuls.contract.tx;


import io.nuls.base.data.BlockHeader;
import io.nuls.contract.basetest.ContractTest;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.tx.base.Base;
import io.nuls.contract.util.ContractUtil;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

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
public class ContractSendTxTest extends Base {

    static String address20 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String address21 = "5MR_2CbdqKcZktcxntG14VuQDy8YHhc6ZqW";
    static String address22 = "5MR_2Cj9tfgQpdeF7nDy5wyaGG6MZ35H3rA";
    static String address23 = "5MR_2CWKhFuoGVraaxL5FYY3RsQLjLDN7jw";
    static String address24 = "5MR_2CgwCFRoJ8KX37xNqjjR7ttYuJsg8rk";

    static String address25 = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
    static String address26 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String address27 = "5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM";
    static String address28 = "5MR_2CfUsasd33vQV3HqGw6M3JwVsuVxJ7r";
    static String address29 = "5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF";

    private Chain chain;
    static int chainId = 2;
    static int assetId = 1;
    static String version = "1.0";

    static String password = "nuls123456";//"nuls123456";

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, 100000000L));
    }

    @Test
    public void getBlockHeader() throws NulsException {
        BlockHeader blockHeader3 = BlockCall.getBlockHeader(chainId, 3L);
        Log.info(Hex.toHexString(ContractUtil.getStateRoot(blockHeader3)) + ", " + blockHeader3.toString());
        BlockHeader blockHeader4 = BlockCall.getBlockHeader(chainId, 4L);
        Log.info(Hex.toHexString(ContractUtil.getStateRoot(blockHeader4)) + ", " + blockHeader4.toString());
    }

    @Test
    public void importPriKeyTest() {
        importPriKey("00d9748b9ba0cdee3bc9d45c09eb9928b5809c4132a0ef70b19779e72a22258f47", password);//打包地址 5MR_2CbDGZXZRc7SnBEKuCubTUkYi9JXcCu
        importPriKey("00def3b0f4bfad2a6abb5f6957829e752a1a30806edc35e98016425d578fdc4e77", password);//25 5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo
        importPriKey("1c2b9fd4417c1aad8ae9f24c982ff294eb50a6462b873b79a879e805a9990346", password);//26 tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG
        importPriKey("00c98ecfd3777745270cacb9afba17ef0284769a83ff2adb4106b8a0baaec9452c", password);//27 5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM
        importPriKey("23848d45b4b34aca8ff24b00949a25a2c9175faf283675128e189eee8b085942", password);//28 5MR_2CfUsasd33vQV3HqGw6M3JwVsuVxJ7r
        importPriKey("009560d5ed6587822b7aee6f318f50b312c281e4f330b6990396881c6d3f870bc1", password);//29 5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF
    }

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String sender = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
        String remark = "create contract test - 空气币";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makeCreateParams(sender, contractCode, remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        Assert.assertTrue(null != result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Log.info("createContract-txHash:{}, contractAddress:{}", hash, contractAddress);
    }
    private Map makeCreateParams(String sender, byte[] contractCode, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("password", password);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractCode", Hex.toHexString(contractCode));
        params.put("args", args);
        params.put("remark", remark);
        return params;
    }


    /**
     * 预创建合约
     */
    @Test
    public void preCreateContract() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String sender = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
        String remark = "create contract test - 空气币";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makePreCreateParams(sender, contractCode, remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, PRE_CREATE, params);
        Assert.assertTrue(cmdResp2.isSuccess());
        Log.info("pre_create-Response:{}", cmdResp2);
    }
    private Map makePreCreateParams(String sender, byte[] contractCode, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("password", password);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractCode", Hex.toHexString(contractCode));
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
        String sender = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makeImputedCreateGasParams(sender, contractCode, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, IMPUTED_CREATE_GAS, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(IMPUTED_CREATE_GAS));
        Assert.assertTrue(null != result);
        Log.info("imputed_create_gas-result:{}", result);
    }
    private Map makeImputedCreateGasParams(String sender, byte[] contractCode, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("contractCode", Hex.toHexString(contractCode));
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
        String sender = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makeValidateCreateParams(sender, contractCode, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, VALIDATE_CREATE, params);
        Assert.assertTrue(cmdResp2.isSuccess());
        Log.info("validate_create-Response:{}", cmdResp2);
    }
    private Map makeValidateCreateParams(String sender, byte[] contractCode, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractCode", Hex.toHexString(contractCode));
        params.put("args", args);
        return params;
    }

    /**
     * 调用合约
     */
    @Test
    public void callContract() throws Exception {
        // txHash:0020ea3c6f17d6edaee865b5c1eae0a50fdc27dbcaee19829799c2722b0180b6ca06
        // contractAddress:5MR_3Q7wWxaKnh4X74kYkubbNa4jouHMMYX
        String sender = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
        BigInteger value = BigInteger.ZERO;
        String contractAddress = "5MR_3Q7wWxaKnh4X74kYkubbNa4jouHMMYX";//TODO pierre 待定
        String methodName = "transfer";
        String methodDesc = "";
        String remark = "call contract test - 空气币转账";
        String toAddress = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String token = BigInteger.TEN.pow(8).toString();
        Map params = this.makeCallParams(sender, value, contractAddress, methodName, methodDesc, remark, toAddress, token);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", result);
    }
    private Map makeCallParams(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        params.put("password", password);
        params.put("remark", remark);
        return params;
    }

    /**
     * 验证调用合约
     */
    @Test
    public void validateCall() throws Exception {
        String sender = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
        BigInteger value = BigInteger.ZERO;
        String contractAddress = "5MR_3Q7wWxaKnh4X74kYkubbNa4jouHMMYX";//TODO pierre 待定
        String methodName = "transfer";
        String methodDesc = "";
        String toAddress = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String token = BigInteger.TEN.pow(8).toString();
        Map params = this.makeValidateCallParams(sender, value, contractAddress, methodName, methodDesc, toAddress, token);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("validateCall-result:{}", result);
    }
    private Map makeValidateCallParams(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("gasLimit", 200000L);
        params.put("price", 25);
        params.put("contractAddress", contractAddress);
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
        String sender = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
        BigInteger value = BigInteger.ZERO;
        String contractAddress = "5MR_3Q7wWxaKnh4X74kYkubbNa4jouHMMYX";//TODO pierre 待定
        String methodName = "transfer";
        String methodDesc = "";
        String toAddress = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        String token = BigInteger.TEN.pow(8).toString();
        Map params = this.makeImputedCallGasParams(sender, value, contractAddress, methodName, methodDesc, toAddress, token);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, IMPUTED_CALL_GAS, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(IMPUTED_CALL_GAS));
        Assert.assertTrue(null != result);
        Log.info("imputed_call_gas-result:{}", result);
    }
    private Map makeImputedCallGasParams(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        return params;
    }

    /**
     * 调用合约视图方法
     */
    @Test
    public void invokeView() throws Exception {
        String contractAddress = "5MR_3Q7wWxaKnh4X74kYkubbNa4jouHMMYX";//TODO pierre 待定
        String methodName = "balanceOf";
        String methodDesc = "";
        String toAddress = "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo";
        Map params = this.makeInvokeViewParams(contractAddress, methodName, methodDesc, toAddress);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, INVOKE_VIEW, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(INVOKE_VIEW));
        Assert.assertTrue(null != result);
        Log.info("invoke_view-result:{}", result);
    }
    private Map makeInvokeViewParams(String contractAddress, String methodName, String methodDesc, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("contractAddress", contractAddress);
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
        Log.info("constructor-result:{}", result);
    }
    private Map makeConstructorParams(byte[] contractCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("contractCode", Hex.toHexString(contractCode));
        return params;
    }

    /**
     * 获取合约基本信息
     */
    @Test
    public void contractInfo() throws Exception {
        String contractAddress = "5MR_3Q7wWxaKnh4X74kYkubbNa4jouHMMYX";//TODO pierre 待定
        Map params = this.makeContractInfoParams(contractAddress);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_INFO, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_INFO));
        Assert.assertTrue(null != result);
        Log.info("contract_info-result:{}", result);
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
        String hash = "0020a8df5ef806e4c65e31ce41e2ebc0e64b19c18fcce8df525a509a3676cb648384";
        Map params = this.makeContractResultParams(hash);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_RESULT, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_RESULT));
        Assert.assertTrue(null != result);
        Log.info("contractResult-result:{}", result);
    }
    private Map makeContractResultParams(String hash) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("hash", hash);
        return params;
    }

    /**
     * 获取合约交易详情
     */
    @Test
    public void contractTx() throws Exception {
        String hash = "0020ea3c6f17d6edaee865b5c1eae0a50fdc27dbcaee19829799c2722b0180b6ca06";
        Map params = this.makeContractTxParams(hash);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CONTRACT_TX, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CONTRACT_TX));
        Assert.assertTrue(null != result);
        Log.info("contractTx-result:{}", result);
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
    public void getTxRecord() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", "5MR_3Q7wWxaKnh4X74kYkubbNa4jouHMMYX");
        params.put("assetChainId", null);
        params.put("assetId", null);
        params.put("type", null);
        params.put("pageSize", null);
        params.put("pageNumber", null);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxs", params);
        Map record = (Map) dpResp.getResponseData();
        Log.info("Page<TransactionPO>:{}", JSONUtils.obj2PrettyJson(record));
    }

    /**
     * 查交易模块的交易
     */
    @Test
    public void getTx() throws Exception {
        String hash = "0020ea3c6f17d6edaee865b5c1eae0a50fdc27dbcaee19829799c2722b0180b6ca06";
        this.getTxClient(hash);
    }
    private void getTxClient(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.info("{}", JSONUtils.obj2PrettyJson(record));
    }

    /**
     * 查交易模块的确认交易
     */
    @Test
    public void getConfirmTx() throws Exception {
        String hash = "0020ea3c6f17d6edaee865b5c1eae0a50fdc27dbcaee19829799c2722b0180b6ca06";
        this.getTxCfmClient(hash);
    }
    private void getTxCfmClient(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.info("", JSONUtils.obj2PrettyJson(record));
    }

    private void importPriKey(String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);

            params.put("priKey", priKey);
            params.put("password", pwd);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
            Log.info("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
