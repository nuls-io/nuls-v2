package io.nuls.consensus.storage;

import io.nuls.base.data.NulsHash;
import io.nuls.consensus.model.po.AgentPo;
import io.nuls.core.exception.NulsException;

import java.util.List;

/**
 * Node storage management
 * Node storage management
 *
 * @author  tag
 * 2018/11/6
 * */
public interface AgentStorageService {
    /**
     * Save Node
     * save agent
     *
     * @param  agentPo   Node Object/agent
     * @param chainID    chainID/chain id
     * @return boolean
     * */
    boolean save(AgentPo agentPo,int chainID);

    /**
     * Based on nodesHASHQuery nodes
     * Query nodes according to node HASH
     *
     * @param  hash   nodehash/agent hash
     * @param chainID chainID/chain id
     * */
    AgentPo get(NulsHash hash,int chainID);

    /**
     * Based on nodeshashDelete node
     * Delete nodes according to node hash
     *
     * @param hash  nodehash/agent hash
     * @param chainID    chainID/chain id
     * @return boolean
     * */
    boolean delete(NulsHash hash,int chainID);

    /**
     * Get all node information
     * Get all agent information
     *
     * @param chainID    chainID/chain id
     * @return Node Information List/agent list
     * @exception Exception
     * */
    List<AgentPo> getList(int chainID) throws NulsException;

    /**
     * Obtain the current number of network nodes
     * Get the number of current network nodes
     *
     * @param chainID    chainID/chain id
     * @return  Number of nodes/Number of agents
     * */
    int size(int chainID);
}
