package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.TransferDto;
import io.nuls.account.rpc.call.LegerCmdCall;
import io.nuls.account.rpc.common.CommonRpcOperation;
import io.nuls.account.util.TxUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class TransactionCmdTest {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    //protected static AccountService accountService;

    protected int chainId = 12345;
    protected int assetChainId = 12345;
    protected String password = "nuls123456";
    protected String newPassword = "c12345678";
    protected String version = "1.0";
    protected String success = "1";
    String address1 = "5MR_2CkYEhXKCmUWTEsWRTnaWgYE8kJdfd5";
    String address2 = "5MR_2CcRgU3vDGp2uEG3rdzLdyMCbsiLFbJ";
    String address3 = "5MR_2CckymYvKM7NKpt6fpZproQYMtnGdaT";
    String address4 = "5MR_4bgJiPmxN4mZV2C89thSEdJ8qWnm9Xi";
    static int assetId = 1;
    //入账金额
    static BigInteger amount = BigInteger.valueOf(1000000000000000L);

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    /**
     * 铸币
     *
     * @throws Exception
     */
    public void addGenesisAsset(String address) throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        Log.info("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTransaction(address);
        params.put("txHex", HexUtil.encode(transaction.serialize()));
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        Log.info("response {}", response);

        List<String> txHexList = Arrays.asList(HexUtil.encode(transaction.serialize()));
        params.put("txHexList", txHexList);
        params.put("isConfirmTx", true);
        params.put("blockHeight", 0);
        params.remove("txHex");
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        Log.info("response {}", response);
    }

    /**
     * 铸币交易
     *
     * @return
     * @throws IOException
     */
    private Transaction buildTransaction(String address) throws IOException {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address));
        coinTo.setAmount(amount);
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms = new ArrayList<>();
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        return tx;
    }

    @Test
    public void testGenesisAsset() throws Exception {
        addGenesisAsset(address1);
        addGenesisAsset(address2);
        addGenesisAsset(address3);
        addGenesisAsset(address4);
//        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(address10), assetChainId, assetId);
//        System.out.println(balance.longValue());
    }

    @Test
    public void transfer() throws Exception {
        //组装普通转账交易
        TransferDto transferDto = this.createTransferTx();
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", JSONUtils.json2map(JSONUtils.obj2json(transferDto)));
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        String txDigestHex = (String) result.get(RpcConstant.VALUE);
        System.out.println(txDigestHex);
    }

    /**
     * 别名转账测试用例
     */
    @Test
    public void transferByAlias() throws Exception {
        //创建账户
        List<String> accoutList = CommonRpcOperation.createAccount(2);
        assertTrue(accoutList != null & accoutList.size() == 2);
        String fromAddress = accoutList.get(0);
        String toAddress = accoutList.get(1);
        //铸币
        addGenesisAsset(fromAddress);
        addGenesisAsset(toAddress); //because the to address need to set alias
        BigInteger balance = LegerCmdCall.getBalance(chainId, assetChainId, assetId, fromAddress);
        BigInteger balance2 = LegerCmdCall.getBalance(chainId, assetChainId, assetId, toAddress);
        System.out.println(fromAddress + "=====" + balance.longValue());
        System.out.println(toAddress + "=====" + balance2.longValue());
        //设置别名
        //String alias = "edwardtest";
        String alias = "edward" + System.currentTimeMillis();
        System.out.println(alias);
        String txHash = CommonRpcOperation.setAlias(toAddress, alias);
        assertNotNull(txHash);
        //查询设置别名是否成功
        String afterSetALias;
        int i = 0;
        do {
            afterSetALias = CommonRpcOperation.getAliasByAddress(toAddress);
            if (afterSetALias == null) {
                Thread.sleep(5000);
            } else {
                break;
            }
            i++;
            logger.warn("getAliasByAddress return null,retry times:{}", i);
        } while (i <= 10);
        assertNotNull(afterSetALias);
        //转账前查询转入方余额

        //别名转账
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", fromAddress);
        params.put("password", password);
        params.put("alias", alias);
        params.put("amount", "1000");
        params.put("remark", "EdwardTest");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transferByAlias", params);
        System.out.println("ac_transferByAlias response:" + JSONUtils.obj2json(cmdResp));
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transferByAlias"));
        String txDigestHex = (String) result.get(RpcConstant.TX_HASH);
        System.out.println(txDigestHex);
        assertNotNull(txDigestHex);
        //转账后查询转入方余额
        //TODO 此处可能需要延时，因为涉及到交易广播与确认
    }

    /**
     * 创建多签转账交易，包括别名转账以及非别名转账
     * <p>
     * 1st:构建别名转账请求参数
     * 2end:将请求发送到账户模块
     * 3ird:检查返回结果
     */
    @Test
    public void createMultiSignTransferTest() throws Exception {
        createMultiSignTransfer();

    }

    /**
     * 多签转账签名
     */
    @Test
    public void signMultiSignTransactionTest() throws Exception {
        Map<String, Object> map = createMultiSignTransfer();
        String txHex = map.get("txHex").toString();
        MultiSigAccount multiSigAccount = (MultiSigAccount) map.get("multiSigAccount");
        String signAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddress(multiSigAccount.getPubKeyList().get(1), chainId));

        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("signAddress", signAddress);
        params.put("password", password);
        params.put("txHex", txHex);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_signMultiSignTransaction", params);
        Log.info("ac_signMultiSignTransaction response:{}", cmdResp);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_signMultiSignTransaction"));
        assertNotNull(result);
        String txHash = (String) result.get("txHash");
        assertNotNull(txHash);

    }

    //连续交易测试
    @Test
    public void contineCtx() throws Exception {
        for (int i = 0; i < 1; i++) {
            //组装普通转账交易
            TransferDto transferDto = this.createTransferTx();
            //调用接口
//            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", JSONUtils.json2map(JSONUtils.obj2json(transferDto)));
//            HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
//            Assert.assertTrue(null != result);
//            Log.info("{}", result.get("value"));
//            System.out.println("transfer: " + result.get("value"));

            //组装创建节点交易
//            Map agentTxMap = this.createAgentTx(address1, address2);
//            //调用接口
//            Response cmdResp2 = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
//            HashMap result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
//            Assert.assertTrue(null != result);
//            String txHex = (String) result.get("txHex");
//            Log.info("{}", txHex);
//            System.out.println("createAgent: " + txHex);

            //创建节点交易提交
            //String agentHash=this.createAgentCommit(txHex);

            //组装委托节点交易
//            String agentHash="00208fb929d0d351f3bb402e94a24d407ea91e9b25f706496ddc69b234c589cd4e26";
//            Map dpTxMap = this.depositToAgent(agentHash);
//            Response dpResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpTxMap);
//            HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
//            String dpTxHex = (String) dpResult.get("txHex");
//            System.out.println("createDeposit" + dpResp.getResponseData());
//
//            //组装退出委托交易
//            withdraw();

            //Thread.sleep(3000L);
        }
    }

    /**
     * 创建普通转账交易
     *
     * @return
     */
    private TransferDto createTransferTx() {
        TransferDto transferDto = new TransferDto();
        transferDto.setChainId(chainId);
        transferDto.setRemark("transfer test");
        List<CoinDto> inputs = new ArrayList<>();
        List<CoinDto> outputs = new ArrayList<>();
        CoinDto inputCoin1 = new CoinDto();
        inputCoin1.setAddress("5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM");
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(1);
        inputCoin1.setAmount(new BigInteger("10000000000"));
        inputs.add(inputCoin1);

        CoinDto outputCoin1 = new CoinDto();
        outputCoin1.setAddress("5MR_4bgJiPmxN4mZV2C89thSEdJ8qWnm9Xi");
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(1);
        outputCoin1.setAmount(new BigInteger("10000000000"));
        outputs.add(outputCoin1);

        transferDto.setInputs(inputs);
        transferDto.setOutputs(outputs);
        return transferDto;
    }

    /**
     * 创建节点
     */
    public Map createAgentTx(String agentAddress, String packingAddress) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("agentAddress", agentAddress);
        params.put("chainId", chainId);
        params.put("deposit", 20000 * 100000000l);
        params.put("commissionRate", 10);
        params.put("packingAddress", packingAddress);
        params.put("password", null);
        params.put("rewardAddress", agentAddress);
        return params;
    }

    @Test
    public void createAgentTx() throws Exception {
        BigInteger balance = LegerCmdCall.getBalance(chainId, assetChainId, assetId, address1);
        System.out.println(balance.longValue());
        //组装创建节点交易
        Map agentTxMap = this.createAgentTx(address1, address2);
        //调用接口
        Response cmdResp2 = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("txHex"));
        System.out.println("transfer: " + result.get("txHex"));  //Thread.sleep(3000L);
    }

    @Test
    public void stopAgentTx() throws Exception {
        //组装创建节点交易
        //Map agentTxMap=this.createAgentTx(address9, address1);
        Map<String, Object> txMap = new HashMap();
        txMap.put("chainId", chainId);
        txMap.put("address", address1);
        txMap.put("password", "");
        //调用接口
        Response cmdResp2 = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_stopAgent"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("txHex"));
        System.out.println("transfer: " + result.get("txHex"));  //Thread.sleep(3000L);
    }

    /**
     * 创建节点交易提交
     *
     * @param caTxHex
     * @return
     */
    public String createAgentCommit(String caTxHex) {
        String agentHash = null;
        try {
            //创建节点交易提交
            Map<String, Object> caTxCommit = new HashMap<>();
            caTxCommit.put("chainId", chainId);
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(0);
            caTxCommit.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
            caTxCommit.put("tx", caTxHex);
            Response caCommitResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgentCommit", caTxCommit);
            HashMap caCommitResult = (HashMap) ((HashMap) caCommitResp.getResponseData()).get("cs_createAgentCommit");
            agentHash = (String) caCommitResult.get("agentHash");
            System.out.println("createAgentCommit:" + caCommitResp.getResponseData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agentHash;
    }

    /**
     * 委托节点交易创建
     */
    public Map depositToAgent(String agentHash) {
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put("chainId", chainId);
        dpParams.put("address", address4);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", "300000000");
        return dpParams;
    }

    /**
     * 委托节点交易创建
     */
    @Test
    public void depositToAgent() throws Exception {
        BigInteger balance = LegerCmdCall.getBalance(chainId, assetChainId, assetId, address4);
        System.out.println(balance.longValue());
        //组装委托节点交易
        String agentHash = "00207ebda6a6a4a8089f358f2a6b96d9257a67ef20defb184acf2c571f54fdec6a08";
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put("chainId", chainId);
        dpParams.put("address", address4);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", 20000 * 100000000L);
        Response dpResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
        HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
        String dpTxHex = (String) dpResult.get("txHex");
        System.out.println(dpTxHex);
        balance = LegerCmdCall.getBalance(chainId, assetChainId, assetId, address4);
        System.out.println(balance.longValue());
    }

    /**
     * 退出共识
     *
     * @throws Exception
     */
    @Test
    public void withdraw() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", address4);
        params.put("txHash", "0020b48a9922396edf0dd4c9dcad7eca7d3b96251acec4c9c22ffd55f3af7467b23b");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        System.out.println(cmdResp.getResponseData());
        BigInteger balance = LegerCmdCall.getBalance(chainId, assetChainId, assetId, address4);
        System.out.println(balance.longValue());
    }

    private Map<String, Object> createMultiSignTransfer() throws Exception {
        //创建多签账户
        MultiSigAccount multiSigAccount = CommonRpcOperation.createMultiSigAccount();
        assertNotNull(multiSigAccount);
        //String fromAddress = AddressTool.getStringAddressByBytes(multiSigAccount.getAddress().getAddressBytes());
        String fromAddress = "5MR_4bgJiPmxN4mZV2C89thSEdJ8qWnm9Xi";
        assertNotNull(fromAddress);
        String signAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddress(multiSigAccount.getPubKeyList().get(0), chainId));
        assertNotNull(signAddress);
        //创建一个接收方账户
        List<String> accoutList = CommonRpcOperation.createAccount(1);
        assertTrue(accoutList != null & accoutList.size() == 1);
        String toAddress = "5MR_2Cc14Ph4ZfJTzsfxma8FGZ7FBzcTS87";//accoutList.get(0);
        //铸币
        //addGenesisAsset(fromAddress);
        BigInteger balance = TxUtil.getBalance(chainId, chainId, assetId, AddressTool.getAddress(fromAddress));
        System.out.println(balance);

        //创建多签账户转账交易
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", fromAddress);
        params.put("signAddress", "5MR_2CkYEhXKCmUWTEsWRTnaWgYE8kJdfd5");
        params.put("password", password);
        params.put("type", 1);
        params.put("toAddress", toAddress);
        params.put("amount", "1000");
        params.put("remark", "EdwardTest");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSignTransfer", params);
        System.out.println("ac_createMultiSignTransfer response:" + JSONUtils.obj2json(cmdResp));
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_createMultiSignTransfer"));
        String txHex = (String) result.get(RpcConstant.TX_DATA_HEX);
        System.out.println(txHex);
        assertNotNull(txHex);
        Map<String, Object> map = new HashMap<>();
        map.put("multiSigAccount", multiSigAccount);
        map.put("txHex", txHex);
        return map;
    }

}
