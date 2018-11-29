package io.nuls.chain.service;


import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.model.dto.ChainAsset;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainService {

    /**
     * init chain
     *
     * @return true/false
     */
    boolean initChain();

    /**
     * Save chain
     *
     * @param chain Chain object that needs to be saved
     * @return true/false
     */
    boolean saveChain(Chain chain);

    /**
     * updateChain
     *
     * @param chain
     * @return
     */
    boolean updateChain(Chain chain);

    /**
     * delChain
     *
     * @param chain
     * @return
     */
    boolean delChain(Chain chain);

    /**
     * Find chain based on key
     *
     * @param chainId The chain ID
     * @return Chain
     */
    Chain getChain(int chainId);


}
