package io.nuls.crosschain.nuls.model.bo;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.nuls.model.bo.config.ConfigBean;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.model.bo.message.WaitBroadSignMessage;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;

import java.security.Signature;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 链信息类
 * Chain information class
 *
 * @author tag
 * 2019/4/10
 **/
public class Chain {
    /**
     * 链基础配置信息
     * Chain Foundation Configuration Information
     */
    private ConfigBean config;

    /**
     * 接收到的其他链发送的交易Hash与当前链节点键值对
     * key:交易Hash
     * value:nodeId
     * */
    private Map<NulsHash, List<NodeType>> otherHashNodeIdMap;


    /**
     * 其他链广播的跨链交易在本链中状态
     * Transactions under processing
     * key:交易Hash
     * value:跨链交易状态1.待接收 2.已收到
     * */
    private Map<NulsHash, Integer> otherCtxStageMap;

    /**
     * 跨链交易处理结果
     * Cross-Chain Transaction Processing Results
     * key:交易Hash
     * value:处理结果列表 0未确认 1主网已确认 2接收链已确认
     * */
    private Map<NulsHash, List<Byte>> ctxStateMap;

    /**
     * 待广播的跨链交易Hash和签名
     * Cross-Chain Transaction Hash and Signature to be Broadcast
     * key:交易Hash
     * value:待广播的交易签名列表
     * */
    private Map<NulsHash, Set<WaitBroadSignMessage>> waitBroadSignMap;

    /**
     * 未处理的其他链广播来的跨链交易Hash消息
     * */
    private LinkedBlockingQueue<UntreatedMessage> hashMessageQueue;

    /**
     * 未处理的本链节点广播来的跨链交易签名消息
     * */
    private LinkedBlockingQueue<UntreatedMessage> signMessageByzantineQueue;

    /**
     * 未处理的本链节点广播来的完整跨链交易消息
     * */
    private LinkedBlockingQueue<UntreatedMessage> otherCtxMessageQueue;

    /**
     * 为处理的跨链验证交易状态请求消息
     * */
    private LinkedBlockingQueue<UntreatedMessage> getCtxStateQueue;


    /**
     * 线程池
     * */
    private final ExecutorService threadPool = ThreadUtils.createThreadPool(8, 100, new NulsThreadFactory("CrossChainProcessor"));

    /**
     * 跨连模块日志
     * */
    private NulsLogger logger;

    /**
     * 在本节点还未提交的跨链
     * key:ctxHash
     * value:投票消息列表
     * */
    private Map<NulsHash,List<UntreatedMessage>> futureMessageMap;

    /**
     * 本链最新投票节点列表（当前最新轮次与当前轮次交集）
     * */
    private List<String> verifierList;

    /**
     * 本链是否为主网
     * */
    private boolean mainChain;

    /**
     * 最后一次验证人变更高度,广播跨链转账交易时需要验证当前高度与该高度的大小
     * */
    private long lastChangeHeight;

    /**
     * 当前正在处理的验证人变更交易
     * 同一时间最多只有一笔验证人变更交易被处理
     * */
    private Transaction verifierChangeTx;

    /**
     * 已拜占庭完成，高度最小的待广播跨链转账交易
     * key:链ID
     * value:最小高度的跨链转账交易
     * */
    private Map<Integer,Long> crossChainTxMap;

    /**
     * 已拜占庭完成，高度最小的待广验证人变更交易
     * key:链ID
     * value:最小高度的验证人变更交易
     * */
    private Map<Integer,Long> verifierChangeTxMap;

    /**
     * 更新验证人时需要的锁（如：拜占庭验证与更新验证人需要互斥执行，防止拜占庭验证时验证人错误的情况）
     * */
    private final ReentrantReadWriteLock switchVerifierLock = new ReentrantReadWriteLock();

    /**
     * 节点同步状态
     * */
    private int syncStatus = 0;

    /**
     * 处理跨链交易的线程池
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
