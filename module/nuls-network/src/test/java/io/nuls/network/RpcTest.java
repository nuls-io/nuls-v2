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

import org.junit.Test;

/**
 * @program: nuls2.0
 * @description: rpc test
 * @author: lan
 * @create: 2018/11/09
 **/
public class RpcTest {
    @Test
    public void getCrossSeeds(){

    }

    @Test
    public void broadcast(){
        try{
//            VersionMessageBody versionMessageBody=new VersionMessageBody();
//            InetAddress inetAddrYou=InetAddress.getByName("192.168.2.3");
//            IpAddress addrYou=new IpAddress(inetAddrYou,8282);
//            versionMessageBody.setAddrYou(addrYou);
//            versionMessageBody.setPortYouCross(8686);
//            IpAddress addrMe=LocalInfoManager.getInstance().getExternalAddress();
//            versionMessageBody.setAddrMe(addrMe);
//            versionMessageBody.setPortMeCross(NetworkParam.getInstance().getCrossPort());
//            VersionMessage versionMessage=new VersionMessage(5000,NetworkConstant.CMD_MESSAGE_VERSION,versionMessageBody);
//            CmdDispatcher.syncKernel(TestConstant.KernelWSServer);
//            String response = CmdDispatcher.call("nw_broadcast", new Object[]{100,"10.13.25.36:5003,20.30.25.65:8009",HexUtil.byteToHex(versionMessage.serialize())},1.0 );
//            System.out.println(response);
        }  catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Test
    public void  nwGetNodes(){
        try {
//            CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
//            String response = CmdDispatcher.call("nw_getNodes", new Object[]{9861, 1, 0, 0, 0}, 1.0);
//            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
