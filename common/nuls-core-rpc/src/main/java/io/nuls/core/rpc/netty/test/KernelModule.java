package io.nuls.core.rpc.netty.test;

import io.nuls.core.rpc.netty.bootstrap.NettyServer;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author tag
 */
public class KernelModule {
    /**
     * Simulate core modules（Manager）, dedicated to testing
     * For internal debugging only
     * Simulate a kernel module
     */
    public static void mockKernel() throws Exception {
        int port = 7771;
        NettyServer.startServer(port);
        // Start server instance
        ConnectManager.LOCAL.setMethods(new ArrayList<>());
        ConnectManager.LOCAL.setAbbreviation(ModuleE.KE.abbr);
        ConnectManager.LOCAL.setModuleName(ModuleE.KE.name);
        ConnectManager.LOCAL.setModuleDomain(ModuleE.KE.domain);
        Map<String, String> connectionInformation = new HashMap<>(2);
        connectionInformation.put(Constants.KEY_IP, HostInfo.getLocalIP());
        connectionInformation.put(Constants.KEY_PORT, port + "");
        ConnectManager.LOCAL.setConnectionInformation(connectionInformation);
        ConnectManager.startService = true;
        SpringLiteContext.init("io.nuls.rpc.cmd.kernel");
        ConnectManager.scanPackage(Set.of("io.nuls.rpc.cmd.kernel"));
        ConnectManager.ROLE_MAP.put(ModuleE.KE.abbr, connectionInformation);
        ConnectManager.updateStatus();
        // Get information from kernel
        //Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * Simulated startup module, dedicated to unit testing
     * Analog Startup Module, Unit Test Specific
     */
    public static void mockModule() throws Exception {
        NettyServer.getInstance("test", "TestModule", "test.com")
                .moduleRoles("test_role", new String[]{"1.0"})
                .moduleVersion("1.0");

        ConnectManager.getConnectByUrl("ws://" + HostInfo.getLocalIP() + ":7771");
        // Get information from kernel
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
    }

    /**
     * Simulated startup module, dedicated to unit testing
     * Analog Startup Module, Unit Test Specific
     */
    public static void mockModule1() throws Exception {
        NettyServer.getInstance("test1", "TestModule1", "test1.com")
                .moduleRoles("test_role1", new String[]{"1.0"})
                .moduleVersion("1.0");

        ConnectManager.getConnectByUrl("ws://" + HostInfo.getLocalIP() + ":7771");
        // Get information from kernel
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
    }

    public static void main(String[] args) {
        try {
            mockKernel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
