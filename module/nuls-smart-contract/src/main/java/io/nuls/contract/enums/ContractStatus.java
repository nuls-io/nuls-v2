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
 * @date: 2019-03-25
 */
public enum ContractStatus {
    // 0 - 不存在或者创建中, 1 - 正常, 2 - 已删除, 3 - 创建失败, 4 - 锁定中
    NOT_EXISTS_OR_CONFIRMING(0),
    NORMAL(1),
    HAS_STOPPED(2),
    CREATION_FAILED(3),
    LOCKED(4);

    private int status;
    private static Map<Integer, ContractStatus> map;

    private ContractStatus(int status) {
        this.status = status;
        putStatus(status, this);
    }

    public int status() {
        return status;
    }

    private static ContractStatus putStatus(int status, ContractStatus statusEnum) {
        if(map == null) {
            map = new HashMap<>(8);
        }
        return map.put(status, statusEnum);
    }

    public static ContractStatus getStatus(int status) {
        return map.get(status);
    }
}
