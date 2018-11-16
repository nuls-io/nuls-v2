package io.nuls.poc.utils.manager;

import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.consensus.PunishType;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.tools.data.DoubleUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author tag
 * 2018/11/14
 * */
public class RoundManager {
    public final Lock ROUND_LOCK = new ReentrantLock();
    /**
     * 各条链的轮次列表
     * */
    private Map<Integer,List<MeetingRound>> chainRoundMap = new HashMap<>();

    /**
     * 控制该类为单例模式
     * */
    public static RoundManager instance = null;
    private RoundManager() { }
    private static Integer LOCK = 0;
    public static RoundManager getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new RoundManager();
            }
            return instance;
        }
    }

    /**
     * 添加轮次信息到轮次列表中
     * @param chain_id       链ID
     * @param meetingRound   需添加的轮次信息
     * */
    public void addRound(int chain_id,MeetingRound meetingRound) {
        List<MeetingRound> roundList = chainRoundMap.get(chain_id);
        if(roundList == null){
            roundList = new ArrayList<>();
        }
        roundList.add(meetingRound);
        chainRoundMap.put(chain_id,roundList);
    }

    /**
     * 清理指定链的轮次信息
     * @param chain_id  链id
     * @param count     保留几轮轮次信息
     * */
    public boolean clearRound(int chain_id,int count) {
        List<MeetingRound> roundList = chainRoundMap.get(chain_id);
        if (roundList.size() > count) {
            roundList = roundList.subList(roundList.size() - count, roundList.size());
            MeetingRound round = roundList.get(0);
            round.setPreRound(null);
        }
        chainRoundMap.put(chain_id,roundList);
        return true;
    }

    /**
     * 获取指定下标的轮次信息
     * @param chain_id     链ID
     * @param roundIndex   轮次下标
     * */
    public MeetingRound getRoundByIndex(int chain_id,long roundIndex) {
        List<MeetingRound> roundList = chainRoundMap.get(chain_id);
        MeetingRound round = null;
        for (int i = roundList.size() - 1; i >= 0; i--) {
            round = roundList.get(i);
            if (round.getIndex() == roundIndex) {
                break;
            }
        }
        return round;
    }

    /**
     * 检查是否需要重置轮次
     * @param chain_id 链ID
     * */
    public void checkIsNeedReset(int chain_id) {
        List<MeetingRound> roundList = chainRoundMap.get(chain_id);
        if(roundList == null || roundList.size() == 0){
            //初始化轮次信息
            initRound(chain_id);
        }else{
            //本地计算的最新轮次
            MeetingRound lastRound = roundList.get(roundList.size() - 1);
            /**
             * todo
             * 调用区块管理模块获取最新区块头
             */
            BlockHeader blockHeader = new BlockHeader();
            BlockExtendsData blockRoundData = new BlockExtendsData(blockHeader.getExtend());
            if(blockRoundData.getRoundIndex() < lastRound.getIndex()){
                roundList.clear();
                //重新初始化轮次信息
                initRound(chain_id);
            }
        }
    }

    /**
     * 获取本地最新轮次信息
     * @param chain_id 链ID
     * */
    public MeetingRound getCurrentRound(int chain_id) {
        ROUND_LOCK.lock();
        List<MeetingRound> roundList = chainRoundMap.get(chain_id);
        try {
            if (roundList == null || roundList.size() == 0) {
                return null;
            }
            MeetingRound round = roundList.get(roundList.size() - 1);
            if (round.getPreRound() == null && roundList.size() >= 2) {
                round.setPreRound(roundList.get(roundList.size() - 2));
            }
            return round;
        } finally {
            ROUND_LOCK.unlock();
        }
    }

    /**
     * 初始化轮次信息（重新计算轮次信息）
     * @param chain_id 链ID
     * */
    public MeetingRound initRound(int chain_id) {
        MeetingRound currentRound = resetRound(chain_id,false);
        //如果当前没有设置它的上一轮次，则找到它的上一轮的轮次并设置
        if (currentRound.getPreRound() == null) {
            /**
             * todo
             * 从区块管理模块获取上一轮次最后一个区块的区块头
             */
            BlockHeader blockHeader = new BlockHeader();
            BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
            /*List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
            for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
                BlockHeader blockHeader = blockHeaderList.get(i);
                extendsData = new BlockExtendsData(blockHeader.getExtend());
                if (extendsData.getRoundIndex() < currentRound.getIndex()) {
                    break;
                }
            }*/
            MeetingRound preRound = getNextRound(chain_id,extendsData,false);
            currentRound.setPreRound(preRound);
        }
        return currentRound;
    }

    /**
     * 获取或重置当前轮次
     * */
    public MeetingRound getOrResetCurrentRound(int chain_id,boolean isRealTime){
        return  resetRound(chain_id,isRealTime);
    }

    /**
     * 重设轮次信息
     * @param chain_id   链ID
     * @param isRealTime 是否根据最新时间计算轮次
     * */
    public MeetingRound resetRound(int chain_id,boolean isRealTime) {
        ROUND_LOCK.lock();
        try {
            MeetingRound round = getCurrentRound(chain_id);
            if (isRealTime) {
                //如果本地最新轮次为空或本地最新轮次打包结束时间小于当前时间则需要计算下一轮次信息
                if (round == null || round.getEndTime() < TimeService.currentTimeMillis()) {
                    MeetingRound nextRound = getNextRound(chain_id,null, true);
                    nextRound.setPreRound(round);
                    addRound(chain_id,nextRound);
                    round = nextRound;
                }
                return round;
            }

            /**
             * todo
             * 区块管理模块获取最新区块头
             * */
            BlockHeader blockHeader = new BlockHeader();
            BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
            //如果本地最新轮次信息不为空&&本地最新轮次与最新区块轮次相等&&最新区块不是本轮次最后一个打包区块则直接返回本地最新轮次
            if (round != null && extendsData.getRoundIndex() == round.getIndex() && extendsData.getPackingIndexOfRound() != extendsData.getConsensusMemberCount()) {
                return round;
            }
            MeetingRound nextRound = getNextRound(chain_id,extendsData, false);
            //如果当前轮次不为空且计算出的下一轮次下标小于当前轮次下标则直接返回计算出的下一轮次信息
            if (round != null && nextRound.getIndex() <= round.getIndex()) {
                return nextRound;
            }
            nextRound.setPreRound(round);
            addRound(chain_id,nextRound);
            return nextRound;
        } finally {
            ROUND_LOCK.unlock();
        }
    }

    /**
     * 获取下一轮的轮次对象
     * @param chain_id    链ID
     * @param roundData   轮次数据
     * @param isRealTime  是否根据最新时间计算轮次
     * */
    public MeetingRound getNextRound(int chain_id,BlockExtendsData roundData, boolean isRealTime) {
        ROUND_LOCK.lock();
        try {
            if (isRealTime && roundData == null) {
                return getNextRoundByRealTime(chain_id);
            } else if (!isRealTime && roundData == null) {
                return getNextRoundByNotRealTime(chain_id);
            } else {
                return getNextRoundByExpectedRound(chain_id,roundData);
            }
        } finally {
            ROUND_LOCK.unlock();
        }
    }

    /**
     * 根据时间计算下一轮次信息
     * @param chain_id  链ID
     * */
    private MeetingRound getNextRoundByRealTime(int chain_id) {
        /**
         * todo
         * 区块管理模块获取最新区块头
         * */
        //BlockHeader bestBlockHeader = chain.getEndBlockHeader();
        BlockHeader bestBlockHeader = new BlockHeader();
        BlockHeader startBlockHeader = bestBlockHeader;
        BlockExtendsData bestRoundData = new BlockExtendsData(bestBlockHeader.getExtend());
        long bestRoundEndTime = bestRoundData.getRoundEndTime(ConfigManager.config_map.get(chain_id).getPacking_interval());
        if (startBlockHeader.getHeight() != 0L) {
            long roundIndex = bestRoundData.getRoundIndex();
            //最新区块打包轮次结束
            if (bestRoundData.getConsensusMemberCount() == bestRoundData.getPackingIndexOfRound() || TimeService.currentTimeMillis() >= bestRoundEndTime) {
                roundIndex += 1;
            }
            startBlockHeader = getFirstBlockOfPreRound(roundIndex);
        }
        long nowTime = TimeService.currentTimeMillis();
        long index = 0L;
        long startTime = 0L;
        long packing_interval = ConfigManager.config_map.get(chain_id).getPacking_interval();
        if (nowTime < bestRoundEndTime) {
            index = bestRoundData.getRoundIndex();
            startTime = bestRoundData.getRoundStartTime();
        } else {
            long diffTime = nowTime - bestRoundEndTime;
            int diffRoundCount = (int) (diffTime / (bestRoundData.getConsensusMemberCount() * packing_interval));
            index = bestRoundData.getRoundIndex() + diffRoundCount + 1;
            startTime = bestRoundEndTime + diffRoundCount * bestRoundData.getConsensusMemberCount() * packing_interval;
        }
        return calculationRound(chain_id,startBlockHeader, index, startTime);
    }

    /**
     * 根据最新区块数据计算下一轮轮次信息
     * @param chain_id   链ID
     * */
    private MeetingRound getNextRoundByNotRealTime(int chain_id) {
        /**
         * todo
         * 区块管理模块获取最新区块头
         * */
        //BlockHeader bestBlockHeader = chain.getEndBlockHeader();
        BlockHeader bestBlockHeader = new BlockHeader();
        BlockExtendsData extendsData = new BlockExtendsData(bestBlockHeader.getExtend());
        extendsData.setRoundStartTime(extendsData.getRoundEndTime(ConfigManager.config_map.get(chain_id).getPacking_interval()));
        extendsData.setRoundIndex(extendsData.getRoundIndex() + 1);
        return getNextRoundByExpectedRound(chain_id,extendsData);
    }

    /**
     * 根据最新区块数据计算下一轮轮次信息
     * @param chain_id   链ID
     * @param roundData  区块里的轮次信息
     * */
    private MeetingRound getNextRoundByExpectedRound(int chain_id,BlockExtendsData roundData) {
        /**
         * todo
         * 区块管理模块获取最新区块头
         * */
        //BlockHeader startBlockHeader = chain.getEndBlockHeader();
        BlockHeader startBlockHeader = new BlockHeader();
        long roundIndex = roundData.getRoundIndex();
        long roundStartTime = roundData.getRoundStartTime();
        if (startBlockHeader.getHeight() != 0L) {
            startBlockHeader = getFirstBlockOfPreRound(roundIndex);
        }
        return calculationRound(chain_id,startBlockHeader, roundIndex, roundStartTime);
    }

    /**
     * todo
     * 获取指定轮次前一轮打包的第一个区块
     * (该接口应该放到区块管理模块)
     * @param roundIndex  轮次下标
     * */
    private BlockHeader getFirstBlockOfPreRound(long roundIndex) {
        BlockHeader firstBlockHeader = null;
        long startRoundIndex = 0L;
        /**
         * todo
         * */
        //List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        List<BlockHeader> blockHeaderList = new ArrayList<>();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            long currentRoundIndex = new BlockExtendsData(blockHeader.getExtend()).getRoundIndex();
            if (roundIndex > currentRoundIndex) {
                if (startRoundIndex == 0L) {
                    startRoundIndex = currentRoundIndex;
                }
                if (currentRoundIndex < startRoundIndex) {
                    firstBlockHeader = blockHeaderList.get(i + 1);
                    BlockExtendsData roundData = new BlockExtendsData(firstBlockHeader.getExtend());
                    if (roundData.getPackingIndexOfRound() > 1) {
                        firstBlockHeader = blockHeader;
                    }
                    break;
                }
            }
        }
        if (firstBlockHeader == null) {
            /**
             * todo
             * */
            //firstBlockHeader = chain.getStartBlockHeader();
            firstBlockHeader = new BlockHeader();
            Log.warn("the first block of pre round not found");
        }
        return firstBlockHeader;
    }

    /**
     * 计算轮次信息
     * @param chain_id           链ID
     * @param startBlockHeader   上一轮次的起始区块
     * @param index              轮次下标
     * @param startTime          轮次开始打包时间
     * */
    private MeetingRound calculationRound(int chain_id,BlockHeader startBlockHeader, long index, long startTime) {
        MeetingRound round = new MeetingRound();
        round.setIndex(index);
        round.setStartTime(startTime);
        setMemberList(chain_id,round, startBlockHeader);
        /**
         * todo
         * 调用账户管理模块获取本地非加密账户地址列表
         * */
        round.calcLocalPacker(new ArrayList<>());
        Log.debug("\ncalculation||index:{},startTime:{},startHeight:{},hash:{}\n" + round.toString() + "\n\n", index, startTime, startBlockHeader.getHeight(), startBlockHeader.getHash());
        return round;
    }

    /**
     * 设置轮次中打包节点信息
     * @param chain_id             链ID
     * @param round                轮次信息
     * @param startBlockHeader     上一轮次的起始区块
     * */
    private void setMemberList(int chain_id,MeetingRound round, BlockHeader startBlockHeader) {
        List<MeetingMember> memberList = new ArrayList<>();
        String seedNodesStr = ConfigManager.config_map.get(chain_id).getSeedNodes();
        String[] seedNodes = seedNodesStr.split(",");
        for (String address : seedNodes) {
            byte[] address_byte = address.getBytes();
            MeetingMember member = new MeetingMember();
            Agent agent = new Agent();
            agent.setAgentAddress(address_byte);
            agent.setPackingAddress(address_byte);
            agent.setRewardAddress(address_byte);
            agent.setCreditVal(0);
            member.setRoundStartTime(round.getStartTime());
            member.setAgent(agent);
            memberList.add(member);
        }
        List<Agent> agentList = getAliveAgentList(chain_id,startBlockHeader.getHeight());
        for (Agent agent : agentList) {
            MeetingMember member = new MeetingMember();
            member.setRoundStartTime(round.getStartTime());
            //获取节点委托信息，用于计算节点总的委托金额
            List<Deposit> cdlist = getDepositListByAgentId(chain_id,agent.getTxHash(), startBlockHeader.getHeight());
            for (Deposit dtx : cdlist) {
                agent.setTotalDeposit(agent.getTotalDeposit().add(dtx.getDeposit()));
            }
            member.setDepositList(cdlist);
            boolean isItIn = agent.getTotalDeposit().isGreaterOrEquals(ConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT);
            if (isItIn) {
                agent.setCreditVal(calcCreditVal(chain_id,member, startBlockHeader));
                member.setAgent(agent);
                memberList.add(member);
            }
        }
        round.init(memberList,chain_id);
    }

    /**
     * 获取节点的委托信息
     * @param chain_id                链ID
     * @param agentHash               节点ID
     * @param startBlockHeight        上一轮次的起始区块
     * */
    private List<Deposit> getDepositListByAgentId(int chain_id,NulsDigestData agentHash, long startBlockHeight) {
        List<Deposit> depositList = ConsensusManager.getInstance().getAllDepositMap().get(chain_id);
        List<Deposit> resultList = new ArrayList<>();
        for (int i = depositList.size() - 1; i >= 0; i--) {
            Deposit deposit = depositList.get(i);
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agentHash)) {
                continue;
            }
            resultList.add(deposit);
        }
        return resultList;
    }

    /**
     * 获取网络中有效的节点列表
     * @param chain_id           链ID
     * @param startBlockHeight   上一轮次的起始区块
     * */
    private List<Agent> getAliveAgentList(int chain_id,long startBlockHeight) {
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
        List<Agent> resultList = new ArrayList<>();
        for (int i = agentList.size() - 1; i >= 0; i--) {
            Agent agent = agentList.get(i);
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            resultList.add(agent);
        }
        return resultList;
    }


    /**
     * 计算节点的信誉值
     * @param chain_id       链ID
     * @param member         打包成员对象
     * @param blockHeader    区块头
     * */
    private double calcCreditVal(int chain_id,MeetingMember member, BlockHeader blockHeader) {
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        long roundStart = roundData.getRoundIndex() - ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long blockCount = getBlockCountByAddress(chain_id,member.getAgent().getPackingAddress(), roundStart, roundData.getRoundIndex() - 1);
        long sumRoundVal = getPunishCountByAddress(chain_id,member.getAgent().getAgentAddress(), roundStart, roundData.getRoundIndex() - 1, PunishType.YELLOW.getCode());
        //能力系数计算
        double ability = DoubleUtils.div(blockCount, ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);
        //惩罚系数计算
        double penalty = DoubleUtils.div(DoubleUtils.mul(ConsensusConstant.CREDIT_MAGIC_NUM, sumRoundVal),
                DoubleUtils.mul(ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT, ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT));

        return DoubleUtils.round(DoubleUtils.sub(ability, penalty), 4);
    }

    /**
     * 获取指定地址获得的红黄牌惩罚数量
     * @param chain_id    链ID
     * @param address     地址
     * @param roundStart  起始轮次
     * @param roundEnd    结束轮次
     * @param code        红黄牌标识
     * */
    private long getPunishCountByAddress(int chain_id,byte[] address, long roundStart, long roundEnd, int code) {
        long count = 0;
        List<PunishLogPo> punishList = ConsensusManager.getInstance().getYellowPunishMap().get(chain_id);
        if (code == PunishType.RED.getCode()) {
            punishList = ConsensusManager.getInstance().getRedPunishMap().get(chain_id);
        }
        for (int i = punishList.size() - 1; i >= 0; i--) {
            if(count>=100){
                break;
            }
            PunishLogPo punish = punishList.get(i);

            if (punish.getRoundIndex() > roundEnd) {
                continue;
            }
            if (punish.getRoundIndex() < roundStart) {
                break;
            }
            if (Arrays.equals(punish.getAddress(), address)) {
                count++;
            }
        }
        //每一轮的惩罚都有可能包含上一轮次的惩罚记录，即计算从a到a+99轮的惩罚记录时，a轮的惩罚中可能是惩罚某个地址在a-1轮未出块，导致100轮最多可能有101个惩罚记录，在这里处理下
        //Each round of punishment is likely to contain a rounds punishment record, calculated from a to a + 99 rounds of punishment record,
        // a round of punishment is likely to be punished in an address in a - 1 round not out of the blocks,
        // lead to round up to 100 May be 101 punishment record, treatment here
        if (count > 100) {
            return 100;
        }
        return count;
    }

    /**
     * todo
     * 该接口应该放到区块管理模块
     * 获取地址出块数量
     * @param chain_id         链ID
     * @param packingAddress   出块地址
     * @param roundStart       起始轮次
     * @param roundEnd         结束轮次
     * */
    private long getBlockCountByAddress(int chain_id,byte[] packingAddress, long roundStart, long roundEnd) {
        long count = 0;
        //List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        List<BlockHeader> blockHeaderList = new ArrayList<>();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
            if (roundData.getRoundIndex() > roundEnd) {
                continue;
            }
            if (roundData.getRoundIndex() < roundStart) {
                break;
            }
            if (Arrays.equals(blockHeader.getPackingAddress(), packingAddress)) {
                count++;
            }
        }
        return count;
    }

    public List<MeetingRound> getRoundList(int chain_id) {
        return chainRoundMap.get(chain_id);
    }
}
