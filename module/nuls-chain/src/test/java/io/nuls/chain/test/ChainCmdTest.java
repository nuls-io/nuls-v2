package io.nuls.chain.test;


import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.parse.JSONUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
public class ChainCmdTest {
    @Before
    public void init() throws Exception {
        NoUse.mockModule();
    }
    @Test
    public void  importPriKey() {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put("chainId", 2);
//            477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75
//            4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a
            params.put("priKey", "4efb6c23991f56626bc77cdb341d64e891e0412b03cbcb948aba6d4defb4e60a");
            params.put("password", "nuls123456");
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
            System.out.println(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void chain() throws Exception {
        Map<String, String> yiFeng = new HashMap<>();
        yiFeng.put("initNumber", "222");
        yiFeng.put("test", "33");
        JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Asset asset = JSONUtils.map2pojo(yiFeng, Asset.class);
        System.out.println(JSONUtils.obj2json(asset));
    }

    @Test
    public void chainReg() throws Exception {
        System.out.println(ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_chainReg", null));
    }

    @Test
    public void chainRegCommit() throws Exception {
       Map<String,Object>  parameters = new HashMap<>();
        parameters.put("chainId",100);
        parameters.put("chainName","ilovess");
        parameters.put("addressType","1");
        parameters.put("magicNumber",2000);
        parameters.put("minAvailableNodeNum",1);
        parameters.put("singleNodeMinConnectionNum",1);
        parameters.put("txConfirmedBlockNum",10);

        parameters.put("address","tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm");
        parameters.put("assetId",1);
        parameters.put("symbol","ns2");
        parameters.put("assetName","nulson2");
        parameters.put("initNumber","100000000");
        parameters.put("decimalPlaces",8);
        parameters.put("password","nuls123456");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_chainReg", parameters);
       System.out.println(JSONUtils.obj2json(response));

    }

    @Test
    public void getChain() throws Exception {
        Map<String,Object>  parameters = new HashMap<>();
        parameters.put("chainId",100);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_chain", parameters);
        System.out.println(JSONUtils.obj2json(response));

    }

    @Test
    public void setChainAssetCurrentNumber() throws Exception {
//        System.out.println(CmdDispatcher.call("setChainAssetCurrentNumber", new Object[]{(short) 867, 1542092573248L, 147258300}, 1.0));
//        System.out.println(CmdDispatcher.call("chain", new Object[]{(short) 867}));
//        System.out.println(CmdDispatcher.call("asset", new Object[]{1542092573248L}));
    }
    @Test
    public void logTest(){
        LoggerUtil.logger().info("this chain log test");
    }
}
