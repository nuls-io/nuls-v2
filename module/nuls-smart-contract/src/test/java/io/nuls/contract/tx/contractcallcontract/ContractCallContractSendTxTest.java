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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractCallContractSendTxTest extends BaseQuery {

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        InputStream in = new FileInputStream(ContractCallContractSendTxTest.class.getResource("/contract_call_contract").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test - 合约内部转账，合约调用合约";
        Map params = this.makeCreateParams(sender, contractCode, "inner_call", remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        Assert.assertTrue(JSONUtils.obj2PrettyJson(cmdResp2), null != result);
        Log.info("Create-InnerCall-Contract-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    /**
     * 向合约地址转账
     */
    @Test
    public void transfer2Contract() throws Exception {
        BigInteger value = BigInteger.valueOf(888834777633L);
        String remark = "transfer 2 contract";
        Map params = this.makeTransferParams(sender, contractAddress, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TRANSFER, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TRANSFER));
        Assert.assertTrue(null != result);
        Log.info("transfer2Contract-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    /**
     * 调用合约 - 合约内部转账
     */
    @Test
    public void callContract_transferOut() throws Exception {
        BigInteger value = BigInteger.ZERO;
        if(StringUtils.isBlank(methodName)) {
            methodName = "multyForAddress";
        }
        String methodDesc = "";
        String remark = "call contract test - 合约内部转账";
        String address1 = toAddress3;
        String address2 = toAddress4;
        String value1 = BigInteger.valueOf(1_0000_0000L).toString();
        String value2 = BigInteger.valueOf(1_0000_0000L).toString();
        Map params = this.makeCallParams(sender, value, contractAddress, methodName, methodDesc, remark,
                address1, value1, address2, value2);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    /**
     * 调用合约 - 合约调用合约
     */
    @Test
    public void callContract_contractCallContract() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "callContractWithReturnValue";
        String methodDesc = "";
        String remark = "call contract test - 合约调用合约";

        String _methodName = "transfer";
        String _token = BigInteger.valueOf(800).toString();
        String[] _args = new String[]{toAddress3, _token};
        BigInteger _value = value;
        Map params = this.makeCallParams(sender, value, contractAddress0, methodName, methodDesc, remark,
                contractAddress_nrc200, _methodName, _args, _value);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    /**
     * 调用合约 - 合约内部转账 && 合约调用合约
     */
    @Test
    public void callContract_transferOut_contractCallContract() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "multyForAddressAndcallContractWithReturnValue";
        String methodDesc = "";
        String remark = "call contract test - 合约内部转账 && 合约调用合约";

        String address1 = toAddress3;
        String address2 = toAddress4;
        String value1 = BigInteger.valueOf(11881188L).toString();
        String value2 = BigInteger.valueOf(22882288L).toString();

        String _methodName = "transfer";
        String _token = BigInteger.valueOf(888L).toString();
        String[] _args = new String[]{toAddress3, _token};
        BigInteger _value = value;
        Map params = this.makeCallParams(sender, value, contractAddress0, methodName, methodDesc, remark,
                address1, value1, address2, value2,
                contractAddress_nrc200, _methodName, _args, _value);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }


    /**
     * 删除合约
     */
    @Test
    public void delete() throws Exception {
        String remark = "delete contract";
        Map params = this.makeDeleteParams(sender, contractAddress, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, DELETE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(DELETE));
        Assert.assertTrue(null != result);
        Log.info("delete-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeDeleteParams(String sender, String contractAddress0, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("contractAddress", contractAddress0);
        params.put("password", password);
        params.put("remark", remark);
        return params;
    }

}
