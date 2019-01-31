package io.nuls.poc.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.tools.exception.NulsException;

import java.util.List;

/**
 * 节点存储管理
 * Node storage management
 *
 * @author  tag
 * 2018/11/6
 * */
public interface AgentStorageService {
    /**
     * 保存节点
     * save agent
     *
     * @param  agentPo   节点对象/agent
     * @param chainID    链ID/chain id
     * @return boolean
     * */
    boolean save(AgentPo agentPo,int chainID);

    /**
     * 根据节点HASH查询节点
     * Query nodes according to node HASH
     *
     * @param  hash   节点hash/agent hash
     * @param chainID 链ID/chain id
     * */
    AgentPo get(NulsDigestData hash,int chainID);

    /**
     * 根据节点hash删除节点
     * Delete nodes according to node hash
     *
     * @param hash  节点hash/agent hash
     * @param chainID    链ID/chain id
     * @return boolean
     * */
    boolean delete(NulsDigestData hash,int chainID);

    /**
     * 获取所有节点信息
     * Get all agent information
     *
     * @param chainID    链ID/chain id
     * @return 节点信息列表/agent list
     * @exception Exception
     * */
    List<AgentPo> getList(int chainID) throws NulsException;

    /**
     * 获取当前网络节点数量
     * Get the number of current network nodes
     *
     * @param chainID    链ID/chain id
     * @return  节点数量/Number of agents
     * */
    int size(int chainID);
}
