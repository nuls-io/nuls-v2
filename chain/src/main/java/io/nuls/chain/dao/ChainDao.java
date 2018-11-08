package io.nuls.chain.dao;

import io.nuls.chain.model.Chain;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainDao {

    /**
     * Save chain information when registering a new chain
     *
     * @param chain Chain information filled in when the user registers
     * @return Number of saves
     */
    int save(Chain chain);

    Chain selectByName(String name);
}
