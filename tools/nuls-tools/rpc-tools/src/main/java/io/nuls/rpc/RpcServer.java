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

import com.alibaba.fastjson.JSON;
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

    public void register(String cmd, int version, String invokeClass) {
        register(cmd, version, invokeClass, RpcInfo.DEFAULT_PATH);
    }

    public void register(String cmd, int version, String invokeClass, String monitorPath) {

        Rpc rpc = buildRpc(cmd, version, invokeClass, monitorPath);
        System.out.println(JSON.toJSONString(rpc));
        RpcInfo.localInterfaceMap.put(rpc.generateKey(), rpc);
    }

    private void registerDefault(String cmd, String invokeClass) {

        Rpc rpc = buildRpc(cmd, RpcInfo.VERSION, invokeClass, RpcInfo.DEFAULT_PATH);
        RpcInfo.defaultInterfaceMap.put(rpc.generateKey(), rpc);
    }

    private Rpc buildRpc(String cmd, int version, String invokeClass, String monitorPath) {
        Rpc rpc = new Rpc();
        rpc.setCmd(cmd);
        rpc.setVersion(version);
        rpc.setUri(getBaseUri());
        rpc.setInvokeClass(invokeClass);
        rpc.setMonitorPath(monitorPath);
        return rpc;
    }

    public void start() {
        registerDefault(RpcInfo.CMD_JOIN, "io.nuls.rpc.cmd.JoinCmd");
        registerDefault(RpcInfo.CMD_LIST, "io.nuls.rpc.cmd.RpcListCmd");
        registerDefault(RpcInfo.CMD_HEARTBEAT, "io.nuls.rpc.cmd.HeartbeatCmd");

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
