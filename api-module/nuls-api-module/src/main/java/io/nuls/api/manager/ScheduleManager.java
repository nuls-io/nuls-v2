package io.nuls.api.manager;

import io.nuls.api.cache.ApiCache;
import io.nuls.api.task.SyncBlockTask;
import io.nuls.tools.core.annotation.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduleManager {

    public void start() {
//        int corePoolSize = ChainManager.getConfigBeanMap().size();
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(corePoolSize);
//        for (ConfigBean bean : ChainManager.getConfigBeanMap().values()) {
//            executorService.scheduleAtFixedRate(new SyncBlockTask(bean.getChainId()), 1, 10, TimeUnit.SECONDS);
//        }

        int corePoolSize = CacheManager.getApiCaches().size();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(corePoolSize);
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            executorService.scheduleAtFixedRate(new SyncBlockTask(apiCache.getChainInfo().getChainId()), 1, 10, TimeUnit.SECONDS);
        }
    }
}
