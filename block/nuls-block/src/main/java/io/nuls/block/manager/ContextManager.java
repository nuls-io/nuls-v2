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

package io.nuls.block.manager;

import io.nuls.block.BlockBootstrap;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.nuls.block.utils.LoggerUtil.commonLog;

/**
 * Context管理器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午10:46
 */
@Data
public class ContextManager {

    public static List<Integer> chainIds = new CopyOnWriteArrayList<>();

    private static Map<Integer, ChainContext> contextMap = new ConcurrentHashMap<>();

    private ContextManager() {
    }

    public static void init(ChainParameters chainParameters) {
        ChainContext chainContext = new ChainContext();
        int chainId = chainParameters.getChainId();
        chainIds.add(chainId);
        ContextManager.contextMap.put(chainId, chainContext);
        chainContext.setChainId(chainId);
        chainContext.setParameters(chainParameters);
        chainContext.init();
        commonLog.info("new chainContext add! chainId-" + chainId);
    }

    public static ChainContext getContext(int chainId) {
        return contextMap.get(chainId);
    }
}