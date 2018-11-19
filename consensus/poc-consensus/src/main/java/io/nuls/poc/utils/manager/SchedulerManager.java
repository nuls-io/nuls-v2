package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务管理器
 * @author tag
 * 2018/11/9
 * */
public class SchedulerManager {

    /**
     * 存储链与任务管理器的对应关系
     * */
    public static Map<Integer, ScheduledThreadPoolExecutor> scheduleMap = new HashMap<>();

    /**
     * 创建多条链的任务
     * @param chainMap  多条链的配置
     * */
    public static void createChainSchefuler(Map<Integer,ConfigBean> chainMap){
        for (Map.Entry<Integer,ConfigBean> entry:chainMap.entrySet()) {
            createChainScheduler(entry.getKey(),entry.getValue());
        }
    }

    /**
     * 创建一条链的任务
     * @param chain_id 链ID
     * @param config   链配置对象
     * */
    public static void createChainScheduler(int chain_id, ConfigBean config){
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(3,new NulsThreadFactory("consensus"+chain_id));
        //创建链相关的任务
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new TestRunnable(),1000L,100L, TimeUnit.MILLISECONDS);
        scheduleMap.put(chain_id,scheduledThreadPoolExecutor);
    }
}
