package io.nuls.chain.service;

import io.nuls.base.data.chain.Chain;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainService {
    /**
     * Save chain information when registering a new chain
     *
     * @param chain Chain information filled in when the user registers
     * @return Number of saves
     */
    int chainRegister(Chain chain);

    /**
     * Destroy a chain
     *
     * @param id The id of the chain to be deleted
     * @return Number of deletions
     */
    int chainDestroy(int id);

    /**
     * Query a chain by id
     *
     * @param id The id of the chain to be queried
     * @return Chain
     */
    Chain chainInfo(int id);

    /**
     * Query all the chains
     *
     * @return List of the chain
     */
    List<Chain> chainsInfo();

    Chain chainInfo(String name);
}
