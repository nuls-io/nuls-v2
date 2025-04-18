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

package io.nuls.contract.tx.nrc20;


import io.nuls.contract.mock.basetest.ContractTest;
import io.nuls.contract.model.dto.AccountAmountDto;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramMultyAssetValue;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
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

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractNRC20TokenSendTxTest extends BaseQuery {

    /**
     * Create Contract
     */
    @Test
    public void createContract() throws Exception {
        //sender = toAddress32;
        InputStream in = new FileInputStream(ContractTest.class.getResource("/contract/cross-locked-nrc20").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test";
        String name = "DDD";
        String symbol = "DDD";
        String amount = "100000000";
        String decimals = "8";
        Map params = this.makeCreateParams("tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", contractCode, "aliaser", remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map)(map.get("contractResult"))).get("success"));
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(map));
    }

    /**
     * Call Contract
     */
    @Test
    public void callContract() throws Exception {
        BigInteger value = BigInteger.ZERO;
        if(StringUtils.isBlank(methodName)) {
            methodName = "transfer";
        }
        if(StringUtils.isBlank(tokenReceiver)) {
            tokenReceiver = toAddress1;
        }
        tokenReceiver = "tNULSeBaN5QRhYVp361kB9bvhvujN7QiBST4wf";
        String methodDesc = "";
        String remark = "call contract test - Air coin transfer";
        String token = BigInteger.valueOf(200000000000000L).toString();
        Map params = this.makeCallParams(sender, value, "tNULSeBaN6fGF3hwSrQsHq7B2GhtQ1W1ZMT9Nx", methodName, methodDesc, remark, tokenReceiver, token);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    public void callContractWithParam(Long gasLimit) throws Exception {
        BigInteger value = BigInteger.ZERO;
        methodName = "transfer";
        tokenReceiver = toAddress1;
        String methodDesc = "";
        String remark = "call contract test - Air coin transfer";
        String token = BigInteger.valueOf(800L).toString();
        Map params = this.makeCallParams(sender, value, gasLimit, contractAddress_nrc20, methodName, methodDesc, remark, tokenReceiver, token);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        //String hash = (String) result.get("txHash");
        //Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    /**
     * tokenTransfer
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

    /**
     * Transfer funds to another account while calling the contract
     */
    @Test
    public void callContractWithNulsValueToOthers() throws Exception {
        sender = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        tokenReceiver = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
        contractAddress_nrc20 = "tNULSeBaN8UY4k5qD9SG8GjMJNKhERBN7cgtEG";

        BigInteger value = BigInteger.ZERO;
        methodName = "transfer";
        // "tNULSeBaMkzsRE6qc9RVoeY6gHq8k1xSMcdrc7",
        // "tNULSeBaMfXDQeT4MJZim1RusCJRPx5j9bMKQN"
        AccountAmountDto[] amountDtos = new AccountAmountDto[]{
                new AccountAmountDto(BigInteger.valueOf(300000000L), "tNULSeBaMkzsRE6qc9RVoeY6gHq8k1xSMcdrc7")
        };
        String methodDesc = "";
        String remark = "call contract test - At the same time as transferring the money to another account, transfer the money to another account";
        String token = BigInteger.valueOf(800L).toString();
        Map params = this.makeCallParams(
                sender, value, 2000000L, 25L, contractAddress_nrc20, methodName, methodDesc, remark, null, amountDtos, new Object[]{tokenReceiver, token});
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    /**
     * Delete contract
     */
    @Test
    public void delete() throws Exception {
        //contractAddress_nrc20 = "tNULSeBaNBfqo6FC8jJJiXFE1gcSQU2D3UwQ1b";
        String remark = "delete contract";
        Map params = this.makeDeleteParams(sender, contractAddress_nrc20, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, DELETE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(DELETE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }
    private Map makeDeleteParams(String sender, String contractAddress, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("contractAddress", contractAddress);
        params.put("password", password);
        params.put("remark", remark);
        return params;
    }

    /**
     * Transfer to the contracted address(Failure situation,NRC20Contract not acceptedNULSTransfer)
     */
    @Test
    public void transfer2ContractFailed() throws Exception {
        BigInteger value = BigInteger.TEN.pow(8);
        String remark = "transfer 2 contract";
        Map params = this.makeTransferParams(sender, contractAddress_nrc20, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TRANSFER, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TRANSFER));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

}
