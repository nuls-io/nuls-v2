package io.nuls.poc.model.bo;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.utils.enumeration.ConsensusStatus;
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
     * 链基础配置信息
     * Chain Foundation Configuration Information
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
     * 最新区块头
     * The most new block
     * */
     private BlockHeader newestHeader;

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
     * 记录链出块地址PackingAddress，同一个高度发出了两个不同的块的证据
     * 下一轮正常则清零， 连续3轮将会被红牌惩罚
     * Record the address of each chain out block Packing Address, and the same height gives evidence of two different blocks.
     * The next round of normal will be cleared, and three consecutive rounds will be punished by red cards.
     */
    private Map<String, List<Evidence>> evidenceMap;

    /**
     * 保存本节点需打包的红牌交易,节点打包时需把该集合中所有红牌交易打包并删除
     * To save the red card transactions that need to be packaged by the node,
     * the node should pack and delete all the red card transactions in the set when packing.
     * */
    private List<Transaction> redPunishTransactionList;

    /**
     * 轮次列表
     * Round list
     * */
    private List<MeetingRound> roundList;

    /**
     * 最新200轮区块头
     * The latest 200 rounds block
     * */
    private List<BlockHeader> blockHeaderList;

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

    public BlockHeader getNewestHeader() {
        return newestHeader;
    }

    public void setNewestHeader(BlockHeader newestHeader) {
        this.newestHeader = newestHeader;
    }

    public List<BlockHeader> getBlockHeaderList() {
        return blockHeaderList;
    }

    public void setBlockHeaderList(List<BlockHeader> blockHeaderList) {
        this.blockHeaderList = blockHeaderList;
    }

    public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public void setScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
    }
}
