/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.rpc;

import io.nuls.rpc.cmd.HeartbeatCmd;
import io.nuls.rpc.cmd.JoinCmd;
import io.nuls.rpc.cmd.RpcListCmd;
import io.nuls.rpc.pojo.Rpc;
import io.nuls.rpc.thread.HeartbeatCheck;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RpcServer {
    private static RpcServer rpcServer = null;

    public static synchronized RpcServer getInstance(int selfPort) {
        try {
            if (rpcServer == null) {
                rpcServer = new RpcServer(RpcInfo.getIpAdd(), selfPort);
            }

            return rpcServer;
        } catch (Exception e) {
            return null;
        }
    }

    private String ip;
    private int port;

    private RpcServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private String getBaseUri() {
        return "http://" + ip + ":" + port + "/";
    }

    public void register(String cmd, int version, Class invokeClass) {
        register(cmd, version, invokeClass, RpcInfo.DEFAULT_PATH);
    }

    public void register(String cmd, int version, Class invokeClass, String monitorPath) {
        Rpc rpc = buildRpc(cmd, version, invokeClass, monitorPath);
        RpcInfo.localInterfaceMap.put(RpcInfo.generateKey(rpc.getCmd(), rpc.getVersion()), rpc);
    }

    private void registerDefault(String cmd, Class invokeClass) {
        Rpc rpc = buildRpc(cmd, RpcInfo.VERSION, invokeClass, RpcInfo.DEFAULT_PATH);
        RpcInfo.defaultInterfaceMap.put(RpcInfo.generateKey(rpc.getCmd(), rpc.getVersion()), rpc);
    }

    private Rpc buildRpc(String cmd, int version, Class invokeClass, String monitorPath) {
        Rpc rpc = new Rpc();
        rpc.setCmd(cmd);
        rpc.setVersion(version);
        rpc.setUri(getBaseUri());
        rpc.setInvokeClass(invokeClass);
        rpc.setMonitorPath(monitorPath);
        return rpc;
    }

    public void start() {
        registerDefault(RpcInfo.CMD_JOIN, JoinCmd.class);
        registerDefault(RpcInfo.CMD_LIST, RpcListCmd.class);
        registerDefault(RpcInfo.CMD_HEARTBEAT, HeartbeatCmd.class);

        final HashMap<String, String> initParams = new HashMap<>(16);
        initParams.put("jersey.config.server.provider.packages", "io.nuls.rpc.handler");

        try {
            GrizzlyWebContainerFactory.create(getBaseUri(), initParams);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 心跳线程
        new Thread(new HeartbeatCheck()).start();
    }
}
