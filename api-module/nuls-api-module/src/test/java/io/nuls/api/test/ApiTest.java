package io.nuls.api.test;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.db.BlockService;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.BlockInfo;
import io.nuls.api.model.po.db.TransactionInfo;
import io.nuls.base.data.Block;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.bootstrap.NettyServer;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static io.nuls.api.constant.Constant.DEFAULT_SCAN_PACKAGE;
import static io.nuls.api.constant.Constant.RPC_DEFAULT_SCAN_PACKAGE;
import static io.nuls.api.utils.LoggerUtil.commonLog;

public class ApiTest {


    //    @Before
    public void before() {
        SpringLiteContext.init(DEFAULT_SCAN_PACKAGE);
        try {
            //rpc服务初始化
            NettyServer.getInstance(ModuleE.AP)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .dependencies(ModuleE.KE.abbr, "1.0")
//                    .dependencies(ModuleE.CM.abbr, "1.0")
//                    .dependencies(ModuleE.AC.abbr, "1.0")
//                    .dependencies(ModuleE.NW.abbr, "1.0")
//                    .dependencies(ModuleE.CS.abbr, "1.0")
                    .dependencies(ModuleE.BL.abbr, "1.0")
//                    .dependencies(ModuleE.LG.abbr, "1.0")
//                    .dependencies(ModuleE.TX.abbr, "1.0")
//                    .dependencies(ModuleE.PU.abbr, "1.0")
                    .scanPackage(RPC_DEFAULT_SCAN_PACKAGE);
            // Get information from kernel
            String kernelUrl = "ws://" + HostInfo.getLocalIP() + ":8887/ws";
            ConnectManager.getConnectByUrl(kernelUrl);
            ResponseMessageProcessor.syncKernel(kernelUrl);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("error occur when init, " + e.getMessage());
        }
    }

    @Test
    public void testCmdCall() {

        for (int i = 0; i < 10000; i++) {
            BlockInfo block = WalletRpcHandler.getBlockInfo(12345, i);
            for (TransactionInfo tx : block.getTxList()) {
                if (tx.getType() == 1) {

                }
            }
        }
    }



}
