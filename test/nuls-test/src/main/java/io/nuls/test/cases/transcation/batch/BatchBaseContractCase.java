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
package io.nuls.test.cases.transcation.batch;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.test.cases.BaseTestCase;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.transcation.contract.CallContractCase;
import io.nuls.test.cases.transcation.contract.CallContractParamCase;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: PierreLuo
 * @date: 2019-06-12
 */
@Component
public class BatchBaseContractCase extends BaseTestCase<String, Map> {

    int THEADH_COUNT = 2;

    @Autowired
    BatchCreateAccountCase batchCreateAccountCase;
    @Autowired
    CallContractCase callContractCase;

    @Override
    public String title() {
        return "批量合约调用交易";
    }

    @Override
    public String doTest(Map param, int depth) throws TestFailException {
        Integer count = (Integer) param.get("count");
        CallContractParamCase callContractParamCase = (CallContractParamCase) param.get("callContractParamCase");

        ThreadUtils.createAndRunThread("batch-start", () -> {
            AtomicInteger doneTotal = new AtomicInteger(0);
            AtomicInteger successTotal = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(THEADH_COUNT);
            Long start = System.currentTimeMillis();
            Log.info("开始创建交易");
            for (int s = 0; s < THEADH_COUNT; s++) {
                ThreadUtils.createAndRunThread("batch-transaction-"+s, () -> {
                    Log.info("Thread - {} begin", Thread.currentThread().getName());
                    int i = doneTotal.getAndIncrement();
                    while (i < count) {
                        int index = i % batchCreateAccountCase.getFormList().size();
                        String from = batchCreateAccountCase.getFormList().get(index);
                        String to = batchCreateAccountCase.getToList().get(index);
                        param.put("from", from);
                        param.put("to", to);
                        try {
                            Map map = callContractParamCase.doTest(param, 0);
                            String txHash = callContractCase.doTest(map, 0);
                            Log.info("txHash: {}", txHash);
                            successTotal.getAndIncrement();
                        } catch (TestFailException e) {
                            Log.error(e);
                        }
                        i = doneTotal.getAndIncrement();
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.info("创建{}笔交易,成功{}笔，消耗时间:{}", count, successTotal, System.currentTimeMillis() - start);
        });

        return "success";
    }
}
