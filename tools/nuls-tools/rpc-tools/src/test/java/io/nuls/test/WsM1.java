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

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.invoke.test.EventCounterInvoke;
import io.nuls.rpc.invoke.test.MyInvoke;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class WsM1 {
    @Test
    public void test() {
        String range = "[11,102]";
//        System.out.println(range.substring(range.indexOf("(")+1,range.indexOf(",")));
//        System.out.println(range.substring(range.indexOf(",")+1,range.indexOf("]")));
        String regex = "[(\\[]\\d+,\\d+[)\\]]";
        System.out.println(range.matches(regex));

        BigDecimal bigDecimal = new BigDecimal("1000").divide(new BigDecimal("3"), 5, RoundingMode.HALF_DOWN);
        System.out.println(bigDecimal.toString());
        System.out.println(bigDecimal.toBigInteger().intValue());
        System.out.println(new BigInteger("10").divide(new BigInteger("4")));

        List<Integer> list = new CopyOnWriteArrayList<>();
        new Thread(() -> {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                list.add(i);
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                list.remove(0);
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            System.out.println("求和线程启动");
            while (true) {
                int total = 0;
//                synchronized (list) {
                    for (int i : list) {
                        total += i;
                    }
//                }
                System.out.println("和为：" + total);
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                .moduleRoles(new String[]{"1.0", "2.4"})
                .moduleVersion("1.2")
                .dependencies(ModuleE.LG.abbr, "1.1")
                .dependencies(ModuleE.BL.abbr, "2.1")
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
        NoUse.mockModule();
        /*
        单元测试专用结束
         */


        // Build params map
        Map<String, Object> params = new HashMap<>();
        // Version information ("1.1" or 1.1 is both available)
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("aaa", "100");

        // Call cmd, get response immediately
        Object object = CmdDispatcher.requestAndResponse(ModuleE.CM.abbr, "getHeight", params);
        System.out.println("requestAndResponse:" + JSONUtils.obj2json(object));

        String messageId1 = CmdDispatcher.requestAndInvokeWithAck
                (ModuleE.CM.abbr, "getBalance", params, "1", "0", new MyInvoke());

        // Call cmd, auto invoke local method after response
        String messageId = CmdDispatcher.requestAndInvoke(ModuleE.CM.abbr, "getBalance", params, "0", "3", new EventCounterInvoke());
        String messageId2 = CmdDispatcher.requestAndInvoke(ModuleE.CM.abbr, "getBalance", params, "0", "5", new EventCounterInvoke());
        System.out.println("接下来会不停打印GetBalance，");
        Thread.sleep(20000);

        // Unsubscribe
        System.out.println("接下来会5秒进入一次EventCount");
        CmdDispatcher.sendUnsubscribe(messageId);

        Thread.sleep(20000);
        //CmdDispatcher.sendUnsubscribe(messageId1);

        System.out.println("我开始一次调用多个方法");
        Request request = MessageUtil.defaultRequest();
        request.setRequestAck("1");
        request.setSubscriptionPeriod("3");
        request.setSubscriptionEventCounter("2");
        request.getRequestMethods().put("getHeight", params);
        request.getRequestMethods().put("getBalance", params);
        String messageId3 = CmdDispatcher.requestAndInvoke(ModuleE.CM.abbr, request, new MyInvoke());
        Thread.sleep(7000);
        CmdDispatcher.sendUnsubscribe(messageId3);
        System.out.println("我已经取消了订阅:" + messageId3);

        Thread.sleep(5000);
        System.out.println("最后队列的数量：");
        System.out.println("RESPONSE_MANUAL_QUEUE：" + ClientRuntime.RESPONSE_MANUAL_QUEUE.size());
        System.out.println("RESPONSE_AUTO_QUEUE：" + ClientRuntime.RESPONSE_AUTO_QUEUE.size());
        System.out.println("NEGOTIATE_RESPONSE_QUEUE：" + ClientRuntime.NEGOTIATE_RESPONSE_QUEUE.size());
        System.out.println("ACK_QUEUE：" + ClientRuntime.ACK_QUEUE.size());

    }
}
