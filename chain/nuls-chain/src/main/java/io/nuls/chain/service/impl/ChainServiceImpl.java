package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
@Service
public class ChainServiceImpl implements ChainService {

    @Autowired
    private ChainStorage chainStorage;

    @Autowired
    private ChainAssetStorage chainAssetStorage;

    /**
     * Save chain
     *
     * @param chain Chain object that needs to be saved
     * @return true/false
     */
    @Override
    public boolean saveChain(Chain chain) {
        return chainStorage.save(chain.getChainId(), chain);
    }

    /**
     * updateChain
     *
     * @param chain
     * @return
     */
    @Override
    public  boolean updateChain(Chain chain){
        return chainStorage.update(chain.getChainId(), chain);
    }

    /**
     * delChain
     *
     * @param chain
     * @return
     */
    @Override
    public  boolean delChain(Chain chain){
        return chainStorage.delete(chain.getChainId());
    }

    /**
     * Find chain based on key
     *
     * @param chainId The chain ID
     * @return Chain
     */
    @Override
    public Chain getChain(int chainId) {
        Chain chain = chainStorage.load(chainId);
        return chain;
    }


}
