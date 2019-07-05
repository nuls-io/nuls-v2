package io.nuls.transaction.model.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.po.TransactionNetPO;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 链的基础数据和运行状态数据
 * Chain information class
 * @author: Charlie
 * @date: 2019/04/16
 */
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
     * 是否处理交易
     * block通知,由节点区块同步状态决定
     */
    private AtomicBoolean processTxStatus;

    /**
     * 日志
     */
    private NulsLogger logger;
//    private Map<String, NulsLogger> loggerMap;//废弃

    /**
     * 交易注册信息
     */
    private Map<Integer, TxRegister> txRegisterMap;

    /**
     * 可打包交易hash集合, 交易已完成交易管理模块的校验(打包的时候从这里取)
     */
    private BlockingDeque<ByteArrayWrapper> packableHashQueue;

    /**
     * 可打包交易hash对应的交易map
     */
    private Map<ByteArrayWrapper, Transaction> packableTxMap;


    /**
     * 未进行验证的交易队列
     */
    private BlockingDeque<TransactionNetPO> unverifiedQueue;


    private LinkedList<TransactionNetPO> orphanList;

    private Map<String, Orphans> orphanMap;

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

    /**
     * 打包时处理孤儿交易的map
     */
    private Map<NulsHash, Integer> txPackageOrphanMap;

    private final Lock packageLock = new ReentrantLock();

    /**
     * 是否可执行打包
     * 交易在打包时,如果正在执行账本正在执行已确认提交或回滚, 则停止当前打包,并重新打包
     */
    private AtomicBoolean packableState;

//    /**
//     * 网络新交易处理
//     */
//    private NetTxThreadPoolExecutor netTxThreadPoolExecutor;

//    /**
//     * 处理一次网络新交易的集合
//     */
//    private List<TransactionNetPO> txNetProcessList;

    /**
     * 执行协议升级的处理
     */
    private AtomicBoolean protocolUpgrade;

    public Chain() {
        this.packaging = new AtomicBoolean(false);
        this.packableState = new AtomicBoolean(true);
        this.processTxStatus = new AtomicBoolean(false);
        this.txRegisterMap = new ConcurrentHashMap<>(TxConstant.INIT_CAPACITY_32);
        this.packableHashQueue = new LinkedBlockingDeque<>();
        this.packableTxMap = new ConcurrentHashMap<>();
        this.contractTxFail = false;
        this.txPackageOrphanMap = new HashMap<>();
        this.orphanList = new LinkedList<>();
//        this.txNetProcessList = new ArrayList<>(TxConstant.NET_TX_PROCESS_NUMBER_ONCE);
        this.orphanMap = new ConcurrentHashMap<>();
        this.protocolUpgrade = new AtomicBoolean(false);
//        this.loggerMap = new HashMap<>();
    }

//    public Map<String, NulsLogger> getLoggerMap() {
//        return loggerMap;
//    }

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

    public NulsLogger getLogger() {
        return logger;
    }

    public void setLogger(NulsLogger logger) {
        this.logger = logger;
    }

    public Map<Integer, TxRegister> getTxRegisterMap() {
        return txRegisterMap;
    }

    public void setTxRegisterMap(Map<Integer, TxRegister> txRegisterMap) {
        this.txRegisterMap = txRegisterMap;
    }

    public BlockingDeque<ByteArrayWrapper> getPackableHashQueue() {
        return packableHashQueue;
    }

    public void setPackableHashQueue(BlockingDeque<ByteArrayWrapper> packableHashQueue) {
        this.packableHashQueue = packableHashQueue;
    }

    public Map<ByteArrayWrapper, Transaction> getPackableTxMap() {
        return packableTxMap;
    }

    public void setPackableTxMap(Map<ByteArrayWrapper, Transaction> packableTxMap) {
        this.packableTxMap = packableTxMap;
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

    public AtomicBoolean getProcessTxStatus() {
        return processTxStatus;
    }

    public boolean getContractTxFail() {
        return contractTxFail;
    }

    public void setContractTxFail(boolean contractTxFail) {
        this.contractTxFail = contractTxFail;
    }

    public Map<NulsHash, Integer> getTxPackageOrphanMap() {
        return txPackageOrphanMap;
    }

    public Lock getPackageLock() {
        return packageLock;
    }

    public void setTxPackageOrphanMap(Map<NulsHash, Integer> txPackageOrphanMap) {
        this.txPackageOrphanMap = txPackageOrphanMap;
    }

    public BlockingDeque<TransactionNetPO> getUnverifiedQueue() {
        return unverifiedQueue;
    }

    public void setUnverifiedQueue(BlockingDeque<TransactionNetPO> unverifiedQueue) {
        this.unverifiedQueue = unverifiedQueue;
    }

    public LinkedList<TransactionNetPO> getOrphanList() {
        return orphanList;
    }

    public void setOrphanList(LinkedList<TransactionNetPO> orphanList) {
        this.orphanList = orphanList;
    }

//    public NetTxThreadPoolExecutor getNetTxThreadPoolExecutor() {
//        return netTxThreadPoolExecutor;
//    }
//
//    public void setNetTxThreadPoolExecutor(NetTxThreadPoolExecutor netTxThreadPoolExecutor) {
//        this.netTxThreadPoolExecutor = netTxThreadPoolExecutor;
//    }

//    public List<TransactionNetPO> getTxNetProcessList() {
//        return txNetProcessList;
//    }
//
//    public void setTxNetProcessList(List<TransactionNetPO> txNetProcessList) {
//        this.txNetProcessList = txNetProcessList;
//    }

    public Map<String, Orphans> getOrphanMap() {
        return orphanMap;
    }

    public void setOrphanMap(Map<String, Orphans> orphanMap) {
        this.orphanMap = orphanMap;
    }

    public AtomicBoolean getProtocolUpgrade() {
        return protocolUpgrade;
    }

    public AtomicBoolean getPackableState() {
        return packableState;
    }
}
