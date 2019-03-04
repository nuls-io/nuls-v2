package io.nuls.api.manager;

import io.nuls.api.task.SyncBlockTask;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduleManager {

    @Autowired
    private SyncBlockTask syncBlockTask;

    /**
     * 创建一条链的任务
     * The task of creating a chain
     *
     * @param chainId 链id
     */
    public void createChainScheduler(Integer chainId) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        executorService.scheduleAtFixedRate(new SyncBlockTask(chainId), 1, 10, TimeUnit.SECONDS);
    }

}
