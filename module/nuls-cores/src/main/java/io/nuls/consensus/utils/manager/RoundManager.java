package io.nuls.consensus.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.round.MeetingMember;
import io.nuls.consensus.model.bo.round.MeetingRound;
import io.nuls.consensus.model.bo.tx.txdata.Agent;
import io.nuls.consensus.model.bo.tx.txdata.Deposit;
import io.nuls.consensus.model.po.PunishLogPo;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.consensus.utils.enumeration.PunishType;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Round information management
 * Round Information Management Class
 *
 * @author tag
 * 2018/11/14
 */
@Component
public class RoundManager {

    @Autowired
    private FixRedPunishBugHelper fixRedPunishBugHelper;

    /**
     * Add round information to the round list
     * Add Round Information to Round List
     *
     * @param chain        chain info
     * @param meetingRound Round information to be added/round info
     */
    public void addRound(Chain chain, MeetingRound meetingRound) {
        List<MeetingRound> roundList = chain.getRoundList();
        if (roundList == null) {
            roundList = new ArrayList<>();
        }else{
            rollBackRound(chain, meetingRound.getIndex()-1);
        }
        if (roundList.size() > 0 && meetingRound.getPreRound() == null) {
            meetingRound.setPreRound(roundList.get(roundList.size() - 1));
        }
        roundList.add(meetingRound);
        if (roundList.size() > ConsensusConstant.ROUND_CACHE_COUNT) {
            roundList.get(0).setPreRound(null);
            roundList.remove(0);
        }
    }

    /**
     * Rollback local round to specified round
     *
     * @param roundIndex Rollback to specified round
     * @param chain      Chain information
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
     * Clean up the round information of the specified chain
     * Clean up the wheel number information of the specified chain
     *
     * @param chain chain info
     * @param count Keep several rounds of information/Keep several rounds of information
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
     * Clean up the round information after the specified round
     * Clean up the wheel number information of the specified chain
     *
     * @param chain chain info
     * @param roundIndex Keep several rounds of information/Keep several rounds of information
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
     * Obtain the round information of the specified index
     * Get round information for specified Subscripts
     *
     * @param chain      chain info
     * @param roundIndex Round index/round index
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
     * Check if it is necessary to reset the round
     * Check if you need to reset rounds
     *
     * @param chain chain info
     */
    public void checkIsNeedReset(Chain chain) throws Exception {
        /*
        1.If there is no round information available locally,Initialize local round information
        2.If it exists and the local latest block round is less than the locally calculated latest round, reset the local round information
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
                BlockExtendsData blockRoundData = blockHeader.getExtendsData();
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
     * Get the latest local round information
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
     * Initialize round information（Recalculate round information）
     * Initialize Round Information (recalculate Round Information)
     *
     * @param chain chain info
     */
    public void initRound(Chain chain) throws Exception {
        //resetRound(chain,false);
        MeetingRound currentRound = resetRound(chain, false);
        /*
        If its previous round is not currently set, find its previous round and set it
        If the previous round is not currently set, find the previous round and set it.
        */
        if (currentRound.getPreRound() == null) {
            BlockHeader newestHeader = chain.getNewestHeader();
            BlockExtendsData extendsData = newestHeader.getExtendsData();
            List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
            for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
                BlockHeader blockHeader = blockHeaderList.get(i);
                extendsData = blockHeader.getExtendsData();
                if (extendsData.getRoundIndex() < currentRound.getIndex()) {
                    break;
                }
            }
            MeetingRound preRound = getRound(chain, extendsData, false);
            currentRound.setPreRound(preRound);
        }
    }

