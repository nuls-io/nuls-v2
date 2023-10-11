/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.util;

import io.nuls.contract.model.bo.ContractResult;

import java.util.Comparator;

/**
 * @author: PierreLuo
 * @date: 2018/11/23
 */
public class CompareTxTimeDesc implements Comparator<ContractResult> {

    private static CompareTxTimeDesc instance = new CompareTxTimeDesc();

    private CompareTxTimeDesc() {

    }

    public static CompareTxTimeDesc getInstance() {
        return instance;
    }

    @Override
    public int compare(ContractResult o1, ContractResult o2) {
        if (o1.getTxTime() > o2.getTxTime()) {
            return -1;
        } else if (o1.getTxTime() < o2.getTxTime()) {
            return 1;
        } else {
            return 0;
        }
    }
}
