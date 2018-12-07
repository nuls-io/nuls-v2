package io.nuls.test;

import io.nuls.base.data.Address;
import io.nuls.base.data.BlockHeader;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.parse.SerializeUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AgentTest {
    protected int chainId = 1;
    protected  String success = "1";
    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    /**
     * 创建节点
     * */
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
    /**
     * 保存节点
     * */
    public void createAgentCommit()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        //组装交易
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(100);
        //组装blockHeader
        params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
        params.put("tx","040091ec9b826701005d204e00000000000000000000000000000100014a25417a133876da5e0cdd04a983a8a5d8e70172010001c833737706ca24c1af266ef599097f796ca05a3c010001c5fb68d127dfde22eac3d79f697766ba449e642d00000000000024406801170100014a25417a133876da5e0cdd04a983a8a5d8e701720100010060900f000000000000000000000000000800000000000000000001170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100204e0000000000000000000000000000ffffffff00");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgentCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 获取节点列表
     * */
    public void getAgentList()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentList", params);
        System.out.println(cmdResp.getResponseData());
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
