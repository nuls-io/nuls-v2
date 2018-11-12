package io.nuls.chain.test;

import io.nuls.base.data.chain.Chain;
import io.nuls.base.data.chain.Seed;
import io.nuls.chain.ChainBootstrap;
import io.nuls.chain.info.CmConstants;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.tools.thread.TimeService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
public class ChainCmdTest {
    @Before
    public void init() {
        ChainBootstrap.getInstance().start();
    }

    @Test
    public void chain() throws Exception {
        System.out.println(CmdDispatcher.call("chain", new Object[]{(short) 867}, 1.0));
    }

    @Test
    public void chainReg() throws Exception {
        System.out.println(CmdDispatcher.call("chainReg", new Object[]{(short) 867, "ilovess", "NULS", 19870921, true, 5, 10, 8, "1.1.2.2:1122,3.3.4.4:3344", false}, 1.0));
    }

    @Test
    public void chainRegValidator() throws Exception {
        Chain chain = new Chain();
        chain.setChainId((short) -5);
        chain.setName("ilovess");
        chain.setAddressType(CmConstants.ADDRESS_TYPE_NULS);
        System.out.println(CmdDispatcher.call("chainRegValidator", new Object[]{chain}, 1.0));
    }

    @Test
    public void chainRegCommit() throws Exception {
        Chain chain = new Chain();
        chain.setChainId((short) 867);
        chain.setName("ilovess");
        chain.setAddressType(CmConstants.ADDRESS_TYPE_NULS);
        chain.setMagicNumber(19870921);
        chain.setSupportInflowAsset(false);
        chain.setSingleNodeMinConnectionNum(6);
        chain.setMinAvailableNodeNum(66);
        chain.setTxConfirmedBlockNum(9);
        List<Seed> seedList = new ArrayList<>();
        Seed seed1 = new Seed();
        seed1.setIp("1.1.2.2");
        seed1.setPort(1122);
        seedList.add(seed1);
        Seed seed2 = new Seed();
        seed2.setIp("3.3.4.4");
        seed2.setPort(3344);
        seedList.add(seed2);
        chain.setSeedList(seedList);
        chain.setAvailable(true);
        chain.setCreateTime(TimeService.currentTimeMillis());
        System.out.println(CmdDispatcher.call("chainRegCommit", new Object[]{chain}, 1.0));
    }
}
