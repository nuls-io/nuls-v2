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

import io.nuls.rpc.pojo.Rpc;
import io.nuls.rpc.pojo.RpcCmd;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RpcInfo {

    /**
     * key format: cmd-version
     * value format: RpcInterface, include uri/handler
     */
    public static ConcurrentHashMap<String, Rpc> defaultInterfaceMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Rpc> localInterfaceMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Rpc> remoteInterfaceMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Long> heartbeatMap = new ConcurrentHashMap<>();

    /**
     * Default Value
     */
    public static final String CMD_JOIN = "join";
    public static final String CMD_LIST = "list";
    public static final String CMD_HEARTBEAT = "heartbeat";

    public static final String DEFAULT_PATH = "nulsrpc";

    public static final int VERSION = 1;

    public static final long HEARTBEAT_INTERVAL_MILLIS = 10 * 1000;
    public static final long HEARTBEAT_OVERTIME_MILLIS = 60 * 1000;
    public static final String HEARTBEAT_REQUEST = "hello nuls?";
    public static final String HEARTBEAT_RESPONSE = "i'm everything!";


    public static void print() {
        System.out.println("接口详细信息如下：");
        System.out.println("系统默认的：" + defaultInterfaceMap.size());
        System.out.println("自己提供的：" + localInterfaceMap.size());
        System.out.println("远程获取的：" + remoteInterfaceMap.size());
    }

    /**
     * 根据网卡获得IP地址
     *
     * @return ip
     */
    public static String getIpAdd() throws SocketException {
        String ip = "";
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            String name = intf.getName();
            if (!name.contains("docker") && !name.contains("lo")) {
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    //获得IP
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddress = inetAddress.getHostAddress();
                        if (!ipAddress.contains("::") && !ipAddress.contains("0:0:") && !ipAddress.contains("fe80")) {
                            if (!"127.0.0.1".equals(ip) && ipAddress.length() <= 16) {
                                ip = ipAddress;
                            }
                        }
                    }
                }
            }
        }
        return ip;
    }

    /**
     * @param rpcCmd rpcCmd包含version和lowestVersion
     *               如果lowestVersion>0, 则不考虑version，调用不低于lowestVersion的最新版
     *               如果lowestVersion<=0，则使用version，精确匹配
     *               如果lowestVersion和version同时<=0，则调用最新版
     *               以上都找不到，则调用失败
     * @return String
     */
    public static Rpc getInvokeRpcByCmd(RpcCmd rpcCmd) {
        if (rpcCmd.getLowestVersion() > 0) {
            return fuzzyMatchRpc(rpcCmd.getCmd(), rpcCmd.getLowestVersion());
        } else if (rpcCmd.getVersion() > 0) {
            return exactMatchRpc(rpcCmd.getCmd(), rpcCmd.getVersion());
        } else {
            return fuzzyMatchRpc(rpcCmd.getCmd(), 0);
        }
    }

    private static Rpc exactMatchRpc(String cmd, int version) {
        String key = generateKey(cmd, version);
        if (RpcInfo.defaultInterfaceMap.containsKey(key)) {
            return RpcInfo.defaultInterfaceMap.get(key);
        } else {
            return RpcInfo.localInterfaceMap.getOrDefault(key, null);
        }
    }

    private static Rpc fuzzyMatchRpc(String cmd, int version) {
        Rpc rpc = null;
        while (true) {
            String key = generateKey(cmd, version);
            Rpc tempRpc = null;
            if (RpcInfo.defaultInterfaceMap.containsKey(key)) {
                tempRpc = RpcInfo.defaultInterfaceMap.get(key);
            } else if (RpcInfo.localInterfaceMap.containsKey(key)) {
                tempRpc = RpcInfo.localInterfaceMap.get(key);
            }

            if (tempRpc == null) {
                return rpc;
            } else {
                rpc = tempRpc;
            }

            version++;
        }
    }

    public static String generateKey(String cmd, int version) {
        return cmd + "-" + version;
    }
}
