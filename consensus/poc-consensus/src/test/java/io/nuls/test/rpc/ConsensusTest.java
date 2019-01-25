package io.nuls.test.rpc;

import io.nuls.base.data.Address;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.BlockRoundData;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.parse.SerializeUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 共识模块相关配置操作测试
 * Configuration Operation Test of Consensus Module
 *
 * @author tag
 * 2018/12/1
 * */
public class ConsensusTest {
    private  int chainId = 12345;
    private  String success = "1";

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    /**
     * 获取当前轮次信息
     * */
    public void getRoundInfo() throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getRoundInfo", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 修改节点共识状态
     * */
    public void updateAgentConsensusStatus()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_updateAgentConsensusStatus", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 修改节点打包状态
     * */
    public void updateAgentStatus()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("status", 1);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_updateAgentStatus", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 获取全网共识信息
     * */
    public void getWholeInfo()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getWholeInfo", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 获取指定账户的共识信息
     * */
    public void getInfo()throws Exception{
        Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", agentAddress.getBase58());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getInfo", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 获取全网或指定账户惩罚信息
     * */
    public void  getPunishList()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        params.put("address",agentAddress.getBase58());
        params.put("type",1);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getPublishList", params);
        System.out.println(cmdResp.getResponseData());
        //"fb8017b246114784749d46eebff4c34f446b500521f0cb19f66118O2ea8c942f7688180141c5a4feb65e24aedb5b529d";
    }

    /**
     * 节点创建到打包数据准备
     * */
    @Test
    public void packing(){
        //1.创建账户
        List<String> accountList;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", 12345);
            params.put("count", 4);
            params.put("password", null);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            if (!cmdResp.isSuccess()) {
                return;
            }
            accountList = (List<String>) ((HashMap)((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
            if(accountList == null || accountList.size() == 0){
                return;
            }
            System.out.println("accountList:"+accountList);

            //2.创建节点交易创建
            String packingAddress = accountList.get(0);
            String agentAddress = accountList.get(1);
            String rewardAddress = accountList.get(2);
            Map<String,Object> caParams = new HashMap<>();
            caParams.put("agentAddress",agentAddress);
            caParams.put("chainId",12345);
            caParams.put("deposit",20000);
            caParams.put("commissionRate",10);
            caParams.put("packingAddress",packingAddress);
            caParams.put("password","");
            caParams.put("rewardAddress",rewardAddress);
            Response caResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", caParams);
            HashMap caResult = (HashMap)((HashMap) caResp.getResponseData()).get("cs_createAgent");
            String caTxHex = (String)caResult.get("txHex");
            System.out.println("createAgent:"+caResp.getResponseData());

            //3.创建节点交易提交
            Map<String,Object>caTxCommit = new HashMap<>();
            caTxCommit.put("chainId",12345);
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(0);
            caTxCommit.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
            caTxCommit.put("tx",caTxHex);
            Response caCommitResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgentCommit", caTxCommit);
            HashMap caCommitResult = (HashMap)((HashMap) caCommitResp.getResponseData()).get("cs_createAgentCommit");
            String agentHash = (String)caCommitResult.get("agentHash");
            System.out.println("createAgentCommit:"+caCommitResp.getResponseData());


            //4.委托节点交易创建
            String depositAddress = accountList.get(3);
            Map<String,Object> dpParams = new HashMap<>();
            dpParams.put("chainId",12345);
            dpParams.put("address",depositAddress);
            dpParams.put("agentHash",agentHash);
            dpParams.put("deposit","300000");
            Response dpResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", dpParams);
            HashMap dpResult = (HashMap)((HashMap) dpResp.getResponseData()).get("cs_depositToAgent");
            String dpTxHex = (String)dpResult.get("txHex");
            System.out.println("createDeposit"+cmdResp.getResponseData());

            //5.委托交易提交
            Map<String,Object>dpTxCommitParams = new HashMap<>();
            dpTxCommitParams.put("chainId",12345);
            BlockHeader blockHeader1 = new BlockHeader();
            blockHeader.setHeight(0);
            dpTxCommitParams.put("blockHeader", HexUtil.encode(blockHeader1.serialize()));
            dpTxCommitParams.put("tx",dpTxHex);
            Response dpCommitResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_depositCommit", dpTxCommitParams);
            System.out.println("deposit transaction commit:"+dpCommitResp.getResponseData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)throws Exception{
        BlockRoundData roundData = new BlockRoundData();
        roundData.setConsensusMemberCount(3);
        roundData.setPackingIndexOfRound(1);
        roundData.setRoundIndex(1);
        roundData.setRoundStartTime(1L);
        System.out.println(HexUtil.encode(roundData.serialize()));
    }
}
