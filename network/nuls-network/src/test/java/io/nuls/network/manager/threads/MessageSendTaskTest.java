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
package io.nuls.network.manager.threads;

import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.message.body.TimeMessageBody;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 连接定时器，主要任务：
 *  1>询问种子节点要更多的地址
 *  2>补充未饱和的网络连接
 *  3>清除无效的网络连接
 * Connection timer, main tasks:
 * 1> Ask the seed node for more addresses
 * 2>Supply an unsaturated network connection
 * 3> Clear invalid network connections
 * ues add peer connect
 * @author lan
 * @date 2018/11/01
 *
 */
public class MessageSendTaskTest implements Runnable  {
    private static long id = 0;
    private void connectPeer(NodeGroup nodeGroup){

    }
    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", 1);
        params.put("nodes", "192.168.0.116:8008");
        TimeMessageBody messageBody =  new TimeMessageBody();
        messageBody.setMessageId(id++);
        try {
            params.put("messageBody", HexUtil.encode(messageBody.serialize()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        params.put("command","test");
        Response response = null;
        try {
            response = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_sendPeersMsg", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoggerUtil.Log.info("response {}", response);
      }
 }

