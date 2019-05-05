package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.utils.thread.ConsensusProcessTask;
import io.nuls.poc.utils.thread.process.ConsensusProcess;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 共识模块任务管理器
 * Consensus Module Task Manager
 *
 * @author tag
 * 2018/11/9
 * */
@Component
public class SchedulerManager {
    /**
     * 创建一条链的任务
     * The task of creating a chain
     *
     * @param chain chain info
     * */
    public void createChainScheduler(Chain chain){
        int chainId = chain.getConfig().getChainId();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(2,new NulsThreadFactory("consensus"+chainId));
        /*
        创建链相关的任务
        Chain-related tasks
        */
        ConsensusProcess consensusProcess = new ConsensusProcess();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new ConsensusProcessTask(chain,consensusProcess),1000L,100L, TimeUnit.MILLISECONDS);
        chain.setScheduledThreadPoolExecutor(scheduledThreadPoolExecutor);
    }
}
