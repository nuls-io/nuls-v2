package io.nuls.api.manager;

import io.nuls.api.ApiContext;
import io.nuls.api.task.*;
import io.nuls.core.core.annotation.Component;

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

//        int corePoolSize = CacheManager.getApiCaches().size();
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool( corePoolSize * 4);
//        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
//            executorService.scheduleAtFixedRate(new SyncBlockTask(apiCache.getChainInfo().getChainId()), 1, 10, TimeUnit.SECONDS);
//            executorService.scheduleAtFixedRate(new StatisticalNulsTask(apiCache.getChainInfo().getChainId()), 1, 20, TimeUnit.MINUTES);
//            executorService.scheduleAtFixedRate(new StatisticalTask(apiCache.getChainInfo().getChainId()), 1, 60, TimeUnit.MINUTES);
//            executorService.scheduleAtFixedRate(new UnConfirmTxTask(apiCache.getChainInfo().getChainId()), 1, 10, TimeUnit.MINUTES);
//        }

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
        executorService.scheduleAtFixedRate(new QueryChainInfoTask(ApiContext.defaultChainId), 1, 60, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(new SyncBlockTask(ApiContext.defaultChainId), 5, 10, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(new StatisticalNulsTask(ApiContext.defaultChainId), 1, 20, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(new StatisticalTask(ApiContext.defaultChainId), 1, 60, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(new UnConfirmTxTask(ApiContext.defaultChainId), 1, 10, TimeUnit.MINUTES);
    }
}
