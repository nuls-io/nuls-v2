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
    public static NulsLogger Log = LoggerBuilder.getLogger("./nwLogs", "nw.log", Level.ALL);
    public static NulsLogger blockMsLog = LoggerBuilder.getLogger("./nwLogs", "block.log", Level.ALL);
    public static NulsLogger txMsLog = LoggerBuilder.getLogger("./nwLogs", "tx.log", Level.ALL);
    public static NulsLogger csMsLog = LoggerBuilder.getLogger("./nwLogs", "cs.log", Level.ALL);
    public static Map<String, NulsLogger> logMap = new HashMap<>();

    static {
        logMap.put(ModuleE.BL.abbr, blockMsLog);
        logMap.put(ModuleE.TX.abbr, txMsLog);
        logMap.put(ModuleE.CS.abbr, csMsLog);
    }

    /**
     *  调试代码
     * @param cmd
     * @param node
     * @param payLoadBody
     * @param sendOrRecieved
     */
    public static void modulesMsgLogs(String cmd, Node node, byte[] payLoadBody, String sendOrRecieved) {
        Collection<ProtocolRoleHandler> protocolRoleHandlers = MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap(cmd);
        if (null == protocolRoleHandlers) {
            Log.error("unknown mssages. cmd={},may be handle had not be registered to network.", cmd);
        } else {
            for (ProtocolRoleHandler protocolRoleHandler : protocolRoleHandlers) {
                if (null != logMap.get(protocolRoleHandler.getRole())) {
                    logMap.get(protocolRoleHandler.getRole()).debug("net {} cmd={},peer={},hash={}", sendOrRecieved, cmd, node.getId(), NulsDigestData.calcDigestData(payLoadBody).getDigestHex());
                } else {
                    Log.debug("net {} cmd={},peer={},hash={}", sendOrRecieved, cmd, node.getId(), NulsDigestData.calcDigestData(payLoadBody).getDigestHex());
                }
            }
        }
    }

    /**
     *  调试代码
     * @param role
     * @param cmd
     * @param node
     * @param payLoadBody
     * @param result
     */
    public static void modulesMsgLogs(String role, String cmd, Node node, byte[] payLoadBody, String result) {
        if (null != logMap.get(role)) {
            logMap.get(role).debug("cmd={},peer={},hash={},rpcResult={}", cmd, node.getId(), NulsDigestData.calcDigestData(payLoadBody).getDigestHex(), result);
        } else {
            Log.debug("cmd={},peer={},hash={},rpcResult={}", cmd, node.getId(), NulsDigestData.calcDigestData(payLoadBody).getDigestHex(), result);
        }
    }
}
