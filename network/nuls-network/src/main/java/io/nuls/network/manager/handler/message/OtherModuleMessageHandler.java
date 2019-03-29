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
package io.nuls.network.manager.handler.message;

import io.nuls.base.data.NulsDigestData;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.rpc.netty.processor.container.ResponseContainer;
import io.nuls.tools.crypto.HexUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

;

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
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("chainId", chainId);
        paramMap.put("nodeId", node.getId());
        paramMap.put("messageBody", HexUtil.byteToHex(payLoadBody));
        long beginTime = System.currentTimeMillis();
        Collection<ProtocolRoleHandler> protocolRoleHandlers = MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap(header.getCommandStr());
        if (null == protocolRoleHandlers) {
            LoggerUtil.logger(chainId).error("unknown mssages. cmd={},handler may be unRegistered to network.", header.getCommandStr());
        } else {
            LoggerUtil.logger(chainId).debug("==============================other module message protocolRoleHandlers-size:{}", protocolRoleHandlers.size());
            long time2 = System.currentTimeMillis();
            long time3 = 0;
            long time4 = 0;
            for (ProtocolRoleHandler protocolRoleHandler : protocolRoleHandlers) {
                try {
                    time3 = System.currentTimeMillis();
                    LoggerUtil.logger(chainId).debug("request：{}=={}", protocolRoleHandler.getRole(), protocolRoleHandler.getHandler());
                    Request request = MessageUtil.newRequest(protocolRoleHandler.getHandler(), paramMap, Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
                    ResponseContainer responseContainer = ResponseMessageProcessor.sendRequest(protocolRoleHandler.getRole(), request);
                    LoggerUtil.logger(chainId).debug("responseContainer：" + responseContainer.getMessageId());
                    LoggerUtil.modulesMsgLogs(protocolRoleHandler.getRole(), header.getCommandStr(), node, payLoadBody, responseContainer.getMessageId());
                    time4 = System.currentTimeMillis();
                } catch (Exception e) {
                    LoggerUtil.logger(chainId).error(e);
                    e.printStackTrace();
                }
            }
            long endTime = System.currentTimeMillis();
            if (endTime - beginTime > 3000) {
                LoggerUtil.TestLog.error("####2-Deal time too long,message cmd ={},useTime={},hash={}", header.getCommandStr(), (endTime - beginTime), NulsDigestData.calcDigestData(payLoadBody).getDigestHex());
                LoggerUtil.TestLog.error("####2-time begin ={},time2={},time3={},time4={},end={}", beginTime, time2, time3, time4, endTime);

            }
        }
        return NetworkEventResult.getResultSuccess();
    }

}
