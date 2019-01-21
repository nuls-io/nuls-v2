package io.nuls.rpc.info;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    public static void mockKernel() throws Exception {
        WsServer wsServer = new WsServer(8887);
        // Start server instance
        ServerRuntime.LOCAL.setApiMethods(new ArrayList<>());
        ServerRuntime.LOCAL.setModuleAbbreviation(ModuleE.KE.abbr);
        ServerRuntime.LOCAL.setModuleName(ModuleE.KE.name);
        ServerRuntime.LOCAL.setModuleDomain(ModuleE.KE.domain);
        Map<String, String> connectionInformation = new HashMap<>(2);
        connectionInformation.put(Constants.KEY_IP, "127.0.0.1");
        connectionInformation.put(Constants.KEY_PORT, wsServer.getPort() + "");
        ServerRuntime.LOCAL.setConnectionInformation(connectionInformation);
        ServerRuntime.startService = true;
        SpringLiteContext.init("io.nuls.rpc.cmd.kernel");
        wsServer.scanPackage("io.nuls.rpc.cmd.kernel").connect("ws://127.0.0.1:8887");

        ClientRuntime.ROLE_MAP.put(ModuleE.KE.abbr,connectionInformation);
        // Get information from kernel
        CmdDispatcher.syncKernel();

        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 模拟启动模块，单元测试专用
     * Analog Startup Module, Unit Test Specific
     */
    public static void mockModule() throws Exception {
        WsServer.getInstance("test", "TestModule", "test.com")
                .moduleRoles("test_role", new String[]{"1.0"})
                .moduleVersion("1.0")
//                .dependencies(ModuleE.CM.abbr, "1.1")
                .connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();
    }
}
