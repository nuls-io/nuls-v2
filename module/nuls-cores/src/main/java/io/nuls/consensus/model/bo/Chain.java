package io.nuls.consensus.model.bo;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.common.ConfigBean;
import io.nuls.consensus.model.bo.consensus.Evidence;
import io.nuls.consensus.model.bo.round.MeetingRound;
import io.nuls.consensus.model.bo.tx.txdata.Agent;
import io.nuls.consensus.model.bo.tx.txdata.Deposit;
import io.nuls.consensus.model.po.PunishLogPo;
import io.nuls.consensus.utils.enumeration.ConsensusStatus;
import io.nuls.core.log.logback.NulsLogger;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 链信息类
 * Chain information class
 *
 * @author tag
 * 2018/12/4
 **/
public class Chain {
    /**
     * 是否为共识节点
     * Is it a consensus node
     */
    private boolean packer;

    /**
     * 链基础配置信息
     * Chain Foundation Configuration Information
     */
    private ConfigBean config;

    /**
     * 运行状态
     * Chain running state
     */
    private ConsensusStatus consensusStatus;

    /**
     * 打包状态
     * Chain packing state
     */
    private boolean canPacking;

    /**
     * 最新区块头
     * The most new block
     */
    private BlockHeader newestHeader;

    /**
     * 节点列表
     * Agent list
     */
    private List<Agent> agentList;

    /**
     * 委托信息列表
     * Deposit list
     */
    private List<Deposit> depositList;

    /**
     * 黄牌列表
     * Yellow punish list
     */
    private List<PunishLogPo> yellowPunishList;

    /**
     * 红牌列表
     * Red punish list
     */
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
     */
    private List<Transaction> redPunishTransactionList;

    /**
     * 轮次列表
     * Round list
     */
    private List<MeetingRound> roundList;

    /**
     * 最新200轮区块头
     * The latest 200 rounds block
     */
    private List<BlockHeader> blockHeaderList;

    private final Lock roundLock = new ReentrantLock();

    private NulsLogger logger;

    private boolean cacheLoaded;

    /**
     * 任务线程池
     * Schedule thread pool
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public Chain() {
        this.consensusStatus = ConsensusStatus.RUNNING;
        this.canPacking = false;
        this.agentList = new ArrayList<>();
        this.depositList = new ArrayList<>();
        this.yellowPunishList = new ArrayList<>();
        this.redPunishList = new ArrayList<>();
        this.evidenceMap = new HashMap<>();
        this.redPunishTransactionList = new ArrayList<>();
        this.roundList = new ArrayList<>();
        this.packer = false;
        this.cacheLoaded = false;
    }

    /**
     * 获取新建或达到出块要求的节点列表
     * Get a list of nodes that meet block requirements
     *
     * @param height
     * @return List<agent>
     **/
    public List<Agent> getNewOrWorkAgentList(long height) {
        List<Agent> workAgentList = new ArrayList<>();
        for (Agent agent : agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= height) {
                continue;
            }
            if (agent.getBlockHeight() > height || agent.getBlockHeight() < 0L) {
                continue;
            }
            workAgentList.add(agent);
        }
        return workAgentList;
    }

    /**
     * 获取达到出块要求的节点列表
     * Get a list of nodes that meet block requirements
     *
     * @param height
     * @return List<agent>
     **/
    public List<Agent> getWorkAgentList(long height) {
        List<Agent> workAgentList = new ArrayList<>();
        for (Agent agent : agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= height) {
                continue;
            }
            if (agent.getBlockHeight() > height || agent.getBlockHeight() < 0L) {
                continue;
            }
            /*
            获取节点委托信息，用于计算节点总的委托金额
            Get the node delegation information for calculating the total amount of the node delegation
            */
            List<Deposit> cdList = getDepositListByAgentId(agent.getTxHash(), height);
            BigInteger totalDeposit = BigInteger.ZERO;
            for (Deposit dtx : cdList) {
                totalDeposit = totalDeposit.add(dtx.getDeposit());
            }
            if (totalDeposit.compareTo(config.getCommissionMin()) >= 0) {
                workAgentList.add(agent);
            }
        }
        return workAgentList;
    }

    /**
     * 获取达到出块要求的节点地址列表
     * Get a list of nodes that meet block requirements
     *
     * @param height
     * @return List<agent>
     **/
    public Set<String> getWorkAddressList(long height) {
        Set<String> workAddressList = new HashSet<>();
        for (Agent agent : agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= height) {
                continue;
            }
            if (agent.getBlockHeight() > height || agent.getBlockHeight() < 0L) {
                continue;
            }
            /*
            获取节点委托信息，用于计算节点总的委托金额
            Get the node delegation information for calculating the total amount of the node delegation
            */
            List<Deposit> cdList = getDepositListByAgentId(agent.getTxHash(), height);
            BigInteger totalDeposit = BigInteger.ZERO;
            for (Deposit dtx : cdList) {
                totalDeposit = totalDeposit.add(dtx.getDeposit());
            }
            if (totalDeposit.compareTo(config.getCommissionMin()) >= 0) {
                workAddressList.add(AddressTool.getStringAddressByBytes(agent.getPackingAddress()));
            }
        }
        return workAddressList;
    }

    /**
     * 获取节点的委托信息
     * Obtaining delegation information of nodes
     *
     * @param agentHash        节点ID/agent hash
     * @param startBlockHeight 上一轮次的起始区块高度/Initial blocks of the last round
     * @return List<Deposit>
     */
    private List<Deposit> getDepositListByAgentId(NulsHash agentHash, long startBlockHeight) {
        List<Deposit> resultList = new ArrayList<>();
        for (int i = depositList.size() - 1; i >= 0; i--) {
            Deposit deposit = depositList.get(i);
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (deposit.getAgentHash().equals(agentHash)) {
                resultList.add(deposit);
            }
        }
        return resultList;
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

    public Lock getRoundLock() {
        return roundLock;
    }

    public boolean isPacker() {
        return packer;
    }

    public void setPacker(boolean packer) {
        this.packer = packer;
    }

    public NulsLogger getLogger() {
        return logger;
    }

    public void setLogger(NulsLogger logger) {
        this.logger = logger;
    }

    public boolean isCacheLoaded() {
        return cacheLoaded;
    }

    public void setCacheLoaded(boolean cacheLoaded) {
        this.cacheLoaded = cacheLoaded;
    }
}
