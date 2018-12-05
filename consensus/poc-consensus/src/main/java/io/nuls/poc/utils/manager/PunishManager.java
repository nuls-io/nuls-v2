package io.nuls.poc.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.consensus.Evidence;
import io.nuls.poc.model.bo.consensus.PunishReasonEnum;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.utils.compare.EvidenceComparator;
import io.nuls.poc.utils.util.ConsensusUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.util.*;

public class PunishManager {
    /**
     * 记录各条链出块地址PackingAddress，同一个高度发出了两个不同的块的证据
     * 下一轮正常则清零， 连续3轮将会被红牌惩罚
     * Record the address of each chain out block Packing Address, and the same height gives evidence of two different blocks.
     * The next round of normal will be cleared, and three consecutive rounds will be punished by red cards.
     */
    private Map<Integer,Map<String, List<Evidence>>> bifurcationEvidenceMap = new HashMap<>();

    /**
     * 保存本节点需打包的红牌交易,节点打包时需把该集合中所有红牌交易打包并删除
     * To save the red card transactions that need to be packaged by the node,
     * the node should pack and delete all the red card transactions in the set when packing.
     * */
    private Map<Integer,Transaction> redPunishTransactionMap = new ConcurrentHashMap<>();

    public static PunishManager instance = null;
    private PunishManager() { }
    private static Integer LOCK = 0;
    public static PunishManager getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new PunishManager();
            }
            return instance;
        }
    }

    /**
     * 添加分叉证据
     * Adding bifurcation evidence
     *
     * @param chainId
     * @param firstHeader
     * @param secondHeader
     * @deprecated
     * */
    public void addEvidenceRecord(int chainId, BlockHeader firstHeader, BlockHeader secondHeader)throws NulsException{
        /*
        找到分叉的节点
        Find the bifurcated nodes
        */
        Agent agent = null;
        for (Agent a:ConsensusManager.getInstance().getAllAgentMap().get(chainId)) {
            if (a.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(a.getPackingAddress(), firstHeader.getPackingAddress())) {
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
        boolean isRedPunish = isRedPunish(chainId,firstHeader,secondHeader);
        if(isRedPunish){
            createRedPunishTransaction(chainId,agent);
        }
    }

    /**
     * 添加双花红牌记录
     * Add Double Flower Red Card Record
     *
     * @param chainId
     * @param txs
     * @param block
     * @deprecated
     * */
    public void addDoubleSpendRecord(int chainId, List<Transaction> txs,Block block)throws NulsException {
        /*
        找到双花交易的节点
        Find the bifurcated nodes
        */
        byte[] packingAddress = AddressTool.getAddress(block.getHeader().getBlockSignature().getPublicKey(),(short)chainId);
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chainId);
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
            CoinData coinData = ConsensusUtil.getStopAgentCoinData(chainId,ConfigManager.config_map.get(chainId).getAssetsId(), agent, redPunishTransaction.getTime() + ConfigManager.config_map.get(chainId).getRedPublishLockTime());
            redPunishTransaction.setCoinData(coinData.serialize());
            redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
            redPunishTransactionMap.put(chainId,redPunishTransaction);
        }catch (IOException e){
            Log.error(e);
        }
    }

    /**
     * todo
     * 跟新惩罚证据列表并验证节点是否应该给红牌惩罚
     * Follow the new penalty evidence list and verify whether the node should give a red card penalty
     *
     * @param chainId
     * @param firstHeader
     * @param secondHeader
     * @return boolean
     * */
    private boolean isRedPunish(int chainId, BlockHeader firstHeader, BlockHeader secondHeader){
        //验证出块地址PackingAddress，记录分叉的连续次数，如达到连续3轮则红牌惩罚
        String packingAddress = AddressTool.getStringAddressByBytes(firstHeader.getPackingAddress());
        BlockExtendsData extendsData = new BlockExtendsData(firstHeader.getExtend());
        long currentRoundIndex = extendsData.getRoundIndex();
        Map<String, List<Evidence>> currentChainEvidences = bifurcationEvidenceMap.get(chainId);
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
            currentChainEvidences = new HashMap<>();
        }

        /*
        查询本地是否存在当前节点的分叉证据，如果不存在则添加
        Query whether there is bifurcation evidence for the current node locally, and if not add
        */
        if(!currentChainEvidences.containsKey(packingAddress)){
            List<Evidence> list = new ArrayList<>();
            list.add(evidence);
            currentChainEvidences.put(packingAddress, list);
            bifurcationEvidenceMap.put(chainId,currentChainEvidences);
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
                bifurcationEvidenceMap.put(chainId,currentChainEvidences);
                return false;
            }else{
                list.add(evidence);
                currentChainEvidences.put(packingAddress,list);
                bifurcationEvidenceMap.put(chainId,currentChainEvidences);
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
     * @param chainId
     * @param agent
     * @deprecated
     * */
    private void createRedPunishTransaction(int chainId, Agent agent)throws NulsException{
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
            Map<String, List<Evidence>> currentChainEvidences = bifurcationEvidenceMap.get(chainId);
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
            Log.error(e);
        }
        try {
            redPunishData.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
            redPunishTransaction.setTxData(redPunishData.serialize());
            redPunishTransaction.setTime(txTime);
            /*
            组装CoinData
            Assemble CoinData
            */
            CoinData coinData = ConsensusUtil.getStopAgentCoinData(chainId,ConfigManager.config_map.get(chainId).getAssetsId(),agent,redPunishTransaction.getTime()+ConfigManager.config_map.get(chainId).getRedPublishLockTime());
            redPunishTransaction.setCoinData(coinData.serialize());
            redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
            /*
            缓存红牌交易
            Assemble Red Punish transaction
            */
            redPunishTransactionMap.put(chainId,redPunishTransaction);
        } catch (IOException e) {
            Log.error(e);
        }
    }

    public Map<Integer, Map<String, List<Evidence>>> getBifurcationEvidenceMap() {
        return bifurcationEvidenceMap;
    }

    public void setBifurcationEvidenceMap(Map<Integer, Map<String, List<Evidence>>> bifurcationEvidenceMap) {
        this.bifurcationEvidenceMap = bifurcationEvidenceMap;
    }

    public Map<Integer, Transaction> getRedPunishTransactionMap() {
        return redPunishTransactionMap;
    }

    public void setRedPunishTransactionMap(Map<Integer, Transaction> redPunishTransactionMap) {
        this.redPunishTransactionMap = redPunishTransactionMap;
    }
}
