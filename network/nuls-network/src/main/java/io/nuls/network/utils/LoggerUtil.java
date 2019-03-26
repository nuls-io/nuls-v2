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
package io.nuls.network.utils;

import ch.qos.logback.classic.Level;
import io.nuls.base.data.NulsDigestData;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.Node;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2018/12/17
 **/
public class LoggerUtil {
    public static final String LOGGER_KEY1 = "nw";
    public static final String LOGGER_KEY2 = ModuleE.BL.abbr;
    public static final String LOGGER_KEY3 = ModuleE.TX.abbr;
    public static final String LOGGER_KEY4 = ModuleE.CS.abbr;
    public static NulsLogger Log = LoggerBuilder.getLogger("nwLogs", "nw", Level.ALL);
    public static NulsLogger NwInfosLog = LoggerBuilder.getLogger("nwLogs", "nwInfos", Level.ALL);
    public static Map<String, NulsLogger> logMap = new HashMap<>();

    public static void createLogs(int chainId) {
        String folderName = "nwLogs/" + chainId;
        if (null == logMap.get(LOGGER_KEY1 + chainId)) {
            logMap.put(LOGGER_KEY1 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY1, Level.ALL));
            logMap.put(LOGGER_KEY2 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY2, Level.ALL));
            logMap.put(LOGGER_KEY3 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY3, Level.ALL));
            logMap.put(LOGGER_KEY4 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY4, Level.ALL));
        }
    }

    public static NulsLogger logger(int chainId) {
        if (null == logMap.get(LOGGER_KEY1 + chainId)) {
            return Log;
        }
        return logMap.get(LOGGER_KEY1 + chainId);
    }

    /**
     * 调试代码
     *
     * @param cmd
     * @param node
     * @param payLoadBody
     * @param sendOrRecieved
     */
    public static void modulesMsgLogs(String cmd, Node node, byte[] payLoadBody, String sendOrRecieved) {
        Collection<ProtocolRoleHandler> protocolRoleHandlers = MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap(cmd);
        int chainId = node.getNodeGroup().getChainId();
        if (null == protocolRoleHandlers) {
            logger(chainId).error("unknown mssages. cmd={},may be handle had not be registered to network.", cmd);
        } else {
            for (ProtocolRoleHandler protocolRoleHandler : protocolRoleHandlers) {
                if (null != logMap.get(protocolRoleHandler.getRole() + chainId)) {
                    logMap.get(protocolRoleHandler.getRole() + chainId).debug("net {} cmd={},peer={},hash={}", sendOrRecieved, cmd, node.getId(), NulsDigestData.calcDigestData(payLoadBody).getDigestHex());
                } else {
                    logger(chainId).debug("net {} cmd={},peer={},hash={}", sendOrRecieved, cmd, node.getId(), NulsDigestData.calcDigestData(payLoadBody).getDigestHex());
                }
            }
        }
    }

    /**
     * 调试代码
     *
     * @param role
     * @param cmd
     * @param node
     * @param payLoadBody
     * @param result
     */
    public static void modulesMsgLogs(String role, String cmd, Node node, byte[] payLoadBody, String result) {
        int chainId = node.getNodeGroup().getChainId();
        if (null != logMap.get(role + chainId)) {
            logMap.get(role + chainId).debug("cmd={},peer={},hash={},rpcResult={}", cmd, node.getId(), NulsDigestData.calcDigestData(payLoadBody).getDigestHex(), result);
        } else {
            logger(chainId).debug("cmd={},peer={},hash={},rpcResult={}", cmd, node.getId(), NulsDigestData.calcDigestData(payLoadBody).getDigestHex(), result);
        }
    }
}
