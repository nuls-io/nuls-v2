package io.nuls.consensus.utils.thread.process;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.ProtocolVersion;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.ModuleHelper;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.BlockData;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.round.MeetingMember;
import io.nuls.consensus.model.bo.round.MeetingRound;
import io.nuls.consensus.model.po.RandomSeedStatusPo;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.consensus.storage.RandomSeedsStorageService;
import io.nuls.consensus.utils.RandomSeedUtils;
import io.nuls.consensus.utils.enumeration.ConsensusStatus;
import io.nuls.consensus.utils.manager.ConsensusManager;
import io.nuls.consensus.utils.manager.RoundManager;

import java.util.*;

/**
 * Consensus processor
 * Consensus processor
 *
 * @author tag
 * 2018/11/15
 */
public class ConsensusProcess {
    private RoundManager roundManager = SpringLiteContext.getBean(RoundManager.class);
    private RandomSeedsStorageService randomSeedsStorageService = SpringLiteContext.getBean(RandomSeedsStorageService.class);

    private NulsLogger consensusLogger;

    private boolean hasPacking;

    public void process(Chain chain) {
        try {
            boolean canPackage = checkCanPackage(chain);
            if (!canPackage) {
                return;
            }
            consensusLogger = chain.getLogger();
            doWork(chain);
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
    }

    /**
     * Check node packaging status
     * Check node packing status
     */
    private boolean checkCanPackage(Chain chain) throws Exception {
        if (chain == null) {
            throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        /*
        Check if the module status is running
        Check whether the module status is in operation
        */
        if (chain.getConsensusStatus().ordinal() <= ConsensusStatus.WAIT_RUNNING.ordinal()) {
            return false;
        }

        /*
        Check if the node status can be packaged(Set this status after the synchronization of the block management module is completed)
        Check whether the node status can be packaged (set up after the block management module completes synchronization)
        */
        if (!chain.isCanPacking()) {
            return false;
        }
        return true;
    }


    private void doWork(Chain chain) throws Exception {
        /*
        Check node status
        Check node status
        */
        if (chain.getConsensusStatus().ordinal() < ConsensusStatus.RUNNING.ordinal()) {
            return;
        }

        /*
        Obtain current round information and verify round information
        Get current round information
         */
        MeetingRound round = roundManager.resetRound(chain, true);
        if (round == null) {
            return;
        }
        MeetingMember member = round.getMyMember();
        if (member == null) {
            return;
        }

        /*
        If it is a consensus node, determine whether it is their turn to generate blocks
        1.Is the node being packaged
        2.Is the current time between the node packaging start time and end time
        If it's a consensus node, it's time to decide whether it's your turn to come out of the block.
        1. Is the node packing?
        2. Is the current time between the start and end of the node packing?
        */
        if (!hasPacking && member.getPackStartTime() < NulsDateUtils.getCurrentTimeSeconds() && member.getPackEndTime() > NulsDateUtils.getCurrentTimeSeconds()) {
            hasPacking = true;
            try {
                if (consensusLogger.getLogger().isDebugEnabled()) {
                    consensusLogger.debug("Current network time： " + NulsDateUtils.convertDate(new Date(NulsDateUtils.getCurrentTimeMillis())) + " , My packaging start time: " +
                            NulsDateUtils.convertDate(new Date(member.getPackStartTime() * 1000)) + " , My packaging end time: " +
                            NulsDateUtils.convertDate(new Date(member.getPackEndTime() * 1000)) + " , Current round start time: " +
                            NulsDateUtils.convertDate(new Date(round.getStartTime() * 1000)) + " , Current round end start time: " +
                            NulsDateUtils.convertDate(new Date(round.getEndTime() * 1000)));
                }
                packing(chain, member, round);
            } catch (Exception e) {
                consensusLogger.error(e);
            }
            while (member.getPackEndTime() > NulsDateUtils.getCurrentTimeSeconds()) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    consensusLogger.error(e);
                }
            }
            hasPacking = false;
        }
    }

    private void packing(Chain chain, MeetingMember self, MeetingRound round) throws Exception {
        waitReceiveNewestBlock(chain, self, round);
        /*
        Waiting for block output
        Wait for blocks
        */
        long start = System.currentTimeMillis();
        Block block = doPacking(chain, self, round);
        consensusLogger.info("doPacking use:" + (System.currentTimeMillis() - start) + "ms" + "\n\n");

        /*
         * After packaging is completed, check if the packaged block and the latest block in the main chain are continuous. If they are not continuous, it indicates that the block packaged by the previous consensus node was received during the packaging process. At this time, the local node needs to repack the block
         * After packaging, check whether the packaged block and the latest block in the main chain are continuous. If the block is not continuous,
         * the local node needs to repackage the block when it receives the packaged block from the previous consensus node in the packaging process.
         */
        if (null == block) {
            consensusLogger.error("make a null block");
            return;
        }
        try {
            CallMethodUtils.receivePackingBlock(chain.getConfig().getChainId(), RPCUtil.encode(block.serialize()), 0);
        } catch (Exception e) {
            consensusLogger.error(e);
        }
    }

