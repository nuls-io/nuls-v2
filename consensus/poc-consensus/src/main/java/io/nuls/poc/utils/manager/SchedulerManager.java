package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.utils.thread.ConsensusProcessTask;
import io.nuls.poc.utils.thread.process.ConsensusProcess;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 共识模块任务管理器
 * Consensus Module Task Manager
 *
 * @author tag
 * 2018/11/9
 * */
public class SchedulerManager {

    /**
     * 存储链与任务管理器的对应关系
     * The Correspondence between Storage Chain and Task Manager
     * */
    public static Map<Integer, ScheduledThreadPoolExecutor> scheduleMap = new ConcurrentHashMap<>();


    /**
     * 创建多条链的任务
     * The task of creating multiple chains
     *
     * @param chainMap  多条链的配置/Configuration of multiple links
     * */
    public static void createChainScheduler(Map<Integer,ConfigBean> chainMap){
        for (Map.Entry<Integer,ConfigBean> entry:chainMap.entrySet()) {
            createChainScheduler(entry.getKey());
        }
    }

    /**
     * 创建一条链的任务
     * The task of creating a chain
     *
     * @param chainId 链ID/chain id
     * */
    public static void createChainScheduler(int chainId){
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(1,new NulsThreadFactory("consensus"+chainId));
        /*
        创建链相关的任务
        Chain-related tasks
        */
        ConsensusProcess consensusProcess = new ConsensusProcess();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new ConsensusProcessTask(chainId,consensusProcess),1000L,100L, TimeUnit.MILLISECONDS);
        scheduleMap.put(chainId,scheduledThreadPoolExecutor);
    }
}