    /**
     * Reset the latest round information
     * Reset the latest round information
     *
     * @param chain      chain info
     * @param isRealTime Is the round calculated based on the latest time/Whether to calculate rounds based on current time
     * @return MeetingRound
     */
    public MeetingRound resetRound(Chain chain, boolean isRealTime) throws Exception {
        chain.getRoundLock().lock();
        try {
            MeetingRound round = getCurrentRound(chain);
            if (isRealTime) {
                /*
                If the local latest round is empty or the packaging end time of the local latest round is less than the current time, the next round information needs to be calculated
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
            BlockExtendsData extendsData = blockHeader.getExtendsData();

            /*
            If the latest local round information is not empty&&The latest local round is equal to the latest block round&&If the latest block is not the last packaged block in this round, it will be directly returned to the local latest round
            If the latest local rounds are not empty & the latest local rounds are equal to the latest block rounds & if the latest block is not the last packaged block in this round,
             the latest local rounds will be returned directly to the latest local rounds
            */
            if (round != null && extendsData.getRoundIndex() == round.getIndex() && extendsData.getPackingIndexOfRound() != extendsData.getConsensusMemberCount()) {
                return round;
            }
            MeetingRound nextRound = getRound(chain, extendsData, false);
            /*
            If the current round is not empty and the calculated next round index is less than the current round index, the calculated next round information will be directly returned
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
     * Obtain information on the next round of rounds
     * Get the next round of round objects
     *
     * @param chain      chain info
     * @param roundData  Round data/block extends entity
     * @param isRealTime Is the round calculated based on the latest time/Whether to calculate rounds based on current time
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
                BlockExtendsData blockExtendsData = blockHeader.getExtendsData();
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
     * Calculate next round information based on time
     * Calculate the next round of information based on time
     *
     * @param chain chain info
     * @return MeetingRound
     */
    private MeetingRound getRoundByRealTime(Chain chain) throws Exception {
        BlockHeader bestBlockHeader = chain.getNewestHeader();
        BlockHeader startBlockHeader = bestBlockHeader;
        BlockExtendsData bestRoundData = bestBlockHeader.getExtendsData();
        long bestRoundEndTime = bestRoundData.getRoundEndTime(chain.getConfig().getPackingInterval());
        if (startBlockHeader.getHeight() != 0L) {
            long roundIndex = bestRoundData.getRoundIndex();
            /*
            If the round of packaging for the latest local block has ended, the round index needs to be added1,Then it is necessary to find the first block from the latest local block round to calculate the next round information
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
        Find the index and start time of the round to be calculated,If the current time<The latest local block time indicates that the round to be calculated is the latest local block round
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
     * Calculate the next round information based on the latest block data
     * Calculate next round information based on the latest block entity
     *
     * @param chain chain info
     * @return MeetingRound
     */
    private MeetingRound getRoundByNewestBlock(Chain chain) throws Exception {
        BlockHeader bestBlockHeader = chain.getNewestHeader();
        BlockExtendsData extendsData = bestBlockHeader.getExtendsData();
        extendsData.setRoundStartTime(extendsData.getRoundEndTime(chain.getConfig().getPackingInterval()));
        extendsData.setRoundIndex(extendsData.getRoundIndex() + 1);
        return getRoundByExpectedRound(chain, extendsData);
    }

    /**
     * Calculate the round information based on the specified block data
     * Calculate next round information based on the latest block entity
     *
     * @param chain     chain info
     * @param roundData Round information in the block/block extends entity
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
     * Calculate round information
     * Calculate wheel information
     *
     * @param chain            chain info
     * @param startBlockHeader Starting block of the previous round/Initial blocks of the last round
     * @param index            Round index/round index
     * @param startTime        Starting packaging time of the round/start time
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
        chain.getLogger().debug("The current round is：" + round.getIndex() + ";Starting packaging time of the current round：" + NulsDateUtils.convertDate(new Date(startTime * 1000)));
        chain.getLogger().debug("\ncalculation||index:{},startTime:{},startHeight:{},hash:{}\n" + round.toString() + "\n\n", index, startTime * 1000, startBlockHeader.getHeight(), startBlockHeader.getHash());
        return round;
    }

    /**
     * Set packaging node information in the round
     * Setting Packing Node Information in Rounds
     *
     * @param chain            chain info
     * @param round            Round information/round info
     * @param startBlockHeader Starting block of the previous round/Initial blocks of the last round
     */
    private void setMemberList(Chain chain, MeetingRound round, BlockHeader startBlockHeader) throws NulsException {
        List<MeetingMember> memberList = new ArrayList<>();
        String seedNodesStr = chain.getConfig().getSeedNodes();
        String[] seedNodes;
        /*
        Seed node packaging information assembly
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
            Obtain node delegation information, used to calculate the total delegation amount of the node
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
            Does the total entrusted amount of the node reach the minimum value of the outgoing node
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
     * Obtain node delegation information
     * Obtaining delegation information of nodes
     *
     * @param chain            chain info
     * @param agentHash        nodeID/agent hash
     * @param startBlockHeight The starting block height of the previous round/Initial blocks of the last round
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
     * Obtain a list of valid nodes in the network
     * Getting a list of valid nodes in the network
     *
     * @param chain            chain info
     * @param startBlockHeight The starting block height of the previous round/Initial blocks of the last round
     * @return List<Agent>
     */
    private List<Agent> getAliveAgentList(Chain chain, long startBlockHeight) {
        List<Agent> agentList = chain.getAgentList();
        List<Agent> resultList = new ArrayList<>();
        for (int i = agentList.size() - 1; i >= 0; i--) {
            Agent agent = agentList.get(i);
            fixRedPunishBugHelper.v13Filter(chain.getConfig().getChainId(), agent,startBlockHeight);
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
     * Calculate the reputation value of nodes
     * Calculating the Node's Credit Value
     *
     * @param chain       chain info
     * @param member      Packaging member objects/packing info
     * @param blockHeader Block head/block header
     * @return double
     */
    private double calcCreditVal(Chain chain, MeetingMember member, BlockHeader blockHeader) throws NulsException {
        BlockExtendsData roundData = blockHeader.getExtendsData();
        long roundStart = roundData.getRoundIndex() - ConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        /*
        The calculation of reputation value is based on the number of blocks and yellow cards produced by nodes within a limited number of rounds
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
     * Obtain the number of red and yellow card penalties obtained from the specified address
     * Get the number of red and yellow card penalties for the specified address
     *
     * @param chain      chain info
     * @param address    address/address
     * @param roundStart Starting round/round start index
     * @param roundEnd   End round/round end index
     * @param code       Red and yellow card identification/Red and yellow logo
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
        Each round of punishment may include the punishment record from the previous round, i.e. calculating fromareacha+99When recording the punishment for a round,aIn the punishment of the round, it may be that a certain address is penalizeda-1The wheel did not come out of the block, resulting in100The maximum possible number of wheels101Punishment records, process them here
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
     * Get the first block packaged in the previous round of the specified round
     * Gets the first block packaged in the previous round of the specified round
     *
     * @param chain      chain info
     * @param roundIndex Round index
     */
    public BlockHeader getFirstBlockOfPreRound(Chain chain, long roundIndex) {
        BlockHeader firstBlockHeader = null;
        long startRoundIndex = 0L;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            long currentRoundIndex = blockHeader.getExtendsData().getRoundIndex();
            if (roundIndex > currentRoundIndex) {
                if (startRoundIndex == 0L) {
                    startRoundIndex = currentRoundIndex;
                }
                if (currentRoundIndex < startRoundIndex) {
                    firstBlockHeader = blockHeaderList.get(i + 1);
                    BlockExtendsData roundData = firstBlockHeader.getExtendsData();
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
     * Obtain information about the previous round of the specified round
     * Gets the first block packaged in the previous round of the specified round
     *
     * @param chain      chain info
     * @param roundIndex Round index
     */
    public MeetingRound getPreRound(Chain chain, long roundIndex)throws Exception{
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        BlockHeader blockHeader;
        BlockExtendsData extendsData = null;
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            blockHeader = blockHeaderList.get(i);
            extendsData = blockHeader.getExtendsData();
            if(extendsData.getRoundIndex() < roundIndex){
                break;
            }
        }
        if(extendsData == null){
            return null;
        }
        return getRound(chain,extendsData,false);
    }

    /**
     * Obtain address block quantity
     * Get the number of address blocks
     *
     * @param chain          chain info
     * @param packingAddress Block address
     * @param roundStart     Starting round
     * @param roundEnd       End round
     */
    private long getBlockCountByAddress(Chain chain, byte[] packingAddress, long roundStart, long roundEnd) {
        long count = 0;
        List<BlockHeader> blockHeaderList = chain.getBlockHeaderList();
        for (int i = blockHeaderList.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = blockHeaderList.get(i);
            BlockExtendsData roundData = blockHeader.getExtendsData();
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
     * Query newly added consensus nodes and logged out consensus nodes between two rounds
     * New consensus nodes and unregistered consensus nodes between queries
     *
     * @param chain                  chain
     * @param lastExtendsData        Previous round information
     * @param currentExtendsData     Current round information
     * @return                       Node change information between two rounds
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
     * Obtain a list of newly added or decreased nodes between two rounds
     * @param lastRound        Previous round
     * @param currentRound     This round
     * @param isRegister       Obtain the list of added nodes（true）Or obtain a list of reduced nodes（false）
     * @return                 Node Change List
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
