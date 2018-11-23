/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.block.utils;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.message.BaseMessage;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.message.body.CompleteMessageBody;
import io.nuls.block.model.Node;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.tools.log.Log;

import java.util.List;

/**
 * 调用网络模块接口的工具
 * @author captain
 * @date 18-11-9 下午3:48
 * @version 1.0
 */
public class NetworkUtil {

    static {
        /*
         * 从kernel获取所有接口列表（实际使用中不需要每次都调用这句话，同步一次即可）
         */
        try {
            CmdDispatcher.syncKernel();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    /**
     * 获取可用节点
     * @date 18-11-9 下午3:49
     * @param
     * @return
     */
    public static List<Node> getAvailableNodes(int chainId) throws Exception {
        /*
         * 参数说明：
         * 1. 调用的命令
         * 2. 调用的命令的最低版本号
         * 3. 调用的命令所需要的参数
         * 返回值为json格式
         */
        String response = CmdDispatcher.request("nw_getNodes", null);
//        List<Node> nodeList = JSONUtils.json2list(response, Node.class);
//        return nodeList;
        return null;
    }

    /**
     * 重置网络节点
     * @date 18-11-9 下午3:49
     * @param
     * @return
     */
    public static void resetNetwork(int chainId){

    }

    /**
     * 给指定节点发送消息
     * @param chainId
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean sendToNode(int chainId, BaseMessage message, String nodeId){
        try {
            CmdDispatcher.request("nw_sendPeersMsg", null);
//            CmdDispatcher.request("", new Object[]{chainId, nodeId, HexUtil.byteToHex(message.serialize())}, 1.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 给网络上节点广播消息
     * @param chainId
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message, String nodeId){
        try {
            CmdDispatcher.request("nw_broadcast", null);
//            CmdDispatcher.request("nw_broadcast", new Object[]{chainId, nodeId, HexUtil.byteToHex(message.serialize())}, 1.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void sendFail(int chainId, NulsDigestData hash, String nodeId) {
        CompleteMessage message = new CompleteMessage();
        CompleteMessageBody body = new CompleteMessageBody(chainId, hash, false);
        message.setMsgBody(body);
        boolean result = sendToNode(chainId, message, nodeId);
        if (!result) {
            Log.warn("send fail message failed:{}, hash:{}", nodeId, hash);
        }
    }

    public static void sendSuccess(int chainId, NulsDigestData hash, String nodeId) {
        CompleteMessage message = new CompleteMessage();
        CompleteMessageBody body = new CompleteMessageBody(chainId, hash, true);
        message.setMsgBody(body);
        boolean result = sendToNode(chainId, message, nodeId);
        if (!result) {
            Log.warn("send success message failed:{}, hash:{}", nodeId, hash);
        }
    }

}
