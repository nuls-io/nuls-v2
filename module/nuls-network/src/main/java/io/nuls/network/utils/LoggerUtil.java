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

import io.nuls.base.data.NulsHash;
import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.network.model.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2018/12/17
 **/
public class LoggerUtil {
    private static final String LOGGER_KEY1 = "network";
    private static final String LOGGER_KEY100 = "peerInfos";
    private static Map<String, NulsLogger> logMap = new HashMap<>();
    public static String logLevel = "DEBUG";

    public static void createLogs(int chainId) {
        if (null == logMap.get(LOGGER_KEY1 + chainId)) {
            logMap.put(LOGGER_KEY1 + chainId, LoggerBuilder.getLogger(LOGGER_KEY1, chainId));
            logMap.put(LOGGER_KEY100 + chainId, LoggerBuilder.getLogger(LOGGER_KEY100, chainId));
        }
    }

    public static NulsLogger logger(int chainId) {
        if (null == logMap.get(LOGGER_KEY1 + chainId)) {
            return io.nuls.core.log.Log.BASIC_LOGGER;
        }
        return logMap.get(LOGGER_KEY1 + chainId);
    }

    public static NulsLogger nwInfosLogger(int chainId) {
        return logMap.get(LOGGER_KEY100 + chainId);
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
        int chainId = node.getNodeGroup().getChainId();
        logger(chainId).debug("net {} cmd={},peer={},hash={}", sendOrRecieved, cmd, node.getId(), NulsHash.calcHash(payLoadBody).toHex());
    }

}
