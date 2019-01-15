package io.nuls.transaction.model.bo;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.cache.LimitHashMap;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.utils.queue.entity.PersistentQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
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
     * 日志
     */
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
    private PersistentQueue unverifiedQueue;

    /**
     * 当前最新高度
     */
    private long bestBlockHeight;

    /**
     * 任务线程池
     * Schedule thread pool
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public Chain() throws Exception {
        this.accountStatus = RunningStatus.INITING;
//        this.crossTxVerifyingMap = new HashMap<>();
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
}
