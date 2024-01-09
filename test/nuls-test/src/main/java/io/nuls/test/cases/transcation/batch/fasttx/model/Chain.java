package io.nuls.test.cases.transcation.batch.fasttx.model;


import io.nuls.test.cases.transcation.batch.fasttx.AccountStatus;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Chain information class
 * Chain information class
 *
 * @author qinyifeng
 * @date 2018/12/11
 **/
public class Chain {
    /**
     * Chain basic configuration information
     * Chain Foundation Configuration Information
     */
    private ConfigBean config;

    /**
     * running state
     * Chain running state
     */
    private AccountStatus accountStatus;

    /**
     * Task Thread Pool
     * Schedule thread pool
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public Chain() {
        this.accountStatus = AccountStatus.INITING;
    }

    public ConfigBean getConfig() {
        return config;
    }

    public int getChainId(){
        return this.config.getChainId();
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
