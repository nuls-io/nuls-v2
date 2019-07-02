package io.nuls.poc.utils.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.round.RoundValidResult;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.model.bo.tx.txdata.YellowPunishData;
import io.nuls.poc.utils.enumeration.PunishReasonEnum;
import io.nuls.poc.utils.manager.CoinDataManager;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.poc.utils.manager.PunishManager;
import io.nuls.poc.utils.manager.RoundManager;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.DoubleUtils;

import java.io.IOException;
import java.util.*;

/**
 * 区块验证工具类
 * Block Verification Tool Class
 *
 * @author tag
 * 2018/11/30
 */
@Component
public class BlockValidator {
    @Autowired
    private RoundManager roundManager;
    @Autowired
    private PunishManager punishManager;
    @Autowired
    private ConsensusManager consensusManager;
    @Autowired
    private CoinDataManager coinDataManager;

    /**
     * 区块头验证
     * Block verification
     *
     * @param isDownload block status
     * @param chain      chain info
     * @param block      block info
     */
    public void validate(boolean isDownload, Chain chain, Block block) throws NulsException, IOException {
        BlockHeader blockHeader = block.getHeader();
        //验证梅克尔哈希]
        if (!blockHeader.getMerkleHash().equals(NulsHash.calcMerkleHash(block.getTxHashList()))) {
            throw new NulsException(ConsensusErrorCode.MERKEL_HASH_ERROR);
        }
        //区块头签名验证
        if (blockHeader.getBlockSignature().verifySignature(blockHeader.getHash()).isFailed()) {
            chain.getLogger().error("Block Header Verification Error!");
            throw new NulsException(ConsensusErrorCode.SIGNATURE_ERROR);
        }
        RoundValidResult roundValidResult;
        String blockHeaderHash = blockHeader.getHash().toHex();
        try {
            roundValidResult = roundValidate(isDownload, chain, blockHeader, blockHeaderHash);
        } catch (Exception e) {
            throw new NulsException(e);
        }
        MeetingRound currentRound = roundValidResult.getRound();
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        MeetingMember member = currentRound.getMember(extendsData.getPackingIndexOfRound());
        boolean validResult = punishValidate(block, currentRound, member, chain, blockHeaderHash);
        if (!validResult) {
            if (roundValidResult.isValidResult()) {
                roundManager.rollBackRound(chain, currentRound.getIndex());
            }
            throw new NulsException(ConsensusErrorCode.BLOCK_PUNISH_VALID_ERROR);
        }
        validResult = coinBaseValidate(block, currentRound, member, chain, blockHeaderHash);
        if (!validResult) {
            if (roundValidResult.isValidResult()) {
                roundManager.rollBackRound(chain, currentRound.getIndex());
            }
            throw new NulsException(ConsensusErrorCode.BLOCK_COINBASE_VALID_ERROR);
        }
    }

