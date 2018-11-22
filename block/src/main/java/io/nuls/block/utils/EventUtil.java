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
package io.nuls.block.utils;

import io.nuls.rpc.cmd.CmdDispatcher;

/**
 * 与事件总线模块交互的工具类
 * @author captain
 * @date 18-11-20 上午10:45
 * @version 1.0
 */
public class EventUtil {

    /**
     * 订阅事件
     * @param moduleId
     * @param topic
     * @return
     * @throws Exception
     */
    public static boolean subscribe(String moduleId, String topic) throws Exception {
        String response = CmdDispatcher.request("subscribe", null);
        return true;
    }

    /**
     * 发布事件
     * @param moduleId
     * @param topic
     * @return
     * @throws Exception
     */
    public static boolean send(String moduleId, String topic, String eventJson) throws Exception {
        String response = CmdDispatcher.request("send", null);
        return true;
    }

}
