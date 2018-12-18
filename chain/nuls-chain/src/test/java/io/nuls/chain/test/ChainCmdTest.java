package io.nuls.chain.test;


import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.model.dto.Seed;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
public class ChainCmdTest {
    @Before
    public void init() throws Exception {
//        NoUse.mockModule();
    }

    @Test
    public void chain() throws Exception {
        Map<String, String> yiFeng = new HashMap<>();
        yiFeng.put("initNumber", "222");
        JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Asset asset = JSONUtils.map2pojo(yiFeng, Asset.class);
        System.out.println(JSONUtils.obj2json(asset));
    }

    @Test
    public void chainReg() throws Exception {
        System.out.println(CmdDispatcher.requestAndResponse(ModuleE.CM.abbr, "cm_chainReg", null));
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
        BlockChain blockChain = new BlockChain();
        blockChain.setChainId((short) 867);
        blockChain.setName("ilovess");
        blockChain.setAddressType(CmConstants.ADDRESS_TYPE_NULS);
        blockChain.setMagicNumber(19870921);
        blockChain.setSupportInflowAsset(false);
        blockChain.setSingleNodeMinConnectionNum(6);
        blockChain.setMinAvailableNodeNum(66);
        blockChain.setTxConfirmedBlockNum(9);
        List<Seed> seedList = new ArrayList<>();
        Seed seed1 = new Seed();
        seed1.setIp("1.1.2.2");
        seed1.setPort(1122);
        seedList.add(seed1);
        Seed seed2 = new Seed();
        seed2.setIp("3.3.4.4");
        seed2.setPort(3344);
        seedList.add(seed2);
        blockChain.setDelete(false);
        blockChain.setCreateTime(TimeService.currentTimeMillis());
//        System.out.println(CmdDispatcher.call("chainRegCommit", new Object[]{chain}, 1.0));
    }

    @Test
    public void setChainAssetCurrentNumber() throws Exception {
//        System.out.println(CmdDispatcher.call("setChainAssetCurrentNumber", new Object[]{(short) 867, 1542092573248L, 147258300}, 1.0));
//        System.out.println(CmdDispatcher.call("chain", new Object[]{(short) 867}));
//        System.out.println(CmdDispatcher.call("asset", new Object[]{1542092573248L}));
    }
}
