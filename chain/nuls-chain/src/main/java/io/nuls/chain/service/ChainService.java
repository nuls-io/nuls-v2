package io.nuls.chain.service;

import io.nuls.base.data.chain.Chain;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainService {
    /**
     * Save chain
     *
     * @param chain Chain object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    int saveChain(Chain chain);

    /**
     * Find chain based on key
     *
     * @param id The chain ID
     * @return Chain
     */
    Chain getChain(short id);

}
