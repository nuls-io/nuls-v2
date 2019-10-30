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

package io.nuls.contract.tx.pocm;


import io.nuls.contract.mock.basetest.ContractTest;
import io.nuls.contract.sdk.annotation.View;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.nuls.contract.constant.ContractCmdConstant.*;
import static io.nuls.contract.constant.ContractCmdConstant.TRANSFER;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractPOCMSendTxTest extends BaseQuery {

    /**
     * 创建合约
     */
    @Test
    public void createConsensusEnhancementContract() throws Exception {
        String filePath = ContractPOCMSendTxTest.class.getResource("/pocmContract-v3-test2.jar").getFile();
        InputStream in = new FileInputStream(filePath);
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "POCM - consensus enhancement contract test - POCM_共识加强合约";
        // String tokenAddress, BigInteger cycleRewardTokenAmount, int awardingCycle,
        // BigInteger minimumDepositNULS, int minimumLocked, boolean openConsensus,
        // int lockedTokenDay, String authorizationCode, String rewardHalvingCycle, String maximumDepositAddressCount
        Object[] args = new Object[]{"tNULSeBaN152GXtPK5MZX57zeFR7QKvjNKkVA5", 5000, 2, 200, 2, false, 1, null, null, null};
        Map params = this.makeCreateParams(sender, contractCode, "pocm_enhancement", remark, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    /**
     * 流程 - 创建TOKEN, POCM, 添加节点，抵押，退出
     */
    @Test
    public void testConsensusDepositOthersProcessor() throws Exception {
        String authCode = "1a4123aa-7cbb-42f5-80be-8d8dc8331522";
        String nrc20Locked = nrc20Locked();
        this.contractAddress_nrc20 = nrc20Locked;
        String pocm = pocm(nrc20Locked, authCode);
        this.contractAddress = pocm;
        tokenTransfer();

        //Log.info("begin openConsensus");
        //this.invokeCall(sender, BigInteger.ZERO, contractAddress, "openConsensus", null, "remark");
        Log.info("begin addOtherAgent");
        this.invokeCall(sender, BigInteger.ZERO, contractAddress, "addOtherAgent", null, "remark", List.of("ceb529616399fa3356d5f7f26705bfc4b61792da1c6de996d8030f3af71edd78").toArray());
        Log.info("begin depositForOwn {}", sender);
        this.invokeCall(sender, BigInteger.valueOf(3000_00000000L), contractAddress, "depositForOwn", null, "remark");
        Log.info("begin depositForOwn {}", toAddress0);
        this.invokeCall(toAddress0, BigInteger.valueOf(2100_00000000L), contractAddress, "depositForOwn", null, "remark");
        Log.info("begin depositForOwn {}", toAddress1);
        this.invokeCall(toAddress1, BigInteger.valueOf(1200_00000000L), contractAddress, "depositForOwn", null, "remark");
        TimeUnit.SECONDS.sleep(50);
        Log.info("begin quit {}", sender);
        this.invokeCall(sender, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        Log.info("begin quit {}", toAddress0);
        this.invokeCall(toAddress0, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        Log.info("begin quit {}", toAddress1);
        this.invokeCall(toAddress1, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
    }

    /**
     * 流程 - 创建TOKEN, POCM, 抵押，领取
     */
    @Test
    public void testConsensusDepositReceiveAwardsProcessor() throws Exception {
        String authCode = "e5fc4203-ac48-4a56-868f-1bebde571006";
        String nrc20Locked = nrc20Locked("nangao", "NanGao", "NG", "100000000", "8");
        this.contractAddress_nrc20 = nrc20Locked;
        String pocm = pocm(nrc20Locked, authCode);
        this.contractAddress = pocm;
        tokenTransfer();

        Log.info("begin depositForOwn {}", sender);
        this.invokeCall(sender, BigInteger.valueOf(3000_00000000L), contractAddress, "depositForOwn", null, "remark");
        Log.info("begin depositForOwn {}", toAddress0);
        this.invokeCall(toAddress0, BigInteger.valueOf(2100_00000000L), contractAddress, "depositForOwn", null, "remark");
        Log.info("begin depositForOwn {}", toAddress1);
        this.invokeCall(toAddress1, BigInteger.valueOf(1200_00000000L), contractAddress, "depositForOwn", null, "remark");
        TimeUnit.SECONDS.sleep(90);
        Log.info("begin receiveAwards {}", sender);
        this.invokeCall(sender, BigInteger.ZERO, contractAddress, "receiveAwards", null, "remark");
        Log.info("begin receiveAwards {}", toAddress0);
        this.invokeCall(toAddress0, BigInteger.ZERO, contractAddress, "receiveAwards", null, "remark");
        Log.info("begin receiveAwards {}", toAddress1);
        this.invokeCall(toAddress1, BigInteger.ZERO, contractAddress, "receiveAwards", null, "remark");
    }

    /**
     * 流程 - 指定合约地址 抵押、领取
     */
    @Test
    public void testDepositReceiveAwardsProcessor() throws Exception {
        contractAddress = "tNULSeBaNA8kLEjf36cVe86PdydQuQ6fregeT4";
        Log.info("begin depositForOwn {}", sender);
        this.invokeCall(sender, BigInteger.valueOf(3000_00000000L), contractAddress, "depositForOwn", null, "remark");
        Log.info("begin depositForOwn {}", toAddress0);
        this.invokeCall(toAddress0, BigInteger.valueOf(2100_00000000L), contractAddress, "depositForOwn", null, "remark");
        Log.info("begin depositForOwn {}", toAddress1);
        this.invokeCall(toAddress1, BigInteger.valueOf(1200_00000000L), contractAddress, "depositForOwn", null, "remark");
        TimeUnit.SECONDS.sleep(90);
        Log.info("begin receiveAwards {}", sender);
        this.invokeCall(sender, BigInteger.ZERO, contractAddress, "receiveAwards", null, "remark");
        Log.info("begin receiveAwards {}", toAddress0);
        this.invokeCall(toAddress0, BigInteger.ZERO, contractAddress, "receiveAwards", null, "remark");
        Log.info("begin receiveAwards {}", toAddress1);
        this.invokeCall(toAddress1, BigInteger.ZERO, contractAddress, "receiveAwards", null, "remark");
    }

    /**
     * 流程 - 指定合约地址 退出
     */
    @Test
    public void testQuitProcessor() throws Exception {
        contractAddress = "tNULSeBaMzXKLBfD3Y4CEUAxY93vUsbgSGxRiC";
        Log.info("begin quit {}", sender);
        this.invokeCall(sender, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        Log.info("begin quit {}", toAddress0);
        this.invokeCall(toAddress0, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        Log.info("begin quit {}", toAddress1);
        this.invokeCall(toAddress1, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
    }

    private String pocm(String nrc20Locked, String authCode) throws Exception {
        Log.info("begin create pocm");
        //String filePath = "/Users/pierreluo/IdeaProjects/pocmContract-ConsensusEnhancement/target/pocmContract-v3-test2.jar";
        String filePath = ContractPOCMSendTxTest.class.getResource("/pocmContract-v3-test2.jar").getFile();
        InputStream in = new FileInputStream(filePath);
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "POCM - consensus enhancement contract test - POCM_共识加强合约";
        Object[] args = new Object[]{nrc20Locked, 5000, 2, 500, 2, true, 1, authCode, null, null};
        Map params = this.makeCreateParams(sender, contractCode, "pocm_enhancement", remark, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map)(map.get("contractResult"))).get("success"));
        return contractAddress;
    }

    private String nrc20Locked(String alias, String name, String symbol, String totalSupply, String decimals) throws Exception {
        Log.info("begin create locked nrc20");
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20-locked-token.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test - " + alias;
        Map params = this.makeCreateParams(sender, contractCode, alias, remark, name, symbol, totalSupply, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), map != null && (Boolean) ((Map)(map.get("contractResult"))).get("success"));
        return contractAddress;
    }

    private String nrc20Locked() throws Exception {
        Log.info("begin create locked nrc20");
        InputStream in = new FileInputStream(ContractTest.class.getResource("/nrc20-locked-token.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test - 锁定空气币";
        String name = "KQB";
        String symbol = "KongQiBi";
        String amount = BigDecimal.TEN.pow(10).toPlainString();
        String decimals = "2";
        Map params = this.makeCreateParams(sender, contractCode, "kqb", remark, name, symbol, amount, decimals);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map)(map.get("contractResult"))).get("success"));
        return contractAddress;
    }

    private void tokenTransfer() throws Exception {
        Log.info("begin tokenTransfer");
        BigInteger value = BigInteger.TEN.pow(10);
        String remark = "token transfer to " + contractAddress;
        Map params = this.makeTokenTransferParams(sender, contractAddress, contractAddress_nrc20, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TOKEN_TRANSFER, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TOKEN_TRANSFER));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map)(map.get("contractResult"))).get("success"));
    }
    /**
     * 调用合约 - 项目发布者创建节点
     */
    @Test
    public void createAgent() throws Exception {
        BigInteger value = BigInteger.valueOf(20000_00000000L);
        String methodName = "createAgentByOwner";
        String methodDesc = "";
        String remark = "项目发布者创建节点";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * 调用合约
     */
    @Test
    public void call() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "addOtherAgent";
        String methodDesc = "";
        String remark = "remark";
        Object[] args = new Object[]{"25326fc2d8ff22ec869baf8c4d55012dfa7f9860c0d1c54dc8569925d15f7e5f"};
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, args);
    }

    /**
     * 调用合约 - 投资者抵押
     */
    @Test
    public void depositForOwn() throws Exception {
        BigInteger value = BigInteger.valueOf(3000_00000000L);
        String methodName = "depositForOwn";
        String methodDesc = "";
        String remark = "投资者抵押";
        this.invokeCall(toAddress1, value, contractAddress, methodName, methodDesc, remark);
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
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 0);
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
     * 调用合约 - 调用_payable
     */
    @Test
    public void payable() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "_payable";
        String methodDesc = "";
        String remark = "payable无参测试";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
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
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    /**
     * 调用视图方法 - 合约整体信息
     */
    @View
    @Test
    public void getContractWholeInfo() throws Exception {
        String methodName = "wholeConsensusInfo";
        Log.info(invokeView(contractAddress, methodName));
    }



    protected void invokeCall(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, String remark, Object... args) throws Exception {
        super.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, args);
        TimeUnit.SECONDS.sleep(1);
        getContractWholeInfo();
    }

}
