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

    public boolean createTransactionScheduler(Chain chain) {
        //统计
        /*ScheduledThreadPoolExecutor netTxExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("StatisticsTask"));
        netTxExecutor.scheduleAtFixedRate(new StatisticsTask(), TxConstant.TX_TASK_INITIALDELAY, TxConstant.TX_TASK_PERIOD, TimeUnit.SECONDS);
        chain.setScheduledThreadPoolExecutor(netTxExecutor);*/

        //网络新交易
        ThreadUtils.createAndRunThread(TxConstant.TX_THREAD, new NetTxProcessTask(chain));
        //孤儿交易
        ScheduledThreadPoolExecutor orphanTxExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory(TxConstant.TX_ORPHAN_THREAD));
        orphanTxExecutor.scheduleAtFixedRate(new OrphanTxProcessTask(chain),
                TxConstant.TX_ORPHAN_TASK_INITIALDELAY, TxConstant.TX_ORPHAN_TASK_PERIOD, TimeUnit.SECONDS);

        //未确认交易清理机制Task
        ScheduledThreadPoolExecutor unconfirmedTxExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory(TxConstant.TX_CLEAN_THREAD));
        //固定延迟时间
        unconfirmedTxExecutor.scheduleWithFixedDelay(new ClearUnconfirmedTxProcessTask(chain),
                TxConstant.TX_CLEAN_TASK_INITIALDELAY, TxConstant.TX_CLEAN_TASK_PERIOD, TimeUnit.MINUTES);

        return true;
    }
}
