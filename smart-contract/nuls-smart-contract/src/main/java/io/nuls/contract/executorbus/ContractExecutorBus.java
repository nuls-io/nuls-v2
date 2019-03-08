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
package io.nuls.contract.executorbus;


import io.nuls.contract.callable.ContractTxCallable;
import io.nuls.contract.model.bo.CallableResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public class ContractExecutorBus {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private List<ContractTxCallable> contractTxCallableList;

    public static ContractExecutorBus newInstance() {
        return new ContractExecutorBus();
    }

    public ContractExecutorBus() {
        this.contractTxCallableList = new ArrayList<>();
    }

    public void add(ContractTxCallable callable) {
        contractTxCallableList.add(callable);
    }

    public List<Future<CallableResult>> execute() {
        try {
            List<Future<CallableResult>> futures = EXECUTOR.invokeAll(contractTxCallableList);
            return futures;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
