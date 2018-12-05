package io.nuls.poc.model.bo;

import io.nuls.base.data.Transaction;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.consensus.ConsensusStatus;
import io.nuls.poc.model.bo.consensus.Evidence;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.po.PunishLogPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 链信息类
 * Chain information class
 *
 * @author tag
 * 2018/12/4
 **/
public class Chain {
    /**
     * 基础配置类
     * Chain infrastructure configuration class
     * */
    private ConfigBean config;

    /**
     * 运行状态
     * Chain running state
     * */
    private ConsensusStatus consensusStatus;

    /**
     * 打包状态
     * Chain packing state
     * */
    private boolean canPacking;

    /**
     * 节点列表
     * Agent list
     * */
    private List<Agent> agentList;

    /**
     * 委托信息列表
     * Deposit list
     * */
    private List<Deposit> depositList;

    /**
     * 黄牌列表
     * Yellow punish list
     * */
    private List<PunishLogPo> yellowPunishList;

    /**
     * 红牌列表
     * Red punish list
     * */
    private List<PunishLogPo> redPunishList;

    /**
     * 分叉证据记录
     * Evidence of bifurcation
     * */
    private Map<String, List<Evidence>> evidenceMap;

    /**
     * 待打包红牌交易列表
     * To Pack Red Card Trading List
     * */
    private List<Transaction> redPunishTransactionList;

    /**
     * 轮次列表
     * Round list
     * */
    private List<MeetingRound> roundList;

    /**
     * 任务线程池
     * Schedule thread pool
     * */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;


    public Chain(){
        this.consensusStatus = ConsensusStatus.INITING;
        this.canPacking = false;
        this.agentList = new ArrayList<>();
        this.depositList = new ArrayList<>();
        this.yellowPunishList = new ArrayList<>();
        this.redPunishList = new ArrayList<>();
        this.evidenceMap = new HashMap<>();
        this.redPunishTransactionList = new ArrayList<>();
        this.roundList = new ArrayList<>();
    }

    public ConfigBean getConfig() {
        return config;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }

    public ConsensusStatus getConsensusStatus() {
        return consensusStatus;
    }

    public void setConsensusStatus(ConsensusStatus consensusStatus) {
        this.consensusStatus = consensusStatus;
    }

    public boolean isCanPacking() {
        return canPacking;
    }

    public void setCanPacking(boolean canPacking) {
        this.canPacking = canPacking;
    }

    public List<Agent> getAgentList() {
        return agentList;
    }

    public void setAgentList(List<Agent> agentList) {
        this.agentList = agentList;
    }

    public List<Deposit> getDepositList() {
        return depositList;
    }

    public void setDepositList(List<Deposit> depositList) {
        this.depositList = depositList;
    }

    public List<PunishLogPo> getYellowPunishList() {
        return yellowPunishList;
    }

    public void setYellowPunishList(List<PunishLogPo> yellowPunishList) {
        this.yellowPunishList = yellowPunishList;
    }

    public List<PunishLogPo> getRedPunishList() {
        return redPunishList;
    }

    public void setRedPunishList(List<PunishLogPo> redPunishList) {
        this.redPunishList = redPunishList;
    }

    public Map<String, List<Evidence>> getEvidenceMap() {
        return evidenceMap;
    }

    public void setEvidenceMap(Map<String, List<Evidence>> evidenceMap) {
        this.evidenceMap = evidenceMap;
    }

    public List<Transaction> getRedPunishTransactionList() {
        return redPunishTransactionList;
    }

    public void setRedPunishTransactionList(List<Transaction> redPunishTransactionList) {
        this.redPunishTransactionList = redPunishTransactionList;
    }

    public List<MeetingRound> getRoundList() {
        return roundList;
    }

    public void setRoundList(List<MeetingRound> roundList) {
        this.roundList = roundList;
    }

    public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public void setScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
    }
}
