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
 * @date: 2019-04-03
 */
public enum LedgerUnConfirmedTxStatus {
    // 1 校验通过，2 孤儿交易 3 双花  4 其他异常  5 交易已存在
    SUCCESS(1),
    ORPHAN(2),
    DOUBLE_SPEND(3),
    OTHER(4),
    TX_EXITS(5);

    private int status;
    private static Map<Integer, LedgerUnConfirmedTxStatus> map;

    private LedgerUnConfirmedTxStatus(int status) {
        this.status = status;
        putStatus(status, this);
    }

    public int status() {
        return status;
    }

    private static LedgerUnConfirmedTxStatus putStatus(int status, LedgerUnConfirmedTxStatus statusEnum) {
        if(map == null) {
            map = new HashMap<>(8);
        }
        return map.put(status, statusEnum);
    }

    public static LedgerUnConfirmedTxStatus getStatus(int status) {
        return map.get(status);
    }
}
