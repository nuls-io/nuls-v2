package io.nuls.transaction.model.bo;

import io.nuls.transaction.model.bo.config.ConfigBean;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 链信息类
 * Chain information class
 *
 * @author qinyifeng
 * @date 2018/12/11
 **/
public class Chain {
    /**
     * 链基础配置信息
     * Chain Foundation Configuration Information
     */
    private ConfigBean config;

    /**
     * 运行状态
     * Chain running state
     */
    private RunningStatus accountStatus;

    /**
     * 任务线程池
     * Schedule thread pool
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public Chain() {
        this.accountStatus = RunningStatus.INITING;
    }

    public ConfigBean getConfig() {
        return config;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }

    public RunningStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(RunningStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public void setScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
    }
}
