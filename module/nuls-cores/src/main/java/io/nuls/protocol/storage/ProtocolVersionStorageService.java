package io.nuls.protocol.storage;

import io.nuls.protocol.model.po.ProtocolVersionPo;

import java.util.List;

public interface ProtocolVersionStorageService {

    /**
     * Save version statistics for the specified chain
     * Save configuration information for the specified chain
     *
     * @param po      Version statistics class/config bean
     * @param chainId chainId/chain id
     * @return Whether the save was successful/Is preservation successful?
     * @throws
     */
    boolean save(int chainId, ProtocolVersionPo po);

    /**
     * Query version statistics of a certain chain
     * Query the configuration information of a chain
     *
     * @param chainId chainId/chain id
     * @param version block height
     * @return Version statistics information class/config bean
     */
    ProtocolVersionPo get(int chainId, short version);

    /**
     * Delete version statistics for a certain chain
     * Delete configuration information for a chain
     *
     * @param chainId chainId/chain id
     * @param version block height
     * @return Whether the deletion was successful/Delete success
     */
    boolean delete(int chainId, short version);

    /**
     * Obtain all chain information of the current node
     * Get all the chain information of the current node
     *
     * @param chainId chainId/chain id
     * @return Node Information List/Node information list
     */
    List<ProtocolVersionPo> getList(int chainId);

    boolean saveCurrentProtocolVersionCount(int chainId, int currentProtocolVersionCount);

    int getCurrentProtocolVersionCount(int chainId);
}
