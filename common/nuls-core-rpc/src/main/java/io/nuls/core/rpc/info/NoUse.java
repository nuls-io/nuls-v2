package io.nuls.core.rpc.info;

import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.bootstrap.NettyServer;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用于测试
 *
 * @author tangyi
 * @date 2018/12/3
 */
public class NoUse {
    /**
     * 模拟核心模块（Manager），测试专用
     * For internal debugging only
     * Simulate a kernel module
     */
    public static int mockKernel() throws Exception {
        SpringLiteContext.init("io.nuls.core.rpc.cmd.kernel");
        return startKernel();
    }

    public static int startKernel() throws Exception {
        int port = 8887;
        NettyServer.startServer(port);
        // Start server instance
        ConnectManager.LOCAL.setMethods(new ArrayList<>());
        ConnectManager.LOCAL.setAbbreviation(ModuleE.KE.abbr);
        ConnectManager.LOCAL.setModuleName(ModuleE.KE.name);
        ConnectManager.LOCAL.setModuleDomain(ModuleE.KE.domain);
        Map<String, String> connectionInformation = new HashMap<>(2);
//        connectionInformation.put(Constants.KEY_IP, HostInfo.getLocalIP());
        connectionInformation.put(Constants.KEY_IP, "0.0.0.0");
        connectionInformation.put(Constants.KEY_PORT, port + "");
        ConnectManager.LOCAL.setConnectionInformation(connectionInformation);
        ConnectManager.startService = true;
        ConnectManager.scanPackage(Set.of("io.nuls.core.rpc.cmd.kernel"));
        ConnectManager.ROLE_MAP.put(ModuleE.KE.abbr, connectionInformation);
        ConnectManager.updateStatus();
        return port;
    }

    /**
     * 模拟启动模块，单元测试专用
     * Analog Startup Module, Unit Test Specific
     */
    public static void mockModule() throws Exception {
        NettyServer.getInstance("test", "TestModule", "test.com")
                .moduleRoles("test_role", new String[]{"1.0"})
                .moduleVersion("1.0");

        ConnectManager.getConnectByUrl("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        // Get information from kernel
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
    }
}
