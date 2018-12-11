package io.nuls.account.model.bo;

import io.nuls.account.model.bo.config.ConfigBean;

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
    private AccountStatus accountStatus;

    /**
     * 任务线程池
     * Schedule thread pool
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public Chain() {
        this.accountStatus = AccountStatus.INITING;
    }

    public ConfigBean getConfig() {
        return config;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public void setScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
    }
}
