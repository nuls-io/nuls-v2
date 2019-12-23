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

package io.nuls.api.manager;

import io.nuls.api.model.entity.Message;
import io.nuls.core.model.DateUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Niels
 */
public class MessageManager {

    private static final Map<String, Message> messageMap = new HashMap<>();

    static {
        ScheduledThreadPoolExecutor executorService = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("clear-message"));
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //以防万一
                if (messageMap.size() > 1000) {
                    messageMap.clear();
                }
                for (Map.Entry<String, Message> entry : messageMap.entrySet()) {
                    if (entry.getValue().getTime() < NulsDateUtils.getCurrentTimeSeconds() - 120) {
                        messageMap.remove(entry.getKey());
                    }
                }
            }
        }, 120, 30, TimeUnit.SECONDS);
    }

    public static void putMessage(String key, Message message) {
        if (StringUtils.isBlank(key) || null == message) {
            return;
        }
        if (messageMap.size() > 5000) {
            messageMap.clear();
        }
        messageMap.put(key, message);

    }

    public static Message getMessage(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return messageMap.get(key);
    }
}
