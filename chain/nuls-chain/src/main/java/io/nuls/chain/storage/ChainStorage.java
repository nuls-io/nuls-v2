package io.nuls.chain.storage;

import io.nuls.base.data.chain.Chain;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainStorage {

    /**
     * Save chain information when registering a new chain
     *
     * @param chain Chain information filled in when the user registers
     * @return Number of saves
     */
    int save(Chain chain);

    Chain selectByName(String name);
}
