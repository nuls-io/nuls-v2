package io.nuls.transaction.model.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.cache.LimitHashMap;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.transaction.constant.TxConstant;
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
     * 运行状态
     * Chain running state
     */
//    private RunningStatus runningStatus;

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
    @JsonIgnore
    private NulsLogger logger;

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
     * 孤儿交易
     */
    private LimitHashMap<NulsDigestData, Transaction> orphanContainer;

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

    public Chain() throws Exception {
//        this.runningStatus = RunningStatus.INITING;
//        this.crossTxVerifyingMap = new HashMap<>();
        this.packaging =  new AtomicBoolean(false);
        this.rePackage = new AtomicBoolean(true);
        this.txRegisterMap = new HashMap<>();
        this.txQueue = new LinkedBlockingDeque<>();
        this.orphanContainer = new LimitHashMap(TxConstant.ORPHAN_CONTAINER_MAX_SIZE);
        this.unverifiedQueue = new PersistentQueue(TxConstant.TX_UNVERIFIED_QUEUE, TxConstant.TX_UNVERIFIED_QUEUE_MAXSIZE);
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

//    public RunningStatus getRunningStatus() {
//        return runningStatus;
//    }
//
//    public void setRunningStatus(RunningStatus runningStatus) {
//        this.runningStatus = runningStatus;
//    }

    public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public void setScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
    }

    public NulsLogger getLogger() {
        return logger;
    }

    public void setLogger(NulsLogger logger) {
        this.logger = logger;
    }

//    public Map<NulsDigestData, CrossTx> getCrossTxVerifyingMap() {
//        return crossTxVerifyingMap;
//    }
//
//    public void setCrossTxVerifyingMap(Map<NulsDigestData, CrossTx> crossTxVerifyingMap) {
//        this.crossTxVerifyingMap = crossTxVerifyingMap;
//    }

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

    public LimitHashMap<NulsDigestData, Transaction> getOrphanContainer() {
        return orphanContainer;
    }

    public void setOrphanContainer(LimitHashMap<NulsDigestData, Transaction> orphanContainer) {
        this.orphanContainer = orphanContainer;
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
}
