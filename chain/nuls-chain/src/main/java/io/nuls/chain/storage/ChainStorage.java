package io.nuls.chain.storage;

import io.nuls.base.data.chain.Chain;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainStorage {

    /**
     * Save chain
     *
     * @param key   The key
     * @param chain Chain object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    int save(short key, Chain chain);

    /**
     * Find chain based on key
     *
     * @param key The key
     * @return Chain object
     */
    Chain load(short key);
}
