package io.nuls.consensus.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.core.basic.VarInt;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.consensus.Evidence;
import io.nuls.consensus.model.bo.round.MeetingMember;
import io.nuls.consensus.model.bo.round.MeetingRound;
import io.nuls.consensus.model.bo.tx.txdata.Agent;
import io.nuls.consensus.model.bo.tx.txdata.CancelDeposit;
import io.nuls.consensus.model.bo.tx.txdata.Deposit;
import io.nuls.consensus.model.bo.tx.txdata.StopAgent;
import io.nuls.consensus.model.po.AgentPo;
import io.nuls.consensus.model.po.DepositPo;
import io.nuls.consensus.model.po.PunishLogPo;
import io.nuls.consensus.storage.AgentStorageService;
import io.nuls.consensus.storage.DepositStorageService;
import io.nuls.consensus.storage.PunishStorageService;
import io.nuls.consensus.utils.compare.PunishLogComparator;
import io.nuls.consensus.utils.enumeration.PunishReasonEnum;
import io.nuls.consensus.utils.enumeration.PunishType;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Punishment information management, used for recording punishment data evidence, generating red and yellow card punishment, etc
 * Punishment information management, records of punishment entity evidence, red and yellow card punishment generation, etc.
 *
 * @author tag
 * 2018/12/5
 */
@Component
public class PunishManager {
    @Autowired
    private PunishStorageService punishStorageService;
    @Autowired
    private CoinDataManager coinDataManager;
    @Autowired
    private AgentStorageService agentStorageService;
    @Autowired
    private DepositStorageService depositStorageService;
    @Autowired
    private DepositManager depositManager;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private RoundManager roundManager;

    /**
     * Load all red card information and recent updatesXYellow card data to cache
     * Load all red card information and latest X rotation card entity to the cache
     *
     * @param chain Chain information/chain info
     */
    public void loadPunishes(Chain chain) throws Exception {
        BlockHeader blockHeader = chain.getNewestHeader();
        if (null == blockHeader) {
            return;
        }
        BlockExtendsData roundData = blockHeader.getExtendsData();
        long breakRoundIndex = roundData.getRoundIndex() - ConsensusConstant.INIT_PUNISH_OF_ROUND_COUNT;
        List<PunishLogPo> punishLogList = punishStorageService.getPunishList(chain.getConfig().getChainId());
        List<PunishLogPo> redPunishList = new ArrayList<>();
        List<PunishLogPo> yellowPunishList = new ArrayList<>();
        for (PunishLogPo po : punishLogList) {
            if (po.getType() == PunishType.RED.getCode()) {
                redPunishList.add(po);
            } else {
                if (po.getRoundIndex() <= breakRoundIndex) {
                    continue;
                }
                yellowPunishList.add(po);
            }
        }
        Collections.sort(redPunishList, new PunishLogComparator());
        Collections.sort(yellowPunishList, new PunishLogComparator());
        chain.setRedPunishList(redPunishList);
        chain.setYellowPunishList(yellowPunishList);
    }

    /**
     * Clean up yellow card data
     * Clean up yellow card entity
     *
     * @param chain Chain information/chain info
     */
    public void clear(Chain chain) {
        BlockHeader blockHeader = chain.getNewestHeader();
        BlockExtendsData roundData = blockHeader.getExtendsData();
        Iterator <PunishLogPo> iterator = chain.getYellowPunishList().iterator();
        long minRound = roundData.getRoundIndex() - ConsensusConstant.INIT_PUNISH_OF_ROUND_COUNT;
        while (iterator.hasNext()){
            PunishLogPo punishLogPo = iterator.next();
            if(punishLogPo.getRoundIndex() >= minRound){
                break;
            }
            iterator.remove();
        }
    }

