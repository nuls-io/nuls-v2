package io.nuls.transaction.model.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.common.ConfigBean;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.po.TransactionNetPO;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Basic data and operational status data of the chain
 * Chain information class
 * @author: Charlie
 * @date: 2019/04/16
 */
public class Chain {

    /**
     * Chain basic configuration information
     * Chain Foundation Configuration Information
     */
    private ConfigBean config;

    /**
     * Is consensus block being reached
     */
    private AtomicBoolean packaging;

    /**
     * Whether to process the transaction
     * blocknotice,Determined by the synchronization status of node blocks
     */
    private AtomicBoolean processTxStatus;

    /**
     * journal
     */
    private NulsLogger logger;

    /**
     * Transaction registration information
     */
    private Map<Integer, TxRegister> txRegisterMap;

    /**
     * Packable transactionshashaggregate, The transaction has been verified by the transaction management module(Take it from here when packing)
     */
    private BlockingDeque<ByteArrayWrapper> packableHashQueue;

    /**
     * Packable transactionshashCorresponding transactionsmap
     */
    private Map<ByteArrayWrapper, Transaction> packableTxMap;


    /**
     * Unverified transaction queue
     */
    private BlockingDeque<TransactionNetPO> unverifiedQueue;


    private LinkedList<TransactionNetPO> orphanList;

    private Map<String, Orphans> orphanMap;

    private AtomicInteger orphanListDataSize;
    /**
     * Current Latest Altitude
     */
    private long bestBlockHeight;

    /**
     * Task Thread Pool
     * Schedule thread pool
     */
    @JsonIgnore
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;


    /**
     * Is there a smart contract transaction during packaging,
     * Verification failed during the second round of module unified verification.
     * (This way, there is no need to obtain the execution result of the smart contract during the packaging process)
     */
    private boolean contractTxFail;

    /**
     * Handling orphan transactions during packagingmap
     */
    private Map<NulsHash, Integer> txPackageOrphanMap;

    private final Lock packageLock = new ReentrantLock();

    /**
     * Can packaging be executed
     * The transaction is being packaged,If the ledger is being executed and confirmed submission or rollback is being executed, Stop the current packaging,And repackage
     */
    private AtomicBoolean packableState;

    /**
     * Handling protocol upgrades
     */
    private AtomicBoolean protocolUpgrade;
    private AtomicBoolean canProtocolUpgrade;



    private Set<Integer> contractGenerateTxTypes;

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
        this.orphanMap = new ConcurrentHashMap<>();
        this.protocolUpgrade = new AtomicBoolean(false);
        this.canProtocolUpgrade = new AtomicBoolean(true);
        this.orphanListDataSize = new AtomicInteger(0);
        this.contractGenerateTxTypes = new HashSet<>();
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

    public Map<String, Orphans> getOrphanMap() {
        return orphanMap;
    }

    public void setOrphanMap(Map<String, Orphans> orphanMap) {
        this.orphanMap = orphanMap;
    }

    public AtomicBoolean getProtocolUpgrade() {
        return protocolUpgrade;
    }

    public AtomicBoolean getCanProtocolUpgrade() {
        return canProtocolUpgrade;
    }

    public void setCanProtocolUpgrade(AtomicBoolean canProtocolUpgrade) {
        this.canProtocolUpgrade = canProtocolUpgrade;
    }

    public AtomicBoolean getPackableState() {
        return packableState;
    }

    public AtomicInteger getOrphanListDataSize() {
        return orphanListDataSize;
    }

    public Set<Integer> getContractGenerateTxTypes() {
        return contractGenerateTxTypes;
    }

    public void setContractGenerateTxTypes(Set<Integer> contractGenerateTxTypes) {
        this.contractGenerateTxTypes = contractGenerateTxTypes;
    }
}
