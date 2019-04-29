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
 * @date: 2019-04-26
 */
public enum CmdRegisterReturnType {
    // 0 - 字符串, 1 - 字符串数组, 2 - 字符串二维数组
    STRING(0),
    STRING_ARRAY(1),
    STRING_TWO_DIMENSIONAL_ARRAY(2);

    private int type;
    private static Map<Integer, CmdRegisterReturnType> map;

    private CmdRegisterReturnType(int type) {
        this.type = type;
        putType(type, this);
    }

    public int type() {
        return type;
    }

    private static CmdRegisterReturnType putType(int type, CmdRegisterReturnType typeEnum) {
        if(map == null) {
            map = new HashMap<>(8);
        }
        return map.put(type, typeEnum);
    }

    public static CmdRegisterReturnType getType(int type) {
        CmdRegisterReturnType cmdRegisterReturnType = map.get(type);
        if(cmdRegisterReturnType == null) {
            throw new RuntimeException(String.format("not support cmd register return type - [%s] ", type));
        }
        return cmdRegisterReturnType;
    }
}
