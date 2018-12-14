package io.nuls.test.rpc;

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

/**
 * 共识模块相关配置操作测试
 * Configuration Operation Test of Consensus Module
 *
 * @author tag
 * 2018/12/1
 * */
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
}
