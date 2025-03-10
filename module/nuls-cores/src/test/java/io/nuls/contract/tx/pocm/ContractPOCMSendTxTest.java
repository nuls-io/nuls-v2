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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.nuls.contract.constant.ContractCmdConstant.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractPOCMSendTxTest extends BaseQuery {

    /**
     * Create Contract
     */
    @Test
    public void createConsensusEnhancementContract() throws Exception {
        //String filePath = ContractPOCMSendTxTest.class.getResource("/pocmContract-v3").getFile();
        InputStream in = new FileInputStream("/Users/pierreluo/IdeaProjects/pocmContract-new/target/pocmContract-new-1.0-SNAPSHOT.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "POCM new";
        //Address candyToken,
        //int candyAssetChainId,
        //int candyAssetId,
        //BigInteger candyPerBlock,
        //BigInteger candySupply,
        //int lockedTokenDay,
        //BigInteger minimumStaking,
        //BigInteger maximumStaking,
        //boolean openConsensus,
        //boolean openAwardConsensusNodeProvider,
        //String authorizationCode
        //int operatingModel
        //int rewardDrawRatioForLp
        Object[] args = new Object[]{"tNULSeBaMwB4nKCwEGiwjzzGHseHEeeSAEtChf", 0, 0, 2000_0000, 2000000_00000000L, 1, 100_00000000L, 10000000_0000_0000L, true, false, "qwerqwerasd", 0, 0};
        Map params = this.makeCreateParams(sender, contractCode, "pocm_new", remark, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Log.info("contractResult:{}", JSONUtils.obj2PrettyJson(waitGetContractTx(hash)));
    }

    /**
     * Create nodes
     * */
    @Test
    public void createAgent()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("agentAddress","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        params.put(Constants.CHAIN_ID,2);
        params.put("deposit","2000000000000");
        params.put("commissionRate",10);
        params.put("packingAddress","tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe");
        params.put("password","nuls123456");
        params.put("rewardAddress","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }

    /**
     * Create nodes2
     * */
    @Test
    public void createAgent2()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("agentAddress","tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD");
        params.put(Constants.CHAIN_ID,2);
        params.put("deposit","2000000000000");
        params.put("commissionRate",10);
        params.put("packingAddress","tNULSeBaMmShSTVwbU4rHkZjpD98JgFgg6rmhF");
        params.put("password","nuls123456");
        params.put("rewardAddress","tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }


    /**
     * Stop node
     * */
    @Test
    public void stopAgent()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        //params.put("address","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        params.put("address","tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD");
        params.put("password", "nuls123456");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void getAgentInfo()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("agentHash","60ae9570af85f4542c4a8a3851c489db4e7753f4f22d31824e16c8d21031e280");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentInfo", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }

    /**
     * Delegate node
     * */
    @Test
    public void depositAgent()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("address",sender);
        params.put("agentHash","af24e302e1dd381890e7c2e9e853b5a2b2c0c991466c1cc4dd5c897566adf244");
        params.put("deposit", new BigDecimal("210000").movePointRight(8).toString());
        params.put("password", "nuls123456");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", params);
        System.out.println(cmdResp.getResponseData());
    }

    /**
     * Cancel delegation node
     * */
    @Test
    public void withdraw()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("address", sender);
        params.put("txHash","22bd85e88c0bd13524946bd8fad03a8a4ff9d02be4870b7239f077820569090c");
        params.put("password", "nuls123456");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        System.out.println(cmdResp.getResponseData());
    }

    /**
     * flow - establishTOKEN, POCM, Add node, mortgage, exit
     */
    @Test
    public void testConsensusDepositOthersProcessor() throws Exception {
        /*String authCode = "1a4123aa-7cbb-42f5-80be-8d8dc8331522";
        String nrc20Locked = nrc20Locked();
        this.contractAddress_nrc20 = nrc20Locked;
        String pocm = pocm(nrc20Locked, authCode);
        this.contractAddress = pocm;
        tokenTransfer();

        //Log.info("begin openConsensus");
        //this.invokeCall(sender, BigInteger.ZERO, contractAddress, "openConsensus", null, "remark");
        Log.info("begin addOtherAgent");
        this.invokeCall(sender, BigInteger.ZERO, contractAddress, "addOtherAgent", null, "remark", List.of("4dce090750b1900c1e741b961b32e75ce9f9d9c69997388ec73e9dd62cb8c835").toArray());
        Log.info("begin depositForOwn {}", sender);*/

        String nrc20Locked = "tNULSeBaN17QmugpEbzHHd8spVUZ5DDjx8beAb";
        contractAddress_nrc20 = nrc20Locked;
        String pocm = "tNULSeBaMwECYR7GWGPvQT6chX5RzDZ1gtmvTS";
        this.contractAddress = pocm;
        tokenTransfer();
        //this.invokeCall(sender, BigInteger.ZERO, contractAddress, "removeAgent", null, "remark", List.of("4dce090750b1900c1e741b961b32e75ce9f9d9c69997388ec73e9dd62cb8c835").toArray());
        //this.invokeCall(sender, BigInteger.ZERO, contractAddress, "addOtherAgent", null, "remark", List.of("4dce090750b1900c1e741b961b32e75ce9f9d9c69997388ec73e9dd62cb8c835").toArray());

        //Log.info("begin quit {}", sender);
        //this.invokeCall(sender, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        //Log.info("begin quit {}", toAddress5);
        //this.invokeCall(toAddress5, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        //Log.info("begin quit {}", toAddress6);
        //this.invokeCall(toAddress6, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        //
        //this.invokeCall(sender, BigInteger.valueOf(150000_00000000L), pocm, "depositForOwn", null, "remark");
        //Log.info("begin depositForOwn {}", toAddress5);
        //this.invokeCall(toAddress5, BigInteger.valueOf(2100_00000000L), contractAddress, "depositForOwn", null, "remark");
        //Log.info("begin depositForOwn {}", toAddress6);
        //this.invokeCall(toAddress6, BigInteger.valueOf(1200_00000000L), contractAddress, "depositForOwn", null, "remark");
        /*TimeUnit.SECONDS.sleep(30);

        Log.info("begin quit {}", sender);
        this.invokeCall(sender, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        Log.info("begin quit {}", toAddress5);
        this.invokeCall(toAddress5, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");
        Log.info("begin quit {}", toAddress6);
        this.invokeCall(toAddress6, BigInteger.ZERO, contractAddress, "quit", null, "remark", "0");

        TimeUnit.SECONDS.sleep(2);
        Log.info("sender balance is {}", this.invokeView(nrc20Locked, "balanceOf", sender));
        Log.info("toAddress5 balance is {}", this.invokeView(nrc20Locked, "balanceOf", toAddress5));
        Log.info("toAddress6 balance is {}", this.invokeView(nrc20Locked, "balanceOf", toAddress6));
        Log.info("pocm balance is {}", this.invokeView(nrc20Locked, "balanceOf", pocm));
        Log.info("sender locked balance is {}", this.invokeView(nrc20Locked, "lockedBalanceOf", sender));
        Log.info("toAddress5 locked balance is {}", this.invokeView(nrc20Locked, "lockedBalanceOf", toAddress5));
        Log.info("toAddress6 locked balance is {}", this.invokeView(nrc20Locked, "lockedBalanceOf", toAddress6));
        Log.info("pocm locked balance is {}", this.invokeView(nrc20Locked, "lockedBalanceOf", pocm));*/
    }

    @Test
    public void t() throws Exception {
        String nrc20Locked = "tNULSeBaN8Kpc1m5jhDWY5Ntnb1yU6ywUrYFS8";
        String pocm = "tNULSeBaN9rBMnfwQLuFzvJ8c3fv4dsCkdJJfE";
        Log.info("sender balance is {}", this.invokeView(nrc20Locked, "balanceOf", sender));
        Log.info("toAddress0 balance is {}", this.invokeView(nrc20Locked, "balanceOf", toAddress0));
        Log.info("toAddress1 balance is {}", this.invokeView(nrc20Locked, "balanceOf", toAddress1));
        Log.info("pocm balance is {}", this.invokeView(nrc20Locked, "balanceOf", pocm));
        Log.info("sender locked balance is {}", this.invokeView(nrc20Locked, "lockedBalanceOf", sender));
        Log.info("toAddress0 locked balance is {}", this.invokeView(nrc20Locked, "lockedBalanceOf", toAddress0));
        Log.info("toAddress1 locked balance is {}", this.invokeView(nrc20Locked, "lockedBalanceOf", toAddress1));
        Log.info("pocm locked balance is {}", this.invokeView(nrc20Locked, "lockedBalanceOf", pocm));
    }

    /**
     * flow - establishTOKEN, POCM, Mortgage, claim
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
     * flow - Specify contract address mortgage、receive
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
     * flow - Specify contract address quit
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
        //String filePath = "/Users/pierreluo/IdeaProjects/pocmContract-ConsensusEnhancement/target/pocmContract-v3-1.0.2.RELEASE.jar";
        String filePath = ContractPOCMSendTxTest.class.getResource("/pocmContract-v3").getFile();
        InputStream in = new FileInputStream(filePath);
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "POCM - consensus enhancement contract test - POCM_Consensus Strengthening Contract";
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
        //String filePath = "/Users/pierreluo/IdeaProjects/NRC20-Locked-Token/target/nrc20-locked-token-test1.jar";
        String filePath = ContractTest.class.getResource("/nrc20-locked-token").getFile();
        InputStream in = new FileInputStream(filePath);
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
        //String filePath = "/Users/pierreluo/IdeaProjects/NRC20-Locked-Token/target/nrc20-locked-token-test1.jar";
        String filePath = ContractTest.class.getResource("/nrc20-locked-token").getFile();
        InputStream in = new FileInputStream(filePath);
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "create contract test - Lock Air Coin";
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
        BigInteger value = new BigDecimal("2000000").movePointRight(8).toBigInteger();
        String remark = "token transfer to " + contractAddress;
        Map params = this.makeTokenTransferParams(toAddress, contractAddress, contractAddress_nrc20, value, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, TOKEN_TRANSFER, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(TOKEN_TRANSFER));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Map map = waitGetContractTx(hash);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(map), (Boolean) ((Map)(map.get("contractResult"))).get("success"));
    }
    /**
     * Call Contract - Project publisher creates nodes
     */
    @Test
    public void createAgentByOwner() throws Exception {
        BigInteger value = BigInteger.valueOf(20000_00000000L);
        String methodName = "createAgentByOwner";
        String methodDesc = "";
        String remark = "Project publisher creates nodes";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * Call Contract
     */
    @Test
    public void addOtherAgent() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "addOtherAgent";
        String methodDesc = "";
        String remark = "remark";
        Object[] args = new Object[]{"60ae9570af85f4542c4a8a3851c489db4e7753f4f22d31824e16c8d21031e280"};
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, args);
    }

    /**
     * Call Contract - Investor collateral
     */
    @Test
    public void depositForOwn() throws Exception {
        BigInteger value = BigInteger.valueOf(30001_00000000L);
        String methodName = "depositForOwn";
        String methodDesc = "";
        String remark = "Investor collateral";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }
    /**
     * Call Contract - Investor withdrawal from mortgage
     */
    @Test
    public void quit() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "quit";
        String methodDesc = "";
        String remark = "Investor withdrawal from mortgage";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 0);
    }

    @Test
    public void pocmWithdraw() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "withdraw";
        String methodDesc = "";
        String remark = "Investor withdrawal from mortgage";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, 50000_00000000L);
    }

    /**
     * Call Contract - After unlocking the consensus deposit, refund the deposit of all users who have applied for withdrawal - Contract owner operation
     */
    @Test
    public void refundAllUnLockDepositByOwner() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "refundAllUnLockDepositByOwner";
        String methodDesc = "";
        String remark = "After unlocking the consensus deposit, refund the deposit of all users who have applied for withdrawal - Contract owner operation";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * Call Contract - After unlocking the consensus deposit, refund the deposit of users who have applied for withdrawal - Investment User Operations
     */
    @Test
    public void takeBackUnLockDeposit() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "takeBackUnLockDeposit";
        String methodDesc = "";
        String remark = "After unlocking the consensus deposit, refund the deposit of users who have applied for withdrawal - Investment User Operations";
        this.invokeCall(toAddress0, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * Call Contract - Contract creator obtains consensus reward amount
     */
    @Test
    public void transferConsensusReward() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "transferConsensusRewardByOwner";
        String methodDesc = "";
        String remark = "Contract creator obtains consensus reward amount";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    @Test
    public void transferProjectCandyAsset() throws Exception {
        sender = "tNULSeBaMuU6sq72mptyghDXDWQXKJ5QUaWhGj";
        BigInteger value = BigInteger.ZERO;
        String methodName = "transferProjectCandyAsset";
        String methodDesc = "";
        String remark = "";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", "199994360000003");
    }

    /**
     * Call Contract - Contract owner redeems consensus margin
     */
    @Test
    public void takeBackConsensusCreateAgentDepositByOwner() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "takeBackConsensusCreateAgentDepositByOwner";
        String methodDesc = "";
        String remark = "Contract owner redeems consensus margin";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * Call Contract - The contract owner entrusts their own node
     */
    @Test
    public void depositConsensusManuallyByOwner() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "depositConsensusManuallyByOwner";
        String methodDesc = "";
        String remark = "The contract owner entrusts their own node";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * Call Contract - Contract owner cancellation node
     */
    @Test
    public void stopAgentManuallyByOwner() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "stopAgentManuallyByOwner";
        String methodDesc = "";
        String remark = "Contract owner cancellation node";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * Call Contract - call_payable
     */
    @Test
    public void payable() throws Exception {
        BigInteger value = BigInteger.ZERO;
        String methodName = "_payable";
        String methodDesc = "";
        String remark = "payableNo parameter testing";
        this.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark);
    }

    /**
     * Transfer to the contracted address
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
     * Call View Method - Overall contract information
     */
    @Test
    public void getContractWholeInfo() throws Exception {
        String methodName = "wholeConsensusInfo";
        Log.info(invokeView(contractAddress, methodName));
    }



    protected Map invokeCall(String sender, BigInteger value, String contractAddress, String methodName, String methodDesc, String remark, Object... args) throws Exception {
        Map map = super.invokeCall(sender, value, contractAddress, methodName, methodDesc, remark, args);
        TimeUnit.SECONDS.sleep(1);
        getContractWholeInfo();
        return map;
    }

}
