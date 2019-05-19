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
package io.nuls.network.rpc;

import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.message.VersionMessage;
import io.nuls.network.model.message.body.VersionMessageBody;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @description test
 * @author  lan
 * @date  2018/11/21
 **/
public class MessageRpcTest {
    @Before
    public void before() throws Exception {
        NoUse.mockModule();
//        CmdDispatcher.syncKernel("ws://127.0.0.1:7771");
    }
    private void addNodeGroup(){
        NodeGroup nodeGroup = new NodeGroup(778899,1000,32,55,43);
        NodeGroupManager.getInstance().addNodeGroup(1000,nodeGroup);
    }

    @Test
    public void broadcast() {
        try {
//            addNodeGroup();
            Map <String,Object>params = new HashMap<>();
            int chainId = 2;
            String excludeNodes = "20.30.1020:5599,26.35.52.64:6688";
            VersionMessageBody versionMessageBody = new VersionMessageBody();
            InetAddress inetAddrYou = InetAddress.getByName("192.168.2.3");
            IpAddress addrYou = new IpAddress(inetAddrYou, 8282);
            versionMessageBody.setAddrYou(addrYou);
            IpAddress addrMe= new IpAddress("",8008);
            versionMessageBody.setAddrMe(addrMe);
            VersionMessage versionMessage = new VersionMessage(0, NetworkConstant.CMD_MESSAGE_VERSION, versionMessageBody);
            versionMessage.getHeader().setPayloadLength(versionMessageBody.size());
//            versionMessage.getHeader().setChecksum(  versionMessage.getHeader().);
            params.put("chainId",chainId);
            params.put("excludeNodes",excludeNodes);
            params.put("messageBody",RPCUtil.encode(versionMessageBody.serialize()));
            params.put("command","block");
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_broadcast", params);
            LoggerUtil.logger().info("response {}", response);

        }catch (Exception e){
            LoggerUtil.logger().error("", e);
        }
    }
}

