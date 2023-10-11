/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.network.manager.handler.message;

import io.nuls.base.RPCUtil;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdPriority;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.RpcCacheMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.network.utils.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/01/18
 **/
public class OtherModuleMessageHandler extends BaseMessageHandler {

    private static OtherModuleMessageHandler instance = new OtherModuleMessageHandler();

    private OtherModuleMessageHandler() {

    }

    public static OtherModuleMessageHandler getInstance() {
        return instance;
    }

    /**
     * 接收消息处理
     * Receive message processing
     *
     * @param message address message
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, Node node) {
        return NetworkEventResult.getResultSuccess();
    }

    /**
     * @param header
     * @param payLoadBody
     * @param node
     * @return
     */
    public NetworkEventResult recieve(MessageHeader header, byte[] payLoadBody, Node node) {
        long magicNum = header.getMagicNumber();
        int chainId = NodeGroupManager.getInstance().getChainIdByMagicNum(magicNum);
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("chainId", chainId);
        paramMap.put("nodeId", node.getId());
        String cmd = header.getCommandStr();
        paramMap.put("cmd", cmd);
        String messageBody = RPCUtil.encode(payLoadBody);
        paramMap.put("messageBody", messageBody);
        Map<String, CmdPriority> protocolRoles = MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap(cmd);
        if (protocolRoles == null || protocolRoles.isEmpty()) {
            LoggerUtil.logger(chainId).error("unknown mssages. cmd={},handler may be unRegistered to network.", cmd);
            return NetworkEventResult.getResultSuccess();
        }
        for (Map.Entry<String,CmdPriority> entry : protocolRoles.entrySet()) {
            try {
                Request request = MessageUtil.newRequest(BaseConstant.MSG_PROCESS, paramMap, Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
                if ("0".equals(ResponseMessageProcessor.requestOnly(entry.getKey(), request))) {
                    if (nodeGroup.getCacheMsgQueue().size() > NetworkConstant.MAX_CACHE_MSG_QUEUE) {
                        LoggerUtil.COMMON_LOG.error("chainId = {},cmd={},CacheMsgQueue size={}.RPC fail,drop msg", chainId, cmd, nodeGroup.getCacheMsgQueue().size());
                    } else {
                        LoggerUtil.COMMON_LOG.error("chainId = {},cmd={},RPC fail,add to cache", chainId, cmd);
                        RpcCacheMessage peerMessage = new RpcCacheMessage(node.getId(), cmd, messageBody);
                        nodeGroup.getCacheMsgQueue().addLast(peerMessage);
                    }
                }
            } catch (Exception e) {
                LoggerUtil.logger(chainId).error("{}", e.getMessage());
            }
        }
//        MessageTestUtil.recievedMessage(cmd);
        return NetworkEventResult.getResultSuccess();
    }

}
