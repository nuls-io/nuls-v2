package io.nuls.poc.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.consensus.Evidence;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.*;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.storage.PunishStorageService;
import io.nuls.poc.utils.compare.EvidenceComparator;
import io.nuls.poc.utils.compare.PunishLogComparator;
import io.nuls.poc.utils.enumeration.PunishReasonEnum;
import io.nuls.poc.utils.enumeration.PunishType;
import io.nuls.tools.basic.VarInt;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.DoubleUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.*;

/**
 * 惩罚信息管理，用于惩罚数据证据的记录，红黄牌惩罚生成等
 * Punishment information management, records of punishment data evidence, red and yellow card punishment generation, etc.
 *
 * @author tag
 * 2018/12/5
 * */
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
    /**
     * 加载所有的红牌信息和最近X黃牌数据到缓存
     * Load all red card information and latest X rotation card data to the cache
     *
     * @param chain 链信息/chain info
     * */
    public void loadPunishes(Chain chain) throws Exception{
        BlockHeader blockHeader = chain.getNewestHeader();
        if (null == blockHeader) {
            return;
        }
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        long breakRoundIndex = roundData.getRoundIndex() - ConsensusConstant.INIT_PUNISH_OF_ROUND_COUNT;
        List<PunishLogPo> punishLogList= punishStorageService.getPunishList(chain.getConfig().getChainId());
        List<PunishLogPo> redPunishList = new ArrayList<>();
        List<PunishLogPo> yellowPunishList = new ArrayList<>();
        for (PunishLogPo po:punishLogList) {
            if(po.getType() == PunishType.RED.getCode()){
                redPunishList.add(po);
            }else{
                if(po.getRoundIndex() <= breakRoundIndex){
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
     * 清理黄牌数据
     * Clean up yellow card data
     *
     * @param chain 链信息/chain info
     * */
    public void clear(Chain chain){
        BlockHeader blockHeader = chain.getNewestHeader();
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        Iterator<PunishLogPo> yellowIterator = chain.getYellowPunishList().iterator();
        while (yellowIterator.hasNext()){
            PunishLogPo po = yellowIterator.next();
            if (po.getRoundIndex() < roundData.getRoundIndex() - ConsensusConstant.INIT_PUNISH_OF_ROUND_COUNT) {
                yellowIterator.remove();
            }
        }
    }



    /**
     * 添加分叉证据
     * Adding bifurcation evidence
     *
     * @param chain
     * @param firstHeader
     * @param secondHeader
     * */
    public void addEvidenceRecord(Chain chain, BlockHeader firstHeader, BlockHeader secondHeader)throws NulsException{
        /*
        找到分叉的节点
        Find the bifurcated nodes
        */
        Agent agent = null;
        for (Agent a:chain.getAgentList()) {
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
        验证节点是否应该受红牌惩罚
        Verify whether the node should be punished by a red card
        */
        boolean isRedPunish = isRedPunish(chain,firstHeader,secondHeader);
        if(isRedPunish){
            createRedPunishTransaction(chain,agent);
        }
    }

    /**
     * 添加双花红牌记录
     * Add Double Flower Red Card Record
     *
     * @param chain
     * @param txs
     * @param block
     * */
    public void addDoubleSpendRecord(Chain chain, List<Transaction> txs,Block block)throws NulsException {
        /*
        找到双花交易的节点
        Find the bifurcated nodes
        */
        byte[] packingAddress = AddressTool.getAddress(block.getHeader().getBlockSignature().getPublicKey(),(short)chain.getConfig().getChainId());
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
        if(agent == null){
            return;
        }
        try {
            /*
            组装双花红牌交易
            Assembled Double Flower Red Card Trading
            */
            Transaction redPunishTransaction = new Transaction(ConsensusConstant.TX_TYPE_RED_PUNISH);
            RedPunishData redPunishData = new RedPunishData();
            redPunishData.setAddress(agent.getAgentAddress());
            SmallBlock smallBlock = new SmallBlock();
            smallBlock.setHeader(block.getHeader());
            smallBlock.setTxHashList(block.getTxHashList());
            for (Transaction tx : txs) {
                smallBlock.addBaseTx(tx);
            }
            redPunishData.setEvidence(smallBlock.serialize());
            redPunishData.setReasonCode(PunishReasonEnum.DOUBLE_SPEND.getCode());
            redPunishTransaction.setTxData(redPunishData.serialize());
            redPunishTransaction.setTime(smallBlock.getHeader().getTime());
            CoinData coinData = coinDataManager.getStopAgentCoinData(chain, agent, redPunishTransaction.getTime() + chain.getConfig().getRedPublishLockTime());
            redPunishTransaction.setCoinData(coinData.serialize());
            redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
            chain.getRedPunishTransactionList().add(redPunishTransaction);
        }catch (IOException e){
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e.getMessage());
        }
    }

    /**
     * 更新惩罚证据列表并验证节点是否应该给红牌惩罚
     * Follow the new penalty evidence list and verify whether the node should give a red card penalty
     *
     * @param chain
     * @param firstHeader
     * @param secondHeader
     * @return boolean
     * */
    private boolean isRedPunish(Chain chain, BlockHeader firstHeader, BlockHeader secondHeader)throws NulsException{
        //验证出块地址PackingAddress，记录分叉的连续次数，如达到连续3轮则红牌惩罚
        String packingAddress = AddressTool.getStringAddressByBytes(firstHeader.getPackingAddress(chain.getConfig().getChainId()));
        BlockExtendsData extendsData = new BlockExtendsData(firstHeader.getExtend());
        long currentRoundIndex = extendsData.getRoundIndex();
        Map<String, List<Evidence>> currentChainEvidences = chain.getEvidenceMap();
        /*
        首先生成一个证据
        First generate an evidence
        */
        Evidence evidence = new Evidence(currentRoundIndex, firstHeader, secondHeader);

        /*
        判断是否有当前链的惩罚记录，如果不存在则添加
        Determine if there is a penalty record for the current chain, and add if not
        */
        if(currentChainEvidences == null){
            currentChainEvidences = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        }

        /*
        查询本地是否存在当前节点的分叉证据，如果不存在则添加
        Query whether there is bifurcation evidence for the current node locally, and if not add
        */
        if(!currentChainEvidences.containsKey(packingAddress)){
            List<Evidence> list = new ArrayList<>();
            list.add(evidence);
            currentChainEvidences.put(packingAddress, list);
            return false;
        }
        /*
        1.如果存在该节点分叉证据，则判断当前分叉轮次与该节点上一次分叉轮次是否连续
        2.如果连续则判断该节点连续分叉数是否已经达到红牌惩罚数，如果达到这生成红牌惩罚交易，如果不连续则清空该节点分叉记录
        1. If there is evidence of bifurcation of the node, it is judged whether the current bifurcation wheel number is continuous with the previous bifurcation wheel number of the node.
        2. If continuous, judge whether the number of consecutive bifurcations of the node has reached the number of red card penalties. If the number of consecutive bifurcations reaches the number of red card penalties,
           generate a red card penalty transaction, and if not, empty the bifurcation records of the node.
        */
        else{
            List<Evidence> list = currentChainEvidences.get(packingAddress);
            Collections.sort(list,new EvidenceComparator());
            long preRoundIndex = list.get(list.size()-1).getRoundIndex();
            if(currentRoundIndex - preRoundIndex != 1){
                currentChainEvidences.remove(packingAddress);
                return false;
            }else{
                list.add(evidence);
                currentChainEvidences.put(packingAddress,list);
                if(list.size()>= ConsensusConstant.REDPUNISH_BIFURCATION){
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * 创建红牌交易并放入缓存中
     * Create a red card transaction and put it in the cache
     *
     * @param chain
     * @param agent
     * */
    private void createRedPunishTransaction(Chain chain, Agent agent)throws NulsException{
        Transaction redPunishTransaction = new Transaction(ConsensusConstant.TX_TYPE_RED_PUNISH);
        RedPunishData redPunishData = new RedPunishData();
        redPunishData.setAddress(agent.getAgentAddress());
        long txTime = 0;
        try{
            /*
            连续3轮 每一轮两个区块头作为证据 一共 3 * 2 个区块头作为证据
            For three consecutive rounds, two blocks in each round are used as evidence, and a total of 3*2 blocks are used as evidence.
            */
            byte[][] headers = new byte[ConsensusConstant.REDPUNISH_BIFURCATION * 2][];
            Map<String, List<Evidence>> currentChainEvidences = chain.getEvidenceMap();
            List<Evidence> list = currentChainEvidences.get(AddressTool.getStringAddressByBytes(agent.getPackingAddress()));
            for (int i = 0; i < list.size() && i < ConsensusConstant.REDPUNISH_BIFURCATION; i++) {
                Evidence evidence = list.get(i);
                int s = i * 2;
                headers[s] = evidence.getBlockHeader1().serialize();
                headers[++s] = evidence.getBlockHeader2().serialize();
                txTime = (evidence.getBlockHeader1().getTime()+evidence.getBlockHeader2().getTime())/2;
            }
            redPunishData.setEvidence(ByteUtils.concatenate(headers));
        }catch (IOException e){
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e.getMessage());
        }
        try {
            redPunishData.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
            redPunishTransaction.setTxData(redPunishData.serialize());
            redPunishTransaction.setTime(txTime);
            /*
            组装CoinData
            Assemble CoinData
            */
            CoinData coinData = coinDataManager.getStopAgentCoinData(chain,agent,redPunishTransaction.getTime()+chain.getConfig().getRedPublishLockTime());
            redPunishTransaction.setCoinData(coinData.serialize());
            redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
            /*
            缓存红牌交易
            Assemble Red Punish transaction
            */
            chain.getRedPunishTransactionList().add(redPunishTransaction);
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e.getMessage());
        }
    }

    /**
     * 组装红/黄牌交易
     * Assemble Red/Yellow Transaction
     *
     * @param chain      Chain info
     * @param bestBlock  Latest local block/本地最新区块
     * @param txList     A list of transactions to be packaged/需打包的交易列表
     * @param self       Local Node Packing Information/本地节点打包信息
     * @param round      Local latest rounds information/本地最新轮次信息
     */
    public void punishTx(Chain chain, BlockHeader bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        Transaction yellowPunishTransaction = createYellowPunishTx(bestBlock, self, round);
        if (null == yellowPunishTransaction) {
            return;
        }
        txList.add(yellowPunishTransaction);
        /*
        当连续100个黄牌时，给出一个红牌
        When 100 yellow CARDS in a row, give a red card.
        */
        YellowPunishData yellowPunishData = new YellowPunishData();
        yellowPunishData.parse(yellowPunishTransaction.getTxData(),0);
        List<byte[]> addressList = yellowPunishData.getAddressList();
        Set<Integer> punishedSet = new HashSet<>();
        for (byte[] address : addressList) {
            MeetingMember member = round.getMemberByAgentAddress(address);
            if (null == member) {
                member = round.getPreRound().getMemberByAgentAddress(address);
            }
            if (DoubleUtils.compare(member.getAgent().getCreditVal(), ConsensusConstant.RED_PUNISH_CREDIT_VAL) == -1) {
                if (!punishedSet.add(member.getPackingIndexOfRound())) {
                    continue;
                }
                if (member.getAgent().getDelHeight() > 0L) {
                    continue;
                }
                Transaction redPunishTransaction = new Transaction(ConsensusConstant.TX_TYPE_RED_PUNISH);
                RedPunishData redPunishData = new RedPunishData();
                redPunishData.setAddress(address);
                redPunishData.setReasonCode(PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode());
                redPunishTransaction.setTxData(redPunishData.serialize());
                redPunishTransaction.setTime(self.getPackEndTime());
                CoinData coinData = coinDataManager.getStopAgentCoinData(chain, redPunishData.getAddress(), redPunishTransaction.getTime() + chain.getConfig().getRedPublishLockTime());
                redPunishTransaction.setCoinData(coinData.serialize());
                redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
                chain.getRedPunishTransactionList().add(redPunishTransaction);
            }
        }
        /*
        * 待打包交易与红牌交易冲突检测
        * Conflict Detection of UnPackaged Trading and Red Card Trading
        * */
        if(chain.getRedPunishTransactionList().size() > 0){
            conflictValid(chain,txList);
        }
    }

    /**
     * 组装黄牌
     * Assemble Yellow Transaction
     *
     * @param preBlock  Latest local block/本地最新区块
     * @param self      A list of transactions to be packaged/需打包的交易列表
     * @param round     Local latest rounds information/本地最新轮次信息
     * @return  Transaction
     */
    public Transaction createYellowPunishTx(BlockHeader preBlock, MeetingMember self, MeetingRound round) throws IOException {
        BlockExtendsData preBlockRoundData = new BlockExtendsData(preBlock.getExtend());
        /*
        如果本节点当前打包轮次比本地最新区块的轮次大一轮以上则返回不生成黄牌交易
        If the current packing rounds of this node are more than one round larger than the rounds of the latest local block,
        no yellow card transaction will be generated.
        */
        if (self.getRoundIndex() - preBlockRoundData.getRoundIndex() > 1) {
            return null;
        }
        /*
        计算需要生成的黄牌数量，即当前出的块与本地最新区块之间相差的区块数
        Calculate the number of yellow cards that need to be generated, that is, the number of blocks that differ from the latest local block
        */
        int yellowCount = 0;

        /*
        如果当前轮次与本地最新区块是同一轮次，则当前节点在本轮次中的出块下标与最新区块之间的差值减一即为需要生成的黄牌交易数
        If the current round is the same as the latest local block, then the difference between the block subscript of the current node and the latest block in this round is reduced by one,
        that is, the number of optical beat transactions that need to be generated.
        */
        if (self.getRoundIndex() == preBlockRoundData.getRoundIndex() && self.getPackingIndexOfRound() != preBlockRoundData.getPackingIndexOfRound() + 1) {
            yellowCount = self.getPackingIndexOfRound() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }

        /*
        如果当前轮次与本地最新区块不是同一轮次，且当前节点不是本轮次中第一个出块的或则本地最新区块不为它所在轮次中最后一个出块的
        则黄牌数为：上一轮次出块数-本地最新区块出块下标+当前节点出块下标-1
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
            本轮次需生成的黄牌
            Yellow cards to be generated in this round
            */
            if (index > 0) {
                member = round.getMember(index);
                if (member.getAgent() == null || member.getAgent().getDelHeight() > 0) {
                    continue;
                }
                addressList.add(member.getAgent().getAgentAddress());
            }
            /*
            上一轮需要生成的黄牌
            Yellow cards needed to be generated in the last round
            */
            else {
                preRound = round.getPreRound();
                member = preRound.getMember(index + preRound.getMemberCount());
                if (member.getAgent() == null || member.getAgent().getDelHeight() > 0) {
                    continue;
                }
                addressList.add(member.getAgent().getAgentAddress());
            }
        }
        if (addressList.isEmpty()) {
            return null;
        }
        Transaction punishTx = new Transaction(ConsensusConstant.TX_TYPE_YELLOW_PUNISH);
        YellowPunishData data = new YellowPunishData();
        data.setAddressList(addressList);
        punishTx.setTxData(data.serialize());
        punishTx.setTime(self.getPackEndTime());
        punishTx.setHash(NulsDigestData.calcDigestData(punishTx.serializeForHash()));
        return punishTx;
    }

    /**
     * 待打包交易与红牌交易冲突检测
     * Conflict Detection of UnPackaged Trading and Red Card Trading
     * */
    private void conflictValid(Chain chain,List<Transaction> txList)throws NulsException{
        Iterator<Transaction> iterator = txList.iterator();
        Transaction tx;
        /*
        * 红牌惩罚的地址
        * */
        Set<String> redPunishAddressSet = redPunishAddressSet(chain);

        /*
        * 无效的节点Hash
        * */
        Set<NulsDigestData> invalidAgentTxHash = new HashSet<>();

        /*
        * 无效的加入共识交易的交易Hash
        * */
        Set<NulsDigestData> invalidDepositTxHash = new HashSet<>();
        while (iterator.hasNext()) {
            tx = iterator.next();
            switch (tx.getType()){
                case ConsensusConstant.TX_TYPE_REGISTER_AGENT:
                    Agent agent = new Agent();
                    agent.parse(tx.getTxData(),0);
                    if(redPunishAddressSet.contains(HexUtil.encode(agent.getPackingAddress())) || redPunishAddressSet.contains(HexUtil.encode(agent.getAgentAddress()))){
                        invalidAgentTxHash.add(agent.getTxHash());
                        iterator.remove();
                    }
                    break;
                case ConsensusConstant.TX_TYPE_STOP_AGENT:
                    StopAgent stopAgent = new StopAgent();
                    stopAgent.parse(tx.getTxData(),0);
                    if(invalidAgentTxHash.contains(stopAgent.getCreateTxHash())){
                        iterator.remove();
                    }
                    break;
                case ConsensusConstant.TX_TYPE_JOIN_CONSENSUS:
                    Deposit deposit = new Deposit();
                    deposit.parse(tx.getTxData(),0);
                    if(invalidAgentTxHash.contains(deposit.getAgentHash())){
                        invalidDepositTxHash.add(deposit.getTxHash());
                        iterator.remove();
                    }
                    break;
                case ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT:
                    CancelDeposit cancelDeposit = new CancelDeposit();
                    cancelDeposit.parse(tx.getTxData(),0);
                    if(invalidDepositTxHash.contains(cancelDeposit.getJoinTxHash())){
                        iterator.remove();
                    }
                    break;
                default:break;
            }
        }
        txList.addAll(chain.getRedPunishTransactionList());
        chain.getRedPunishTransactionList().clear();
    }

    /**
     * 红牌惩罚列表
     * Red Card Punishment List
     * */
    private Set<String> redPunishAddressSet(Chain chain)throws NulsException{
        Set<String> redPunishAddressSet = new HashSet<>();
        RedPunishData redPunishData = new RedPunishData();
        for (Transaction tx : chain.getRedPunishTransactionList()) {
            redPunishData.parse(tx.getTxData(),0);
            String addressHex = HexUtil.encode(redPunishData.getAddress());
            redPunishAddressSet.add(addressHex);
        }
        return  redPunishAddressSet;
    }

    public boolean redPunishCommit(Transaction tx,Chain chain,BlockHeader blockHeader)throws Exception{
        int chainId = chain.getConfig().getChainId();
        RedPunishData punishData = new RedPunishData();
        punishData.parse(tx.getTxData(),0);
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        PunishLogPo punishLogPo = new PunishLogPo();
        punishLogPo.setAddress(punishData.getAddress());
        punishLogPo.setHeight(tx.getBlockHeight());
        punishLogPo.setRoundIndex(roundData.getRoundIndex());
        punishLogPo.setTime(tx.getTime());
        punishLogPo.setType(PunishType.RED.getCode());
        punishLogPo.setEvidence(punishData.getEvidence());
        punishLogPo.setReasonCode(punishData.getReasonCode());

        /*
        找到被惩罚的节点
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
        找到被惩罚节点的委托
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
            po.setDelHeight(tx.getBlockHeight());
            boolean b = depositStorageService.save(po,chainId);
            if (!b) {
                for (DepositPo po2 : updatedList) {
                    po2.setDelHeight(-1);
                    this.depositStorageService.save(po2,chainId);
                    depositManager.updateDeposit(chain,depositManager.poToDeposit(po2));
                }
                return false;
            }
            depositManager.updateDeposit(chain,depositManager.poToDeposit(po));
            updatedList.add(po);
        }

        boolean success = punishStorageService.save(punishLogPo,chainId);
        if (!success) {
            for (DepositPo po2 : updatedList) {
                po2.setDelHeight(-1);
                this.depositStorageService.save(po2,chainId);
                depositManager.updateDeposit(chain,depositManager.poToDeposit(po2));
            }
            return false;
        }
        chain.getRedPunishList().add(punishLogPo);

        AgentPo agentPo = agent;
        agentPo.setDelHeight(tx.getBlockHeight());
        success = agentStorageService.save(agentPo,chainId);
        if (!success) {
            for (DepositPo po2 : updatedList) {
                po2.setDelHeight(-1);
                this.depositStorageService.save(po2,chainId);
                depositManager.updateDeposit(chain,depositManager.poToDeposit(po2));
            }
            this.punishStorageService.delete(punishLogPo.getKey(),chainId);
            chain.getRedPunishList().remove(punishLogPo);
            return false;
        }
        agentManager.updateAgent(chain,agentManager.poToAgent(agentPo));
        return true;
    }

    public boolean redPunishRollback(Transaction tx,Chain chain,BlockHeader blockHeader)throws Exception{
        int chainId = chain.getConfig().getChainId();
        RedPunishData punishData = new RedPunishData();
        punishData.parse(tx.getTxData(),0);
        /*
        找到被惩罚的节点
        Find the punished node
         */
        List<AgentPo> agentList = agentStorageService.getList(chainId);
        AgentPo agent = null;
        for (AgentPo agentPo : agentList) {
            if (agentPo.getDelHeight() > 0) {
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
        找到被惩罚节点的委托
        Delegation to Find Penalized Nodes
         */
        List<DepositPo> depositPoList = depositStorageService.getList(chainId);
        List<DepositPo> updatedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            po.setDelHeight(-1);
            boolean success = this.depositStorageService.save(po,chainId);
            if (!success) {
                for (DepositPo po2 : depositPoList) {
                    po2.setDelHeight(tx.getBlockHeight());
                    this.depositStorageService.save(po2,chainId);
                    depositManager.updateDeposit(chain,depositManager.poToDeposit(po2));
                }
                return false;
            }
            depositManager.updateDeposit(chain,depositManager.poToDeposit(po));
            updatedList.add(po);
        }

        AgentPo agentPo = agent;
        agentPo.setDelHeight(-1L);
        boolean success = agentStorageService.save(agentPo,chainId);
        if (!success) {
            for (DepositPo po2 : depositPoList) {
                po2.setDelHeight(tx.getBlockHeight());
                this.depositStorageService.save(po2,chainId);
                depositManager.updateDeposit(chain,depositManager.poToDeposit(po2));
            }
            return false;
        }
        agentManager.updateAgent(chain,agentManager.poToAgent(agentPo));

        byte[] key = ByteUtils.concatenate(punishData.getAddress(), new byte[]{PunishType.RED.getCode()}, SerializeUtils.uint64ToByteArray(tx.getBlockHeight()), new byte[]{0});
        success = punishStorageService.delete(key,chainId);
        if (!success) {
            for (DepositPo po2 : depositPoList) {
                po2.setDelHeight(tx.getBlockHeight());
                this.depositStorageService.save(po2,chainId);
                depositManager.updateDeposit(chain,depositManager.poToDeposit(po2));
            }
            agentPo.setDelHeight(tx.getBlockHeight());
            agentStorageService.save(agentPo,chainId);
            agentManager.updateAgent(chain,agentManager.poToAgent(agentPo));
            return false;
        }
        return true;
    }

    public boolean yellowPunishCommit(Transaction tx,Chain chain,BlockHeader blockHeader)throws NulsException{
        YellowPunishData punishData = new YellowPunishData();
        punishData.parse(tx.getTxData(),0);
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        List<PunishLogPo> savedList = new ArrayList<>();
        int index = 1;
        int chainId = chain.getConfig().getChainId();
        for (byte[] address : punishData.getAddressList()) {
            PunishLogPo po = new PunishLogPo();
            po.setAddress(address);
            po.setHeight(tx.getBlockHeight());
            po.setRoundIndex(roundData.getRoundIndex());
            po.setTime(tx.getTime());
            po.setIndex(index++);
            po.setType(PunishType.YELLOW.getCode());
            boolean result = punishStorageService.save(po,chainId);
            if (!result) {
                for (PunishLogPo punishLogPo : savedList) {
                    punishStorageService.delete(getPoKey(punishLogPo.getAddress(), PunishType.YELLOW.getCode(), punishLogPo.getHeight(), punishLogPo.getIndex()),chainId);
                }
                throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
            } else {
                savedList.add(po);
            }
        }
        chain.getYellowPunishList().addAll(savedList);
        return true;
    }

    public boolean yellowPunishRollback(Transaction tx,Chain chain,BlockHeader blockHeader)throws NulsException{
        YellowPunishData punishData = new YellowPunishData();
        punishData.parse(tx.getTxData(),0);
        List<PunishLogPo> deletedList = new ArrayList<>();
        BlockExtendsData roundData = new BlockExtendsData(blockHeader.getExtend());
        int deleteIndex = 1;
        int chainId = chain.getConfig().getChainId();
        for (byte[] address : punishData.getAddressList()) {
            boolean result = punishStorageService.delete(getPoKey(address, PunishType.YELLOW.getCode(), tx.getBlockHeight(), deleteIndex++),chainId);
            if (!result) {
                for (PunishLogPo po : deletedList) {
                    punishStorageService.save(po,chainId);
                }
                deletedList.clear();
                throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
            } else {
                PunishLogPo po = new PunishLogPo();
                po.setAddress(address);
                po.setHeight(tx.getBlockHeight());
                po.setRoundIndex(roundData.getRoundIndex());
                po.setTime(tx.getTime());
                po.setIndex(deleteIndex);
                po.setType(PunishType.YELLOW.getCode());
                deletedList.add(po);
            }
        }
        chain.getYellowPunishList().removeAll(deletedList);
        return true;
    }

    /**
     * 获取固定格式的key
     */
    private byte[] getPoKey(byte[] address, byte type, long blockHeight, int index) {
        return ByteUtils.concatenate(address, new byte[]{type}, SerializeUtils.uint64ToByteArray(blockHeight), new VarInt(index).encode());
    }
}