    /**
     * Add fork evidence
     * Adding bifurcation evidence
     *
     * @param chain
     * @param firstHeader
     * @param secondHeader
     */
    public void addEvidenceRecord(Chain chain, BlockHeader firstHeader, BlockHeader secondHeader) throws NulsException {
        /*
        Find the forked node
        Find the bifurcated nodes
        */
        Agent agent = null;
        for (Agent a : chain.getAgentList()) {
            if (a.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(a.getPackingAddress(), firstHeader.getPackingAddress(chain.getConfig().getChainId()))) {
                agent = a;
                break;
            }
        }
        if (null == agent) {
            return;
        }
        /*
        Verify whether the node should be penalized with a red card
        Verify whether the node should be punished by a red card
        */
        boolean isRedPunish = isRedPunish(chain, firstHeader, secondHeader);
        if (isRedPunish) {
            createRedPunishTransaction(chain, agent);
        }
    }

    /**
     * Add Double Flower Red Card Record
     * Add Double Flower Red Card Record
     *
     * @param chain
     * @param txs
     * @param block
     */
    public void addDoubleSpendRecord(Chain chain, List<Transaction> txs, Block block) throws NulsException {
        /*
        Find the node for the Double Flower transaction
        Find the bifurcated nodes
        */
        byte[] packingAddress = AddressTool.getAddress(block.getHeader().getBlockSignature().getPublicKey(), chain.getConfig().getChainId());
        List<Agent> agentList = chain.getAgentList();
        Agent agent = null;
        for (Agent a : agentList) {
            if (a.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(a.getPackingAddress(), packingAddress)) {
                agent = a;
                break;
            }
        }
        if (agent == null) {
            return;
        }
        try {
            /*
            Assembling Double Flower Red Card Trading
            Assembled Double Flower Red Card Trading
            */
            Transaction redPunishTransaction = new Transaction(TxType.RED_PUNISH);
            RedPunishData redPunishData = new RedPunishData();
            redPunishData.setAddress(agent.getAgentAddress());
            SmallBlock smallBlock = new SmallBlock();
            smallBlock.setHeader(block.getHeader());
            smallBlock.setTxHashList((ArrayList<NulsHash>) block.getTxHashList());
            for (Transaction tx : txs) {
                smallBlock.addSystemTx(tx);
            }
            redPunishData.setEvidence(smallBlock.serialize());
            redPunishData.setReasonCode(PunishReasonEnum.DOUBLE_SPEND.getCode());
            redPunishTransaction.setTxData(redPunishData.serialize());
            redPunishTransaction.setTime(smallBlock.getHeader().getTime());
            CoinData coinData = coinDataManager.getStopAgentCoinData(chain, agent, redPunishTransaction.getTime() + chain.getConfig().getRedPublishLockTime());
            redPunishTransaction.setCoinData(coinData.serialize());
            redPunishTransaction.setHash(NulsHash.calcHash(redPunishTransaction.serializeForHash()));
            chain.getRedPunishTransactionList().add(redPunishTransaction);
        } catch (IOException e) {
            chain.getLogger().error(e.getMessage());
        }
    }

