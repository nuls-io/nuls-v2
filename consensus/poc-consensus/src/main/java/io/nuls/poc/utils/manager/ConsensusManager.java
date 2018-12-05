package io.nuls.poc.utils.manager;

import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.consensus.ConsensusStatus;
import io.nuls.poc.model.bo.consensus.PunishType;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.storage.PunishStorageService;
import io.nuls.poc.utils.compare.AgentComparator;
import io.nuls.poc.utils.compare.DepositComparator;
import io.nuls.poc.utils.compare.PunishLogComparator;
import io.nuls.poc.utils.util.PoConvertUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统启动时加载缓存的处理器
 * The cmd that loads the cache when the system starts.
 * @author  tag
 * 2018/11/14
 * */
public class ConsensusManager {
    private AgentStorageService agentStorageService = SpringLiteContext.getBean(AgentStorageService.class);
    private DepositStorageService depositStorageService = SpringLiteContext.getBean(DepositStorageService.class);
    private PunishStorageService punishStorageService = SpringLiteContext.getBean(PunishStorageService.class);

    /**
     * 节点各条链的状态
     * The state of each chain of nodes
     * */
    private Map<Integer, ConsensusStatus> agentStatus = new ConcurrentHashMap<>();

    //TODO 信息每条链单独村
    /**
     * 节点各条链打包状态
     * Packing status of each chain of nodes
     * */
    private Map<Integer,Boolean> packingStatus = new ConcurrentHashMap<>();

    /**
     * 存放各条链所有节点信息列表
     * Store a list of all nodes in each chain
     * */
    private Map<Integer,List<Agent>> allAgentMap = new ConcurrentHashMap<>();

    /**
     * 存放各条链所有的共识信息列表
     * Store a list of all consensus information for each chain
     * */
     private Map<Integer,List<Deposit>> allDepositMap = new ConcurrentHashMap<>();

    /**
     * 存放各条链黄牌交易列表
     * Store a list of yellow-card transactions in each chain
     * */
    private Map<Integer,List<PunishLogPo>> yellowPunishMap = new ConcurrentHashMap<>();

    /**
     * 存放各条链红牌交易列表
     * Store the list of red card transactions in each chain
     * */
    private Map<Integer,List<PunishLogPo>> redPunishMap = new ConcurrentHashMap<>();

