package io.nuls.test;

import io.nuls.base.data.Address;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.parse.SerializeUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RpcTest {
    protected int chainId = 1;
    protected  String success = "1";
    @BeforeClass
    public static void start() throws Exception {
        WsServer.mockModule();
    }

    @Test
    public void createAgent()throws Exception{
        Address agentgAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        Address rewardAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmgd".getBytes()));
        Address packingAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fegfgqmd".getBytes()));
        Map<String,Object> params = new HashMap<>();
        params.put("agentAddress",agentgAddress.getBase58());
        params.put("assetId",1);
        params.put("chainId",1);
        params.put("deposit",20000);
        params.put("commissionRate",10);
        params.put("packingAddress",rewardAddress.getBase58());
        params.put("password","");
        params.put("rewardAddress",packingAddress.getBase58());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
        if(success.equals(cmdResp.getResponseStatus())){
            System.out.println(cmdResp.getResponseData());
        }
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void createAgentCommit(){
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        //组装交易

        //组装blockHeader
    }

    @Test
    public void getRoundInfo() throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getRoundInfo", params);
        System.out.println(cmdResp.getResponseData());
        if(success.equals(cmdResp.getResponseStatus())){
            System.out.println(cmdResp.getResponseData());
        }
    }
}