    /**
     * Update the list of punishment evidence and verify whether the node should be given a red card punishment
     * Follow the new penalty evidence list and verify whether the node should give a red card penalty
     *
     * @param chain
     * @param firstHeader
     * @param secondHeader
     * @return boolean
     */
    private boolean isRedPunish(Chain chain, BlockHeader firstHeader, BlockHeader secondHeader) {
        //Verify block addressPackingAddressRecord the number of consecutive forks, if they reach a continuous state3Wheel based red card punishment/recently100In the wheel, there are3Secondary bifurcation
        String packingAddress = AddressTool.getStringAddressByBytes(firstHeader.getPackingAddress(chain.getConfig().getChainId()));
        BlockExtendsData extendsData = firstHeader.getExtendsData();
        long currentRoundIndex = extendsData.getRoundIndex();
        Map<String, List<Evidence>> currentChainEvidences = chain.getEvidenceMap();
        /*
        First, generate evidence
        First generate an evidence
        */
        Evidence evidence = new Evidence(currentRoundIndex, firstHeader, secondHeader);

        /*
        Determine if there is a penalty record for the current chain, and add it if it does not exist
        Determine if there is a penalty record for the current chain, and add if not
        */
        if (currentChainEvidences == null) {
            currentChainEvidences = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        }

        /*
        Check if there is evidence of a fork in the current node locally. If it does not exist, add it
        Query whether there is bifurcation evidence for the current node locally, and if not add
        */
        if (!currentChainEvidences.containsKey(packingAddress)) {
            List<Evidence> list = new ArrayList<>();
            list.add(evidence);
            currentChainEvidences.put(packingAddress, list);
            return false;
        }
        /*
        1.If there is evidence of a fork in the node, determine whether the current fork cycle is continuous with the previous fork cycle of the node
        2.If it is continuous, determine whether the number of consecutive forks of the node has reached the red card penalty number. If it reaches this, generate a red card penalty transaction. If it is not continuous, clear the fork record of the node
        1. If there is evidence of bifurcation of the node, it is judged whether the current bifurcation wheel number is continuous with the previous bifurcation wheel number of the node.
        2. If continuous, judge whether the number of consecutive bifurcations of the node has reached the number of red card penalties. If the number of consecutive bifurcations reaches the number of red card penalties,
           generate a red card penalty transaction, and if not, empty the bifurcation records of the node.
        */
        else {
            List<Evidence> list = currentChainEvidences.get(packingAddress);

            Iterator<Evidence> iterator = list.iterator();
            while (iterator.hasNext()) {
                Evidence e = iterator.next();
                if (e.getRoundIndex() <= currentRoundIndex - ConsensusConstant.CREDIT_MAGIC_NUM) {
                    iterator.remove();
                }
            }
            list.add(evidence);
            currentChainEvidences.put(packingAddress, list);
            chain.setEvidenceMap(currentChainEvidences);
            if (list.size() >= ConsensusConstant.REDPUNISH_BIFURCATION) {
                return true;
            }
            return false;
        }
    }

    /**
     * Create a red card transaction and place it in the cache
     * Create a red card transaction and put it in the cache
     *
     * @param chain
     * @param agent
     */
    private void createRedPunishTransaction(Chain chain, Agent agent) throws NulsException {
        Transaction redPunishTransaction = new Transaction(TxType.RED_PUNISH);
        RedPunishData redPunishData = new RedPunishData();
        redPunishData.setAddress(agent.getAgentAddress());
        long txTime = 0;
        try {
            /*
            continuity3round Two block heads per round as evidence altogether 3 * 2 Using block heads as evidence
            For three consecutive rounds, two blocks in each round are used as evidence, and a total of 3*2 blocks are used as evidence.
            */
            byte[][] headers = new byte[ConsensusConstant.REDPUNISH_BIFURCATION * 2][];
            Map<String, List<Evidence>> currentChainEvidences = chain.getEvidenceMap();
            List<Evidence> list = currentChainEvidences.remove(AddressTool.getStringAddressByBytes(agent.getPackingAddress()));
            for (int i = 0; i < list.size() && i < ConsensusConstant.REDPUNISH_BIFURCATION; i++) {
                Evidence evidence = list.get(i);
                int s = i * 2;
                headers[s] = evidence.getBlockHeader1().serialize();
                headers[++s] = evidence.getBlockHeader2().serialize();
                txTime = (evidence.getBlockHeader1().getTime() + evidence.getBlockHeader2().getTime()) / 2;
            }
            redPunishData.setEvidence(ByteUtils.concatenate(headers));
        } catch (IOException e) {
            chain.getLogger().error(e.getMessage());
        }
        try {
            redPunishData.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
            redPunishTransaction.setTxData(redPunishData.serialize());
            redPunishTransaction.setTime(txTime);
            /*
            assembleCoinData
            Assemble CoinData
            */
            CoinData coinData = coinDataManager.getStopAgentCoinData(chain, agent, redPunishTransaction.getTime() + chain.getConfig().getRedPublishLockTime());
            redPunishTransaction.setCoinData(coinData.serialize());
            redPunishTransaction.setHash(NulsHash.calcHash(redPunishTransaction.serializeForHash()));
            /*
            Cache red card transactions
            Assemble Red Punish transaction
            */
            chain.getRedPunishTransactionList().add(redPunishTransaction);
        } catch (IOException e) {
            chain.getLogger().error(e.getMessage());
        }
    }

