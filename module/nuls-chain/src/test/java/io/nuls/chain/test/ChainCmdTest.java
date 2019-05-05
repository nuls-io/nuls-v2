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
    public void chainRegValidator() throws Exception {
        BlockChain blockChain = new BlockChain();
        blockChain.setChainId((short) -5);
        blockChain.setAddressType(CmConstants.ADDRESS_TYPE_NULS);
//        System.out.println(CmdDispatcher.call("chainRegValidator", new Object[]{chain}, 1.0));
    }

    @Test
    public void chainRegCommit() throws Exception {
       Map<String,Object>  parameters = new HashMap<>();
        parameters.put("chainId",200);
        parameters.put("chainName","ilovess");
        parameters.put("addressType","1");
        parameters.put("magicNumber",3000);
        parameters.put("minAvailableNodeNum",1);
        parameters.put("singleNodeMinConnectionNum",1);
        parameters.put("txConfirmedBlockNum",10);

        parameters.put("address","tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm");
        parameters.put("assetId",1);
        parameters.put("symbol","ns2");
        parameters.put("assetName","nulson2");
        parameters.put("initNumber","10000000000000");
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
