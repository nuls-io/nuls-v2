package io.nuls.poc.utils.manager;

import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.utils.compare.AgentComparator;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

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
            if(agent.getTxHash().equals(agentList.get(index))){
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
}
