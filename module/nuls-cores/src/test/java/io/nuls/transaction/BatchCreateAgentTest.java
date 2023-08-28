package io.nuls.transaction;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.rpc.call.TransactionCall;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

public class BatchCreateAgentTest {
    private int chainId = 2;
    private int assetId = 1;
    private String password = "nuls123456";
    private String version = "1.0";
    private String fromAddress = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    private TxValid txValid = new TxValid();

    private List<String> agentAddressList = new ArrayList<>(
            Arrays.asList(
                    "tNULSeBaMgU6sQHj9YXWBg21UpAhkTjePjb7ww","tNULSeBaMjvQRJ7mpXU7juMJZYCNmrvLfy56WS","tNULSeBaMuJBjgh3jQA5mBtzVfz3mnF7dSnfQf",
                    "tNULSeBaMftYPSYHxxqrnVfSA3CWtECMy7bvzA","tNULSeBaMgMpaTknyhXtgv2XbDuFWNG8Lv68yV","tNULSeBaMkTi9XGtSQrYf2R5sDPrZRAsa1zuw1",
                    "tNULSeBaMqXwp7TifvGGoSA5Nvic6kxtoNz4T8","tNULSeBaMnV7Uc8BFSSbEwtep3FnciKNcMiJLG","tNULSeBaMtXzgUkzmWVRMH95JmzgXgfpqFXZzj",
                    "tNULSeBaMvgtHsrd1Y1w5CosUnkxvZYSvvyoYs","tNULSeBaMmGEyop9ZV1KkqN1jMZdwvfKjfNo43","tNULSeBaMpZmSavdTZdcM6qtZhb9TtTp1dA68Z",
                    "tNULSeBaMutQcCzVBDwzWzVRqyMLe4DwJJn38j","tNULSeBaMo6NtQhjJd68Axou6zzqZUR39Fd1F1"
            ));


    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void batchCreateAgent() throws Exception{
        int agentCount = 20;

        //创建创建节点地址并转账
        List<String> agentAddressList = createAccount(chainId, agentCount, password);
        Log.info(agentAddressList.toString() );
        NulsHash hash = null;
        for (int index = 0; index <agentCount; index++){
            Map transferMap = txValid.createTransferTx(fromAddress, agentAddressList.get(index), new BigInteger("3000000000000"));
            Transaction tx = txValid.assemblyTransaction((int) transferMap.get(Constants.CHAIN_ID), (List<CoinDTO>) transferMap.get("inputs"),
                    (List<CoinDTO>) transferMap.get("outputs"), (String) transferMap.get("remark"), hash);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
            hash = tx.getHash();
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            Log.debug(result.toString());
        }

        Thread.sleep(30000L);

        //创建出块账户，然后删除
        List<String> packAddressList = createAccount(chainId, agentCount, password);
        for (String packAddress:packAddressList) {
            getPrivateKey(packAddress);
            removeAccount(packAddress);
        }

        //创建节点
        List<String> agentHashList = new ArrayList<>();
        for (int index = 0; index < agentCount ; index++){
            agentHashList.add(createAgent(agentAddressList.get(index), packAddressList.get(index)));
        }

        Thread.sleep(30000);

        //委托账户
        for (String agentHash:agentHashList) {
            boolean confirmed = getAgentInfo(agentHash);
            while (!confirmed){
                Thread.sleep(2000);
                confirmed = getAgentInfo(agentHash);
            }
            depositAgent(agentHash);
        }
    }


    @Test
    public void batchStopAgent() throws Exception{
        for (String agentAddress:agentAddressList) {
            stopAgent(agentAddress);
        }
    }


    public String createAgent(String agentAddress,String packAddress)throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("agentAddress",agentAddress);
        params.put(Constants.CHAIN_ID,chainId);
        params.put("deposit","2000000000000");
        params.put("commissionRate",10);
        params.put("packingAddress",packAddress);
        params.put("password",password);
        params.put("rewardAddress",agentAddress);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
        System.out.println(cmdResp.getResponseData());
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_createAgent");
        return (String)result.get("txHash");
    }

    private void depositAgent(String agentHash)throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID,chainId);
        params.put("address","tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24");
        params.put("agentHash",agentHash);
        params.put("deposit","20000000000000");
        params.put("password",password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", params);
        System.out.println(cmdResp.getResponseData());

    }

    @SuppressWarnings("unchecked")
    private List<String> createAccount(int chainId, int count, String password)throws Exception {
        List<String> accountList = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("count", count);
            params.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            if (!cmdResp.isSuccess()) {
                return null;
            }
            accountList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountList;
    }


    private void getPrivateKey(String address) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
            System.out.println(address + "-----" +result.toString());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeAccount(String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        params.put("password", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params);
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_removeAccount");
        System.out.println(result.toString());
    }

    public boolean getAgentInfo(String agentHash)throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("agentHash",agentHash);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentInfo", params);
        System.out.println(cmdResp.getResponseData());
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_getAgentInfo");
        if(result != null){
            return true;
        }
        return false;
    }

    private void stopAgent(String address)throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("address",address);
        params.put("password", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", params);
        System.out.println(cmdResp.getResponseData());
    }

    private String createTransfer(String addressFrom, String addressTo, BigInteger amount) throws Exception {
        Map transferMap = this.createTransferParam(addressFrom, addressTo, amount);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", transferMap);
        if (!cmdResp.isSuccess()) {
            return "fail";
        }
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        String hash = (String) result.get("value");
        Log.debug("{}", hash);
        return hash;
    }

    private Map createTransferParam(String addressFrom, String addressTo, BigInteger amount) {
        Map transferMap = new HashMap();
        transferMap.put("chainId", chainId);
        transferMap.put("remark", "abc");
        List<CoinDTO> inputs = new ArrayList<>();
        List<CoinDTO> outputs = new ArrayList<>();
        CoinDTO inputCoin1 = new CoinDTO();
        inputCoin1.setAddress(addressFrom);
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(assetId);
        inputCoin1.setAmount(new BigInteger("100000").add(amount));
        inputs.add(inputCoin1);

        CoinDTO outputCoin1 = new CoinDTO();
        outputCoin1.setAddress(addressTo);
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(assetId);
        outputCoin1.setAmount(amount);
        outputs.add(outputCoin1);

        transferMap.put("inputs", inputs);
        transferMap.put("outputs", outputs);
        return transferMap;
    }
}
