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
package io.nuls.network.other;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description
 * @author lan
 * @date 2018/12/21
 **/
public class AtomicIntegerTest {
    private static final AtomicInteger atomicInteger = new AtomicInteger();
    public static void main(String[] args) throws InterruptedException
    {
        atomicIntegerTest();
    Thread.sleep(3000);
    System.out.println("最终结果是" + atomicInteger.get());
    }
    private static void atomicIntegerTest() {
        ExecutorService executorService = Executors.newFixedThreadPool(10000);
        for (int i = 0; i < 10000; i++) {
            executorService.execute(() -> { for (int j = 0; j < 4; j++) {
                System.out.println(atomicInteger.getAndDecrement());
            }
            });
        }
        executorService.shutdown();
    }

}
