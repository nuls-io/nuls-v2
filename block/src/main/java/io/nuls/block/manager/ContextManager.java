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

package io.nuls.block.manager;

import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.context.Context;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.log.logback.LoggerBuilder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Context管理器
 * @author captain
 * @date 18-11-20 上午10:46
 * @version 1.0
 */
@Data
public class ContextManager {

    public static List<Integer> chainIds = new CopyOnWriteArrayList<>();

    private static Map<Integer, Context> contextMap = new HashMap<>();

    private ContextManager() {
    }

    public static final <T> T getServiceBean(Class<T> tClass) {
        return SpringLiteContext.getBean(tClass);
    }

    public static <T> List<T> getServiceBeanList(Class<T> tClass) {
        try {
            return SpringLiteContext.getBeanList(tClass);
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    public static void init(int chainId) {
        Context context = new Context();
        context.setChainId(chainId);
        context.setStatus(RunningStatusEnum.INITIALIZING);
        chainIds.add(chainId);
        ContextManager.contextMap.put(chainId, context);
        Log.info("new context add! chainId-{}", chainId);
    }

    public static Context getContext(int chainId) {
        return contextMap.get(chainId);
    }
}