    /**
     * 控制该类为单例模式
     * Control this class as a singleton pattern
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

    /**
     * 初始化数据
     * Initialization data
     *
     * @param chainId 链ID
     * */
    public void initData(int chainId){
        try {
            //初始化节点状态
            packingStatus.put(chainId,false);
            agentStatus.put(chainId, ConsensusStatus.INITING);
            //从数据库中读取节点信息，共识信息，红黄牌信息存放到对应的map中
            loadAgents(chainId);
            loadDeposits(chainId);
            loadPunishes(chainId);
            RoundManager.getInstance().initRound(chainId);
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 数据黄牌数据
     * Data yellow card data
     *
     * @param chainId 链ID
     * */
    public void clear(int chainId){
        /*todo
          从区块管理模块获取最后一个区块的高度,清除200轮之前的红黄牌数据
          Get the height of the last block from the module management module and clear the red and yellow card data before 200 rounds.
         */
        BlockHeader blockHeader = new BlockHeader();
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        Iterator<PunishLogPo> yellowIterator = yellowPunishMap.get(chainId).iterator();
        while (yellowIterator.hasNext()){
            PunishLogPo po = yellowIterator.next();
            if (po.getRoundIndex() < roundData.getRoundIndex() - ConsensusConstant.INIT_PUNISH_OF_ROUND_COUNT) {
                yellowIterator.remove();
            }
        }
    }

    /**
     * 初始化节点信息
     * Initialize node information
     *
     * @param chainId  链ID
     * */
    public void loadAgents(int chainId) throws Exception{
        List<Agent> allAgentList = new ArrayList<>();
        List<AgentPo> poList = this.agentStorageService.getList(chainId);
        for (AgentPo po : poList) {
            Agent agent = PoConvertUtil.poToAgent(po);
            allAgentList.add(agent);
        }
        Collections.sort(allAgentList, new AgentComparator());
        allAgentMap.put(chainId,allAgentList);
    }

    /**
     * 初始化委托信息
     * Initialize delegation information
     *
     * @param chainId  链ID
     * */
    public void loadDeposits(int chainId) throws Exception{
        List<Deposit> allDepositList = new ArrayList<>();
        List<DepositPo> poList = depositStorageService.getList(chainId);
        for (DepositPo po : poList) {
            Deposit deposit = PoConvertUtil.poToDeposit(po);
            allDepositList.add(deposit);
        }
        Collections.sort(allDepositList, new DepositComparator());
        allDepositMap.put(chainId,allDepositList);
    }

    /**
     * 加载所有的红牌信息和指定轮次的黄牌信息
     * Load all red card information and yellow card information for specified rounds
     *
     * @param chainId  链ID
     * */
    public void loadPunishes(int chainId) throws Exception{
        List<PunishLogPo> punishLogList= punishStorageService.getPunishList(chainId);
        List<PunishLogPo> yellowPunishList = new ArrayList<>();
        List<PunishLogPo> redPunishList = new ArrayList<>();
        /**
         * todo
         * 从网络模块获取最新区块
         * */
        BlockHeader blockHeader = new BlockHeader();
        if (null == blockHeader) {
            return;
        }
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        long breakRoundIndex = roundData.getRoundIndex() - ConsensusConstant.INIT_PUNISH_OF_ROUND_COUNT;
        for (PunishLogPo po : punishLogList){
            if(po.getType() == PunishType.RED.getCode()){
                redPunishList.add(po);
            }else{
                if(po.getRoundIndex() <= breakRoundIndex){
                    continue;
                }
                yellowPunishList.add(po);
            }
        }
        Collections.sort(yellowPunishList, new PunishLogComparator());
        Collections.sort(redPunishList, new PunishLogComparator());
        yellowPunishMap.put(chainId,yellowPunishList);
        redPunishMap.put(chainId,redPunishList);
    }

    /**
     * 添加或修改指定链节点
     * Adding or modifying specified chain nodes
     *
     * @param chainId 链ID
     * @param agent    节点信息
     * */
    public void addAgent(int chainId,Agent agent) throws Exception{
        removeAgent(chainId,agent.getTxHash());
        allAgentMap.get(chainId).add(agent);
    }

    /**
     * 删除指定链节点
     * Delete the specified link node
     *
     * @param chainId 链ID
     * @param txHash   创建该节点交易的ID
     * */
    public void removeAgent(int chainId, NulsDigestData txHash)throws Exception {
        List<Agent> agentList = allAgentMap.get(chainId);
        if(agentList == null || agentList.size() == 0){
            return;
        }
        for (Agent agent:agentList) {
            //todo
            if(Arrays.equals(txHash.serialize(),agent.getTxHash().serialize())){
                agentList.remove(agent);
                return;
            }
        }
    }

    public void addDeposit(int chainId,Deposit deposit) throws  Exception{
        removeAgent(chainId,deposit.getTxHash());
        allDepositMap.get(chainId).add(deposit);
    }

    /**
     * 删除指定链的委托信息
     * Delete delegate information for a specified chain
     *
     * @param chainId 链ID
     * @param txHash   创建该委托交易的ID
     * */
    public void removeDeposit(int chainId,NulsDigestData txHash) throws Exception{
        List<Deposit> depositList = allDepositMap.get(chainId);
        if(depositList == null || depositList.size() == 0){
            loadDeposits(chainId);
            return;
        }
        for (Deposit deposit:depositList) {
            if(Arrays.equals(txHash.serialize(),deposit.getTxHash().serialize())){
                depositList.remove(deposit);
                return;
            }
        }
    }

    public Map<Integer, Boolean> getPackingStatus() {
        return packingStatus;
    }

    public void setPackingStatus(Map<Integer, Boolean> packingStatus) {
        this.packingStatus = packingStatus;
    }

    public Map<Integer, List<Agent>> getAllAgentMap() {
        return allAgentMap;
    }

    public void setAllAgentMap(Map<Integer, List<Agent>> allAgentMap) {
        this.allAgentMap = allAgentMap;
    }

    public Map<Integer, List<Deposit>> getAllDepositMap() {
        return allDepositMap;
    }

    public void setAllDepositMap(Map<Integer, List<Deposit>> allDepositMap) {
        this.allDepositMap = allDepositMap;
    }

    public Map<Integer, List<PunishLogPo>> getYellowPunishMap() {
        return yellowPunishMap;
    }

    public void setYellowPunishMap(Map<Integer, List<PunishLogPo>> yellowPunishMap) {
        this.yellowPunishMap = yellowPunishMap;
    }

    public Map<Integer, List<PunishLogPo>> getRedPunishMap() {
        return redPunishMap;
    }

    public void setRedPunishMap(Map<Integer, List<PunishLogPo>> redPunishMap) {
        this.redPunishMap = redPunishMap;
    }

    public Map<Integer, ConsensusStatus> getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(Map<Integer, ConsensusStatus> agentStatus) {
        this.agentStatus = agentStatus;
    }
}
