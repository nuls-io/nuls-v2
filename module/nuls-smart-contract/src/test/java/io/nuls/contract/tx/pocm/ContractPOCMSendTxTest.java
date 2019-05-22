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

package io.nuls.contract.tx.pocm;


import io.nuls.contract.basetest.ContractTest;
import io.nuls.contract.sdk.annotation.View;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.nuls.contract.constant.ContractCmdConstant.CALL;
import static io.nuls.contract.constant.ContractCmdConstant.CREATE;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractPOCMSendTxTest extends BaseQuery {

    /**
     * 创建合约
     */
    @Test
    public void createContract() throws Exception {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/pocmContract-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        System.out.println(HexUtil.encode(contractCode));
        String remark = "POCM - consensus contract test - POCM_共识合约";
        Object[] args = new Object[]{"pocManager", "POCM", 100000000, 8, 5000, 5, 200, 5, true, "tNULSeBaMtEPLXxUgyfnBt9bpb5Xv84dyJV98p", null, null, null, null};
        Map params = this.makeCreateParams(sender, contractCode, remark, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    /**
     * 调用合约 - 项目发布者创建节点
     */
    @Test
    public void createAgent() throws Exception {
        BigInteger value = BigInteger.valueOf(20001_00000000L);
        String methodName = "createAgentByOwner";
        String methodDesc = "";
        String remark = "项目发布者创建节点";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * 调用合约 - 投资者抵押
     */
    @Test
    public void depositForOwn() throws Exception {
        BigInteger value = BigInteger.valueOf(20000_00000000L);
        String methodName = "depositForOwn";
        String methodDesc = "";
        String remark = "投资者抵押";
        this.invokeCall(toAddress2, value, contractAddress, methodName, methodDesc, remark);
    }
    /**
     * 调用合约 - 投资者退出抵押
     */
    @Test
    public void quit() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "quit";
        String methodDesc = "";
        String remark = "投资者退出抵押";
        this.invokeCall(toAddress1, value, contractAddress, methodName, methodDesc, remark, 0);
    }

    /**
     * 调用合约 - 共识保证金解锁后，退还所有申请过退出的用户的押金 - 合约拥有者操作
     */
    @Test
    public void refundAllUnLockDepositByOwner() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "refundAllUnLockDepositByOwner";
        String methodDesc = "";
        String remark = "共识保证金解锁后，退还所有申请过退出的用户的押金 - 合约拥有者操作";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * 调用合约 - 共识保证金解锁后，退还申请过退出的用户的押金 - 投资用户操作
     */
    @Test
    public void takeBackUnLockDeposit() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "takeBackUnLockDeposit";
        String methodDesc = "";
        String remark = "共识保证金解锁后，退还申请过退出的用户的押金 - 投资用户操作";
        this.invokeCall(toAddress0, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * 调用合约 - 合约创建者获取共识奖励金额
     */
    @Test
    public void transferConsensusReward() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "transferConsensusRewardByOwner";
        String methodDesc = "";
        String remark = "合约创建者获取共识奖励金额";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * 调用合约 - 合约拥有者赎回共识保证金
     */
    @Test
    public void takeBackConsensusCreateAgentDepositByOwner() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "takeBackConsensusCreateAgentDepositByOwner";
        String methodDesc = "";
        String remark = "合约拥有者赎回共识保证金";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * 调用合约 - 合约拥有者委托自己的节点
     */
    @Test
    public void depositConsensusManuallyByOwner() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "depositConsensusManuallyByOwner";
        String methodDesc = "";
        String remark = "合约拥有者委托自己的节点";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * 调用合约 - 合约拥有者注销节点
     */
    @Test
    public void stopAgentManuallyByOwner() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "stopAgentManuallyByOwner";
        String methodDesc = "";
        String remark = "合约拥有者注销节点";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * 调用视图方法 - 合约整体信息
     */
    @View
    @Test
    public void getContractWholeInfo() throws Exception {
        String methodName = "wholeConsensusInfoForTest";
        Log.info(invokeView(contractAddress, methodName));
    }

    private void invokeCall(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, String remark, Object... args) throws Exception {
        Map params = this.makeCallParams(sender, value, contractAddress, methodName, methodDesc, remark, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
        TimeUnit.SECONDS.sleep(1);
        getContractWholeInfo();
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

}
