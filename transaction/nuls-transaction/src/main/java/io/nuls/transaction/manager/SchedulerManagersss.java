package io.nuls.transaction.manager;

import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.task.TxUnverifiedProcessTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
@Service
public class SchedulerManagersss {

    /*public boolean createTransactionScheduler(Chain chain) {
        ScheduledThreadPoolExecutor threadPool = ThreadUtils.createScheduledThreadPool(1,
                new NulsThreadFactory(TxConstant.MODULE_CODE));
        threadPool.scheduleAtFixedRate(new TxUnverifiedProcessTask(chain), 5, 1, TimeUnit.SECONDS);
        chain.setScheduledThreadPoolExecutor(threadPool);
        return true;
    }*/
}
