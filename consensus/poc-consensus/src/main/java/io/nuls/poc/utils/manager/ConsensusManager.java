package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.consensus.AgentDeposits;
import io.nuls.poc.model.bo.consensus.AgentPunishs;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.po.PunishLogPo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsensusManager {
    /**
     * 节点各条链打包状态
     * */
    private Map<Integer,Boolean> packing_status = new HashMap<>();

    /**
     * 存放各条链所有节点信息列表
     * */
    private Map<Integer,List<Agent>> allAgentMap = new HashMap<>();

    /**
     * 存放各条链处于打包状态的共识节点信息列表
     * */
    private Map<Integer,List<Agent>> validAgentMap = new HashMap<>();

    /**
     * 存放各条链所有的共识信息列表
     * */
    private Map<Integer,List<Deposit>> allDepositMap = new HashMap<>();

    /**
     * 存放各条条链所有处于共识中的共识信息
     * */
    private Map<Integer,List<Deposit>> validDepositMap = new HashMap<>();

    /**
     * 存放各链中，节点ID与参与该节点共识的共识信息列表
     * */
    private Map<Integer, AgentDeposits> agentDepositsMap = new HashMap<>();

    /**
     * 存放各条链黄牌交易列表
     * */
    private Map<Integer,List<PunishLogPo>> yellowPunishMap = new HashMap<>();

    /**
     * 存放各条链红牌交易列表
     * */
    private Map<Integer,List<PunishLogPo>> redPunishList = new HashMap<>();

    /**
     * 账户对应的红黄牌列表
     * */
    private Map<Integer, AgentPunishs> agentPunishsMap = new HashMap<>();

    /**
     * 控制该类为单例模式
     * */
    public static ConsensusManager instance = null;
    private ConsensusManager() { }
    private static Integer LOCK = 0;
    public static ConsensusManager getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new ConsensusManager();
            }
            return instance;
        }
    }

    public Map<Integer, Boolean> getPacking_status() {
        return packing_status;
    }

    public void setPacking_status(Map<Integer, Boolean> packing_status) {
        this.packing_status = packing_status;
    }

    public Map<Integer, List<Agent>> getAllAgentMap() {
        return allAgentMap;
    }

    public void setAllAgentMap(Map<Integer, List<Agent>> allAgentMap) {
        this.allAgentMap = allAgentMap;
    }

    public Map<Integer, List<Agent>> getValidAgentMap() {
        return validAgentMap;
    }

    public void setValidAgentMap(Map<Integer, List<Agent>> validAgentMap) {
        this.validAgentMap = validAgentMap;
    }

    public Map<Integer, List<Deposit>> getAllDepositMap() {
        return allDepositMap;
    }

    public void setAllDepositMap(Map<Integer, List<Deposit>> allDepositMap) {
        this.allDepositMap = allDepositMap;
    }

    public Map<Integer, List<Deposit>> getValidDepositMap() {
        return validDepositMap;
    }

    public void setValidDepositMap(Map<Integer, List<Deposit>> validDepositMap) {
        this.validDepositMap = validDepositMap;
    }

    public Map<Integer, AgentDeposits> getAgentDepositsMap() {
        return agentDepositsMap;
    }

    public void setAgentDepositsMap(Map<Integer, AgentDeposits> agentDepositsMap) {
        this.agentDepositsMap = agentDepositsMap;
    }

    public Map<Integer, List<PunishLogPo>> getYellowPunishMap() {
        return yellowPunishMap;
    }

    public void setYellowPunishMap(Map<Integer, List<PunishLogPo>> yellowPunishMap) {
        this.yellowPunishMap = yellowPunishMap;
    }

    public Map<Integer, List<PunishLogPo>> getRedPunishList() {
        return redPunishList;
    }

    public void setRedPunishList(Map<Integer, List<PunishLogPo>> redPunishList) {
        this.redPunishList = redPunishList;
    }

    public Map<Integer, AgentPunishs> getAgentPunishsMap() {
        return agentPunishsMap;
    }

    public void setAgentPunishsMap(Map<Integer, AgentPunishs> agentPunishsMap) {
        this.agentPunishsMap = agentPunishsMap;
    }
}
