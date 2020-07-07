package io.nuls.provider.task;

import io.nuls.core.core.annotation.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduleManager {

    public void start() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new QueryChainInfoTask(), 1, 60, TimeUnit.SECONDS);
    }
}
