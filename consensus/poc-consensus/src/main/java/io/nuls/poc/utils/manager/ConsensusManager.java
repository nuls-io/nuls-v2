package io.nuls.poc.utils.manager;

import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
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
import io.nuls.poc.storage.PunihStorageService;
import io.nuls.poc.utils.PoConvertUtil;
import io.nuls.poc.utils.compare.AgentComparator;
import io.nuls.poc.utils.compare.DepositComparator;
import io.nuls.poc.utils.compare.PunishLogComparator;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;

import java.util.*;

/**
 * 系统启动时加载缓存的处理器
 * The cmd that loads the cache when the system starts.
 * @author  tag
 * 2018/11/14
 * */
public class ConsensusManager {
    private AgentStorageService agentStorageService = SpringLiteContext.getBean(AgentStorageService.class);
    private DepositStorageService depositStorageService = SpringLiteContext.getBean(DepositStorageService.class);
    private PunihStorageService punishStorageService = SpringLiteContext.getBean(PunihStorageService.class);

    /**
     * 节点各条链的状态
     * */
    private Map<Integer, ConsensusStatus> agent_status = new HashMap<>();

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
     * 存放各条链黄牌交易列表
     * */
    private Map<Integer,List<PunishLogPo>> yellowPunishMap = new HashMap<>();

    /**
     * 存放各条链红牌交易列表
     * */
    private Map<Integer,List<PunishLogPo>> redPunishMap = new HashMap<>();

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

    /**
     * 初始化数据
     * */
    public void initData(int chain_id){
        try {
            //从数据库中读取节点信息，共识信息，红黄牌信息存放到对应的map中
            loadAgents(chain_id);
            loadDeposits(chain_id);
            loadPunishes(chain_id);
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 数据黄牌数据
     * */
    public void clear(int chain_id){
        /**
         * todo
         * 从区块管理模块获取最后一个区块的高度,清除200轮之前的红黄牌数据
         * */
        //BlockHeader bestBlockHeader = chain.getEndBlockHeader();
        BlockHeader blockHeader = new BlockHeader();
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        Iterator<PunishLogPo> yellowIterator = yellowPunishMap.get(chain_id).iterator();
        while (yellowIterator.hasNext()){
            PunishLogPo po = yellowIterator.next();
            if (po.getRoundIndex() < roundData.getRoundIndex() - ConsensusConstant.INIT_PUNISH_OF_ROUND_COUNT) {
                yellowIterator.remove();
            }
        }
    }

    /**
     * 初始化节点信息
     * */
    public void loadAgents(int chain_id) throws Exception{
        List<Agent> allAgentList = new ArrayList<>();
        List<Agent> validAgentList = new ArrayList<>();
        List<AgentPo> poList = this.agentStorageService.getList(chain_id);
        for (AgentPo po : poList) {
            Agent agent = PoConvertUtil.poToAgent(po);
            allAgentList.add(agent);
            if(agent.getDelHeight() == -1){
                validAgentList.add(agent);
            }
        }
        Collections.sort(allAgentList, new AgentComparator());
        Collections.sort(validAgentList, new AgentComparator());
        allAgentMap.put(chain_id,allAgentList);
        validAgentMap.put(chain_id,validAgentList);
    }

    /**
     * 初始化委托信息
     * */
    public void loadDeposits(int chain_id) throws Exception{
        List<Deposit> allDepositList = new ArrayList<>();
        List<Deposit> validDepositList = new ArrayList<>();
        List<DepositPo> poList = depositStorageService.getList(chain_id);
        for (DepositPo po : poList) {
            Deposit deposit = PoConvertUtil.poToDeposit(po);
            allDepositList.add(deposit);
            if(deposit.getDelHeight() == -1){
                validDepositList.add(deposit);
            }
        }
        Collections.sort(allDepositList, new DepositComparator());
        Collections.sort(validDepositList, new DepositComparator());
        allDepositMap.put(chain_id,allDepositList);
        validDepositMap.put(chain_id,validDepositList);
    }

    /**
     * 加载所有的红牌信息
     * 加载指定轮次的黄牌信息
     * */
    public void loadPunishes(int chain_id) throws Exception{
        List<PunishLogPo> punishLogList= punishStorageService.getPunishList(chain_id);
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
        yellowPunishMap.put(chain_id,yellowPunishList);
        redPunishMap.put(chain_id,redPunishList);
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

    public Map<Integer, ConsensusStatus> getAgent_status() {
        return agent_status;
    }

    public void setAgent_status(Map<Integer, ConsensusStatus> agent_status) {
        this.agent_status = agent_status;
    }
}
