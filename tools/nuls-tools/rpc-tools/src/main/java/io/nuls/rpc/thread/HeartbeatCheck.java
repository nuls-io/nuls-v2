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
            try {
                List<String> removeKeyList = new ArrayList<>();

                for (String key : RpcInfo.remoteInterfaceMap.keySet()) {
                    Rpc rpc = RpcInfo.remoteInterfaceMap.get(key);
                    RpcClient rpcClient = new RpcClient(rpc.getUri());
                    try {
                        String response = rpcClient.callRpc(rpc.getMonitorPath(), RpcInfo.CMD_HEARTBEAT, RpcInfo.HEARTBEAT_REQUEST);
                        if (RpcInfo.HEARTBEAT_RESPONSE.equals(response)) {
                            // 心跳成功，更新时间
                            System.out.println("心跳成功: " + rpc.getUri());
                            RpcInfo.heartbeatMap.put(rpc.getUri(), System.currentTimeMillis());
                        }
                    } catch (Exception e) {
                        // 心跳失败，不更新时间
                        System.out.println("心跳失败：" + e.getMessage());
                    }

                    if (System.currentTimeMillis() - RpcInfo.heartbeatMap.get(rpc.getUri()) > RpcInfo.HEARTBEAT_OVERTIME_MILLIS) {
                        // 心跳超时，判定为不可连接
                        System.out.println("心跳超时：" + rpc.getUri() + "," + rpc.getMonitorPath() + "," + rpc.getInvokeClass());
                        removeKeyList.add(key);
                    }
                }

                for (String key : removeKeyList) {
                    RpcInfo.remoteInterfaceMap.remove(key);
                }

                RpcInfo.print();

                Thread.sleep(RpcInfo.HEARTBEAT_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
