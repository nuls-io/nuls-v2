/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-04-25
 */
public enum CmdRegisterMode {
    // 0 - 创建新交易, 1 - 数据查询
    NEW_TX(0),
    QUERY_DATA(1);

    private int mode;
    private static Map<Integer, CmdRegisterMode> map;

    private CmdRegisterMode(int mode) {
        this.mode = mode;
        putMode(mode, this);
    }

    public int mode() {
        return mode;
    }

    private static CmdRegisterMode putMode(int mode, CmdRegisterMode modeEnum) {
        if(map == null) {
            map = new HashMap<>(8);
        }
        return map.put(mode, modeEnum);
    }

    public static CmdRegisterMode getMode(int mode) {
        CmdRegisterMode cmdRegisterMode = map.get(mode);
        if(cmdRegisterMode == null) {
            throw new RuntimeException(String.format("not support cmd register mode - [%s] ", mode));
        }
        return cmdRegisterMode;
    }
}
