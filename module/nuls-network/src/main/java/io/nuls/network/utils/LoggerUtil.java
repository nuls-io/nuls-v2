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
import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.Node;
import io.nuls.network.model.dto.ProtocolRoleHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2018/12/17
 **/
public class LoggerUtil {
    private static final String LOGGER_KEY1 = "nw";
    private static final String LOGGER_KEY2 = ModuleE.BL.abbr;
    private static final String LOGGER_KEY3 = ModuleE.TX.abbr;
    private static final String LOGGER_KEY4 = ModuleE.CS.abbr;
    private static final String LOGGER_KEY5 = ModuleE.CC.abbr;
    private static final String LOGGER_KEY100 = "nwInfos";
    private static Map<String, NulsLogger> logMap = new HashMap<>();
    private static NulsLogger logger = null;
    public static String logLevel = "DEBUG";

    public static void defaultLogInit() {
        logger = LoggerBuilder.getLogger("nw", Level.valueOf(logLevel));
    }

    public static void createLogs(int chainId, String logLevel) {
        String folderName = "chain-" + chainId + "/network";
        if (null == logMap.get(LOGGER_KEY1 + chainId)) {
            logMap.put(LOGGER_KEY1 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY1, Level.valueOf(logLevel)));
            logMap.put(LOGGER_KEY2 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY2, Level.valueOf(logLevel)));
            logMap.put(LOGGER_KEY3 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY3, Level.valueOf(logLevel)));
            logMap.put(LOGGER_KEY4 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY4, Level.valueOf(logLevel)));
            logMap.put(LOGGER_KEY5 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY5, Level.valueOf(logLevel)));
            logMap.put(LOGGER_KEY100 + chainId, LoggerBuilder.getLogger(folderName, LOGGER_KEY100, Level.valueOf(logLevel)));
        }
    }

    public static NulsLogger logger(int chainId) {
        if (null == logMap.get(LOGGER_KEY1 + chainId)) {
            return logger;
        }
        return logMap.get(LOGGER_KEY1 + chainId);
    }

    public static NulsLogger nwInfosLogger(int chainId) {
        if (null == logMap.get(LOGGER_KEY100 + chainId)) {
            return logger;
        }
        return logMap.get(LOGGER_KEY100 + chainId);
    }

    public static NulsLogger logger() {
        if (null == logger) {
            defaultLogInit();
        }
        return logger;
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
