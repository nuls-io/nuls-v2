package io.nuls.poc.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Evidence;
import io.nuls.poc.model.bo.consensus.PunishReasonEnum;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.utils.compare.EvidenceComparator;
import io.nuls.poc.utils.util.ConsensusUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.io.IOException;
import java.util.*;

public class PunishManager {
    /**
     * 记录各条链出块地址PackingAddress，同一个高度发出了两个不同的块的证据
     * 下一轮正常则清零， 连续3轮将会被红牌惩罚
     */
    private Map<Integer,Map<String, List<Evidence>>> bifurcationEvidenceMap = new HashMap<>();

    /**
     * 保存本节点需打包的红牌交易,节点打包时需把该集合中所有红牌交易打包并删除
     * */
    private Map<Integer,Transaction> redPunishTransactionMap = new HashMap<>();

    /**
     * 控制该类为单例模式
     * */
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
     * */
    public void addEvidenceRecord(int chain_id, BlockHeader firstHeader, BlockHeader secondHeader)throws NulsException{
        //找到分叉的节点
        Agent agent = null;
        for (Agent a:ConsensusManager.getInstance().getAllAgentMap().get(chain_id)) {
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
        //验证节点是否应该受红牌惩罚
        boolean isRedPunish = isRedPunish(chain_id,firstHeader,secondHeader);
        //创建红牌交易
        if(isRedPunish){
            createRedPunishTransaction(chain_id,agent);
        }
    }

    /**
     * 添加双花红牌记录
     * */
    public void addDoubleSpendRecord(int chain_id, List<Transaction> txs,Block block)throws NulsException {
        byte[] packingAddress = AddressTool.getAddress(block.getHeader().getBlockSignature().getPublicKey(),(short)chain_id);
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
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
            CoinData coinData = ConsensusUtil.getStopAgentCoinData(chain_id, agent, redPunishTransaction.getTime() + ConfigManager.config_map.get(chain_id).getRedPublish_lockTime());
            redPunishTransaction.setCoinData(coinData.serialize());
            redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
            redPunishTransactionMap.put(chain_id,redPunishTransaction);
        }catch (IOException e){
            Log.error(e);
        }
    }

    /**
     * 跟新惩罚证据列表并验证节点是否应该给红牌惩罚
     * */
    private boolean isRedPunish(int chain_id, BlockHeader firstHeader, BlockHeader secondHeader){
        //验证出块地址PackingAddress，记录分叉的连续次数，如达到连续3轮则红牌惩罚
        String packingAddress = AddressTool.getStringAddressByBytes(firstHeader.getPackingAddress());
        //首先生成一个证据
        BlockExtendsData extendsData = new BlockExtendsData(firstHeader.getExtend());
        long currentRoundIndex = extendsData.getRoundIndex();
        Map<String, List<Evidence>> currentChainEvidences = bifurcationEvidenceMap.get(chain_id);
        Evidence evidence = new Evidence(currentRoundIndex, firstHeader, secondHeader);
        //判断是否有当前链的惩罚记录，如果不存在则添加
        if(currentChainEvidences == null){
            currentChainEvidences = new HashMap<>();
        }
        //查询本地是否存在当前节点的分叉证据，如果不存在则添加
        if(!currentChainEvidences.containsKey(packingAddress)){
            List<Evidence> list = new ArrayList<>();
            list.add(evidence);
            currentChainEvidences.put(packingAddress, list);
            bifurcationEvidenceMap.put(chain_id,currentChainEvidences);
            return false;
        }else{
            List<Evidence> list = currentChainEvidences.get(packingAddress);
            //将惩罚证据按轮次升序排列
            Collections.sort(list,new EvidenceComparator());
            long preRoundIndex = list.get(list.size()-1).getRoundIndex();
            //判断惩罚轮次是否连续，如果连续则将该条惩罚记录添加到本地缓存中，如果不连续则清空该节点惩罚证据记录
            if(currentRoundIndex - preRoundIndex != 1){
                currentChainEvidences.remove(packingAddress);
                bifurcationEvidenceMap.put(chain_id,currentChainEvidences);
                return false;
            }else{
                list.add(evidence);
                currentChainEvidences.put(packingAddress,list);
                bifurcationEvidenceMap.put(chain_id,currentChainEvidences);
                if(list.size()>= ConsensusConstant.REDPUNISH_BIFURCATION){
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * 创建红牌交易并放入缓存中
     * */
    private void createRedPunishTransaction(int chain_id, Agent agent)throws NulsException{
        Transaction redPunishTransaction = new Transaction(ConsensusConstant.TX_TYPE_RED_PUNISH);
        RedPunishData redPunishData = new RedPunishData();
        redPunishData.setAddress(agent.getAgentAddress());
        long txTime = 0;
        try{
            //连续3轮 每一轮两个区块头作为证据 一共 3 * 2 个区块头作为证据
            byte[][] headers = new byte[ConsensusConstant.REDPUNISH_BIFURCATION * 2][];
            Map<String, List<Evidence>> currentChainEvidences = bifurcationEvidenceMap.get(chain_id);
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
            //组装CoinData
            CoinData coinData = ConsensusUtil.getStopAgentCoinData(chain_id,agent,redPunishTransaction.getTime()+ConfigManager.config_map.get(chain_id).getRedPublish_lockTime());
            redPunishTransaction.setCoinData(coinData.serialize());
            redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
            //缓存红牌交易
            redPunishTransactionMap.put(chain_id,redPunishTransaction);
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
