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
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.RuntimeInfo;
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

//    public void init(ModuleE moduleE, String scanPackage) throws Exception {
//        RuntimeInfo.local.setAbbr(moduleE.abbr);
//        RuntimeInfo.local.setName(moduleE.name);
//        RuntimeInfo.local.setAddress(HostInfo.getIpAdd());
//        RuntimeInfo.local.setPort(this.getPort());
//        RegisterApi registerApi = new RegisterApi();
//        registerApi.setApiMethods(new ArrayList<>());
//        registerApi.setServiceSupportedAPIVersions(new ArrayList<>());
//        registerApi.setAbbr(RuntimeInfo.local.getAbbr());
//        registerApi.setName(RuntimeInfo.local.getName());
//        registerApi.setAddress(RuntimeInfo.local.getAddress());
//        registerApi.setPort(RuntimeInfo.local.getPort());
//        RuntimeInfo.local.setRegisterApi(registerApi);
//        RuntimeInfo.scanPackage(scanPackage);
//    }

    public void connect(String kernelUrl) throws Exception {
        this.start();
        Thread.sleep(1000);
        RuntimeInfo.kernelUrl = kernelUrl;
        if (!CmdDispatcher.handshakeKernel()) {
            throw new Exception("Handshake kernel failed");
        } else {
            Log.info("Handshake success." + RuntimeInfo.local.getName() + " ready!");
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
            Log.info("Server<" + RuntimeInfo.local.getAbbr() + ":" + RuntimeInfo.local.getPort() + "> receive:" + msg);
            RuntimeInfo.REQUEST_QUEUE.add(new Object[]{webSocket, msg});
            RuntimeInfo.fixedThreadPool.execute(new WsProcessor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        Log.error("ws server-> on error");
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Log.info("Server<" + RuntimeInfo.local.getAbbr() + ":" + RuntimeInfo.local.getPort() + ">-> started.");
    }

    public static void mockKernel() throws Exception {
        WsServer wsServer = new WsServer(8887);
        // Start server instance
        wsServer.setModuleE(ModuleE.KE);
        wsServer.setScanPackage("io.nuls.rpc.cmd.kernel").connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();

        Thread.sleep(Integer.MAX_VALUE);
    }

    private void setModuleE(ModuleE moduleE) {
        RuntimeInfo.local.setAbbr(moduleE.abbr);
        RuntimeInfo.local.setName(moduleE.name);
        RuntimeInfo.local.setAddress(HostInfo.getIpAdd());
        RuntimeInfo.local.setPort(getPort());
    }

    public static WsServer getInstance(ModuleE moduleE) {
        WsServer wsServer = new WsServer(HostInfo.randomPort());
        RuntimeInfo.local.setAbbr(moduleE.abbr);
        RuntimeInfo.local.setName(moduleE.name);
        RuntimeInfo.local.setAddress(HostInfo.getIpAdd());
        RuntimeInfo.local.setPort(wsServer.getPort());
        return wsServer;
    }

    public static WsServer getInstance(String abbr, String name) {
        WsServer wsServer = new WsServer(HostInfo.randomPort());
        RuntimeInfo.local.setAbbr(abbr);
        RuntimeInfo.local.setName(name);
        RuntimeInfo.local.setAddress(HostInfo.getIpAdd());
        RuntimeInfo.local.setPort(wsServer.getPort());
        return wsServer;
    }

    public WsServer setScanPackage(String scanPackage) throws Exception {
        RegisterApi registerApi = new RegisterApi();
        registerApi.setApiMethods(new ArrayList<>());
        registerApi.setServiceSupportedAPIVersions(new ArrayList<>());
        registerApi.setAbbr(RuntimeInfo.local.getAbbr());
        registerApi.setName(RuntimeInfo.local.getName());
        registerApi.setAddress(RuntimeInfo.local.getAddress());
        registerApi.setPort(RuntimeInfo.local.getPort());
        RuntimeInfo.local.setRegisterApi(registerApi);
        RuntimeInfo.scanPackage(scanPackage);
        return this;
    }
}
