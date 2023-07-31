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
package io.nuls.common;

import io.nuls.core.basic.VersionChangeInvoker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2023/7/31
 */
public class CommonVersionChangeInvoker implements VersionChangeInvoker {

    private static CommonVersionChangeInvoker commonVersionChangeInvoker = new CommonVersionChangeInvoker();
    private CommonVersionChangeInvoker() {}
    public static CommonVersionChangeInvoker instance() {
        return commonVersionChangeInvoker;
    }

    private static List<VersionChangeInvoker> invokers = new ArrayList<>();
    public static void addProcess(VersionChangeInvoker invoker) {
        invokers.add(invoker);
    }

    @Override
    public void process(int chainId) {
        for (VersionChangeInvoker invoker : invokers) {
            invoker.process(chainId);
        }
    }
}
