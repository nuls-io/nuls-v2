package io.nuls.poc.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.model.po.AgentPo;

import java.util.List;

/**
 * 节点信息管理
 * @author  tag
 * 2018/11/6
 * */
public interface AgentStorageService {
    /**
     * 保存节点
     * @param  agentPo   节点对象
     * */
    boolean save(AgentPo agentPo,int chainID);

    /**
     * 根据节点HASH查询节点
     * @param  hash   节点hash
     * */
    AgentPo get(NulsDigestData hash,int chainID);

    /**
     * 根据节点hash删除节点
     * @param hash  节点hash
     * */
    boolean delete(NulsDigestData hash,int chainID);

    /**
     * 获取所有节点信息
     * */
    List<AgentPo> getList(int chainID) throws  Exception;

    /**
     * 获取当前网络节点数量
     * */
    int size(int chainID);
}
