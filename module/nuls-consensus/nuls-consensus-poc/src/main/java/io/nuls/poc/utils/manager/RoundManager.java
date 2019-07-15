package io.nuls.poc.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.utils.enumeration.PunishType;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 轮次信息管理类
 * Round Information Management Class
 *
 * @author tag
 * 2018/11/14
 */
@Component
public class RoundManager {
    /**
     * 添加轮次信息到轮次列表中
     * Add Round Information to Round List
     *
     * @param chain        chain info
     * @param meetingRound 需添加的轮次信息/round info
     */
    public void addRound(Chain chain, MeetingRound meetingRound) {
        List<MeetingRound> roundList = chain.getRoundList();
        if (roundList == null) {
            roundList = new ArrayList<>();
        }
        if (roundList.size() > 0 && meetingRound.getPreRound() == null) {
            meetingRound.setPreRound(roundList.get(roundList.size() - 1));
        }
        roundList.add(meetingRound);
        if (roundList.size() > ConsensusConstant.ROUND_CACHE_COUNT) {
            roundList.remove(0);
        }
    }

    /**
     * 回滚本地轮次到指定轮次
     *
     * @param roundIndex 回滚到指定轮次
     * @param chain      链信息
     */
    public void rollBackRound(Chain chain, long roundIndex) {
        List<MeetingRound> roundList = chain.getRoundList();
        for (int index = roundList.size() - 1; index >= 0; index--) {
            if (roundList.get(index).getIndex() > roundIndex) {
                roundList.remove(index);
            } else {
                break;
            }
        }
    }

    /**
     * 清理指定链的轮次信息
     * Clean up the wheel number information of the specified chain
     *
     * @param chain chain info
     * @param count 保留几轮轮次信息/Keep several rounds of information
     * @return boolean
     */
    public boolean clearRound(Chain chain, int count) {
        List<MeetingRound> roundList = chain.getRoundList();
        if (roundList.size() > count) {
            roundList = roundList.subList(roundList.size() - count, roundList.size());
            MeetingRound round = roundList.get(0);
            round.setPreRound(null);
        }
        return true;
    }

    /**
     * 清理比指定轮次之后的轮次信息
     * Clean up the wheel number information of the specified chain
     *
     * @param chain chain info
     * @param roundIndex 保留几轮轮次信息/Keep several rounds of information
     * @return boolean
     */
    public boolean clearRound(Chain chain, long roundIndex) {
        List<MeetingRound> roundList = chain.getRoundList();
        MeetingRound round;
        for (int i = roundList.size() - 1; i >= 0; i--) {
            round = roundList.get(i);
            if (round.getIndex() > roundIndex) {
                roundList.remove(i);
            } else{
                break;
            }
        }
        return true;
    }

    /**
     * 获取指定下标的轮次信息
     * Get round information for specified Subscripts
     *
     * @param chain      chain info
     * @param roundIndex 轮次下标/round index
     * @return MeetingRound
     */
    public MeetingRound getRoundByIndex(Chain chain, long roundIndex) {
        List<MeetingRound> roundList = chain.getRoundList();
        MeetingRound round;
        for (int i = roundList.size() - 1; i >= 0; i--) {
            round = roundList.get(i);
            if (round.getIndex() == roundIndex) {
                return round;
            } else if (round.getIndex() < roundIndex) {
                break;
            }
        }
        return null;
    }

    /**
     * 检查是否需要重置轮次
     * Check if you need to reset rounds
     *
     * @param chain chain info
     */
    public void checkIsNeedReset(Chain chain) throws Exception {
        /*
        1.如果本地不存在轮次信息,则初始化本地轮次信息
        2.如果存在且，本地最新区块轮次小于本地计算的最新轮次，则重置本地轮次信息
        1. Initialize local round information if there is no local round information
        2. If there is and the latest local block rounds are less than the latest local rounds, the local rounds information will be reset.
        */

        List<MeetingRound> roundList = chain.getRoundList();
        if (roundList == null || roundList.size() == 0) {
            initRound(chain);
        } else {
            chain.getRoundLock().lock();
            try {
                MeetingRound lastRound = roundList.get(roundList.size() - 1);
                BlockHeader blockHeader = chain.getNewestHeader();
                BlockExtendsData blockRoundData = new BlockExtendsData(blockHeader.getExtend());
                if (blockRoundData.getRoundIndex() < lastRound.getIndex()) {
                    roundList.clear();
                    initRound(chain);
                }
            } finally {
                chain.getRoundLock().unlock();
            }
        }
    }

