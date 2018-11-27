/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.rpc.server;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.RegisterApi;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class WsServer extends WebSocketServer {
    public WsServer(int port) {
        super(new InetSocketAddress(port));
    }

    public void connect(String kernelUrl) throws Exception {
        this.start();
        Thread.sleep(1000);
        Constants.kernelUrl = kernelUrl;
        if (!CmdDispatcher.handshakeKernel()) {
            throw new Exception("Handshake kernel failed");
        } else {
            Log.info("Handshake success." + ServerRuntime.local.getModuleName() + " ready!");
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake handshake) {
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket webSocket, String msg) {
        try {
            Log.info("ServerMsgFrom<" + webSocket.getRemoteSocketAddress().getHostString() + ":" + webSocket.getRemoteSocketAddress().getPort() + ">: " + msg);
            ServerRuntime.REQUEST_QUEUE.add(new Object[]{webSocket, msg});
            ServerRuntime.serverThreadPool.execute(new ServerProcessor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        Log.error(ex);
    }

    @Override
    public void onStart() {
        Log.info("Server<" + ServerRuntime.local.getConnectionInformation().get(Constants.KEY_IP) + ":" + ServerRuntime.local.getConnectionInformation().get(Constants.KEY_PORT) + ">-> started.");
    }

    /**
     * Get a server instance
     * Using predefined
     */
    public static WsServer getInstance(ModuleE moduleE) {
        return getInstance(moduleE.abbr, moduleE.name, moduleE.domain);
    }

    /**
     * Get a server instance
     * Using Abbreviation & Name
     */
    public static WsServer getInstance(String abbr, String name, String domain) {
        WsServer wsServer = new WsServer(HostInfo.randomPort());
        RegisterApi registerApi = new RegisterApi();
        registerApi.setModuleAbbreviation(abbr);
        registerApi.setModuleName(name);
        registerApi.setModuleDomain(domain);
        Map<String, String> connectionInformation = new HashMap<>(2);
        connectionInformation.put(Constants.KEY_IP, HostInfo.getIpAdd());
        connectionInformation.put(Constants.KEY_PORT, wsServer.getPort() + "");
        registerApi.setConnectionInformation(connectionInformation);
        registerApi.setApiMethods(new ArrayList<>());
        registerApi.setDependencies(new HashMap<>(16));
        registerApi.setModuleRoles(new HashMap<>(1));
        ServerRuntime.local = registerApi;

        return wsServer;
    }

    public WsServer dependencies(String key, String value) {
        ServerRuntime.local.getDependencies().put(key, value);
        return this;
    }

    public WsServer moduleRoles(String[] value) {
        ServerRuntime.local.getModuleRoles().put(ServerRuntime.local.getModuleAbbreviation(), value);
        return this;
    }

    public WsServer moduleRoles(String key, String[] value) {
        ServerRuntime.local.getModuleRoles().put(key, value);
        return this;
    }

    public WsServer moduleVersion(String moduleVersion) {
        ServerRuntime.local.setModuleVersion(moduleVersion);
        return this;
    }

    /**
     * Scanning the CMD provided by this module
     */
    public WsServer scanPackage(String scanPackage) throws Exception {
        ServerRuntime.scanPackage(scanPackage);
        return this;
    }

    /**
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
        connectionInformation.put(Constants.KEY_IP, HostInfo.getIpAdd());
        connectionInformation.put(Constants.KEY_PORT, wsServer.getPort() + "");
        registerApi.setConnectionInformation(connectionInformation);

        ServerRuntime.local = registerApi;

        wsServer.scanPackage("io.nuls.rpc.cmd.kernel").connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();

        System.out.println("Local:" + JSONUtils.obj2json(ServerRuntime.local));
        Thread.sleep(Integer.MAX_VALUE);
    }

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
