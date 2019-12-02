package io.nuls.test.rpc;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.utils.manager.CoinDataManager;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
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
        Map<String,Object> params = new HashMap<>();
        params.put("agentAddress","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        params.put(Constants.CHAIN_ID,2);
        params.put("deposit","2000000000000");
        params.put("commissionRate",10);
        params.put("packingAddress","tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn");
        params.put("password","nuls123456");
        params.put("rewardAddress","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgent", params);
        System.out.println(cmdResp.getResponseData());
        //22b7d4dfbffab1bf5be5f1b63fd8ab8e1fdf84c306c5be297bd9996cc58320c7
    }

    @Test
    /**
     * 保存节点
     * */
    public void createAgentCommit()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        //组装交易
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(100);
        //组装blockHeader
        params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
        //params.put("tx","0400b55977c06701005d204e00000000000000000000000000000100014a25417a133876da5e0cdd04a983a8a5d8e70172010001c5fb68d127dfde22eac3d79f697766ba449e642d010001c833737706ca24c1af266ef599097f796ca05a3c00000000000024406801170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100c0d401000000000000000000000000000800000000000000000001170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100204e0000000000000000000000000000ffffffff00");
        params.put("tx","0400a9a87ac06701005d204e0000000000000000000000000000010001f6db7f28888015528eae577ae82f985589dc63f20100012f627a02ca063f0c1c9466290c376c97a86edf95010001197a64059dd812fcf6e2d4c2bf22f0b320554c770000000000002440680117010001f6db7f28888015528eae577ae82f985589dc63f201000100c0d40100000000000000000000000000080000000000000000000117010001f6db7f28888015528eae577ae82f985589dc63f201000100204e0000000000000000000000000000ffffffff00");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgentCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 保存节点交易回滚
     * */
    public void createAgentRollback()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        params.put("tx","0400a50140a16701005d204e0000000000000000000000000000010001f6db7f28888015528eae577ae82f985589dc63f20100012f627a02ca063f0c1c9466290c376c97a86edf95010001197a64059dd812fcf6e2d4c2bf22f0b320554c770000000000002440680117010001f6db7f28888015528eae577ae82f985589dc63f201000100c0d40100000000000000000000000000080000000000000000000117010001f6db7f28888015528eae577ae82f985589dc63f201000100204e0000000000000000000000000000ffffffff00");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_createAgentRollBack", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 停止节点
     * */
    public void stopAgent()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("address","tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        params.put("password", "nuls123456");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgent", params);
        System.out.println(cmdResp.getResponseData());
    }

    /**
     * 停止节点
     * */
    @Test
    public void stopOtherAgent() throws Exception{
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode("0900e5d6e45d002022b7d4dfbffab1bf5be5f1b63fd8ab8e1fdf84c306c5be297bd9996cc58320c7fd16010217020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010000204aa9d1010000000000000000000000000000000000000000000000000000087bd9996cc58320c7ff170200018f44b8662e78871f44ef1e1608282fd59560dcd0020001000030ef7dba0200000000000000000000000000000000000000000000000000000802dbee733174b634ff0217020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100609948a9d101000000000000000000000000000000000000000000000000000065cbe85d00000000170200018f44b8662e78871f44ef1e1608282fd59560dcd0020001000030ef7dba0200000000000000000000000000000000000000000000000000000000000000000000692103958b790c331954ed367d37bac901de5c2f06ac8368b37d7bd6cd5ae143c1d7e34630440220319657c4de48083d18ff6ad004b4293cec7d27ede32bfe6134b2fca8c5e08f5002207192a3ce6b75b0bddda35e8700399ff3b2ca68593c577af11626b9ce4b991a25"), 0);

//        tx.setTransactionSignature(null);
//        HashMap callResult = CallMethodUtils.accountValid(2, "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm", "nuls123456");
//        String priKey = (String) callResult.get("priKey");
//        CallMethodUtils.transactionSignature(2, "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm", "nuls123456", priKey, tx);

        String txStr = RPCUtil.encode(tx.serialize());
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(2);
        chain.setConfig(configBean);
        CallMethodUtils.sendTx(chain, txStr);
    }


    @Test
    /**
     * 停止节点提交
     * */
    public void stopAgentCommit()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        //组装交易
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(100);
        //组装blockHeader
        params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
        params.put("tx","");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgentCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 停止节点交易回滚
     * */
    public void stopAgentRollback()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        params.put("tx","");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_stopAgentRollBack", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    /**
     * 获取节点列表
     * */
    public void getAgentList()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentList", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void getAgentInfo()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,2);
        params.put("agentHash","232c772c667323d7cca96074cf7c06dba52777d88b0c6275177cc7bdc2196b70");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentInfo", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void getAgentStatus()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,1);
        params.put("agentHash","0020fef3f394953c601f6abe82f223d5c5673d3b4d7461e575f663954a7c4e055317");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentStatus", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void testBigInteger()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put(Constants.CHAIN_ID,new BigInteger("26778686868678686867"));
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_testBigInteger", params);
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
