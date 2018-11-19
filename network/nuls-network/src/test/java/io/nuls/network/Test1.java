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
package io.nuls.network;

import io.nuls.rpc.server.WsServer;
import io.nuls.tools.data.ByteUtils;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Test1 {


    @Test
    public void test1(){

        String ip="ssf_dsfANfsf";
        System.out.println(ip.getBytes().length);
        try {
            InetAddress addr=InetAddress.getByAddress(new byte[]{1,15,1,32});
            System.out.println(addr.getHostAddress());// 这个快

            InetAddress addr2=InetAddress.getByName( "AD80:0000:0000:0000:ABAA:0000:00C2:0002");
            System.out.println(addr2.getHostAddress());// 这个快

            InetAddress addr3=InetAddress.getByName( "0.0.0.0");
            System.out.println(addr3.getHostAddress());// 这个快

//            System.out.println(addr.getHostName());// 这个慢
//            System.out.println(new String(addr.getAddress()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
    @Test
    public void test2(){
       byte a[]={-114,32,56,-113};
       System.out.println(ByteUtils.byteToLong(a));

    }
    @Test
    public void test3(){
        int port = 8887;
        WsServer s = new WsServer(port);
// 注意，下面这句话不要改，模拟实现在"io.nuls.rpc.cmd.kernel"中
        try {
            s.init("kernel", null, "io.nuls.rpc.cmd.kernel");
            s.startAndSyncKernel("ws://127.0.0.1:8887");

            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
