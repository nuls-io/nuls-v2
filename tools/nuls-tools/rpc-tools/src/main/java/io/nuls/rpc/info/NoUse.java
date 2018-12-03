package io.nuls.rpc.info;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.RegisterApi;
import io.nuls.rpc.server.ServerRuntime;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.parse.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/12/3
 * @description
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
        RegisterApi registerApi = new RegisterApi();
        registerApi.setApiMethods(new ArrayList<>());
        registerApi.setModuleAbbreviation(ModuleE.KE.abbr);
        registerApi.setModuleName(ModuleE.KE.name);
        registerApi.setModuleDomain(ModuleE.KE.domain);
        Map<String, String> connectionInformation = new HashMap<>(2);
        connectionInformation.put(Constants.KEY_IP, HostInfo.getLocalIP());
        connectionInformation.put(Constants.KEY_PORT, wsServer.getPort() + "");
        registerApi.setConnectionInformation(connectionInformation);

        ServerRuntime.local = registerApi;

        wsServer.scanPackage("io.nuls.rpc.cmd.kernel").connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();

        System.out.println("Local:" + JSONUtils.obj2json(ServerRuntime.local));
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 模拟启动模块，单元测试专用
     * Analog Startup Module, Unit Test Specific
     */
    public static void mockModule() throws Exception {
        WsServer.getInstance(ModuleE.TEST)
                .moduleRoles(new String[]{"1.0"})
                .moduleVersion("1.0")
                .dependencies(ModuleE.CM.abbr, "1.1")
                .connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();
    }
}
