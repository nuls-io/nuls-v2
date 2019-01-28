package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.TransferDto;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.SerializeUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //protected static AccountService accountService;

    protected int chainId = 12345;
    protected int assetChainId = 12345;
    protected String password = "nuls123456";
    protected String newPassword = "c12345678";
    protected String version = "1.0";
    protected String success = "1";
    String address1 = "SPWAxuodkw222367N88eavYDWRraG3930";
    String address2 = "Rnt57eZnH8Dd7K3LudJXmmEutYJZD3930";
    String address3 = "XroY3cLWTfgKMRRRLCP5rhvo1gHY63930";
    String address4 = "WEXAmsUJSNAvCx2zUaXziy3ZYX1em3930";
    static int assetId = 1;
    //入账金额
    static BigInteger amount = BigInteger.valueOf(1000000000000000L);

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    /**
     * 铸币
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

        params.put("isConfirmTx",true);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        Log.info("response {}", response);
    }

    /**
     * 铸币交易
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
    public void testGenesisAsset() throws Exception{
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
            Map agentTxMap=this.createAgentTx();
            //调用接口
            Response cmdResp2 = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", agentTxMap);
            HashMap result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get("cs_createAgent"));
            Assert.assertTrue(null != result);
            String txHex=(String)result.get("txHex");
            Log.info("{}", txHex);
            System.out.println("createAgent: "+txHex);

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
        inputCoin1.setAddress("LU6eNP3pJ5UMn5yn8LeDE3Pxeapsq3930");
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(1);
        inputCoin1.setAmount(new BigInteger("10000000"));
        inputs.add(inputCoin1);

        CoinDto outputCoin1 = new CoinDto();
        outputCoin1.setAddress("JcgbDRvBqQ67Uq4Tb52U22ieJdr3G3930");
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(1);
        outputCoin1.setAmount(new BigInteger("100000000"));
        outputs.add(outputCoin1);

        transferDto.setInputs(inputs);
        transferDto.setOutputs(outputs);
        return transferDto;
    }

    /**
     * 创建节点
     */
    public Map createAgentTx() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("agentAddress", address1);
        params.put("chainId", chainId);
        params.put("deposit", 20000*100000000l);
        params.put("commissionRate", 10);
        params.put("packingAddress", address2);
        params.put("password", null);
        params.put("rewardAddress", address3);
        return params;
    }

    /**
     * 创建节点交易提交
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
     * 退出共识
     * @throws Exception
     */
    public void withdraw()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",chainId);
        //Address depositAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        params.put("address",address4);
        params.put("txHash","");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        System.out.println(cmdResp.getResponseData());
    }

}
