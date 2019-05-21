package io.nuls.crosschain.nuls.model.bo;

import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.nuls.model.bo.config.ConfigBean;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

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
     * 接收到的交易Hash与当前链节点键值对
     * key:交易Hash
     * value:nodeId
     * */
    private Map<NulsDigestData, List<NodeType>> hashNodeIdMap;


    /**
     * 跨链交易在本链中状态状态
     * Transactions under processing
     * key:交易Hash
     * value:跨链交易状态1.待接收 2.已收到
     * */
    private Map<NulsDigestData, Integer> ctxStageMap;

    /**
     * 跨链交易验证结果
     * Verification results of cross-chain transactions
     * key:交易Hash
     * value：验证结果列表
     * */
    private Map<NulsDigestData, List<Boolean>> verifyCtxResultMap;

    /**
     * 跨链交易处理结果
     * Cross-Chain Transaction Processing Results
     * key:交易Hash
     * value:处理结果列表
     * */
    private Map<NulsDigestData, List<Boolean>> ctxStateMap;

    /**
     * 待广播的跨链交易Hash和签名
     * Cross-Chain Transaction Hash and Signature to be Broadcast
     * key:交易Hash
     * value:待广播的交易签名列表
     * */
    private Map<NulsDigestData, Set<BroadCtxSignMessage>> waitBroadSignMap;

    /**
     * 未处理的其他链广播来的跨链交易Hash消息
     * */
    private LinkedBlockingQueue<UntreatedMessage> hashMessageQueue;

    /**
     * 未处理的其他链广播来的完整跨链交易消息
     * */
    private LinkedBlockingQueue<UntreatedMessage> ctxMessageQueue;

    /**
     * 未处理的本链节点广播来的跨链交易签名消息
     * */
    private LinkedBlockingQueue<UntreatedMessage> signMessageQueue;

    /**
     * 未处理的本链节点广播来的完整跨链交易消息
     * */
    private LinkedBlockingQueue<UntreatedMessage> otherCtxMessageQueue;

    /**
     * 线程池
     * */
    private final ExecutorService threadPool = ThreadUtils.createThreadPool(8, 100, new NulsThreadFactory("CrossChainProcessor"));

    /**
     * 跨链模块基础日志类
     * */
    private NulsLogger basicLog;

    /**
     * 跨链模块消息协议处理日志
     * */
    private NulsLogger messageLog;

    /**
     * 跨链模块Rpc接口调用处理类
     * */
    private NulsLogger rpcLogger;

    /**
     * 本链是否为主网
     * */
    private boolean mainChain;

    public Chain(){
        hashNodeIdMap = new ConcurrentHashMap<>();
        ctxStageMap = new ConcurrentHashMap<>();
        verifyCtxResultMap = new ConcurrentHashMap<>();
        ctxStateMap = new ConcurrentHashMap<>();
        waitBroadSignMap = new ConcurrentHashMap<>();
        hashMessageQueue = new LinkedBlockingQueue<>();
        ctxMessageQueue = new LinkedBlockingQueue<>();
        signMessageQueue = new LinkedBlockingQueue<>();
        otherCtxMessageQueue = new LinkedBlockingQueue<>();
        mainChain = false;
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

    public Map<NulsDigestData, Integer> getCtxStageMap() {
        return ctxStageMap;
    }

    public void setCtxStageMap(Map<NulsDigestData, Integer> ctxStageMap) {
        this.ctxStageMap = ctxStageMap;
    }

    public Map<NulsDigestData, List<Boolean>> getVerifyCtxResultMap() {
        return verifyCtxResultMap;
    }

    public void setVerifyCtxResultMap(Map<NulsDigestData, List<Boolean>> verifyCtxResultMap) {
        this.verifyCtxResultMap = verifyCtxResultMap;
    }

    public Map<NulsDigestData, List<Boolean>> getCtxStateMap() {
        return ctxStateMap;
    }

    public void setCtxStateMap(Map<NulsDigestData, List<Boolean>> ctxStateMap) {
        this.ctxStateMap = ctxStateMap;
    }

    public NulsLogger getBasicLog() {
        return basicLog;
    }

    public void setBasicLog(NulsLogger basicLog) {
        this.basicLog = basicLog;
    }

    public NulsLogger getMessageLog() {
        return messageLog;
    }

    public void setMessageLog(NulsLogger messageLog) {
        this.messageLog = messageLog;
    }

    public NulsLogger getRpcLogger() {
        return rpcLogger;
    }

    public void setRpcLogger(NulsLogger rpcLogger) {
        this.rpcLogger = rpcLogger;
    }

    public boolean isMainChain() {
        return mainChain;
    }

    public void setMainChain(boolean mainChain) {
        this.mainChain = mainChain;
    }

    public Map<NulsDigestData, Set<BroadCtxSignMessage>> getWaitBroadSignMap() {
        return waitBroadSignMap;
    }

    public void setWaitBroadSignMap(Map<NulsDigestData, Set<BroadCtxSignMessage>> waitBroadSignMap) {
        this.waitBroadSignMap = waitBroadSignMap;
    }

    public Map<NulsDigestData, List<NodeType>> getHashNodeIdMap() {
        return hashNodeIdMap;
    }

    public void setHashNodeIdMap(Map<NulsDigestData, List<NodeType>> hashNodeIdMap) {
        this.hashNodeIdMap = hashNodeIdMap;
    }

    public LinkedBlockingQueue<UntreatedMessage> getHashMessageQueue() {
        return hashMessageQueue;
    }

    public void setHashMessageQueue(LinkedBlockingQueue<UntreatedMessage> hashMessageQueue) {
        this.hashMessageQueue = hashMessageQueue;
    }

    public LinkedBlockingQueue<UntreatedMessage> getCtxMessageQueue() {
        return ctxMessageQueue;
    }

    public void setCtxMessageQueue(LinkedBlockingQueue<UntreatedMessage> ctxMessageQueue) {
        this.ctxMessageQueue = ctxMessageQueue;
    }

    public LinkedBlockingQueue<UntreatedMessage> getSignMessageQueue() {
        return signMessageQueue;
    }

    public void setSignMessageQueue(LinkedBlockingQueue<UntreatedMessage> signMessageQueue) {
        this.signMessageQueue = signMessageQueue;
    }

    public LinkedBlockingQueue<UntreatedMessage> getOtherCtxMessageQueue() {
        return otherCtxMessageQueue;
    }

    public void setOtherCtxMessageQueue(LinkedBlockingQueue<UntreatedMessage> otherCtxMessageQueue) {
        this.otherCtxMessageQueue = otherCtxMessageQueue;
    }

    public boolean canSendMessage(){
        try {
            int linkedNode = NetWorkCall.getAvailableNodeAmount(getChainId(), true);
            if(linkedNode >= config.getMinNodeAmount()){
                return true;
            }
        }catch (NulsException e){
            basicLog.error(e);
        }
        return false;
    }

    public void clearCache(NulsDigestData hash, NulsDigestData originalHash) {
        ctxStageMap.remove(originalHash);
        verifyCtxResultMap.remove(hash);
    }

    public boolean verifyResult(NulsDigestData hash,int threshold){
        int count = 0;
        if(verifyCtxResultMap.get(hash).size() < threshold){
            return false;
        }
        for (boolean verifyResult:verifyCtxResultMap.get(hash)) {
            if(verifyResult){
                count++;
                if(count >= threshold){
                    return true;
                }
            }

        }
        return false;
    }

    public boolean statisticsCtxState(NulsDigestData hash,int threshold){
        int count = 0;
        if(ctxStateMap.get(hash).size() < threshold){
            return false;
        }
        for (boolean ctxState:ctxStateMap.get(hash)) {
            if(ctxState){
                count++;
                if(count >= threshold){
                    return true;
                }
            }

        }
        return false;
    }
}
