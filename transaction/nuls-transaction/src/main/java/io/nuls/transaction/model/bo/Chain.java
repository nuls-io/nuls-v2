package io.nuls.transaction.model.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.utils.queue.entity.PersistentQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * 是否正在共识出块中
     */
    private AtomicBoolean packaging;

    /**
     * 是否需要重新打包,开始打包区块交易时设为false. 打包同时,收到新区块时设为true,则需要重新打包
     */
    private AtomicBoolean rePackage;

    /**
     * 日志
     */
    private Map<String, NulsLogger> loggerMap;

    /**
     * 管理接收的其他链创建的跨链交易(如果有), 暂存验证中的跨链交易.
     * //TODO 初始化时需查数据库
     */
//    private Map<NulsDigestData, CrossTx> crossTxVerifyingMap;

    /**
     * 交易注册信息
     */
    private Map<Integer, TxRegister> txRegisterMap;

    /**
     * 交易已完成交易管理模块的校验(打包的时候从这里取)
     */
    private BlockingDeque<Transaction> txQueue;

    /**
     * 未进行验证的交易队列
     */
    @JsonIgnore
    private PersistentQueue unverifiedQueue;

    /**
     * 当前最新高度
     */
    private long bestBlockHeight;

    /**
     * 任务线程池
     * Schedule thread pool
     */
    @JsonIgnore
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;


    /**
     * 是否有智能合约交易在打包时,
     * 模块统一验证的二次验证时验证不通过.
     * (这样在当次打包时就不需要获取智能合约的执行结果)
     */
    private boolean contractTxFail;

    private Map<NulsDigestData, Integer> txPackageOrphanMap;

    public Chain() {
        this.packaging =  new AtomicBoolean(false);
        this.rePackage = new AtomicBoolean(true);
        this.txRegisterMap = new HashMap<>();
        this.txQueue = new LinkedBlockingDeque<>();
        this.loggerMap = new HashMap<>();
        contractTxFail = false;
        txPackageOrphanMap = new HashMap<>();
    }

    public int getChainId(){
        return config.getChainId();
    }
    public ConfigBean getConfig() {
        return config;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }


    public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public void setScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
    }

    public Map<String, NulsLogger> getLoggerMap() {
        return loggerMap;
    }

    public void setLoggerMap(Map<String, NulsLogger> loggerMap) {
        this.loggerMap = loggerMap;
    }

    public Map<Integer, TxRegister> getTxRegisterMap() {
        return txRegisterMap;
    }

    public void setTxRegisterMap(Map<Integer, TxRegister> txRegisterMap) {
        this.txRegisterMap = txRegisterMap;
    }

    public BlockingDeque<Transaction> getTxQueue() {
        return txQueue;
    }

    public void setTxQueue(BlockingDeque<Transaction> txQueue) {
        this.txQueue = txQueue;
    }

    public PersistentQueue getUnverifiedQueue() {
        return unverifiedQueue;
    }

    public void setUnverifiedQueue(PersistentQueue unverifiedQueue) {
        this.unverifiedQueue = unverifiedQueue;
    }

    public long getBestBlockHeight() {
        return bestBlockHeight;
    }

    public void setBestBlockHeight(long bestBlockHeight) {
        this.bestBlockHeight = bestBlockHeight;
    }

    public AtomicBoolean getPackaging() {
        return packaging;
    }

    public void setPackaging(AtomicBoolean packaging) {
        this.packaging = packaging;
    }

    public AtomicBoolean getRePackage() {
        return rePackage;
    }

    public void setRePackage(AtomicBoolean rePackage) {
        this.rePackage = rePackage;
    }

    public boolean getContractTxFail() {
        return contractTxFail;
    }

    public void setContractTxFail(boolean contractTxFail) {
        this.contractTxFail = contractTxFail;
    }

    public Map<NulsDigestData, Integer> getTxPackageOrphanMap() {
        return txPackageOrphanMap;
    }

    public void setTxPackageOrphanMap(Map<NulsDigestData, Integer> txPackageOrphanMap) {
        this.txPackageOrphanMap = txPackageOrphanMap;
    }
}
