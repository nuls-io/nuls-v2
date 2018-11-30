package io.nuls.transaction.scheduler;

import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.task.TxUnverifiedProcessTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
public class TransactionScheduler {

    private final static TransactionScheduler INSTANCE = new TransactionScheduler();

    public static TransactionScheduler getInstance(){
        return INSTANCE;
    }

    private ScheduledThreadPoolExecutor threadPool;

    public boolean start() {
        threadPool = ThreadUtils.createScheduledThreadPool(1,
                new NulsThreadFactory(TxConstant.MODULE_CODE));
        threadPool.scheduleAtFixedRate(new TxUnverifiedProcessTask(), 5, 1, TimeUnit.SECONDS);

        return true;
    }
}
