package io.nuls.test.rpc;

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

/**
 * 节点相关操作测试
 * Node-related operation testing
 *
 * @author tag
 * 2018/12/1
 * */
public class AgentTest {
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
        /*Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        Address rewardAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmgd".getBytes()));
        Address packingAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fegfgqmd".getBytes()));*/
        Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        Address rewardAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmgd".getBytes()));
        Address packingAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fegfgqmd".getBytes()));
        Map<String,Object> params = new HashMap<>();
        params.put("agentAddress",agentAddress.getBase58());
        params.put("chainId",1);
        params.put("deposit",20000);
        params.put("commissionRate",10);
        params.put("packingAddress",packingAddress.getBase58());
        params.put("password","");
        params.put("rewardAddress",rewardAddress.getBase58());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
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
        //params.put("tx","040091ec9b826701005d204e00000000000000000000000000000100014a25417a133876da5e0cdd04a983a8a5d8e70172010001c833737706ca24c1af266ef599097f796ca05a3c010001c5fb68d127dfde22eac3d79f697766ba449e642d00000000000024406801170100014a25417a133876da5e0cdd04a983a8a5d8e701720100010060900f000000000000000000000000000800000000000000000001170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100204e0000000000000000000000000000ffffffff00");
        params.put("tx","0400a50140a16701005d204e0000000000000000000000000000010001f6db7f28888015528eae577ae82f985589dc63f20100012f627a02ca063f0c1c9466290c376c97a86edf95010001197a64059dd812fcf6e2d4c2bf22f0b320554c770000000000002440680117010001f6db7f28888015528eae577ae82f985589dc63f201000100c0d40100000000000000000000000000080000000000000000000117010001f6db7f28888015528eae577ae82f985589dc63f201000100204e0000000000000000000000000000ffffffff00");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgentCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 保存节点交易回滚
     * */
    public void createAgentRollback()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        params.put("tx","0400a50140a16701005d204e0000000000000000000000000000010001f6db7f28888015528eae577ae82f985589dc63f20100012f627a02ca063f0c1c9466290c376c97a86edf95010001197a64059dd812fcf6e2d4c2bf22f0b320554c770000000000002440680117010001f6db7f28888015528eae577ae82f985589dc63f201000100c0d40100000000000000000000000000080000000000000000000117010001f6db7f28888015528eae577ae82f985589dc63f201000100204e0000000000000000000000000000ffffffff00");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_createAgentRollBack", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 停止节点
     * */
    public void stopAgent()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        params.put("address",agentAddress.getBase58());
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 停止节点提交
     * */
    public void stopAgentCommit()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        //组装交易
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(100);
        //组装blockHeader
        params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
        params.put("tx","");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgentCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 停止节点交易回滚
     * */
    public void stopAgentRollback()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        params.put("tx","");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgentRollBack", params);
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
    public void getAgentInfo()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        params.put("agentHash","0020fef3f394953c601f6abe82f223d5c5673d3b4d7461e575f663954a7c4e055317");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentInfo", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void getAgentStatus()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        params.put("agentHash","0020fef3f394953c601f6abe82f223d5c5673d3b4d7461e575f663954a7c4e055317");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentStatus", params);
        System.out.println(cmdResp.getResponseData());
    }

    public static void main(String[] args){
        Address agentAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfa".getBytes()));
        Address rewardAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fe2Xqmga".getBytes()));
        Address packingAddress = new Address(1,(byte)1,SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fegfgqma".getBytes()));
        System.out.println(agentAddress.getBase58());
        System.out.println(rewardAddress.getBase58());
        System.out.println(packingAddress.getBase58());
    }
}