    /**
     * 获取本地最新轮次信息
     * Get the latest local rounds
     *
     * @param chain chain info
     * @return MeetingRound
     */
    public MeetingRound getCurrentRound(Chain chain) {
        chain.getRoundLock().lock();
        List<MeetingRound> roundList = chain.getRoundList();
        try {
            if (roundList == null || roundList.size() == 0) {
                return null;
            }
            return roundList.get(roundList.size() - 1);
        } finally {
            chain.getRoundLock().unlock();
        }
    }

    /**
     * 初始化轮次信息（重新计算轮次信息）
     * Initialize Round Information (recalculate Round Information)
     *
     * @param chain chain info
     */
    public void initRound(Chain chain) throws Exception {
        //resetRound(chain,false);
        MeetingRound currentRound = resetRound(chain, false);
        /*
        如果当前没有设置它的上一轮次，则找到它的上一轮的轮次并设置
        If the previous round is not currently set, find the previous round and set it.
        */
        if (currentRound.getPreRound() == null) {
            BlockHeader newestHeader = chain.getNewestHeader();
            BlockExtendsData extendsData = new BlockExtendsData(newestHeader.getExtend());
            List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
            for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
                BlockHeader blockHeader = blockHeaderList.get(i);
                extendsData = new BlockExtendsData(blockHeader.getExtend());
                if (extendsData.getRoundIndex() < currentRound.getIndex()) {
                    break;
                }
            }
            MeetingRound preRound = getRound(chain, extendsData, false);
            currentRound.setPreRound(preRound);
        }
    }

    /**
     * 重设最新轮次信息
     * Reset the latest round information
     *
     * @param chain      chain info
     * @param isRealTime 是否根据最新时间计算轮次/Whether to calculate rounds based on current time
     * @return MeetingRound
     */
    public MeetingRound resetRound(Chain chain, boolean isRealTime) throws Exception {
        chain.getRoundLock().lock();
        try {
            MeetingRound round = getCurrentRound(chain);
            if (isRealTime) {
                /*
                如果本地最新轮次为空或本地最新轮次打包结束时间小于当前时间则需要计算下一轮次信息
                If the local latest round is empty or the local latest round is packaged less than the current time,
                the next round of information needs to be calculated.
                */
                if (round == null || round.getEndTime() < NulsDateUtils.getCurrentTimeSeconds()) {
                    MeetingRound nextRound = getRound(chain, null, true);
                    nextRound.setPreRound(round);
                    addRound(chain, nextRound);
                    round = nextRound;
                }
                return round;
            }

            BlockHeader blockHeader = chain.getNewestHeader();
            BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());

            /*
            如果本地最新轮次信息不为空&&本地最新轮次与最新区块轮次相等&&最新区块不是本轮次最后一个打包区块则直接返回本地最新轮次
            If the latest local rounds are not empty & the latest local rounds are equal to the latest block rounds & if the latest block is not the last packaged block in this round,
             the latest local rounds will be returned directly to the latest local rounds
            */
            if (round != null && extendsData.getRoundIndex() == round.getIndex() && extendsData.getPackingIndexOfRound() != extendsData.getConsensusMemberCount()) {
                return round;
            }
            MeetingRound nextRound = getRound(chain, extendsData, false);
            /*
            如果当前轮次不为空且计算出的下一轮次下标小于当前轮次下标则直接返回计算出的下一轮次信息
            If the current round is not empty and the calculated next round subscript is less than the current round subscript,
            the calculated next round information will be returned directly.
            */
            if (round != null && nextRound.getIndex() <= round.getIndex()) {
                return nextRound;
            }
            nextRound.setPreRound(round);
            addRound(chain, nextRound);
            return nextRound;
        } finally {
            chain.getRoundLock().unlock();
        }
    }

    /**
     * 获取下一轮的轮次信息
     * Get the next round of round objects
     *
     * @param chain      chain info
     * @param roundData  轮次数据/block extends entity
     * @param isRealTime 是否根据最新时间计算轮次/Whether to calculate rounds based on current time
     * @return MeetingRound
     */
    public MeetingRound getRound(Chain chain, BlockExtendsData roundData, boolean isRealTime) throws Exception {
        chain.getRoundLock().lock();
        try {
            if (isRealTime && roundData == null) {
                return getRoundByRealTime(chain);
            } else if (!isRealTime && roundData == null) {
                return getRoundByNewestBlock(chain);
            } else {
                return getRoundByExpectedRound(chain, roundData);
            }
        } finally {
            chain.getRoundLock().unlock();
        }
    }

    public MeetingRound getRoundByTime(Chain chain, long time) throws Exception {
        int blockHeaderSize = chain.getBlockHeaderList().size();
        for (int index = blockHeaderSize - 1; index >= 0; index--) {
            BlockHeader blockHeader = chain.getBlockHeaderList().get(index);
            if (blockHeader.getTime() <= time) {
                BlockExtendsData blockExtendsData = new BlockExtendsData();
                blockExtendsData.parse(blockHeader.getExtend(), 0);
                long roundStartTime = blockExtendsData.getRoundStartTime();
                long roundEndTime = roundStartTime + chain.getConfig().getPackingInterval() * blockExtendsData.getConsensusMemberCount();
                if (roundStartTime <= time) {
                    if (roundEndTime >= time) {
                        return getRound(chain, blockExtendsData, false);
                    } else {
                        int realIndex = index + 1;
                        while (realIndex <= blockHeaderSize - 1) {
                            blockExtendsData.parse(chain.getBlockHeaderList().get(realIndex).getExtend(), 0);
                            roundStartTime = blockExtendsData.getRoundStartTime();
                            roundEndTime = roundStartTime + chain.getConfig().getPackingInterval() * blockExtendsData.getConsensusMemberCount();
                            if (roundStartTime > time) {
                                return null;
                            }
                            if (roundEndTime >= time) {
                                return getRound(chain, blockExtendsData, false);
                            }
                            realIndex++;
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }


    /**
     * 根据时间计算下一轮次信息
     * Calculate the next round of information based on time
     *
     * @param chain chain info
     * @return MeetingRound
     */
    private MeetingRound getRoundByRealTime(Chain chain) throws Exception {
        BlockHeader bestBlockHeader = chain.getNewestHeader();
        BlockHeader startBlockHeader = bestBlockHeader;
        BlockExtendsData bestRoundData = new BlockExtendsData(bestBlockHeader.getExtend());
        long bestRoundEndTime = bestRoundData.getRoundEndTime(chain.getConfig().getPackingInterval());
        if (startBlockHeader.getHeight() != 0L) {
            long roundIndex = bestRoundData.getRoundIndex();
            /*
            本地最新区块所在轮次已经打包结束，则轮次下标需要加1,则需找到本地最新区块轮次中出的第一个块来计算下一轮的轮次信息
            If the latest block in this area has been packaged, the subscription of the round will need to be added 1.
            */
            if (bestRoundData.getConsensusMemberCount() == bestRoundData.getPackingIndexOfRound() || NulsDateUtils.getCurrentTimeSeconds() >= bestRoundEndTime) {
                roundIndex += 1;
            }
            startBlockHeader = getFirstBlockOfPreRound(chain, roundIndex);
        }
        long nowTime = NulsDateUtils.getCurrentTimeSeconds();
        long index;
        long startTime;
        long packingInterval = chain.getConfig().getPackingInterval();
        /*
        找到需计算的轮次下标及轮次开始时间,如果当前时间<本地最新区块时间，则表示需计算轮次就是本地最新区块轮次
        Find the rounds subscripts to be calculated and the start time of rounds
        */
        if (nowTime < bestRoundEndTime) {
            index = bestRoundData.getRoundIndex();
            startTime = bestRoundData.getRoundStartTime();
        } else {
            long diffTime = nowTime - bestRoundEndTime;
            int consensusMemberCount = bestRoundData.getConsensusMemberCount();
            if (bestBlockHeader.getHeight() == 0) {
                consensusMemberCount = chain.getConfig().getSeedNodes().split(",").length;
            }
            int diffRoundCount = (int) (diffTime / (consensusMemberCount * packingInterval));
            index = bestRoundData.getRoundIndex() + diffRoundCount + 1;
            startTime = bestRoundEndTime + diffRoundCount * consensusMemberCount * packingInterval;
        }
        return calculationRound(chain, startBlockHeader, index, startTime);
    }

    /**
     * 根据最新区块数据计算下一轮轮次信息
     * Calculate next round information based on the latest block entity
     *
     * @param chain chain info
     * @return MeetingRound
     */
    private MeetingRound getRoundByNewestBlock(Chain chain) throws Exception {
        BlockHeader bestBlockHeader = chain.getNewestHeader();
        BlockExtendsData extendsData = new BlockExtendsData(bestBlockHeader.getExtend());
        extendsData.setRoundStartTime(extendsData.getRoundEndTime(chain.getConfig().getPackingInterval()));
        extendsData.setRoundIndex(extendsData.getRoundIndex() + 1);
        return getRoundByExpectedRound(chain, extendsData);
    }

    /**
     * 根据指定区块数据计算所在轮次信息
     * Calculate next round information based on the latest block entity
     *
     * @param chain     chain info
     * @param roundData 区块里的轮次信息/block extends entity
     * @return MeetingRound
     */
    private MeetingRound getRoundByExpectedRound(Chain chain, BlockExtendsData roundData) throws Exception {
        BlockHeader startBlockHeader = chain.getNewestHeader();
        long roundIndex = roundData.getRoundIndex();
        long roundStartTime = roundData.getRoundStartTime();
        if (startBlockHeader.getHeight() != 0L) {
            startBlockHeader = getFirstBlockOfPreRound(chain, roundIndex);
        }
        return calculationRound(chain, startBlockHeader, roundIndex, roundStartTime);
    }

    public MeetingRound getRoundByRoundIndex(Chain chain, long roundIndex, long roundStartTime) throws Exception {
        BlockHeader startBlockHeader = chain.getNewestHeader();
        if (startBlockHeader.getHeight() != 0L) {
            startBlockHeader = getFirstBlockOfPreRound(chain, roundIndex);
        }
        return calculationRound(chain, startBlockHeader, roundIndex, roundStartTime);
    }

    /**
     * 计算轮次信息
     * Calculate wheel information
     *
     * @param chain            chain info
     * @param startBlockHeader 上一轮次的起始区块/Initial blocks of the last round
     * @param index            轮次下标/round index
     * @param startTime        轮次开始打包时间/start time
     */
    @SuppressWarnings("unchecked")
    private MeetingRound calculationRound(Chain chain, BlockHeader startBlockHeader, long index, long startTime) throws Exception {
        MeetingRound round = new MeetingRound();
        round.setIndex(index);
        round.setStartTime(startTime);
        setMemberList(chain, round, startBlockHeader);
        List<byte[]> packingAddressList = CallMethodUtils.getEncryptedAddressList(chain);
        if (!packingAddressList.isEmpty()) {
            round.calcLocalPacker(packingAddressList, chain);
        }
        chain.getLogger().debug("当前轮次为：" + round.getIndex() + ";当前轮次开始打包时间：" + NulsDateUtils.convertDate(new Date(startTime * 1000)));
        chain.getLogger().debug("\ncalculation||index:{},startTime:{},startHeight:{},hash:{}\n" + round.toString() + "\n\n", index, startTime * 1000, startBlockHeader.getHeight(), startBlockHeader.getHash());
        return round;
    }

    /**
     * 设置轮次中打包节点信息
     * Setting Packing Node Information in Rounds
     *
     * @param chain            chain info
     * @param round            轮次信息/round info
     * @param startBlockHeader 上一轮次的起始区块/Initial blocks of the last round
     */
    private void setMemberList(Chain chain, MeetingRound round, BlockHeader startBlockHeader) throws NulsException {
        List<MeetingMember> memberList = new ArrayList<>();
        String seedNodesStr = chain.getConfig().getSeedNodes();
        String[] seedNodes;
        /*
        种子节点打包信息组装
        Seed node packaging information assembly
        */
        if (StringUtils.isNotBlank(seedNodesStr)) {
            seedNodes = seedNodesStr.split(",");
            for (String address : seedNodes) {
                byte[] addressByte = AddressTool.getAddress(address);
                MeetingMember member = new MeetingMember();
                Agent agent = new Agent();
                agent.setAgentAddress(addressByte);
                agent.setPackingAddress(addressByte);
                agent.setRewardAddress(addressByte);
                agent.setCreditVal(0);
                agent.setDeposit(BigInteger.ZERO);
                member.setRoundStartTime(round.getStartTime());
                member.setAgent(agent);
                member.setRoundIndex(round.getIndex());
                memberList.add(member);
            }
        }
        List<Agent> agentList = getAliveAgentList(chain, startBlockHeader.getHeight());
        for (Agent agent : agentList) {
            Agent realAgent = new Agent();
            try {
                realAgent.parse(agent.serialize(), 0);
                realAgent.setTxHash(agent.getTxHash());
            } catch (IOException io) {
                Log.error(io);
                return;
            }
            MeetingMember member = new MeetingMember();
            member.setRoundStartTime(round.getStartTime());
            /*
            获取节点委托信息，用于计算节点总的委托金额
            Get the node delegation information for calculating the total amount of the node delegation
            */
            List<Deposit> cdList = getDepositListByAgentId(chain, realAgent.getTxHash(), startBlockHeader.getHeight());
            BigInteger totalDeposit = BigInteger.ZERO;
            for (Deposit dtx : cdList) {
                totalDeposit = totalDeposit.add(dtx.getDeposit());
            }
            agent.setTotalDeposit(totalDeposit);
            realAgent.setTotalDeposit(totalDeposit);
            member.setDepositList(cdList);
            member.setRoundIndex(round.getIndex());
            member.setAgent(realAgent);
            /*
            节点总的委托金额是否达到出块节点的最小值
            Does the total delegation amount of the node reach the minimum value of the block node?
            */
            boolean isItIn = realAgent.getTotalDeposit().compareTo(chain.getConfig().getCommissionMin()) >= 0 ? true : false;
            if (isItIn) {
                realAgent.setCreditVal(calcCreditVal(chain, member, startBlockHeader));
                memberList.add(member);
            }
        }
        round.init(memberList, chain);
    }

    /**
     * 获取节点的委托信息
     * Obtaining delegation information of nodes
     *
     * @param chain            chain info
     * @param agentHash        节点ID/agent hash
     * @param startBlockHeight 上一轮次的起始区块高度/Initial blocks of the last round
     * @return List<Deposit>
     */
    private List<Deposit> getDepositListByAgentId(Chain chain, NulsHash agentHash, long startBlockHeight) {
        List<Deposit> depositList = chain.getDepositList();
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
     * Getting a list of valid nodes in the network
     *
     * @param chain            chain info
     * @param startBlockHeight 上一轮次的起始区块高度/Initial blocks of the last round
     * @return List<Agent>
     */
    private List<Agent> getAliveAgentList(Chain chain, long startBlockHeight) {
        List<Agent> agentList = chain.getAgentList();
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
     * Calculating the Node's Credit Value
     *
     * @param chain       chain info
     * @param member      打包成员对象/packing info
     * @param blockHeader 区块头/block header
     * @return double
     */
    private double calcCreditVal(Chain chain, MeetingMember member, BlockHeader blockHeader) throws NulsException {
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        long roundStart = roundData.getRoundIndex() - ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        /*
        信誉值计算是通过限定轮次内节点出块数与黄牌数计算出的
        Credit value is calculated by limiting the number of blocks and yellow cards of nodes in rounds.
        */
        long blockCount = getBlockCountByAddress(chain, member.getAgent().getPackingAddress(), roundStart, roundData.getRoundIndex() - 1);
        long sumRoundVal = getPunishCountByAddress(chain, member.getAgent().getAgentAddress(), roundStart, roundData.getRoundIndex() - 1, PunishType.YELLOW.getCode());
        double ability = DoubleUtils.div(blockCount, ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);
        /*double penalty = DoubleUtils.div(DoubleUtils.mul(ConsensusConstant.CREDIT_MAGIC_NUM, sumRoundVal),
                DoubleUtils.mul(ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT, ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT));*/
        double penalty = DoubleUtils.div(sumRoundVal, ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);

        return DoubleUtils.round(DoubleUtils.sub(ability, penalty), 4);
    }

    /**
     * 获取指定地址获得的红黄牌惩罚数量
     * Get the number of red and yellow card penalties for the specified address
     *
     * @param chain      chain info
     * @param address    地址/address
     * @param roundStart 起始轮次/round start index
     * @param roundEnd   结束轮次/round end index
     * @param code       红黄牌标识/Red and yellow logo
     * @return long
     */
    private long getPunishCountByAddress(Chain chain, byte[] address, long roundStart, long roundEnd, int code) throws NulsException {
        long count = 0;
        List<PunishLogPo> punishList = chain.getYellowPunishList();
        if (code == PunishType.RED.getCode()) {
            punishList = chain.getRedPunishList();
        }
        for (int i = punishList.size() - 1; i >= 0; i--) {
            if (count >= ConsensusConstant.CREDIT_MAGIC_NUM) {
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
        /*
        每一轮的惩罚都有可能包含上一轮次的惩罚记录，即计算从a到a+99轮的惩罚记录时，a轮的惩罚中可能是惩罚某个地址在a-1轮未出块，导致100轮最多可能有101个惩罚记录，在这里处理下
        Each round of punishment is likely to contain a rounds punishment record, calculated from a to a + 99 rounds of punishment record,
        a round of punishment is likely to be punished in an address in a - 1 round not out of the blocks,
        lead to round up to 100 May be 101 punishment record, treatment here
        */
        if (count > ConsensusConstant.CREDIT_MAGIC_NUM) {
            return ConsensusConstant.CREDIT_MAGIC_NUM;
        }
        return count;
    }


    /**
     * 获取指定轮次前一轮打包的第一个区块
     * Gets the first block packaged in the previous round of the specified round
     *
     * @param chain      chain info
     * @param roundIndex 轮次下标
     */
    public BlockHeader getFirstBlockOfPreRound(Chain chain, long roundIndex) {
        BlockHeader firstBlockHeader = null;
        long startRoundIndex = 0L;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
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
            firstBlockHeader = chain.getNewestHeader();
            chain.getLogger().warn("the first block of pre round not found");
        }
        return firstBlockHeader;
    }

    /**
     * 获取地址出块数量
     * Get the number of address blocks
     *
     * @param chain          chain info
     * @param packingAddress 出块地址
     * @param roundStart     起始轮次
     * @param roundEnd       结束轮次
     */
    private long getBlockCountByAddress(Chain chain, byte[] packingAddress, long roundStart, long roundEnd) {
        long count = 0;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
            if (roundData.getRoundIndex() > roundEnd) {
                continue;
            }
            if (roundData.getRoundIndex() < roundStart) {
                break;
            }
            if (Arrays.equals(blockHeader.getPackingAddress(chain.getConfig().getChainId()), packingAddress)) {
                count++;
            }
        }
        return count;
    }


    /**
     * 查询两轮次之间新增的共识节点和注销的共识节点
     * New consensus nodes and unregistered consensus nodes between queries
     *
     * @param chain                  chain
     * @param lastExtendsData        上一轮的轮次信息
     * @param currentExtendsData     本轮轮次信息
     * @return                       两轮次之间节点变化信息
     * */
    public Map<String,List<String>> getAgentChangeInfo(Chain chain, BlockExtendsData lastExtendsData, BlockExtendsData currentExtendsData){
        Map<String, List<String>> resultMap = new HashMap<>(2);
        List<String> registerAgentList;
        List<String> cancelAgentList;
        long lastRoundIndex = -1;
        if(lastExtendsData != null){
            lastRoundIndex = lastExtendsData.getRoundIndex();
        }
        long currentRoundIndex = currentExtendsData.getRoundIndex();
        MeetingRound lastRound = null;
        MeetingRound currentRound;
        try {
            if(lastRoundIndex != -1){
                lastRound = getRoundByIndex(chain, lastRoundIndex);
                if(lastRound == null){
                    lastRound = getRound(chain, lastExtendsData, false);
                }
            }
            currentRound = getRoundByIndex(chain, currentRoundIndex);
            if(currentRound == null){
                currentRound = getRound(chain, currentExtendsData, false);
            }
            registerAgentList = getAgentChangeList(lastRound, currentRound, true);
            cancelAgentList = getAgentChangeList(lastRound, currentRound, false);
        }catch (Exception e){
            chain.getLogger().error(e);
            return null;
        }
        resultMap.put("registerAgentList", registerAgentList);
        resultMap.put("cancelAgentList", cancelAgentList);
        return  resultMap;
    }

    /**
     * 获取两轮次之间新增或减少的节点列表
     * @param lastRound        上一轮
     * @param currentRound     本轮
     * @param isRegister       获取增加节点列表（true）或获取减少的节点列表（false）
     * @return                 节点变化列表
     * */
    private List<String> getAgentChangeList(MeetingRound lastRound, MeetingRound currentRound , boolean isRegister){
        List<String> lastRoundAgentList = new ArrayList<>();
        List<String> currentRoundAgentList  = new ArrayList<>();
        if(lastRound != null){
            for (MeetingMember member:lastRound.getMemberList()) {
                lastRoundAgentList.add(AddressTool.getStringAddressByBytes(member.getAgent().getPackingAddress()));
            }
        }
        for (MeetingMember member:currentRound.getMemberList()) {
            currentRoundAgentList.add(AddressTool.getStringAddressByBytes(member.getAgent().getPackingAddress()));
        }
        if(isRegister){
            currentRoundAgentList.removeAll(lastRoundAgentList);
            return currentRoundAgentList;
        }else{
            lastRoundAgentList.removeAll(currentRoundAgentList);
            return lastRoundAgentList;
        }
    }
}
