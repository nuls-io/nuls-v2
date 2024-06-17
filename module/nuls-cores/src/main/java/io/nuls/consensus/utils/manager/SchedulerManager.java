package io.nuls.consensus.utils.manager;

import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.utils.thread.ConsensusProcessTask;
import io.nuls.consensus.utils.thread.process.ConsensusProcess;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Consensus Module Task Manager
 * Consensus Module Task Manager
 *
 * @author tag
 * 2018/11/9
 * */
@Component
public class SchedulerManager {
    /**
     * The task of creating a chain
     * The task of creating a chain
     *
     * @param chain chain info
     * */
    public void createChainScheduler(Chain chain){
        int chainId = chain.getConfig().getChainId();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(2,new NulsThreadFactory("consensus"+chainId));
        /*
        Create chain related tasks
        Chain-related tasks
        */
        ConsensusProcess consensusProcess = new ConsensusProcess();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new ConsensusProcessTask(chain,consensusProcess),1000L,100L, TimeUnit.MILLISECONDS);
        chain.setScheduledThreadPoolExecutor(scheduledThreadPoolExecutor);
    }
}
