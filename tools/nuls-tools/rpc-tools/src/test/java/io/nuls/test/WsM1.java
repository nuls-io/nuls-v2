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

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Ack;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class WsM1 {
    @Test
    public void test() throws Exception {
        System.out.println((int) 3.1415f);
        System.out.println((int) 3.99415d);
        System.out.println(new Ack().toString());
        Ack ack = new Ack("asdfasdf");
        System.out.println(ack.getRequestID());
        System.out.println(Integer.MAX_VALUE / 1000f / 3600f / 24f);
    }

    @Test
    public void handshake() throws Exception {
        Constants.kernelUrl = "ws://127.0.0.1:8887";
        System.out.println("handshake:" + CmdDispatcher.handshakeKernel());
    }

    @Test
    public void startServer() throws Exception {
        // Start server instance
        WsServer.getInstance(ModuleE.CM)
                .supportedAPIVersions(new String[]{"1.1","1.2"})
                .moduleRoles("Role_Chain", new String[]{"1.1","1.2"})
                .moduleVersion("1.2")
                .dependencies("Role_Ledger", "1.1")
                .scanPackage("io.nuls.rpc.cmd.test")
                .connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();

        Thread.sleep(Integer.MAX_VALUE);
    }

    @Test
    public void testHeight() throws Exception {
        /*
        单元测试专用：单元测试时需要告知内核地址，以及同步接口列表
        如果不是单元测试，在模块中进行连调测试，下面两句话是不需要的
         */
        // Set kernel url
        Constants.kernelUrl = "ws://127.0.0.1:8887";

        // Get information from kernel
        CmdDispatcher.syncKernel();
        /*
        单元测试专用结束
         */


        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("paramName", "value");

        // Call cmd
        String messageId = CmdDispatcher.request("getHeight", params, "5");

        for (int i = 0; i < 5; i++) {
            System.out.println(JSONUtils.obj2json(CmdDispatcher.callMessageResponse(messageId)));
            Thread.sleep(5000);
        }

        CmdDispatcher.unsubscribe(messageId);
        System.out.println("我已经取消了订阅");

        Thread.sleep(3000);
        // Call cmd
        System.out.println(CmdDispatcher.requestAndResponse("getHeight", params));
        //Thread.sleep(Integer.MAX_VALUE);
    }
}
