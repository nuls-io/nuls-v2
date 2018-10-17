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

package io.nuls.test;

import io.nuls.rpc.RpcClient;
import io.nuls.rpc.RpcServer;
import io.nuls.rpc.cmd.VersionCmd1;
import io.nuls.rpc.info.RpcInfo;
import org.junit.Test;

/**
 * @author tangyi
 * @date 2018/10/15
 * @description
 */
public class TestJoinCmd {

    @Test
    public void test() {

        RpcServer rpcServer = RpcServer.getInstance(8092);

        assert rpcServer != null;

        rpcServer.register("cmd1", 1, null);
        rpcServer.register("cmd2", 2, null);
        rpcServer.register("cmd3", 3, null);
        rpcServer.register("cmd4", 4, null);
        rpcServer.register("cmd5", 5, null);
        rpcServer.register("version", 1, VersionCmd1.class);
        rpcServer.start();

        RpcInfo.print();

        System.out.println("启动Client");
        RpcClient rpcClient = new RpcClient("192.168.1.65", 8091);
        String response = rpcClient.callRpc(RpcInfo.DEFAULT_PATH, RpcInfo.CMD_JOIN, RpcInfo.localInterfaceMap);
        System.out.println(response);

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