    /**
     * Has it reached the time point when the block was generated by the node? If the latest local block was generated by the previous node in this round, the block will be directly packaged. Otherwise, after waiting for a certain time, if the block from the previous node has not been received, the block will be directly packaged
     * Whether or not to arrive at the time point when the node is out of the block, if the latest local block is out of the previous node in this round, it will be packaged directly.
     * Otherwise, if the block from the previous node has not been received after waiting for a certain time, it will be packed directly.
     */
    private void waitReceiveNewestBlock(Chain chain, MeetingMember self, MeetingRound round) {
        int waitRatio = 60;
        long timeout = chain.getConfig().getPackingInterval() * waitRatio / ConsensusConstant.VALUE_OF_ONE_HUNDRED;
        long endTime = self.getPackStartTime() + timeout;
        boolean hasReceiveNewestBlock;
        if (NulsDateUtils.getCurrentTimeSeconds() >= endTime) {
            return;
        }
        try {
            while (true) {
                /*
                Determine whether the latest local block is from the previous node in the round
                Determine whether the latest local block is from the last node in the round
                */
                hasReceiveNewestBlock = hasReceiveNewestBlock(chain, self, round);
                if (hasReceiveNewestBlock) {
                    break;
                }
                Thread.sleep(100L);
                if (NulsDateUtils.getCurrentTimeSeconds() >= endTime) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            consensusLogger.error(e.getMessage());
        }
    }

    /**
     * Determine whether the latest local block is from the previous block node in this round
     * Judging whether the latest block in this region is from the last block in this round
     */
    private boolean hasReceiveNewestBlock(Chain chain, MeetingMember self, MeetingRound round) {
        int myIndex = self.getPackingIndexOfRound();
        MeetingMember preMember;
        MeetingRound preRound = round;
        /*
        If the current node is the first out of block node in that round, then the latest local block should be from the last out of block node in the previous round
        If the current node is the first out-of-block node in the round, the latest local block should be the last out-of-block node in the previous round.
        */
        if (myIndex == 1) {
            preRound = round.getPreRound();
            if (preRound == null) {
                consensusLogger.error("PreRound is null!");
                return true;
            }
            preMember = preRound.getMember(preRound.getMemberCount());
        } else {
            preMember = round.getMember(self.getPackingIndexOfRound() - 1);
        }
        if (preMember == null) {
            return true;
        }

        /*
        Compare the latest local block output address with the output address of the previous node to see if they are equal. If they are equal, it indicates that the previous node has output the block and the current node can output the block
        Comparing whether the block address of the latest local block is equal to that of the previous node, if equal,
        it means that the previous node has already blocked, and the current node can blocked.
        */
        BlockHeader bestBlockHeader = chain.getNewestHeader();
        BlockExtendsData blockRoundData = bestBlockHeader.getExtendsData();
        byte[] bestPackingAddress = bestBlockHeader.getPackingAddress(chain.getConfig().getChainId());
        long bestRoundIndex = blockRoundData.getRoundIndex();
        int bestPackingIndex = blockRoundData.getPackingIndexOfRound();

        byte[] prePackingAddress = preMember.getAgent().getPackingAddress();
        long preRoundIndex = preRound.getIndex();
        int prePackingIndex = preMember.getPackingIndexOfRound();
        if (Arrays.equals(bestPackingAddress, prePackingAddress) && bestRoundIndex == preRoundIndex && bestPackingIndex == prePackingIndex) {
            return true;
        } else {
            return false;
        }
    }

    private void fillProtocol(BlockExtendsData extendsData, int chainId) throws NulsException {
        if (ModuleHelper.isSupportProtocolUpdate()) {
            Map map = CallMethodUtils.getVersion(chainId);
            ProtocolVersion currentProtocolVersion = JSONUtils.map2pojo((Map) map.get("currentProtocolVersion"), ProtocolVersion.class);
            ProtocolVersion localProtocolVersion = JSONUtils.map2pojo((Map) map.get("localProtocolVersion"), ProtocolVersion.class);
            extendsData.setMainVersion(currentProtocolVersion.getVersion());
            extendsData.setBlockVersion(localProtocolVersion.getVersion());
            extendsData.setEffectiveRatio(localProtocolVersion.getEffectiveRatio());
            extendsData.setContinuousIntervalCount(localProtocolVersion.getContinuousIntervalCount());
        } else {
            extendsData.setMainVersion((short) 1);
            extendsData.setBlockVersion((short) 1);
            extendsData.setEffectiveRatio((byte) 80);
            extendsData.setContinuousIntervalCount((short) 100);
        }
    }

    @SuppressWarnings("unchecked")
    private Block doPacking(Chain chain, MeetingMember self, MeetingRound round) throws Exception {
        BlockHeader bestBlock = chain.getNewestHeader();
        long packageHeight = bestBlock.getHeight() + 1;
        BlockData bd = new BlockData();
        bd.setHeight(packageHeight);
        bd.setPreHash(bestBlock.getHash());
        bd.setTime(self.getPackEndTime());
        BlockExtendsData extendsData = new BlockExtendsData();
        extendsData.setRoundIndex(round.getIndex());
        extendsData.setConsensusMemberCount(round.getMemberCount());
        extendsData.setPackingIndexOfRound(self.getPackingIndexOfRound());
        extendsData.setRoundStartTime(round.getStartTime());
        fillProtocol(extendsData, chain.getConfig().getChainId());
        /*
         * Add support for underlying random numbers
         */
        int chainId = chain.getConfig().getChainId();
        byte[] packingAddress = self.getAgent().getPackingAddress();
        RandomSeedStatusPo status = randomSeedsStorageService.getAddressStatus(chainId, packingAddress);
        byte[] seed = ConsensusConstant.EMPTY_SEED;
        if (null != status && status.getNextSeed() != null) {
            seed = status.getNextSeed();
        }
        extendsData.setSeed(seed);
        byte[] nextSeed = RandomSeedUtils.createRandomSeed();
        byte[] nextSeedHash = RandomSeedUtils.getLastDigestEightBytes(nextSeed);
        extendsData.setNextSeedHash(nextSeedHash);
        RandomSeedStatusPo po = new RandomSeedStatusPo();
        po.setAddress(packingAddress);
        po.setSeedHash(nextSeedHash);
        po.setNextSeed(nextSeed);
        po.setHeight(bd.getHeight());
        RandomSeedUtils.CACHE_SEED = po;

        /*
         * Get packaged transactions
         */
        String packingAddressString = AddressTool.getStringAddressByBytes(packingAddress);
        Map<String, Object> resultMap = CallMethodUtils.getPackingTxList(chain, bd.getTime(), packingAddressString);
        List<Transaction> packingTxList = new ArrayList<>();

        /*
         * Check if new blocks have been received during the assembly transaction process
         * Verify that new blocks are received halfway through packaging
         * */
        bestBlock = chain.getNewestHeader();
        long realPackageHeight = bestBlock.getHeight() + 1;
        if (!(bd.getPreHash().equals(bestBlock.getHash()) && realPackageHeight > packageHeight)) {
            bd.setHeight(realPackageHeight);
            bd.setPreHash(bestBlock.getHash());
            bestBlock = chain.getNewestHeader();
        }

        BlockExtendsData bestExtendsData = bestBlock.getExtendsData();
        boolean stateRootIsNull = false;
        if (resultMap == null) {
            extendsData.setStateRoot(bestExtendsData.getStateRoot());
            stateRootIsNull = true;
        } else {
            long txPackageHeight = Long.parseLong(resultMap.get("packageHeight").toString());
            String stateRoot = (String) resultMap.get("stateRoot");
            if (StringUtils.isBlank(stateRoot)) {
                extendsData.setStateRoot(bestExtendsData.getStateRoot());
                stateRootIsNull = true;
            } else {
                extendsData.setStateRoot(RPCUtil.decode(stateRoot));
            }
            if (realPackageHeight >= txPackageHeight) {
                List<String> txHexList = (List) resultMap.get("list");
                for (String txHex : txHexList) {
                    Transaction tx = new Transaction();
                    tx.parse(RPCUtil.decode(txHex), 0);
                    packingTxList.add(tx);
                }
            }
        }
        bd.setExtendsData(extendsData);
        /*
        Assembly system transactions（CoinBase/Red card/Yellow card）+ Create blocks
        Assembly System Transactions (CoinBase/Red/Yellow)+ Create blocks
        */
        ConsensusManager consensusManager = SpringLiteContext.getBean(ConsensusManager.class);
        consensusManager.addConsensusTx(chain, bestBlock, packingTxList, self, round, extendsData);
        bd.setTxList(packingTxList);
        Block newBlock = consensusManager.createBlock(chain, bd, packingAddress, packingAddressString);
        /*
         * Verify if new blocks have been received during packaging
         * Verify that new blocks are received halfway through packaging
         * */
        bestBlock = chain.getNewestHeader();
        if (!newBlock.getHeader().getPreHash().equals(bestBlock.getHash())) {
            packingTxList.clear();
            consensusManager.addConsensusTx(chain, bestBlock, packingTxList, self, round, extendsData);
            bd.setTxList(packingTxList);
            bd.setPreHash(bestBlock.getHash());
            bd.setHeight(bestBlock.getHeight() + 1);
            newBlock = consensusManager.createBlock(chain, bd, packingAddress, packingAddressString);
            if (stateRootIsNull) {
                bestExtendsData = bestBlock.getExtendsData();
                extendsData.setStateRoot(bestExtendsData.getStateRoot());
                newBlock.getHeader().setExtend(extendsData.serialize());
            }
        }
        consensusLogger.info("make block height:" + newBlock.getHeader().getHeight() + ",txCount: " + newBlock.getTxs().size() + " , block size: " + newBlock.size() + " , time:" + NulsDateUtils.convertDate(new Date(newBlock.getHeader().getTime() * 1000)) + ",packEndTime:" +
                NulsDateUtils.convertDate(new Date(self.getPackEndTime() * 1000)) + ",hash:" + newBlock.getHeader().getHash().toHex() + ",preHash:" + newBlock.getHeader().getPreHash().toHex());
        return newBlock;
    }
}
