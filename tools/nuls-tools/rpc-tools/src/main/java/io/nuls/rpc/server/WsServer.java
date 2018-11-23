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

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.ServerRuntime;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.RegisterApi;
import io.nuls.tools.log.Log;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;

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
            Log.info("Handshake success." + ServerRuntime.local.getName() + " ready!");
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
            Log.info("Server<" + ServerRuntime.local.getAbbr() + ":" + ServerRuntime.local.getPort() + "> receive:" + msg);
            ServerRuntime.REQUEST_QUEUE.add(new Object[]{webSocket, msg});
            ServerRuntime.fixedThreadPool.execute(new WsProcessor());
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
        Log.info("Server<" + ServerRuntime.local.getAbbr() + ":" + ServerRuntime.local.getPort() + ">-> started.");
    }

    /**
     * Get a server instance
     * Using predefined
     */
    public static WsServer getInstance(ModuleE moduleE) {
        return getInstance(moduleE.abbr, moduleE.name);
    }

    /**
     * Get a server instance
     * Using Abbreviation & Name
     */
    public static WsServer getInstance(String abbr, String name) {
        WsServer wsServer = new WsServer(HostInfo.randomPort());
        ServerRuntime.local.setAbbr(abbr);
        ServerRuntime.local.setName(name);
        ServerRuntime.local.setAddress(HostInfo.getIpAdd());
        ServerRuntime.local.setPort(wsServer.getPort());
        return wsServer;
    }

    /**
     * Scanning the CMD provided by this module
     */
    public WsServer setScanPackage(String scanPackage) throws Exception {
        RegisterApi registerApi = new RegisterApi();
        registerApi.setApiMethods(new ArrayList<>());
        registerApi.setServiceSupportedAPIVersions(new ArrayList<>());
        registerApi.setAbbr(ServerRuntime.local.getAbbr());
        registerApi.setName(ServerRuntime.local.getName());
        registerApi.setAddress(ServerRuntime.local.getAddress());
        registerApi.setPort(ServerRuntime.local.getPort());
        ServerRuntime.local.setRegisterApi(registerApi);
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
        ServerRuntime.local.setAbbr(ModuleE.KE.abbr);
        ServerRuntime.local.setName(ModuleE.KE.name);
        ServerRuntime.local.setAddress(HostInfo.getIpAdd());
        ServerRuntime.local.setPort(wsServer.getPort());

        wsServer.setScanPackage("io.nuls.rpc.cmd.kernel").connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();

        Thread.sleep(Integer.MAX_VALUE);
    }
}
