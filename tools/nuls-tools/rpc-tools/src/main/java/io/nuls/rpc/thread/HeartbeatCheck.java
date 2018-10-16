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

package io.nuls.rpc.thread;


import io.nuls.rpc.RpcClient;
import io.nuls.rpc.RpcInfo;
import io.nuls.rpc.pojo.Rpc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/15
 * @description
 */
public class HeartbeatCheck implements Runnable {

    @Override
    public void run() {
        while (true) {
            System.out.println("start heartbeat :");
            List<String> removeKeyList = new ArrayList<>();
            for (String key : RpcInfo.remoteInterfaceMap.keySet()) {
                Rpc rpc = RpcInfo.remoteInterfaceMap.get(key);
                System.out.println("uri:" + rpc.getUri());
                System.out.println("path:" + rpc.getMonitorPath());
                RpcClient rpcClient = new RpcClient(rpc.getUri());
                String response = rpcClient.callRpc(rpc.getMonitorPath(), RpcInfo.CMD_HEARTBEAT, 1, RpcInfo.HEARTBEATREQUEST);
                if (RpcInfo.HEARTBEATRESPONSE.equals(response)) {
                    RpcInfo.heartbeatMap.put(rpc.getUri(), System.currentTimeMillis());
                    System.out.println("心跳成功：" + key + "," + rpc.getUri());
                } else if (System.currentTimeMillis() - RpcInfo.heartbeatMap.get(rpc.getUri()) > RpcInfo.HEARTBEATTIMEMILLIS) {
                    // 判定为不可连接
                    removeKeyList.add(key);
                }
            }

            for (String key : removeKeyList) {
                RpcInfo.remoteInterfaceMap.remove(key);
            }

            RpcInfo.print();

            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
