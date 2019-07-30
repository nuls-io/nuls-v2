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

import io.nuls.core.rpc.model.CmdPriority;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lanjinsheng
 * @date 2019-07-30
 */
public class MessageUtil {
    /**
     * 目前测试使用，后期使用协议注册来处理优先级。
     */
    public static Map<String, Integer> lowerLeverCmd = new HashMap<>();

    static {
        lowerLeverCmd.put("newHash", 1);
        lowerLeverCmd.put("askTx", 1);
        lowerLeverCmd.put("receiveTx", 1);
        lowerLeverCmd.put("block", 1);
    }

    public static Map<String, Integer> highLeverCmd = new HashMap<>();

    public static boolean isLowerLeverCmd(String cmd) {
        return (lowerLeverCmd.get(cmd) != null);
    }

    public static void addCmdPriority(String cmd, CmdPriority cmdPriority) {
        switch (cmdPriority) {
            case HIGH:
                highLeverCmd.put(cmd, 1);
                break;
            case DEFAULT:
                break;
            case LOWER:
                lowerLeverCmd.put(cmd, 1);
                break;
            default:
                break;
        }
    }
   
}
