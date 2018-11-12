package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.consensus.AgentDeposits;
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
    public static Map<Integer,Boolean> packing_status = new HashMap<>();

    /**
     * 存放各条链所有节点信息列表
     * */
    public static Map<Integer,List<Agent>> allAgentMap = new HashMap<>();

    /**
     * 存放各条链处于打包状态的共识节点信息列表
     * */
    public static Map<Integer,List<Agent>> validAgentMap = new HashMap<>();

    /**
     * 存放各条链所有的共识信息列表
     * */
    public static Map<Integer,List<Deposit>> allDepositMap = new HashMap<>();

    /**
     * 存放各条条链所有处于共识中的共识信息
     * */
    public static Map<Integer,List<Deposit>> validDepositMap = new HashMap<>();

    /**
     * 存放各链中，节点ID与参与该节点共识的共识信息列表
     * */
    public static Map<Integer, AgentDeposits> agentDepositsMap = new HashMap<>();

    /**
     * 存放各条链黄牌交易列表
     * */
    public static Map<Integer,List<PunishLogPo>> yellowPublishMap = new HashMap<>();

    /**
     * 存放各条链红牌交易列表
     * */
    public static Map<Integer,List<PunishLogPo>> redPublishList = new HashMap<>();
}
