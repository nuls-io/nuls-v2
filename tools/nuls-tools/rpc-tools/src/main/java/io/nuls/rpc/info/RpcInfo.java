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

package io.nuls.rpc.info;

import io.nuls.rpc.model.Module;
import io.nuls.rpc.model.Rpc;
import io.nuls.rpc.model.RpcCmd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RpcInfo {

    public static Module local;
    public static ConcurrentMap<String, Module> remoteModuleMap = new ConcurrentHashMap<>();

    public static void printLocalRpc() {
        System.out.println("打印localRpcList");
        for (Rpc rpc : local.getRpcList()) {
            System.out.println(rpc);
        }
    }

    public static List<String> getRemoteUri(RpcCmd rpcCmd) {
        List<String> remoteUriList = new ArrayList<>();
        for (Module module : remoteModuleMap.values()) {
            for (Rpc rpc : module.getRpcList()) {
                if (rpc.getCmd().equals(rpcCmd.getCmd())) {
                    remoteUriList.add("http://" + module.getAddr() + ":" + module.getPort());
                    break;
                }
            }
        }
        return remoteUriList;
    }

    public static Rpc getLocalInvokeRpc(String cmd, double minVersion) {

        local.getRpcList().sort(Comparator.comparingDouble(Rpc::getVersion));

        Rpc findRpc = null;
        for (Rpc rpc : local.getRpcList()) {
            if (rpc.getCmd().equals(cmd) && rpc.getVersion() >= minVersion) {
                if (findRpc == null) {
                    findRpc = rpc;
                } else if (rpc.getVersion() > findRpc.getVersion()) {
                    if (rpc.isPreCompatible()) {
                        findRpc = rpc;
                    } else {
                        break;
                    }
                }
            }
        }
        return findRpc;
    }

    public static void registerRpc(Rpc registerRpc) throws Exception {
        if (isRegister(registerRpc)) {
            throw new Exception("Duplicate cmd found: " + registerRpc.getCmd() + "-" + registerRpc.getVersion());
        } else {
            local.getRpcList().add(registerRpc);
        }
    }

    private static boolean isRegister(Rpc sourceRpc) {
        boolean exist = false;
        for (Rpc rpc : local.getRpcList()) {
            if (rpc.getCmd().equals(sourceRpc.getCmd()) && rpc.getVersion() == sourceRpc.getVersion()) {
                exist = true;
                break;
            }
        }

        return exist;
    }
}