    /**
     * Assembly Red/Yellow card trading
     * Assemble Red/Yellow Transaction
     *
     * @param chain     Chain info
     * @param bestBlock Latest local block/Latest local blocks
     * @param txList    A list of transactions to be packaged/List of transactions that need to be packaged
     * @param self      Local Node Packing Information/Local node packaging information
     * @param round     Local latest rounds information/Latest local round information
     */
    public void punishTx(Chain chain, BlockHeader bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws Exception {
        Transaction yellowPunishTransaction = createYellowPunishTx(chain, bestBlock, self, round);
        if (null == yellowPunishTransaction) {
            if (chain.getRedPunishTransactionList().size() > 0) {
                conflictValid(chain, txList);
            }
            return;
        }
        txList.add(yellowPunishTransaction);
        /*
        When continuous100When a yellow card is given, give a red card
        When 100 yellow CARDS in a row, give a red card.
        */
        YellowPunishData yellowPunishData = new YellowPunishData();
        yellowPunishData.parse(yellowPunishTransaction.getTxData(), 0);
        List<byte[]> addressList = yellowPunishData.getAddressList();
        Set<Integer> punishedSet = new HashSet<>();
        for (byte[] address : addressList) {
            MeetingMember member = round.getMemberByAgentAddress(address);
            if (null == member) {
                member = round.getPreRound().getMemberByAgentAddress(address);
            }
            boolean changeNet = bestBlock.getHeight() >= ConsensusConstant.CHANGE_NET_HEIGHT_MIN && bestBlock.getHeight() <= ConsensusConstant.CHANGE_NET_HEIGHT_MAX;
            if (!changeNet && DoubleUtils.compare(member.getAgent().getRealCreditVal(), ConsensusConstant.RED_PUNISH_CREDIT_VAL) <= 0) {
                if (!punishedSet.add(member.getPackingIndexOfRound())) {
                    continue;
                }
                if (member.getAgent().getDelHeight() > 0L) {
                    continue;
                }
                Transaction redPunishTransaction = new Transaction(TxType.RED_PUNISH);
                RedPunishData redPunishData = new RedPunishData();
                redPunishData.setAddress(address);
                redPunishData.setReasonCode(PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode());
                redPunishTransaction.setTxData(redPunishData.serialize());
                redPunishTransaction.setTime(self.getPackEndTime());
                CoinData coinData = coinDataManager.getStopAgentCoinData(chain, redPunishData.getAddress(), redPunishTransaction.getTime() + chain.getConfig().getRedPublishLockTime());
                if (coinData != null) {
                    redPunishTransaction.setCoinData(coinData.serialize());
                    redPunishTransaction.setHash(NulsHash.calcHash(redPunishTransaction.serializeForHash()));
                    chain.getRedPunishTransactionList().add(redPunishTransaction);
                }
            }
        }
        /*
         * Conflict detection between packaged transactions and red card transactions
         * Conflict Detection of UnPackaged Trading and Red Card Trading
         * */
        if (chain.getRedPunishTransactionList().size() > 0) {
            conflictValid(chain, txList);
        }
    }

    /**
     * Assembling yellow cards
     * Assemble Yellow Transaction
     *
     * @param preBlock Latest local block/Latest local blocks
     * @param self     A list of transactions to be packaged/List of transactions that need to be packaged
     * @param round    Local latest rounds information/Latest local round information
     * @return Transaction
     */
    public Transaction createYellowPunishTx(Chain chain, BlockHeader preBlock, MeetingMember self, MeetingRound round) throws Exception {
        BlockExtendsData preBlockRoundData = preBlock.getExtendsData();
        /*
        If the current packaging round of this node is more than one round larger than the round of the latest local block, return no yellow card transactions generated
        If the current packing rounds of this node are more than one round larger than the rounds of the latest local block,
        no yellow card transaction will be generated.
        */
        if (self.getRoundIndex() - preBlockRoundData.getRoundIndex() > 1) {
            return null;
        }
        /*
        Calculate the number of yellow cards that need to be generated, that is, the number of blocks that differ between the current block and the latest local block
        Calculate the number of yellow cards that need to be generated, that is, the number of blocks that differ from the latest local block
        */
        int yellowCount = 0;

        /*
        If the current round is the same as the latest local block, the difference between the block index of the current node and the latest block in this round minus one is the number of yellow card transactions that need to be generated
        If the current round is the same as the latest local block, then the difference between the block subscript of the current node and the latest block in this round is reduced by one,
        that is, the number of optical beat transactions that need to be generated.
        */
        if (self.getRoundIndex() == preBlockRoundData.getRoundIndex() && self.getPackingIndexOfRound() != preBlockRoundData.getPackingIndexOfRound() + 1) {
            yellowCount = self.getPackingIndexOfRound() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }

        /*
        If the current round is not the same as the local latest block, and the current node is not the first block to be generated in this round, or if the local latest block is not the last block to be generated in its round
        So the number of yellow cards is：Number of blocks produced in the previous round-Local latest block output index+Current node block index-1
        If the current round is not the same as the latest local block, and the current node is not the first block in this round, or the latest local block is not the last block in its round.
        The yellow card number is: the number of blocks out in the last round - local latest block out subscript + current node out block subscript - 1
        */
        if (self.getRoundIndex() != preBlockRoundData.getRoundIndex() && (self.getPackingIndexOfRound() != 1 || preBlockRoundData.getPackingIndexOfRound() != preBlockRoundData.getConsensusMemberCount())) {
            yellowCount = self.getPackingIndexOfRound() + preBlockRoundData.getConsensusMemberCount() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }
        if (yellowCount == 0) {
            return null;
        }
        List<byte[]> addressList = new ArrayList<>();
        MeetingMember member;
        MeetingRound preRound;
        for (int i = 1; i <= yellowCount; i++) {
            int index = self.getPackingIndexOfRound() - i;
            /*
            Yellow cards to be generated in this round
            Yellow cards to be generated in this round
            */
            if (index > 0) {
                member = round.getMember(index);
                if (member.getAgent() == null || member.getAgent().getDelHeight() > 0 || member.getAgent().getDeposit().equals(BigInteger.ZERO)) {
                    continue;
                }
                addressList.add(member.getAgent().getAgentAddress());
            }
            /*
            Yellow cards that need to be generated in the previous round
            Yellow cards needed to be generated in the last round
            */
            else {
                preRound = round.getPreRound();
                if (preRound == null) {
                    /*
                     * findroundThe first block in the previous round
                     * */
                    if (chain.getRoundList().size() > 0) {
                        preRound = roundManager.getRoundByIndex(chain, round.getIndex()-1);
                    }
                    if(preRound == null){
                        preRound = roundManager.getPreRound(chain, round.getIndex());
                    }
                }
                if(preRound != null){
                    member = preRound.getMember(index + preRound.getMemberCount());
                    if (member.getAgent() == null || member.getAgent().getDelHeight() > 0 || member.getAgent().getDeposit().equals(BigInteger.ZERO)) {
                        continue;
                    }
                    addressList.add(member.getAgent().getAgentAddress());
                }
            }
        }
        if (addressList.isEmpty()) {
            return null;
        }
        Transaction punishTx = new Transaction(TxType.YELLOW_PUNISH);
        YellowPunishData data = new YellowPunishData();
        data.setAddressList(addressList);
        punishTx.setTxData(data.serialize());
        punishTx.setTime(self.getPackEndTime());
        punishTx.setHash(NulsHash.calcHash(punishTx.serializeForHash()));
        return punishTx;
    }

    /**
     * Conflict detection between packaged transactions and red card transactions
     * Conflict Detection of UnPackaged Trading and Red Card Trading
     */
    private void conflictValid(Chain chain, List<Transaction> txList) throws NulsException {
        /*
         * Address for red card punishment
         * */
        Set<String> redPunishAddressSet = redPunishAddressSet(chain);

        if(redPunishAddressSet.isEmpty()){
            return;
        }

        Iterator<Transaction> iterator = txList.iterator();
        Transaction tx;

        /*
         * Invalid nodeHash
         * */
        Set<NulsHash> invalidAgentTxHash = new HashSet<>();

        /*
         * Invalid transaction to join consensus transactionHash
         * */
        Set<NulsHash> invalidDepositTxHash = new HashSet<>();
        while (iterator.hasNext()) {
            tx = iterator.next();
            switch (tx.getType()) {
                case TxType.REGISTER_AGENT:
                    Agent agent = new Agent();
                    agent.parse(tx.getTxData(), 0);
                    if (redPunishAddressSet.contains(HexUtil.encode(agent.getPackingAddress())) || redPunishAddressSet.contains(HexUtil.encode(agent.getAgentAddress()))) {
                        invalidAgentTxHash.add(agent.getTxHash());
                        iterator.remove();
                    }
                    break;
                case TxType.STOP_AGENT:
                    StopAgent stopAgent = new StopAgent();
                    stopAgent.parse(tx.getTxData(), 0);
                    if (invalidAgentTxHash.contains(stopAgent.getCreateTxHash())) {
                        iterator.remove();
                    }
                    break;
                case TxType.DEPOSIT:
                    Deposit deposit = new Deposit();
                    deposit.parse(tx.getTxData(), 0);
                    if (invalidAgentTxHash.contains(deposit.getAgentHash())) {
                        invalidDepositTxHash.add(deposit.getTxHash());
                        iterator.remove();
                    }
                    break;
                case TxType.CANCEL_DEPOSIT:
                    CancelDeposit cancelDeposit = new CancelDeposit();
                    cancelDeposit.parse(tx.getTxData(), 0);
                    if (invalidDepositTxHash.contains(cancelDeposit.getJoinTxHash())) {
                        iterator.remove();
                    }
                    break;
                default:
                    break;
            }
        }
        txList.addAll(chain.getRedPunishTransactionList());
        chain.getRedPunishTransactionList().clear();
    }

    /**
     * Red Card Penalty List
     * Red Card Punishment List
     */
    private Set<String> redPunishAddressSet(Chain chain) throws NulsException {
        Set<String> redPunishAddressSet = new HashSet<>();
        Transaction redPunishTx;
        RedPunishData redPunishData = new RedPunishData();
        Iterator<Transaction> iterator = chain.getRedPunishTransactionList().iterator();
        while (iterator.hasNext()){
            redPunishTx = iterator.next();
            redPunishData.parse(redPunishTx.getTxData(), 0);
            Agent agent = agentManager.getAgentByAgentAddress(chain, redPunishData.getAddress());
            if(agent == null || agent.getDelHeight() > 0){
                iterator.remove();
            }else {
                redPunishAddressSet.add(HexUtil.encode(redPunishData.getAddress()));
            }
        }
        return redPunishAddressSet;
    }

    public boolean redPunishCommit(Transaction tx, Chain chain, BlockHeader blockHeader) throws NulsException {
        long blockHeight = blockHeader.getHeight();
        int chainId = chain.getConfig().getChainId();
        RedPunishData punishData = new RedPunishData();
        punishData.parse(tx.getTxData(), 0);
        BlockExtendsData roundData = blockHeader.getExtendsData();
        PunishLogPo punishLogPo = new PunishLogPo();
        punishLogPo.setAddress(punishData.getAddress());
        punishLogPo.setHeight(blockHeight);
        punishLogPo.setRoundIndex(roundData.getRoundIndex());
        punishLogPo.setTime(tx.getTime());
        punishLogPo.setType(PunishType.RED.getCode());
        punishLogPo.setEvidence(punishData.getEvidence());
        punishLogPo.setReasonCode(punishData.getReasonCode());

        /*
        Find the penalized node
        Find the punished node
         */
        List<AgentPo> agentList = agentStorageService.getList(chainId);
        AgentPo agent = null;
        for (AgentPo agentPo : agentList) {
            if (agentPo.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(agentPo.getAgentAddress(), punishLogPo.getAddress())) {
                agent = agentPo;
                break;
            }
        }
        if (null == agent) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }

        /*
        Find the delegate of the penalized node
        Delegation to Find Penalized Nodes
         */
        List<DepositPo> depositPoList = depositStorageService.getList(chainId);
        List<DepositPo> updatedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            if (po.getDelHeight() >= 0) {
                continue;
            }
            if (!po.getAgentHash().equals(agent.getHash())) {
                continue;
            }
            po.setDelHeight(blockHeight);
            boolean b = depositStorageService.save(po, chainId);
            if (!b) {
                for (DepositPo po2 : updatedList) {
                    po2.setDelHeight(-1);
                    this.depositStorageService.save(po2, chainId);
                }
                throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
            }
            updatedList.add(po);
        }
        /*
         * Save red card penalty information
         * */
        boolean success = punishStorageService.save(punishLogPo, chainId);
        if (!success) {
            for (DepositPo po2 : updatedList) {
                po2.setDelHeight(-1);
                this.depositStorageService.save(po2, chainId);
            }
            throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
        }
        /*
         * Modify penalty node information
         * */
        AgentPo agentPo = agent;
        agentPo.setDelHeight(blockHeight);
        success = agentStorageService.save(agentPo, chainId);
        if (!success) {
            for (DepositPo po2 : updatedList) {
                po2.setDelHeight(-1);
                this.depositStorageService.save(po2, chainId);
            }
            this.punishStorageService.delete(punishLogPo.getKey(), chainId);
            throw new NulsException(ConsensusErrorCode.SAVE_FAILED);

        }

