package io.nuls.poc.utils.manager;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.utils.compare.AgentComparator;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 节点管理类，负责节点的相关操作
 * Node management class, responsible for the operation of the node
 *
 * @author tag
 * 2018/12/5
 * */
@Component
public class AgentManager{
    @Autowired
    private AgentStorageService agentStorageService;

    @Autowired
    private DepositStorageService depositStorageService;

    @Autowired
    private DepositManager depositManager;
    /**
     * 加载节点信息
     * Initialize node information
     *
     * @param chain 链信息/chain info
     * */
    public void loadAgents(Chain chain) throws Exception{
        List<Agent> allAgentList = new ArrayList<>();
        List<AgentPo> poList = this.agentStorageService.getList(chain.getConfig().getChainId());
        for (AgentPo po : poList) {
            Agent agent = poToAgent(po);
            allAgentList.add(agent);
        }
        Collections.sort(allAgentList, new AgentComparator());
        chain.setAgentList(allAgentList);
    }

    /**
     * 添加指定链节点
     * Adding specified chain nodes
     *
     * @param chain      chain info
     * @param agent      agent info
     * */
    public void addAgent(Chain chain,Agent agent){
        chain.getAgentList().add(agent);
    }

    /**
     * 修改指定链节点
     * Modifying specified chain nodes
     *
     * @param chain      chain info
     * @param agent      agent info
     * */
    public void updateAgent(Chain chain,Agent agent){
        List<Agent> agentList = chain.getAgentList();
        if(agentList == null || agentList.size() == 0){
            agentList.add(agent);
            return;
        }
        for(int index = 0 ;index < agentList.size();index++){
            if(agent.getTxHash().equals(agentList.get(index).getTxHash())){
                agentList.set(index,agent);
            }
        }
    }

    /**
     *
     * 删除指定链节点
     * Delete the specified link node
     *
     * @param chain       chain info
     * @param txHash      创建该节点交易的HASH/Creating the node transaction hash
     * */
    public void removeAgent(Chain chain, NulsDigestData txHash){
        List<Agent> agentList = chain.getAgentList();
        if(agentList == null || agentList.size() == 0){
            return;
        }
        for (Agent agent:agentList) {
            if(txHash.equals(agent.getTxHash())){
                agentList.remove(agent);
                return;
            }
        }
    }

    /**
     * AgentPo to Agent
     *
     * @param agentPo  agentPo对象/agentPo object
     * @return Agent
     * */
    public Agent poToAgent(AgentPo agentPo) {
        if (agentPo == null) {
            return null;
        }
        Agent agent = new Agent();
        agent.setAgentAddress(agentPo.getAgentAddress());
        agent.setBlockHeight(agentPo.getBlockHeight());
        agent.setCommissionRate(agentPo.getCommissionRate());
        agent.setDeposit(agentPo.getDeposit());
        agent.setPackingAddress(agentPo.getPackingAddress());
        agent.setRewardAddress(agentPo.getRewardAddress());
        agent.setTxHash(agentPo.getHash());
        agent.setTime(agentPo.getTime());
        agent.setDelHeight(agentPo.getDelHeight());
        return agent;
    }

    /**
     * Agent to AgentPo
     *
     * @param agent  Agent对象/Agent object
     * @return AgentPo
     * */
    public AgentPo agentToPo(Agent agent) {
        if (agent == null) {
            return null;
        }
        AgentPo agentPo = new AgentPo();
        agentPo.setAgentAddress(agent.getAgentAddress());
        agentPo.setBlockHeight(agent.getBlockHeight());
        agentPo.setCommissionRate(agent.getCommissionRate());
        agentPo.setDeposit(agent.getDeposit());
        agentPo.setPackingAddress(agent.getPackingAddress());
        agentPo.setRewardAddress(agent.getRewardAddress());
        agentPo.setHash(agent.getTxHash());
        agentPo.setTime(agent.getTime());
        return agentPo;
    }

    /**
     * 获取节点id
     * Get agent id
     *
     * @param hash  节点HASH/Agent hash
     * @return String
     * */
    public String getAgentId(NulsDigestData hash) {
        String hashHex = hash.getDigestHex();
        return hashHex.substring(hashHex.length() - 8).toUpperCase();
    }

    public boolean createAgentCommit(Transaction transaction, BlockHeader blockHeader, Chain chain)throws NulsException {
        Agent agent = new Agent();
        agent.parse(transaction.getTxData(), 0);
        agent.setTxHash(transaction.getHash());
        agent.setBlockHeight(blockHeader.getHeight());
        agent.setTime(transaction.getTime());
        AgentPo agentPo = agentToPo(agent);
        if (!agentStorageService.save(agentPo, chain.getConfig().getChainId())) {
            throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
        }
        addAgent(chain, agent);
        return true;
    }

    public boolean createAgentRollBack(Transaction transaction,Chain chain) throws NulsException{
        if (!agentStorageService.delete(transaction.getHash(), chain.getConfig().getChainId())) {
           throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
        }
        removeAgent(chain, transaction.getHash());
        return true;
    }

    public boolean stopAgentCommit(Transaction transaction, Chain chain)throws NulsException{
        int chainId = chain.getConfig().getChainId();
        //找到需要注销的节点信息
        StopAgent stopAgent = new StopAgent();
        stopAgent.parse(transaction.getTxData(), 0);
        AgentPo agentPo = agentStorageService.get(stopAgent.getCreateTxHash(), chain.getConfig().getChainId());
        if (agentPo == null || agentPo.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        //找到该节点的委托信息,并设置委托状态为退出
        List<DepositPo> depositPoList = depositStorageService.getList(chainId);
        for (DepositPo depositPo : depositPoList) {
            if (depositPo.getDelHeight() > -1L) {
                continue;
            }
            if (!depositPo.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            depositPo.setDelHeight(transaction.getBlockHeight());
            if (!depositStorageService.save(depositPo, chainId)) {
                throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
            }
            depositManager.updateDeposit(chain, depositManager.poToDeposit(depositPo));
        }
        agentPo.setDelHeight(transaction.getBlockHeight());
        //保存数据库和缓存
        if (!agentStorageService.save(agentPo, chainId)) {
            throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
        }
        updateAgent(chain, poToAgent(agentPo));
        return true;
    }

    public boolean stopAgentRollBack(Transaction transaction,Chain chain) throws NulsException{
        int chainId = chain.getConfig().getChainId();
        StopAgent stopAgent = new StopAgent();
        stopAgent.parse(transaction.getTxData(), 0);
        AgentPo agentPo = agentStorageService.get(stopAgent.getCreateTxHash(), chainId);
        agentPo.setDelHeight(-1);
        //找到该节点的委托信息,并设置委托状态为退出
        List<DepositPo> depositPoList = depositStorageService.getList(chainId);
        for (DepositPo depositPo : depositPoList) {
            if (depositPo.getDelHeight() != transaction.getBlockHeight()) {
                continue;
            }
            if (!depositPo.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            depositPo.setDelHeight(-1);
            if (!depositStorageService.save(depositPo, chainId)) {
                throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            depositManager.updateDeposit(chain, depositManager.poToDeposit(depositPo));
        }
        //保存数据库和缓存
        if (!agentStorageService.save(agentPo, chainId)) {
            throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
        }
        updateAgent(chain,poToAgent(agentPo));
        return true;
    }
}
