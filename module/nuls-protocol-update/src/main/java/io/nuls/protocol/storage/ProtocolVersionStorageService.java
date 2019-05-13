package io.nuls.protocol.storage;

import io.nuls.protocol.model.po.ProtocolVersionPo;

import java.util.List;

public interface ProtocolVersionStorageService {

    /**
     * 保存指定链的版本统计信息
     * Save configuration information for the specified chain
     *
     * @param po      版本统计类/config bean
     * @param chainId 链Id/chain id
     * @return 保存是否成功/Is preservation successful?
     * @throws
     */
    boolean save(int chainId, ProtocolVersionPo po);

    /**
     * 查询某条链的版本统计信息
     * Query the configuration information of a chain
     *
     * @param chainId 链Id/chain id
     * @param version 区块高度
     * @return 版本统计信息类/config bean
     */
    ProtocolVersionPo get(int chainId, short version);

    /**
     * 删除某条链的版本统计信息
     * Delete configuration information for a chain
     *
     * @param chainId 链Id/chain id
     * @param version 区块高度
     * @return 删除是否成功/Delete success
     */
    boolean delete(int chainId, short version);

    /**
     * 获取当前节点所有的链信息
     * Get all the chain information of the current node
     *
     * @param chainId 链Id/chain id
     * @return 节点信息列表/Node information list
     */
    List<ProtocolVersionPo> getList(int chainId);

    boolean saveCurrentProtocolVersionCount(int chainId, int currentProtocolVersionCount);

    int getCurrentProtocolVersionCount(int chainId);
}