        /*
         * Update cache
         * */
        if (!updatedList.isEmpty()) {
            for (DepositPo depositPo : updatedList) {
                depositManager.updateDeposit(chain, depositManager.poToDeposit(depositPo));
            }
        }
        chain.getRedPunishList().add(punishLogPo);
        agentManager.updateAgent(chain, agentManager.poToAgent(agentPo));
        return true;
    }

    public boolean redPunishRollback(Transaction tx, Chain chain, BlockHeader blockHeader) throws NulsException {
        long blockHeight = blockHeader.getHeight();
        int chainId = chain.getConfig().getChainId();
        RedPunishData punishData = new RedPunishData();
        punishData.parse(tx.getTxData(), 0);
        /*
        Find the penalized node
        Find the punished node
         */
        List<AgentPo> agentList = agentStorageService.getList(chainId);
        AgentPo agent = null;
        for (AgentPo agentPo : agentList) {
            if (agentPo.getDelHeight() <= 0) {
                continue;
            }
            if (Arrays.equals(agentPo.getAgentAddress(), punishData.getAddress())) {
                agent = agentPo;
                break;
            }
        }
        if (null == agent) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }

        /*
        Find the delegate of the penalized node
        Delegation to Find Penalized Nodes
         */
        List<DepositPo> depositPoList = depositStorageService.getList(chainId);
        List<DepositPo> updatedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            if(!po.getAgentHash().equals(agent.getHash())){
                continue;
            }
            if (po.getDelHeight() == blockHeight) {
                po.setDelHeight(-1);
            }
            boolean success = this.depositStorageService.save(po, chainId);
            if (!success) {
                for (DepositPo po2 : updatedList) {
                    po2.setDelHeight(blockHeight);
                    this.depositStorageService.save(po2, chainId);
                }
                throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            updatedList.add(po);
        }

        AgentPo agentPo = agent;
        agentPo.setDelHeight(-1L);
        boolean success = agentStorageService.save(agentPo, chainId);
        if (!success) {
            for (DepositPo po2 : depositPoList) {
                po2.setDelHeight(blockHeight);
                this.depositStorageService.save(po2, chainId);
            }
            throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
        }

        byte[] key = ByteUtils.concatenate(punishData.getAddress(), new byte[]{PunishType.RED.getCode()}, SerializeUtils.uint64ToByteArray(blockHeight), new byte[]{0});
        success = punishStorageService.delete(key, chainId);
        if (!success) {
            for (DepositPo po2 : depositPoList) {
                po2.setDelHeight(blockHeight);
                this.depositStorageService.save(po2, chainId);
            }
            agentPo.setDelHeight(blockHeight);
            agentStorageService.save(agentPo, chainId);
            throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
        }

        /*
         * Modify cache
         * */
        if (!updatedList.isEmpty()) {
            for (DepositPo po2 : updatedList) {
                depositManager.updateDeposit(chain, depositManager.poToDeposit(po2));
            }
        }
        agentManager.updateAgent(chain, agentManager.poToAgent(agentPo));
        return true;
    }

    public boolean yellowPunishCommit(Transaction tx, Chain chain, BlockHeader blockHeader) throws NulsException {
        YellowPunishData punishData = new YellowPunishData();
        punishData.parse(tx.getTxData(), 0);
        BlockExtendsData roundData = blockHeader.getExtendsData();
        List<PunishLogPo> savedList = new ArrayList<>();
        int index = 1;
        int chainId = chain.getConfig().getChainId();
        for (byte[] address : punishData.getAddressList()) {
            PunishLogPo po = new PunishLogPo();
            po.setAddress(address);
            po.setHeight(blockHeader.getHeight());
            po.setRoundIndex(roundData.getRoundIndex());
            po.setTime(tx.getTime());
            po.setIndex(index++);
            po.setType(PunishType.YELLOW.getCode());
            boolean result = punishStorageService.save(po, chainId);
            if (!result) {
                for (PunishLogPo punishLogPo : savedList) {
                    punishStorageService.delete(getPoKey(punishLogPo.getAddress(), PunishType.YELLOW.getCode(), punishLogPo.getHeight(), punishLogPo.getIndex()), chainId);
                }
                savedList.clear();
                throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
            } else {
                savedList.add(po);
            }
        }
        chain.getYellowPunishList().addAll(savedList);
        return true;
    }

    public boolean yellowPunishRollback(Transaction tx, Chain chain, BlockHeader blockHeader) throws NulsException {
        long blockHeight = blockHeader.getHeight();
        YellowPunishData punishData = new YellowPunishData();
        punishData.parse(tx.getTxData(), 0);
        List<PunishLogPo> deletedList = new ArrayList<>();
        BlockExtendsData roundData = blockHeader.getExtendsData();
        int deleteIndex = 1;
        int chainId = chain.getConfig().getChainId();
        for (byte[] address : punishData.getAddressList()) {
            boolean result = punishStorageService.delete(getPoKey(address, PunishType.YELLOW.getCode(), blockHeight, deleteIndex), chainId);
            if (!result) {
                for (PunishLogPo po : deletedList) {
                    punishStorageService.save(po, chainId);
                }
                deletedList.clear();
                throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
            } else {
                PunishLogPo po = new PunishLogPo();
                po.setAddress(address);
                po.setHeight(blockHeight);
                po.setRoundIndex(roundData.getRoundIndex());
                po.setTime(tx.getTime());
                po.setIndex(deleteIndex);
                po.setType(PunishType.YELLOW.getCode());
                deletedList.add(po);
            }
            deleteIndex++;
        }
        chain.getYellowPunishList().removeAll(deletedList);
        return true;
    }

    /**
     * Get fixed formatkey
     */
    private byte[] getPoKey(byte[] address, byte type, long blockHeight, int index) {
        return ByteUtils.concatenate(address, new byte[]{type}, SerializeUtils.uint64ToByteArray(blockHeight), new VarInt(index).encode());
    }
}
