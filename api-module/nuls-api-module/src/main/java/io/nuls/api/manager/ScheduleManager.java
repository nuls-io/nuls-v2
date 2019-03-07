package io.nuls.api.manager;

import io.nuls.api.model.po.config.ConfigBean;
import io.nuls.api.task.SyncBlockTask;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduleManager {

    public void start() {
        int corePoolSize = ChainManager.getConfigBeanMap().size();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(corePoolSize);
        for (ConfigBean bean : ChainManager.getConfigBeanMap().values()) {
            executorService.scheduleAtFixedRate(new SyncBlockTask(bean.getChainID()), 1, 10, TimeUnit.SECONDS);
        }
    }
}