    /**
     * 区块轮次验证
     * Block round validation
     *
     * @param isDownload  block status 0同步中  1接收最新区块
     * @param chain       chain info
     * @param blockHeader block header info
     */
    private RoundValidResult roundValidate(boolean isDownload, Chain chain, BlockHeader blockHeader, String blockHeaderHash) throws Exception {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        BlockHeader bestBlockHeader = chain.getNewestHeader();
        BlockExtendsData bestExtendsData = new BlockExtendsData(bestBlockHeader.getExtend());

        RoundValidResult roundValidResult = new RoundValidResult();

      /*
      该区块为本地最新区块之前的区块
      * */
        boolean isBeforeBlock = extendsData.getRoundIndex() < bestExtendsData.getRoundIndex() || (extendsData.getRoundIndex() == bestExtendsData.getRoundIndex() && extendsData.getPackingIndexOfRound() <= bestExtendsData.getPackingIndexOfRound());
        if (isBeforeBlock) {
            chain.getLogger().error("new block roundData error, block height : " + blockHeader.getHeight() + " , hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        if (chain.getNewestHeader().getHeight() == 0) {
            chain.getRoundList().clear();
        }
        MeetingRound currentRound = roundManager.getCurrentRound(chain);
        boolean hasChangeRound = false;

        if (currentRound == null || extendsData.getRoundIndex() < currentRound.getIndex()) {
            MeetingRound round = roundManager.getRoundByIndex(chain, extendsData.getRoundIndex());
            if (round != null) {
                currentRound = round;
            } else {
                currentRound = roundManager.getRound(chain, extendsData, false);
            }
            if (chain.getRoundList().isEmpty()) {
                hasChangeRound = true;
            }
        } else if (extendsData.getRoundIndex() > currentRound.getIndex()) {
            if (extendsData.getRoundStartTime() < currentRound.getEndTime()) {
                chain.getLogger().error("block height " + blockHeader.getHeight() + " round index and start time not match! hash :" + blockHeaderHash);
                throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            if (extendsData.getRoundStartTime() > NulsDateUtils.getCurrentTimeSeconds() + chain.getConfig().getPackingInterval()) {
                chain.getLogger().error("block height " + blockHeader.getHeight() + " round startTime is error, greater than current time! hash :" + blockHeaderHash);
                throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            if (extendsData.getRoundStartTime() + (extendsData.getPackingIndexOfRound() - 1) * chain.getConfig().getPackingInterval() > NulsDateUtils.getCurrentTimeSeconds() + chain.getConfig().getPackingInterval()) {
                chain.getLogger().error("block height " + blockHeader.getHeight() + " is the block of the future and received in advance! hash :" + blockHeaderHash);
                throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            MeetingRound tempRound = roundManager.getRound(chain, extendsData, !isDownload);
            if (tempRound.getIndex() > currentRound.getIndex()) {
                tempRound.setPreRound(currentRound);
                hasChangeRound = true;
            }
            currentRound = tempRound;
        }
        if (extendsData.getRoundIndex() != currentRound.getIndex() || extendsData.getRoundStartTime() != currentRound.getStartTime()) {
            chain.getLogger().error("block height " + blockHeader.getHeight() + " round startTime is error! hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        if (extendsData.getConsensusMemberCount() != currentRound.getMemberCount()) {
            chain.getLogger().error("block height " + blockHeader.getHeight() + " packager count is error! hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        // 验证打包人是否正确
        MeetingMember member = currentRound.getMember(extendsData.getPackingIndexOfRound());
        if (!Arrays.equals(member.getAgent().getPackingAddress(), blockHeader.getPackingAddress(chain.getConfig().getChainId()))) {
            chain.getLogger().error("block height " + blockHeader.getHeight() + " packager error! hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        if (member.getPackEndTime() != blockHeader.getTime()) {
            chain.getLogger().error("block height " + blockHeader.getHeight() + " time error! hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        if (hasChangeRound) {
            roundManager.addRound(chain, currentRound);
            roundValidResult.setValidResult(true);
        }
        roundValidResult.setRound(currentRound);
        return roundValidResult;
    }

    /**
     * 区块惩罚交易验证
     * Block Penalty Trading Verification
     *
     * @param block        block info
     * @param currentRound Block round information
     * @param member       Node packing information
     * @param chain        chain info
     */
    private boolean punishValidate(Block block, MeetingRound currentRound, MeetingMember member, Chain chain, String blockHeaderHash) throws NulsException {
        List<Transaction> txs = block.getTxs();
        List<Transaction> redPunishTxList = new ArrayList<>();
        Transaction yellowPunishTx = null;
        Transaction tx;
      /*
      检查区块交中是否存在多个黄牌交易
      Check whether there are multiple yellow trades in block handover
      */
        for (int index = 1; index < txs.size(); index++) {
            tx = txs.get(index);
            if (tx.getType() == TxType.COIN_BASE) {
                chain.getLogger().debug("Coinbase transaction more than one! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                return false;
            }
            if (tx.getType() == TxType.YELLOW_PUNISH) {
                if (yellowPunishTx == null) {
                    yellowPunishTx = tx;
                } else {
                    chain.getLogger().debug("Yellow punish transaction more than one! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                    return false;
                }
            } else if (tx.getType() == TxType.RED_PUNISH) {
                redPunishTxList.add(tx);
            }
        }
      /*
      校验区块交易中的黄牌交易是否正确
      Check the correctness of yellow card trading in block trading
      */
        try {
            Transaction newYellowPunishTX = punishManager.createYellowPunishTx(chain, chain.getNewestHeader(), member, currentRound);
            boolean isMatch = (yellowPunishTx == null && newYellowPunishTX == null) || (yellowPunishTx != null && newYellowPunishTX != null);
            if(!isMatch){
                chain.getLogger().debug("The yellow punish tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                return false;
            }else if(yellowPunishTx != null && !yellowPunishTx.getHash().equals(newYellowPunishTX.getHash())){
                chain.getLogger().debug("The yellow punish tx's hash is wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                return false;
            }
        } catch (Exception e) {
            chain.getLogger().debug("The tx's wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash, e);
            return false;
        }
      /*
      区块中红牌交易验证
      Verification of Red Card Trading in Blocks
      */
        if (!redPunishTxList.isEmpty()) {
            Set<String> punishAddress = new HashSet<>();
            if (null != yellowPunishTx) {
                YellowPunishData yellowPunishData = new YellowPunishData();
                yellowPunishData.parse(yellowPunishTx.getTxData(), 0);
                List<byte[]> addressList = yellowPunishData.getAddressList();
                for (byte[] address : addressList) {
                    MeetingMember item = currentRound.getMemberByAgentAddress(address);
                    if (null == item) {
                        item = currentRound.getPreRound().getMemberByAgentAddress(address);
                    }
                    if (DoubleUtils.compare(item.getAgent().getRealCreditVal(), ConsensusConstant.RED_PUNISH_CREDIT_VAL) <= 0) {
                        punishAddress.add(AddressTool.getStringAddressByBytes(item.getAgent().getAgentAddress()));
                    }
                }
            }
            int countOfTooMuchYP = 0;
            for (Transaction redTx : redPunishTxList) {
                RedPunishData data = new RedPunishData();
                data.parse(redTx.getTxData(), 0);
                if (data.getReasonCode() == PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode()) {
                    countOfTooMuchYP++;
                    if (!punishAddress.contains(AddressTool.getStringAddressByBytes(data.getAddress()))) {
                        chain.getLogger().debug("There is a wrong red punish tx!" + blockHeaderHash);
                        return false;
                    }
                    if (redTx.getTime() != block.getHeader().getTime()) {
                        chain.getLogger().debug("red punish CoinData & TX time is wrong! " + blockHeaderHash);
                        return false;
                    }
                }
                boolean result = verifyRedPunish(chain, redTx);
                if (!result) {
                    return false;
                }
            }
            if (countOfTooMuchYP != punishAddress.size()) {
                chain.getLogger().debug("There is a wrong red punish tx!" + blockHeaderHash);
                return false;
            }
        }
        return true;
    }


    /**
     * 红牌交易验证
     *
     * @param chain chain info
     * @param tx    transaction info
     */
    private boolean verifyRedPunish(Chain chain, Transaction tx) throws NulsException {
        RedPunishData punishData = new RedPunishData();
        punishData.parse(tx.getTxData(), 0);
      /*
      红牌交易类型为连续分叉
      The type of red card transaction is continuous bifurcation
      */
        if (punishData.getReasonCode() == PunishReasonEnum.BIFURCATION.getCode()) {
            NulsByteBuffer byteBuffer = new NulsByteBuffer(punishData.getEvidence());
            long[] roundIndex = new long[ConsensusConstant.REDPUNISH_BIFURCATION];
            for (int i = 0; i < ConsensusConstant.REDPUNISH_BIFURCATION && !byteBuffer.isFinished(); i++) {
                BlockHeader header1 = null;
                BlockHeader header2 = null;
                try {
                    header1 = byteBuffer.readNulsData(new BlockHeader());
                    header2 = byteBuffer.readNulsData(new BlockHeader());
                } catch (NulsException e) {
                    chain.getLogger().error(e.getMessage());
                }
                if (null == header1 || null == header2) {
                    throw new NulsException(ConsensusErrorCode.DATA_NOT_EXIST);
                }
                if (header1.getHeight() != header2.getHeight()) {
                    throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
                }
                if (i == ConsensusConstant.REDPUNISH_BIFURCATION - 1 && (header1.getTime() + header2.getTime()) / 2 != tx.getTime()) {
                    return false;
                }
                if (!Arrays.equals(header1.getBlockSignature().getPublicKey(), header2.getBlockSignature().getPublicKey())) {
                    throw new NulsException(ConsensusErrorCode.BLOCK_SIGNATURE_ERROR);
                }
                BlockExtendsData blockExtendsData = new BlockExtendsData(header1.getExtend());
                roundIndex[i] = blockExtendsData.getRoundIndex();
            }
            //验证轮次是否连续
            boolean rs = true;
            for (int i = 0; i < roundIndex.length; i++) {
                if (i < roundIndex.length - 2 && roundIndex[i + 1] - roundIndex[i] != 1) {
                    rs = false;
                    break;
                }
            }
            if (!rs) {
                throw new NulsException(ConsensusErrorCode.BLOCK_RED_PUNISH_ERROR);
            }
        }
      /*
      红牌交易类型为黄牌过多
      The type of red card trading is too many yellow cards
      */
        else if (punishData.getReasonCode() != PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode()) {
            throw new NulsException(ConsensusErrorCode.BLOCK_PUNISH_VALID_ERROR);
        }

      /*
      CoinData验证
      CoinData verification
      */
        if (!coinDataValidate(chain, tx)) {
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        return true;
    }


    /**
     * 区块CoinBase交易验证
     * Block CoinBase transaction verification
     *
     * @param block        block info
     * @param currentRound Block round information
     * @param member       Node packing information
     * @param chain        chain info
     */
    private boolean coinBaseValidate(Block block, MeetingRound currentRound, MeetingMember member, Chain chain, String blockHeaderHash) throws NulsException, IOException {
        Transaction tx = block.getTxs().get(0);
        if (tx.getType() != TxType.COIN_BASE) {
            chain.getLogger().debug("CoinBase transaction order wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
            return false;
        }

        Transaction coinBaseTransaction = consensusManager.createCoinBaseTx(chain, member, block.getTxs(), currentRound, 0);
        if (null == coinBaseTransaction || !tx.getHash().equals(coinBaseTransaction.getHash())) {
            chain.getLogger().error("the coin base tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
            return false;
        }
        return true;
    }

    /**
     * CoinData 验证
     * CoinData Verification
     *
     * @param tx    red punish transaction
     * @param chain chain info
     * @return 验证是否成功/Verify success
     */
    private boolean coinDataValidate(Chain chain, Transaction tx) throws NulsException {
        Agent punishAgent = null;
        RedPunishData punishData;
        punishData = new RedPunishData();
        punishData.parse(tx.getTxData(), 0);
        for (Agent agent : chain.getAgentList()) {
            if (agent.getDelHeight() > 0 && (tx.getBlockHeight() <= 0 || agent.getDelHeight() < tx.getBlockHeight())) {
                continue;
            }
            if (Arrays.equals(punishData.getAddress(), agent.getAgentAddress())) {
                punishAgent = agent;
                break;
            }
        }
        if (null == punishAgent) {
            Log.info(ConsensusErrorCode.AGENT_NOT_EXIST.getMsg());
            return false;
        }
        CoinData coinData = coinDataManager.getStopAgentCoinData(chain, punishAgent, tx.getTime() + chain.getConfig().getRedPublishLockTime());
        try {
            if (!Arrays.equals(coinData.serialize(), tx.getCoinData())) {
                chain.getLogger().error("++++++++++ RedPunish verification does not pass, redPunish type:{}, - height:{}, - redPunish tx timestamp:{}", punishData.getReasonCode(), tx.getBlockHeight(), tx.getTime());
                return false;
            }
        } catch (IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }
}
