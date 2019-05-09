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

package io.nuls.contract.tx.consensus;


import io.nuls.contract.basetest.ContractTest;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
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

import static io.nuls.contract.constant.ContractCmdConstant.CALL;
import static io.nuls.contract.constant.ContractCmdConstant.CREATE;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ConsensusSendTxTest extends BaseQuery {

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/contract-consensus-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "consensus contract test - 共识合约";
        Map params = this.makeCreateParams(sender, contractCode, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        Assert.assertTrue(null != result);
        Log.info("Create-Contract-result:{}", JSONUtils.obj2PrettyJson(result));
    }

    private Map makeCreateParams(String sender, byte[] contractCode, String remark, Object... args) {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
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
     * 调用视图方法 - 合约委托信息
     */
    @Test
    public void getContractDepositInfo() throws Exception {
        String methodName = "getContractDepositInfo";
        String joinAgentHash = "";
        Log.info(invokeView(contractAddress, methodName, joinAgentHash));
    }

    /**
     * 调用视图方法 - 合约节点信息
     */
    @Test
    public void getContractAgentInfo() throws Exception {
        String methodName = "getContractAgentInfo";
        String agentHash = "eb3ad5519066962cf4da81036ba77847d7199529043fb3e6f1d93803cc0bfd74";
        Log.info(invokeView(contractAddress, methodName, agentHash));
    }

    /**
     * 调用视图方法 - 创建节点hash
     */
    @Test
    public void getAgentHash() throws Exception {
        String methodName = "getAgentHash";
        Log.info(invokeView(contractAddress, methodName));
    }

    /**
     * 调用视图方法 - 委托节点hash
     */
    @Test
    public void getDepositHash() throws Exception {
        String methodName = "getDepositHash";
        Log.info(invokeView(contractAddress, methodName));
    }

    /**
     * 调用视图方法 - 退出节点hash
     */
    @Test
    public void getWithdrawHash() throws Exception {
        String methodName = "getWithdrawHash";
        Log.info(invokeView(contractAddress, methodName));
    }

    /**
     * 调用视图方法 - 注销节点hash
     */
    @Test
    public void getStopHash() throws Exception {
        String methodName = "getStopHash";
        Log.info(invokeView(contractAddress, methodName));
    }

    /**
     * 调用视图方法 - 获取指定地址的奖励明细
     */
    @Test
    public void getMinerInfo() throws Exception {
        String methodName = "getMinerInfo";
        String address = "";
        Log.info(invokeView(contractAddress, methodName, address));
    }

    /**
     * 调用视图方法 - 获取所有委托人地址
     */
    @Test
    public void getMiners() throws Exception {
        String methodName = "getMiners";
        Log.info(invokeView(contractAddress, methodName));
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
     * 调用合约 - 创建共识节点
     */
    @Test
    public void createAgent() throws Exception {
        BigInteger value = BigInteger.valueOf(30001L);
        String methodName = "createAgent";
        String methodDesc = "";
        String remark = "createAgent test - 合约创建节点";
        Map params = this.makeCallParams(sender, value, contractAddress, methodName, methodDesc, remark, 200001);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    /**
     * 调用合约 - 合约委托共识节点
     */
    @Test
    public void deposit() throws Exception {
        BigInteger value = BigInteger.valueOf(3000L);
        String methodName = "deposit";
        String methodDesc = "";
        String remark = "contract deposit test - 合约委托共识节点";
        String agentHash = "";//TODO pierre
        int depositNuls = 2001;

        Map params = this.makeCallParams(sender, value, contractAddress, methodName, methodDesc, remark,
                agentHash, depositNuls);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    /**
     * 调用合约 - 合约退出委托共识节点
     */
    @Test
    public void withdraw() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "withdraw";
        String methodDesc = "";
        String remark = "contract deposit test - 合约退出委托共识节点";
        String joinAgentHash = "";//TODO pierre

        Map params = this.makeCallParams(sender, value, contractAddress, methodName, methodDesc, remark,
                joinAgentHash);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

    /**
     * 调用合约 - 合约注销共识节点
     */
    @Test
    public void stopAgent() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "stopAgent";
        String methodDesc = "";
        String remark = "contract stop agent test - 合约注销共识节点";

        Map params = this.makeCallParams(sender, value, contractAddress, methodName, methodDesc, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        Assert.assertTrue(null != result);
        Log.info("call-result:{}", JSONUtils.obj2PrettyJson(cmdResp2));
    }

}
