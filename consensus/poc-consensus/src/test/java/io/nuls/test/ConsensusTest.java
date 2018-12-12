package io.nuls.test;

import io.nuls.base.data.Address;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.parse.SerializeUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ConsensusTest {
    private  int chainId = 1;
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
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_updateAgentStatus", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void getWholeInfo()throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getWholeInfo", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void getInfo()throws Exception{
        Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("address", agentAddress.getBase58());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getInfo", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void  getPunishList()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        params.put("address",agentAddress.getBase58());
        params.put("type",1);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getPublishList", params);
        System.out.println(cmdResp.getResponseData());
    }
}
