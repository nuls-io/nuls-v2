package io.nuls.chain.test;

import io.nuls.base.data.chain.Asset;
import io.nuls.chain.ChainBootstrap;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.tools.thread.TimeService;
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
        System.out.println(CmdDispatcher.call("asset", new Object[]{1542092573248l}));
        System.out.println(CmdDispatcher.call("asset", new Object[]{1542092632850l}));
    }

    @Test
    public void assetReg() throws Exception {
        System.out.println(
                CmdDispatcher.call(
                        "assetReg",
                        new Object[]{(short) 867, "G", "Gold", 200000, 21000000, 8, true}));
    }

    @Test
    public void assetRegValidator() throws Exception {
        Asset asset = new Asset();
        asset.setAssetId(1542092573248L);
        asset.setSymbol("g");
        System.out.println(CmdDispatcher.call("assetRegValidator", new Object[]{asset}));
    }

    @Test
    public void assetRegCommit() throws Exception {
        Asset asset = new Asset();
        asset.setChainId((short) 867);
        asset.setAssetId(1542092573248L);
        asset.setSymbol("B");
        asset.setName("bts");
        asset.setDepositNuls(200000);
        asset.setInitNumber(147258369);
        asset.setDecimalPlaces((short) 8);
        asset.setAvailable(true);
        asset.setCreateTime(TimeService.currentTimeMillis());
        System.out.println(CmdDispatcher.call("assetRegCommit", new Object[]{asset}));
    }

    @Test
    public void assetRegRollback() throws Exception {

    }

    @Test
    public void assetEnable() throws Exception{
        System.out.println(CmdDispatcher.call("assetEnable", new Object[]{1542092573248L}));
        System.out.println(CmdDispatcher.call("asset", new Object[]{1542092573248L}));
    }

    @Test
    public void assetDisable() throws Exception{
        System.out.println(CmdDispatcher.call("assetDisable", new Object[]{1542092573248L}));
        System.out.println(CmdDispatcher.call("asset", new Object[]{1542092573248L}));
    }
}
