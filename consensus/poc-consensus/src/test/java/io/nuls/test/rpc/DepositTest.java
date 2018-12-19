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
 * 委托相关操作测试
 * Delegate related operation testing
 *
 * @author tag
 * 2018/12/1
 * */
public class DepositTest {
    protected  String success = "1";

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    public void depositAgent()throws Exception{
        Map<String,Object> params = new HashMap<>();
        Address depositAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        params.put("chainId",1);
        params.put("address",depositAddress.getBase58());
        params.put("agentHash","002029d557bd094f1ef708d9cdd8507357bcbeebde496ef3bd1129b4365012cddfd3");
        //params.put("agentHash","00207d53655ffdb1bd3b5a05bc4d6e14d7c9980ff22e889fa7c2374e2c4b9cd8119f");
        params.put("deposit","300000");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_depositToAgent", params);
        System.out.println(cmdResp.getResponseData());

    }

    @Test
    public void depositCommit()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        //组装交易
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(100);
        //组装blockHeader
        params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
        params.put("tx","0500599b7fc067010049e09304000000000000000000000000000100014a25417a133876da5e0cdd04a983a8a5d8e70172002029d557bd094f1ef708d9cdd8507357bcbeebde496ef3bd1129b4365012cddfd36801170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100801a06000000000000000000000000000800000000000000000001170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100e0930400000000000000000000000000ffffffff00");
        //params.put("tx","0500bdc742a167010049e09304000000000000000000000000000100014a25417a133876da5e0cdd04a983a8a5d8e7017200207d53655ffdb1bd3b5a05bc4d6e14d7c9980ff22e889fa7c2374e2c4b9cd8119f6801170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100801a06000000000000000000000000000800000000000000000001170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100e0930400000000000000000000000000ffffffff00");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_depositCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void depositRollback()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        //组装交易
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(100);
        //组装blockHeader
        params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
        params.put("tx","0500bdc742a167010049e09304000000000000000000000000000100014a25417a133876da5e0cdd04a983a8a5d8e7017200207d53655ffdb1bd3b5a05bc4d6e14d7c9980ff22e889fa7c2374e2c4b9cd8119f6801170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100801a06000000000000000000000000000800000000000000000001170100014a25417a133876da5e0cdd04a983a8a5d8e7017201000100e0930400000000000000000000000000ffffffff00");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_depositRollBack", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void withdraw()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        Address depositAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        params.put("address",depositAddress.getBase58());
        params.put("txHash","");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_withdraw", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void withdrawCommit()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        params.put("tx","");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_withdrawCommit", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void withdrawRollback()throws Exception{
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        params.put("tx","");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_withdrawRollBack", params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void getDepositList()throws Exception{
        Address depositAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fe2Xqmfd".getBytes()));
        Map<String,Object>params = new HashMap<>();
        params.put("chainId",1);
        params.put("address",depositAddress.getBase58());
        params.put("agentHash","0020fef3f394953c601f6abe82f223d5c5673d3b4d7461e575f663954a7c4e055317");
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getDepositList", params);
        System.out.println(cmdResp.getResponseData());
    }
}
