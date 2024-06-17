package io.nuls.crosschain.model.bo;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.common.ConfigBean;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.crosschain.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.model.bo.message.WaitBroadSignMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Chain information class
 * Chain information class
 *
 * @author tag
 * 2019/4/10
 **/
public class Chain {
    /**
     * Chain basic configuration information
     * Chain Foundation Configuration Information
     */
    private ConfigBean config;

    /**
     * Received transactions sent by other chainsHashPaired with the current chain node key value
     * key:transactionHash
     * value:nodeId
     * */
    private Map<NulsHash, List<NodeType>> otherHashNodeIdMap;


    /**
     * The status of cross chain transactions broadcasted by other chains in this chain
     * Transactions under processing
     * key:transactionHash
     * value:Cross chain transaction status1.To be received 2.Received
     * */
    private Map<NulsHash, Integer> otherCtxStageMap;

    /**
     * Cross chain transaction processing results
     * Cross-Chain Transaction Processing Results
     * key:transactionHash
     * value:Processing result list 0Unconfirmed 1Main network confirmed 2Receiving chain confirmed
     * */
    private Map<NulsHash, List<Byte>> ctxStateMap;

    /**
     * Cross chain transactions to be broadcastedHashAnd signature
     * Cross-Chain Transaction Hash and Signature to be Broadcast
     * key:transactionHash
     * value:List of transaction signatures to be broadcasted
     * */
    private Map<NulsHash, Set<WaitBroadSignMessage>> waitBroadSignMap;

    /**
     * Unprocessed cross chain transactions broadcasted from other chainsHashnews
     * */
    private LinkedBlockingQueue<UntreatedMessage> hashMessageQueue;

    /**
     * Unprocessed cross chain transaction signature messages broadcasted by nodes in this chain
     * */
    private LinkedBlockingQueue<UntreatedMessage> signMessageByzantineQueue;

    /**
     * Complete cross chain transaction messages broadcasted by unprocessed nodes in this chain
     * */
    private LinkedBlockingQueue<UntreatedMessage> otherCtxMessageQueue;

    /**
     * Cross chain verification transaction status request message for processing
     * */
    private LinkedBlockingQueue<UntreatedMessage> getCtxStateQueue;


    /**
     * Thread pool
     * */
    private final ExecutorService threadPool = ThreadUtils.createThreadPool(8, 100, new NulsThreadFactory("CrossChainProcessor"));

    /**
     * Cross connect module logs
     * */
    private NulsLogger logger;

    /**
     * Cross chain not yet submitted at this node
     * key:ctxHash
     * value:Voting message list
     * */
    private Map<NulsHash,List<UntreatedMessage>> futureMessageMap;

    /**
     * The latest list of voting nodes in this chain（Intersection between the latest and current rounds）
     * */
    private List<String> verifierList;

    /**
     * Is this chain the main network
     * */
    private boolean mainChain;

    /**
     * The height of the last verifier change,When broadcasting cross chain transfer transactions, it is necessary to verify the current height and the size of that height
     * */
    private long lastChangeHeight;

    /**
     * Currently processing validator change transactions
     * At most one validator change transaction can be processed at the same time
     * */
    private Transaction verifierChangeTx;

    /**
     * Completed by Byzantium, the smallest pending broadcast cross chain transfer transaction
     * key:chainID
     * value:Cross chain transfer transactions with minimum height
     * */
    private Map<Integer,Long> crossChainTxMap;

    /**
     * Completed by Byzantium, with the smallest height of pending witness change transaction
     * key:chainID
     * value:Minimum height validator change transaction
     * */
    private Map<Integer,Long> verifierChangeTxMap;

    /**
     * Locks required for updating validators（as：Byzantine verification and update validators need to be mutually exclusive to prevent errors in validators during Byzantine verification）
     * */
    private final ReentrantReadWriteLock switchVerifierLock = new ReentrantReadWriteLock();

    /**
     * Node synchronization status
     * */
    private int syncStatus = 0;

