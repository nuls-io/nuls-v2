package io.nuls.chain.test;

import io.nuls.chain.ChainBootstrap;
import io.nuls.rpc.cmd.CmdDispatcher;
import org.junit.Before;
import org.junit.Test;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
public class AssetCmdTest {
    @Before
    public void init() {
        ChainBootstrap.getInstance().start();
    }

    @Test
    public void asset() throws Exception {
        System.out.println(CmdDispatcher.call("asset", new Object[]{(short) 1}));
    }
}
