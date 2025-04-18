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

package io.nuls.transaction.manager;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.task.ClearUnconfirmedTxProcessTask;
import io.nuls.transaction.task.NetTxProcessTask;
import io.nuls.transaction.task.OrphanTxProcessTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Charlie
 * @date: 2018-12-19
 */
@Component
public class SchedulerManager {

    /**
     * Creates transaction processing schedulers
     * This method initializes and starts multiple threads for processing different types of transactions
     * It includes the creation of online transaction processing threads, orphan transaction processing schedulers, and unconfirmed transaction cleanup mechanism schedulers
     * The purpose is to ensure that transactions are processed in a timely and effective manner, improving the efficiency and stability of the system
     *
     * @param chain The chain object, representing the specific blockchain network in which the transactions are processed
     * @return Returns a boolean value indicating whether the transaction processing scheduler was successfully created and started
     */
    public boolean createTransactionScheduler(Chain chain) {
        //New online transactions
        ThreadUtils.createAndRunThread(TxConstant.TX_THREAD, new NetTxProcessTask(chain));
        //Orphan Trading
        ScheduledThreadPoolExecutor orphanTxExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory(TxConstant.TX_ORPHAN_THREAD));
        orphanTxExecutor.scheduleAtFixedRate(new OrphanTxProcessTask(chain),
                TxConstant.TX_ORPHAN_TASK_INITIALDELAY, TxConstant.TX_ORPHAN_TASK_PERIOD, TimeUnit.SECONDS);

        //Unconfirmed transaction clearance mechanismTask
        ScheduledThreadPoolExecutor unconfirmedTxExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory(TxConstant.TX_CLEAN_THREAD));
        //Fixed delay time
        unconfirmedTxExecutor.scheduleWithFixedDelay(new ClearUnconfirmedTxProcessTask(chain),
                TxConstant.TX_CLEAN_TASK_INITIALDELAY, TxConstant.TX_CLEAN_TASK_PERIOD, TimeUnit.SECONDS);
        return true;
    }
}
