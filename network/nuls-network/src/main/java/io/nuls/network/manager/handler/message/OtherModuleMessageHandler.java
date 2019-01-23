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

import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.message.base.MessageHeader;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;

import java.util.Collection;
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
     *
     * 接收消息处理
     * Receive message processing
     * @param message   address message
     * @param nodeKey      peer node key
     * @param isServer client=false or server=true
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, String nodeKey, boolean isServer) {
        return   NetworkEventResult.getResultSuccess();
    }

    /**
     *
     * @param header
     * @param payLoadBody
     * @param isServer
     * @return
     */
    public NetworkEventResult recieve(MessageHeader header,byte[] payLoadBody,String nodeKey, boolean isServer) {
        long magicNum = header.getMagicNumber();
        int chainId = NodeGroupManager.getInstance().getChainIdByMagicNum(magicNum);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("chainId", chainId);
        paramMap.put("nodeId", nodeKey);
        paramMap.put("messageBody",HexUtil.byteToHex(payLoadBody));
        Collection<ProtocolRoleHandler> protocolRoleHandlers = MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap( header.getCommandStr());
        if (null == protocolRoleHandlers) {
            Log.error("unknown mssages. cmd={},may be handle had not be registered to network.", header.getCommandStr());
        } else {
            Log.debug("==============================other module message protocolRoleHandlers-size:{}", protocolRoleHandlers.size());
            for (ProtocolRoleHandler protocolRoleHandler : protocolRoleHandlers) {
                try {
                    Log.debug("request：{}=={}", protocolRoleHandler.getRole(), protocolRoleHandler.getHandler());
                    Response response = CmdDispatcher.requestAndResponse(protocolRoleHandler.getRole(), protocolRoleHandler.getHandler(), paramMap);
                    Log.debug("response：" + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return   NetworkEventResult.getResultSuccess();
    }

}
