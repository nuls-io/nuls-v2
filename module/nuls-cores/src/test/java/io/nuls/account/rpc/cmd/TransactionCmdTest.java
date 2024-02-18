package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.TransferDTO;
import io.nuls.account.rpc.call.LedgerCall;
import io.nuls.account.rpc.common.CommonRpcOperation;
import io.nuls.account.util.TxUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.common.ConfigBean;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class TransactionCmdTest {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    //protected static AccountService accountService;

    protected int chainId = 2;
    protected int assetChainId = 2;
    protected static String password = "nuls123456";
    protected String newPassword = "c12345678";
    protected String version = "1.0";
    protected String success = "1";
    String address = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    String address1 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    String address2 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    String address3 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    String address4 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";

    static int assetId = 1;
    //Entry amount
    static BigInteger amount = BigInteger.valueOf(1000000000000000L);

    Chain chain;
    @BeforeClass
    public void start() throws Exception {
        NoUse.mockModule();
        importKeyStore();
        chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        configBean.setMaxViewGas(100000000L);
        chain.setConfig(configBean);
    }


    public static void importKeyStore() throws Exception {
        CommonRpcOperation.importAccountByPriKeyWithOverwrite("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
                "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b",password);
//        CommonRpcOperation.importAccountByPriKeyWithOverwrite("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
//                "00c940482596e30265f9f9f6216f7d7b507eebc9857c3689efa4378527bab3ba3d",password);
//        CommonRpcOperation.importAccountByPriKeyWithOverwrite("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
//                "00ea8818c00c9fd20a54a93bbc749b903ee69617990af9bf0d6c6bc17d51820f",password);
//        CommonRpcOperation.importAccountByPriKeyWithOverwrite("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
//                "00d9a2384a3bc9fca252c8e2c8728efcb98e007b5290d7f01a008731669e433043",password);
    }

    /**
     * Coinage
     *
     * @throws Exception
     */
    public void addGenesisAsset(String address) throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        Log.info("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTransaction(address);
        params.put("txHex", HexUtil.encode(transaction.serialize()));
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        Log.info("response {}", response);

        List<String> txHexList = Arrays.asList(HexUtil.encode(transaction.serialize()));
        params.put("txHexList", txHexList);
        params.put("isConfirmTx", true);
        params.put("blockHeight", 0);
        params.remove("txHex");
        response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        Log.info("response {}", response);
    }

    /**
     * Coinage trading
     *
     * @return
     * @throws IOException
     */
    private Transaction buildTransaction(String address) throws IOException {
        //Encapsulation transaction execution
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
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
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
        //Assembly of ordinary transfer transactions
        //TransferDto transferDto = CommonRpcOperation.createTransferTx("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",new BigInteger("10000000000"));
        TransferDTO transferDto = CommonRpcOperation.createTransferTx("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",new BigInteger("199800000"));
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", JSONUtils.json2map(JSONUtils.obj2json(transferDto)));
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        String txDigestHex = (String) result.get(RpcConstant.VALUE);
        System.out.println(txDigestHex);
    }

    /**
     * Alias transfer test case
     */
    @Test
    public void transferByAlias() throws Exception {
        //Create an account
        List<String> accoutList = CommonRpcOperation.createAccount(1);
        assertTrue(accoutList != null & accoutList.size() == 1);
        String fromAddress = address;
        String toAddress = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        //Coinage
        //addGenesisAsset(fromAddress);
        //ddGenesisAsset(toAddress); //because the to address need to set alias
        BigInteger balance = LedgerCall.getBalance(chain, assetChainId, assetId, fromAddress);
        BigInteger balance2 = LedgerCall.getBalance(chain, assetChainId, assetId, toAddress);
        System.out.println(fromAddress + "=====" + balance.longValue());
        System.out.println(toAddress + "=====" + balance2.longValue());
        //Set alias
        //String alias = "edwardtest";
        String alias = "alias_1550831248049";
        //String txHash = CommonRpcOperation.setAlias(toAddress, alias);
        //assertNotNull(txHash);
        //Check if the alias setting was successful
//        String afterSetALias;
//        int i = 0;
//        do {
//            afterSetALias = CommonRpcOperation.getAliasByAddress(toAddress);
//            if (afterSetALias == null) {
//                Thread.sleep(5000);
//            } else {
//                break;
//            }
//            i++;
//            LOG.warn("getAliasByAddress return null,retry times:{}", i);
//        } while (i <= 10);
//        assertNotNull(afterSetALias);
        //Query the balance of the transferor before transfer

        //Alias transfer
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", fromAddress);
        params.put("password", password);
        params.put("alias", alias);
        params.put("amount", "10000");
        params.put("remark", "EdwardTest");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", params);
        System.out.println("ac_transfer response:" + JSONUtils.obj2json(cmdResp));
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        String txDigestHex = (String) result.get(RpcConstant.TX_HASH);
        System.out.println(txDigestHex);
        assertNotNull(txDigestHex);
        //Query the balance of the transferee after the transfer
        //TODO Delay may be required here as it involves transaction broadcasting and confirmation
    }

    /**
     * Create multi signature transfer transactions, including alias transfers and non alias transfers
     * <p>
     * 1st:Build alias transfer request parameters
     * 2end:Send the request to the account module
     * 3ird:Check the returned results
     */
    @Test
    public void createMultiSignTransferTest() throws Exception {
        createMultiSignTransfer();

    }

    /**
     * Multiple transfer signatures
     */
    @Test
    public void signMultiSignTransactionTest() throws Exception {
        Map<String, Object> map = createMultiSignTransfer();
        String txHex = map.get("txHex").toString();
        MultiSigAccount multiSigAccount = (MultiSigAccount) map.get("multiSigAccount");
        String signAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddress(multiSigAccount.getPubKeyList().get(1), chainId));

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("signAddress", signAddress);
        params.put("password", password);
        params.put("txHex", txHex);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signMultiSignTransaction", params);
        Log.info("ac_signMultiSignTransaction response:{}", cmdResp);
        assertNotNull(cmdResp);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_signMultiSignTransaction"));
        assertNotNull(result);
        String txHash = (String) result.get("txHash");
        assertNotNull(txHash);

    }

    //Continuous trading test
    @Test
    public void contineCtx() throws Exception {
        for (int i = 0; i < 1; i++) {
            //Assembly of ordinary transfer transactions
            TransferDTO transferDto = this.createTransferTx();
            //Calling interfaces
//            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", JSONUtils.json2map(JSONUtils.obj2json(transferDto)));
//            HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
//            Assert.assertTrue(null != result);
//            Log.info("{}", result.get("value"));
//            System.out.println("transfer: " + result.get("value"));

            //Assemble and create node transactions
//            Map agentTxMap = this.createAgentTx(address1, address2);
//            //Calling interfaces
//            Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
//            HashMap result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
//            Assert.assertTrue(null != result);
//            String txHex = (String) result.get("txHex");
//            Log.info("{}", txHex);
//            System.out.println("createAgent: " + txHex);

            //Create node transaction submission
            //String agentHash=this.createAgentCommit(txHex);

            //Assembly commission node transaction
//            String agentHash="00208fb929d0d351f3bb402e94a24d407ea91e9b25f706496ddc69b234c589cd4e26";
//            Map dpTxMap = this.depositToAgent(agentHash);
//            Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpTxMap);
//            HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
//            String dpTxHex = (String) dpResult.get("txHex");
//            System.out.println("createDeposit" + dpResp.getResponseData());
//
//            //Assembly exit commission transaction
//            withdraw();

            //Thread.sleep(3000L);
        }
    }

    /**
     * Create a regular transfer transaction
     *
     * @return
     */
    private TransferDTO createTransferTx() {
        return CommonRpcOperation.createTransferTx("tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",new BigInteger("10000000000"));
    }

    /**
     * Create nodes
     */
    public Map createAgentTx(String agentAddress, String packingAddress) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("agentAddress", agentAddress);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("deposit", 20000 * 100000000l);
        params.put("commissionRate", 10);
        params.put("packingAddress", packingAddress);
        params.put("password", null);
        params.put("rewardAddress", agentAddress);
        return params;
    }

    @Test
    public void createAgentTx() throws Exception {
        BigInteger balance = LedgerCall.getBalance(chain, assetChainId, assetId, address1);
        System.out.println(balance.longValue());
        //Assemble and create node transactions
        Map agentTxMap = this.createAgentTx(address1, address2);
        //Calling interfaces
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("txHex"));
        System.out.println("transfer: " + result.get("txHex"));  //Thread.sleep(3000L);
    }

    @Test
    public void stopAgentTx() throws Exception {
        //Assemble and create node transactions
        //Map agentTxMap=this.createAgentTx(address9, address1);
        Map<String, Object> txMap = new HashMap();
        txMap.put("chainId", chainId);
        txMap.put("address", address1);
        txMap.put("password", "");
        //Calling interfaces
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", txMap);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_stopAgent"));
        Assert.assertTrue(null != result);
        Log.info("{}", result.get("txHex"));
        System.out.println("transfer: " + result.get("txHex"));  //Thread.sleep(3000L);
    }

    /**
     * Create node transaction submission
     *
     * @param caTxHex
     * @return
     */
    public String createAgentCommit(String caTxHex) {
        String agentHash = null;
        try {
            //Create node transaction submission
            Map<String, Object> caTxCommit = new HashMap<>();
            caTxCommit.put("chainId", chainId);
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(0);
            caTxCommit.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
            caTxCommit.put("tx", caTxHex);
            Response caCommitResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgentCommit", caTxCommit);
            HashMap caCommitResult = (HashMap) ((HashMap) caCommitResp.getResponseData()).get("cs_createAgentCommit");
            agentHash = (String) caCommitResult.get("agentHash");
            System.out.println("createAgentCommit:" + caCommitResp.getResponseData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agentHash;
    }

    /**
     * Entrust node transaction creation
     */
    public Map depositToAgent(String agentHash) {
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put(Constants.CHAIN_ID, chainId);
        dpParams.put("address", address4);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", "300000000");
        return dpParams;
    }

    /**
     * Entrust node transaction creation
     */
    @Test
    public void depositToAgent() throws Exception {
        BigInteger balance = LedgerCall.getBalance(chain, assetChainId, assetId, address4);
        System.out.println(balance.longValue());
        //Assembly commission node transaction
        String agentHash = "00207ebda6a6a4a8089f358f2a6b96d9257a67ef20defb184acf2c571f54fdec6a08";
        Map<String, Object> dpParams = new HashMap<>();
        dpParams.put(Constants.CHAIN_ID, chainId);
        dpParams.put("address", address4);
        dpParams.put("agentHash", agentHash);
        dpParams.put("deposit", 20000 * 100000000L);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
        HashMap dpResult = (HashMap) ((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
        String dpTxHex = (String) dpResult.get("txHex");
        System.out.println(dpTxHex);
        balance = LedgerCall.getBalance(chain, assetChainId, assetId, address4);
        System.out.println(balance.longValue());
    }

    /**
     * Exit consensus
     *
     * @throws Exception
     */
    @Test
    public void withdraw() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address4);
        params.put("txHash", "0020b48a9922396edf0dd4c9dcad7eca7d3b96251acec4c9c22ffd55f3af7467b23b");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        System.out.println(cmdResp.getResponseData());
        BigInteger balance = LedgerCall.getBalance(chain, assetChainId, assetId, address4);
        System.out.println(balance.longValue());
    }

    private Map<String, Object> createMultiSignTransfer() throws Exception {
        //Create a multi signature account
        MultiSigAccount multiSigAccount = CommonRpcOperation.createMultiSigAccount();
        assertNotNull(multiSigAccount);
        //String fromAddress = AddressTool.getStringAddressByBytes(multiSigAccount.getAddress().getAddressBytes());
        String fromAddress = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
        assertNotNull(fromAddress);
        String signAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddress(multiSigAccount.getPubKeyList().get(0), chainId));
        assertNotNull(signAddress);
        //Create a recipient account
        List<String> accoutList = CommonRpcOperation.createAccount(1);
        assertTrue(accoutList != null & accoutList.size() == 1);
        String toAddress = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";//accoutList.get(0);
        //Coinage
        //addGenesisAsset(fromAddress);
        BigInteger balance = TxUtil.getBalance(chain, chainId, assetId, AddressTool.getAddress(fromAddress));
        System.out.println(balance);

        //Create multi signature account transfer transactions
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", fromAddress);
        params.put("signAddress", "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        params.put("password", password);
        params.put("type", 1);
        params.put("toAddress", toAddress);
        params.put("amount", "1000");
        params.put("remark", "EdwardTest");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSignTransfer", params);
        System.out.println("ac_createMultiSignTransfer response:" + JSONUtils.obj2json(cmdResp));
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_createMultiSignTransfer"));
        String txHex = (String) result.get(RpcConstant.TX_DATA);
        System.out.println(txHex);
        assertNotNull(txHex);
        Map<String, Object> map = new HashMap<>();
        map.put("multiSigAccount", multiSigAccount);
        map.put("txHex", txHex);
        return map;
    }

    /**
     * Query balance
     */
    @Test
    public void getBalance() {
        BigInteger balance = LedgerCall.getBalance(chain, assetChainId, assetId, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        System.out.println(balance.longValue());
    }

}
