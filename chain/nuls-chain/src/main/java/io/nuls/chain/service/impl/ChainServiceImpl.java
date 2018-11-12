package io.nuls.chain.service.impl;

import io.nuls.base.data.chain.Chain;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
@Service
public class ChainServiceImpl implements ChainService {

    @Autowired
    private ChainStorage chainStorage;

    /**
     * Save chain
     *
     * @param chain Chain object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    @Override
    public int saveChain(Chain chain) {
        return chainStorage.save(chain.getChainId(), chain);
    }

    /**
     * Find chain based on key
     *
     * @param id The chain ID
     * @return Chain
     */
    @Override
    public Chain getChain(short id) {
        return chainStorage.load(id);
    }
}
