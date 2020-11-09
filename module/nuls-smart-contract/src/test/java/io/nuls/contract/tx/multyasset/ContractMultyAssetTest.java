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
package io.nuls.contract.tx.multyasset;

import io.nuls.contract.mock.basetest.ContractTest;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.CALL;
import static io.nuls.contract.constant.ContractCmdConstant.CREATE;

/**
 * @author: PierreLuo
 * @date: 2020-10-30
 */
public class ContractMultyAssetTest extends BaseQuery {

    protected long gasLimit = 200000L;
    protected long gasPrice = 1L;
    protected long minutes_3 = 60 * 3;

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/multi-asset-contract").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "test multy asset";
        Map params = this.makeCreateParams(sender, contractCode, "asset", remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Map map = waitGetContractTx(hash);
        assertTrue(map);
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(map));
    }

    /**
     * 注册一个资产
     */
    @Test
    public void assetRegisterTest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("assetSymbol", "MTA");
        params.put("assetName", "MTA");
        params.put("initNumber", 100000000);
        params.put("decimalPlace", 8);
        params.put("txCreatorAddress", sender);
        params.put("assetOwnerAddress", sender);
        params.put("password", "nuls123456");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "chainAssetTxReg", params);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    /**
     * 转入NULS，转出NULS，转出NULS锁定
     */
    @Test
    public void nulsTest() throws Exception {
        // 转入 3.2 NULS
        this.callByParams("_payable", "3.2", null);
        // 转出 1.1 NULS
        Object[] args = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger()};
        this.callByParams("transferNuls", "0", args);
        // 转出 1.2 NULS
        Object[] argsLock = new Object[]{toAddress17, new BigDecimal("1.2").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), minutes_3};
        this.callByParams("transferNulsLock", "0", argsLock);
    }

    /**
     * 其他资产转入、转出、转出锁定
     * <p>
     * 如 2-2, 假设资产decimals=8
     */
    @Test
    public void otherAssetTest() throws Exception {
        // 转入 3.2
        this.callOfDesignatedAssetByParams("_payableMultyAsset", "3.2", null, 2, 2);
        // 转出 1.1
        Object[] args = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 2, 2};
        this.callOfDesignatedAssetByParams("transferDesignatedAsset", "0", args, 0, 0);
        // 转出 1.2
        Object[] argsLock = new Object[]{toAddress17, new BigDecimal("1.2").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 2, 2, minutes_3};
        this.callOfDesignatedAssetByParams("transferDesignatedAssetLock", "0", argsLock, 0, 0);
    }


    /**
     * 内部调用其他合约, 转入NULS，转出NULS，转出NULS(锁定)
     */
    @Test
    public void innerCall() throws Exception {
        String methodName = "callOtherContract";
        String otherContract = "tNULSeBaN2zgVKHYKQknBbMgegR5X7DzNet8xh";
        // 转入 6.6 NULS (外部合约)
        this.callByParams("_payable", "6.6", null);
        // 转入 6.6 NULS (内部合约)
        this.innerCallByParams(methodName, otherContract, "_payable", null, "6.6");
        // 转出 3.3 NULS
        Object[] innerArgs = new Object[]{toAddress17, new BigDecimal("3.3").multiply(BigDecimal.TEN.pow(8)).toBigInteger()};
        this.innerCallByParams(methodName, otherContract, "transferNuls", innerArgs, "0");
        // 转出 1.1 NULS(锁定)
        Object[] innerArgsLock = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), minutes_3};
        this.innerCallByParams(methodName, otherContract, "transferNulsLock", innerArgsLock, "0");
    }

    /**
     * 内部调用其他合约, 转入NULS，转出NULS，转出NULS(锁定)
     * <p>
     * 内部调用带返回值
     */
    @Test
    public void innerCallWithReturnValue() throws Exception {
        String methodName = "callWithReturnValueOfOtherContract";
        String otherContract = "tNULSeBaN2zgVKHYKQknBbMgegR5X7DzNet8xh";
        // 转入 6.6 NULS (外部合约)
        this.callByParams("_payable", "6.6", null);
        // 转入 6.6 NULS (内部合约)
        this.innerCallByParams(methodName, otherContract, "_payable", null, "6.6");
        // 转出 3.3 NULS
        Object[] innerArgs = new Object[]{toAddress17, new BigDecimal("3.3").multiply(BigDecimal.TEN.pow(8)).toBigInteger()};
        this.innerCallByParams(methodName, otherContract, "transferNuls", innerArgs, "0");
        // 转出 1.1 NULS(锁定)
        Object[] innerArgsLock = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), minutes_3};
        this.innerCallByParams(methodName, otherContract, "transferNulsLock", innerArgsLock, "0");
    }

    /**
     * 内部调用其他合约, 转入、转出、转出锁定 其他资产
     * 如 2-2, 假设资产decimals=8
     * 内部调用带返回值
     */
    @Test
    public void innerCallWithReturnValueOfDesignatedAsset() throws Exception {
        String methodName = "callWithReturnValueOfOtherContractOfDesignatedAsset";
        String otherContract = "tNULSeBaN2zgVKHYKQknBbMgegR5X7DzNet8xh";
        // 转入 6.6 2-2 (外部合约)
        this.callOfDesignatedAssetByParams("_payableMultyAsset", "6.6", null, 2, 2);
        // 转入 6.6 2-2 (内部合约)
        this.innerCallOfDesignatedAssetByParams(methodName, otherContract, "_payableMultyAsset", null, "6.6", 2, 2);
        // 转出 3.3 2-2
        Object[] innerArgs = new Object[]{toAddress17, new BigDecimal("3.3").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 2, 2};
        this.innerCallOfDesignatedAssetByParams(methodName, otherContract, "transferDesignatedAsset", innerArgs, "0", 0, 0);
        // 转出 1.1 2-2(锁定)
        Object[] innerArgsLock = new Object[]{toAddress17, new BigDecimal("1.1").multiply(BigDecimal.TEN.pow(8)).toBigInteger(), 2, 2, minutes_3};
        this.innerCallOfDesignatedAssetByParams(methodName, otherContract, "transferDesignatedAssetLock", innerArgsLock, "0", 0, 0);
    }

    /**
     * 根据注册资产的交易hash查询资产信息
     */
    @Test
    public void getAssetRegInfoByHashTest() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("txHash", "b51947d09b1eeca55de84703f840faf2638257f6d1b833e46efcc62229383b43");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetRegInfoByHash", params);
        System.out.println(JSONUtils.obj2PrettyJson(response));
    }

    protected void callByParams(String methodName, String valueStr, Object[] args) throws Exception {
        BigInteger value = new BigDecimal(valueStr).multiply(BigDecimal.TEN.pow(8)).toBigInteger();
        Map params = this.makeCallParams(sender, value, contractAddress, methodName, null, "", args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    protected void callOfDesignatedAssetByParams(String methodName, String valueStr, Object[] args, int assetChainId, int assetId) throws Exception {
        BigInteger value = new BigDecimal(valueStr).multiply(BigDecimal.TEN.pow(8)).toBigInteger();
        Map params = this.makeCallParams(sender, value, gasLimit, gasPrice, contractAddress, methodName, null, "", assetChainId, assetId, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    protected void innerCallByParams(String methodName, String otherContract, String innerMethod, Object[] innerArgs, String innerValueStr) throws Exception {
        BigInteger innerValue = new BigDecimal(innerValueStr).multiply(BigDecimal.TEN.pow(8)).toBigInteger();
        Object[] args = new Object[]{otherContract, innerMethod, innerArgs, innerValue};
        Map params = this.makeCallParams(sender, BigInteger.ZERO, contractAddress, methodName, null, "", args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    protected void innerCallOfDesignatedAssetByParams(String methodName, String otherContract, String innerMethod, Object[] innerArgs, String innerValueStr, int assetChainId, int assetId) throws Exception {
        BigInteger innerValue = new BigDecimal(innerValueStr).multiply(BigDecimal.TEN.pow(8)).toBigInteger();
        Object[] args = new Object[]{otherContract, innerMethod, innerArgs, innerValue, assetChainId, assetId};
        Map params = this.makeCallParams(sender, BigInteger.ZERO, contractAddress, methodName, null, "", args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }
}
