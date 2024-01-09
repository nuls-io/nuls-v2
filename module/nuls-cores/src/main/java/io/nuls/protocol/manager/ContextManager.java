/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.protocol.manager;

import io.nuls.base.basic.ProtocolVersion;
import io.nuls.common.ConfigBean;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.utils.LoggerUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.nuls.protocol.utils.LoggerUtil.COMMON_LOG;

/**
 * Context管理器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午10:46
 */
public class ContextManager {

    static List<Integer> chainIds = new CopyOnWriteArrayList<>();

    private static Map<Integer, ProtocolContext> contextMap = new ConcurrentHashMap<>();

    private ContextManager() {
    }

    public static void init(ConfigBean parameter, List<ProtocolVersion> versions) {
        ProtocolContext protocolContext = new ProtocolContext();
        int chainId = parameter.getChainId();
        chainIds.add(chainId);
        ContextManager.contextMap.put(chainId, protocolContext);
        protocolContext.setChainId(chainId);
        protocolContext.setParameters(parameter);
        versions.sort(ProtocolVersion.COMPARATOR);
        protocolContext.setLocalVersionList(versions);
        protocolContext.init();
        LoggerUtil.init(chainId);
        COMMON_LOG.info("new protocolContext add! chainId-" + chainId);
    }

    public static ProtocolContext getContext(int chainId) {
        return contextMap.get(chainId);
    }
}