    /**
     * Thread pool for handling cross chain transactions
     * */
    private final ExecutorService crossTxThreadPool = ThreadUtils.createThreadPool(4, 10000, new NulsThreadFactory("CROSS_TX_THREAD_POOL"));
    public Chain(){
        otherHashNodeIdMap = new ConcurrentHashMap<>();
        ctxStateMap = new ConcurrentHashMap<>();
        otherCtxStageMap = new ConcurrentHashMap<>();
        waitBroadSignMap = new ConcurrentHashMap<>();
        hashMessageQueue = new LinkedBlockingQueue<>();
        signMessageByzantineQueue = new LinkedBlockingQueue<>();
        otherCtxMessageQueue = new LinkedBlockingQueue<>();
        getCtxStateQueue = new LinkedBlockingQueue<>();
        futureMessageMap = new ConcurrentHashMap<>();
        verifierList = new ArrayList<>();
        mainChain = false;
        verifierChangeTx = null;
        crossChainTxMap = new ConcurrentHashMap<>();
        verifierChangeTxMap = new ConcurrentHashMap<>();
        lastChangeHeight = 0;
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

    public Map<NulsHash, List<Byte>> getCtxStateMap() {
        return ctxStateMap;
    }

    public void setCtxStateMap(Map<NulsHash, List<Byte>> ctxStateMap) {
        this.ctxStateMap = ctxStateMap;
    }

    public NulsLogger getLogger() {
        return logger;
    }

    public void setLogger(NulsLogger logger) {
        this.logger = logger;
    }

    public boolean isMainChain() {
        return mainChain;
    }

    public void setMainChain(boolean mainChain) {
        this.mainChain = mainChain;
    }

    public Map<NulsHash, Set<WaitBroadSignMessage>> getWaitBroadSignMap() {
        return waitBroadSignMap;
    }

    public void setWaitBroadSignMap(Map<NulsHash, Set<WaitBroadSignMessage>> waitBroadSignMap) {
        this.waitBroadSignMap = waitBroadSignMap;
    }


    public LinkedBlockingQueue<UntreatedMessage> getHashMessageQueue() {
        return hashMessageQueue;
    }

    public void setHashMessageQueue(LinkedBlockingQueue<UntreatedMessage> hashMessageQueue) {
        this.hashMessageQueue = hashMessageQueue;
    }

    public LinkedBlockingQueue<UntreatedMessage> getSignMessageByzantineQueue() {
        return signMessageByzantineQueue;
    }

    public void setSignMessageByzantineQueue(LinkedBlockingQueue<UntreatedMessage> signMessageByzantineQueue) {
        this.signMessageByzantineQueue = signMessageByzantineQueue;
    }

    public LinkedBlockingQueue<UntreatedMessage> getOtherCtxMessageQueue() {
        return otherCtxMessageQueue;
    }

    public void setOtherCtxMessageQueue(LinkedBlockingQueue<UntreatedMessage> otherCtxMessageQueue) {
        this.otherCtxMessageQueue = otherCtxMessageQueue;
    }

    public Map<NulsHash, List<NodeType>> getOtherHashNodeIdMap() {
        return otherHashNodeIdMap;
    }

    public void setOtherHashNodeIdMap(Map<NulsHash, List<NodeType>> otherHashNodeIdMap) {
        this.otherHashNodeIdMap = otherHashNodeIdMap;
    }

    public Map<NulsHash, Integer> getOtherCtxStageMap() {
        return otherCtxStageMap;
    }

    public void setOtherCtxStageMap(Map<NulsHash, Integer> otherCtxStageMap) {
        this.otherCtxStageMap = otherCtxStageMap;
    }

    public LinkedBlockingQueue<UntreatedMessage> getGetCtxStateQueue() {
        return getCtxStateQueue;
    }

    public void setGetCtxStateQueue(LinkedBlockingQueue<UntreatedMessage> getCtxStateQueue) {
        this.getCtxStateQueue = getCtxStateQueue;
    }

    public Map<NulsHash, List<UntreatedMessage>> getFutureMessageMap() {
        return futureMessageMap;
    }

    public List<String> getVerifierList() {
        return verifierList;
    }

    public void setVerifierList(List<String> verifierList) {
        this.verifierList = verifierList;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public Transaction getVerifierChangeTx() {
        return verifierChangeTx;
    }

    public void setVerifierChangeTx(Transaction verifierChangeTx) {
        this.verifierChangeTx = verifierChangeTx;
    }

    public Map<Integer, Long> getCrossChainTxMap() {
        return crossChainTxMap;
    }

    public void setCrossChainTxMap(Map<Integer, Long> crossChainTxMap) {
        this.crossChainTxMap = crossChainTxMap;
    }

    public Map<Integer, Long> getVerifierChangeTxMap() {
        return verifierChangeTxMap;
    }

    public void setVerifierChangeTxMap(Map<Integer, Long> verifierChangeTxMap) {
        this.verifierChangeTxMap = verifierChangeTxMap;
    }

    public ExecutorService getCrossTxThreadPool() {
        return crossTxThreadPool;
    }

    public ReentrantReadWriteLock getSwitchVerifierLock() {
        return switchVerifierLock;
    }

    public long getLastChangeHeight() {
        return lastChangeHeight;
    }

    public void setLastChangeHeight(long lastChangeHeight) {
        this.lastChangeHeight = lastChangeHeight;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public synchronized Transaction isExistVerifierChangeTx(Transaction verifierChangeTx){
        if(this.verifierChangeTx == null){
            this.verifierChangeTx = verifierChangeTx;
            return null;
        }
        return this.verifierChangeTx;
    }
}
